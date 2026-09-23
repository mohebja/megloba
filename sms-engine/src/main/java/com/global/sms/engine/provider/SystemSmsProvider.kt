package com.global.sms.engine.provider

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.provider.Telephony
import android.util.Log

/**
 * Keeps the system SMS provider (`content://sms`) in sync with the app's own database.
 *
 * Android requires the **default SMS app** to be the one writing to this provider: it is how every other
 * app that reads SMS (backup tools, other launchers' "recent messages" widgets, `SmsRetriever`-style
 * integrations, a future default-app switch) sees the user's messages. A non-default app's writes are
 * rejected by the OS; every function here fails soft (logs and returns `null`/`false`) instead of crashing,
 * both because that rejection is expected whenever the user has changed the default app, and because this
 * sync is secondary to the app's own (encrypted) database, which is queried in [isDefaultSmsApp] first.
 *
 * The row id returned by the two `insertXxx` calls is stored as `MessageEntity.systemSmsId` so later updates
 * (sent/delivered/failed) and deletes touch the same provider row instead of creating duplicates.
 */
object SystemSmsProvider {

    private const val TAG = "SystemSmsProvider"

    /**
     * True when this app is currently the default SMS app and is therefore allowed to write to the
     * provider. Call this before every write; the user can change the default app at any time.
     */
    fun isDefaultSmsApp(context: Context): Boolean =
        context.packageName == Telephony.Sms.getDefaultSmsPackage(context)

    /**
     * Records a message the app just received. Must only be called while handling
     * `SMS_DELIVER_ACTION` (i.e. while this app is the default SMS app) — `SMS_RECEIVED_ACTION` means a
     * different app is default and has already written the message.
     *
     * @return the new row's id in `content://sms`, or `null` if the write could not be performed
     *   (not the default app any more, or the provider rejected it).
     */
    fun insertIncoming(
        context: Context,
        address: String,
        body: String,
        timestampMillis: Long,
        subId: Int,
        isRead: Boolean
    ): Long? {
        val values = ContentValues().apply {
            put(Telephony.Sms.ADDRESS, address)
            put(Telephony.Sms.BODY, body)
            put(Telephony.Sms.DATE, timestampMillis)
            put(Telephony.Sms.DATE_SENT, timestampMillis)
            put(Telephony.Sms.TYPE, Telephony.Sms.MESSAGE_TYPE_INBOX)
            put(Telephony.Sms.READ, if (isRead) 1 else 0)
            put(Telephony.Sms.SEEN, if (isRead) 1 else 0)
            putThreadId(context, address)
            putSubId(subId)
        }
        return insert(context, Telephony.Sms.Inbox.CONTENT_URI, values, "incoming message")
    }

    /**
     * Records a message the app is about to send, in the Outbox. Called right after the message is queued
     * locally, before [android.telephony.SmsManager] is asked to send it.
     *
     * @return the new row's id in `content://sms`, or `null` if the write could not be performed.
     */
    fun insertOutbox(
        context: Context,
        address: String,
        body: String,
        timestampMillis: Long,
        subId: Int
    ): Long? {
        val values = ContentValues().apply {
            put(Telephony.Sms.ADDRESS, address)
            put(Telephony.Sms.BODY, body)
            put(Telephony.Sms.DATE, timestampMillis)
            put(Telephony.Sms.TYPE, Telephony.Sms.MESSAGE_TYPE_OUTBOX)
            put(Telephony.Sms.READ, 1) // outgoing messages are the user's own, never "unread"
            put(Telephony.Sms.SEEN, 1)
            put(Telephony.Sms.STATUS, Telephony.Sms.STATUS_PENDING)
            putThreadId(context, address)
            putSubId(subId)
        }
        return insert(context, Telephony.Sms.Outbox.CONTENT_URI, values, "outbox message")
    }

    /** Moves a provider row from Outbox to Sent once [android.telephony.SmsManager] confirms the send. */
    fun markSent(context: Context, systemSmsId: Long, dateSentMillis: Long = System.currentTimeMillis()) {
        val values = ContentValues().apply {
            put(Telephony.Sms.TYPE, Telephony.Sms.MESSAGE_TYPE_SENT)
            put(Telephony.Sms.DATE_SENT, dateSentMillis)
            put(Telephony.Sms.STATUS, Telephony.Sms.STATUS_COMPLETE)
        }
        update(context, systemSmsId, values, "mark sent")
    }

    /** Records that the carrier confirmed delivery of an already-sent message. */
    fun markDelivered(context: Context, systemSmsId: Long) {
        val values = ContentValues().apply { put(Telephony.Sms.STATUS, Telephony.Sms.STATUS_COMPLETE) }
        update(context, systemSmsId, values, "mark delivered")
    }

    /** Moves a provider row to Failed after every retry has been exhausted (see `SmsRetryManager`). */
    fun markFailed(context: Context, systemSmsId: Long) {
        val values = ContentValues().apply {
            put(Telephony.Sms.TYPE, Telephony.Sms.MESSAGE_TYPE_FAILED)
            put(Telephony.Sms.STATUS, Telephony.Sms.STATUS_FAILED)
        }
        update(context, systemSmsId, values, "mark failed")
    }

    /** Marks a provider row read/unread, mirroring `MessageDao.markThreadAsRead` and friends. */
    fun markRead(context: Context, systemSmsId: Long, isRead: Boolean) {
        val values = ContentValues().apply {
            put(Telephony.Sms.READ, if (isRead) 1 else 0)
            put(Telephony.Sms.SEEN, if (isRead) 1 else 0)
        }
        update(context, systemSmsId, values, "mark read")
    }

    /** Deletes a provider row, mirroring a local message deletion. */
    fun delete(context: Context, systemSmsId: Long) {
        try {
            val uri = ContentUris.withAppendedId(Telephony.Sms.CONTENT_URI, systemSmsId)
            context.contentResolver.delete(uri, null, null)
        } catch (e: SecurityException) {
            Log.w(TAG, "Not the default SMS app any more; skipped deleting system row $systemSmsId", e)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete system row $systemSmsId", e)
        }
    }

    /** Deletes several provider rows in one pass. Best-effort: one failure does not stop the rest. */
    fun deleteAll(context: Context, systemSmsIds: Collection<Long>) {
        systemSmsIds.forEach { delete(context, it) }
    }

    // ---------------------------------------------------------------------------------------------

    private fun ContentValues.putThreadId(context: Context, address: String) {
        try {
            // The provider's own canonical thread id (Telephony.Threads), independent of and not to be
            // confused with this app's local `MessageEntity.threadId`. Other apps that read `content://sms`
            // group by this value, so it must come from the provider itself.
            put(Telephony.Sms.THREAD_ID, Telephony.Threads.getOrCreateThreadId(context, address))
        } catch (e: Exception) {
            Log.w(TAG, "Could not resolve a system thread id for this address; the provider will assign one", e)
        }
    }

    private fun ContentValues.putSubId(subId: Int) {
        if (subId >= 0) {
            put(Telephony.Sms.SUBSCRIPTION_ID, subId)
        }
    }

    private fun insert(context: Context, uri: android.net.Uri, values: ContentValues, what: String): Long? =
        try {
            context.contentResolver.insert(uri, values)?.let { ContentUris.parseId(it) }
        } catch (e: SecurityException) {
            Log.w(TAG, "Not the default SMS app any more; skipped writing $what to the system provider", e)
            null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write $what to the system provider", e)
            null
        }

    private fun update(context: Context, systemSmsId: Long, values: ContentValues, what: String) {
        try {
            val uri = ContentUris.withAppendedId(Telephony.Sms.CONTENT_URI, systemSmsId)
            context.contentResolver.update(uri, values, null, null)
        } catch (e: SecurityException) {
            Log.w(TAG, "Not the default SMS app any more; skipped '$what' for system row $systemSmsId", e)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to '$what' for system row $systemSmsId", e)
        }
    }
}
