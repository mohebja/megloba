package com.global.sms.engine.importer

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.Telephony
import android.util.Log
import androidx.core.content.ContextCompat
import com.global.sms.core.contact.ContactManager
import com.global.sms.core.security.FieldEncryptionManager
import com.global.sms.data.db.GlobalSmsDatabase
import com.global.sms.data.entity.ConversationEntity
import com.global.sms.data.entity.MessageCategory
import com.global.sms.data.entity.MessageEntity
import com.global.sms.data.entity.MessageType
import com.global.sms.data.entity.SmsImportLogEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception

object SmsImporter {

    private const val TAG = "SmsImporter"

    suspend fun importSystemSms(
        context: Context,
        onProgress: (progress: Float, current: Int, total: Int) -> Unit = { _, _, _ -> }
    ): Int = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()

        val hasReadPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED

        val database = GlobalSmsDatabase.getInstance(context)
        val messageDao = database.messageDao()
        val conversationDao = database.conversationDao()
        val importLogDao = database.smsImportLogDao()

        if (!hasReadPermission) {
            Log.w(TAG, "Skipping SMS import: android.permission.READ_SMS not granted yet")
            try {
                importLogDao.insertImportLog(
                    SmsImportLogEntity(
                        timestamp = System.currentTimeMillis(),
                        status = "PERMISSION_DENIED",
                        durationMs = System.currentTimeMillis() - startTime
                    )
                )
            } catch (_: Exception) {}
            return@withContext 0
        }

        // 1. Load existing message signatures for 100% deduplication
        val existingMessages = try {
            messageDao.getAllMessagesSync()
        } catch (e: Exception) {
            emptyList()
        }
        val existingSignatures = existingMessages.mapTo(HashSet(existingMessages.size)) {
            "${it.address.trim()}|${it.timestamp}|${it.body.trim()}|${it.type}"
        }

        // 2. Load existing conversations to preserve custom flags (isHidden, isPinned, etc.)
        val existingConversations = try {
            conversationDao.getAllConversationsSync()
        } catch (e: Exception) {
            emptyList()
        }
        val conversationEntityMap = existingConversations.associateBy { it.threadId }.toMutableMap()

        val contentResolver = context.contentResolver
        val uri: Uri = Telephony.Sms.CONTENT_URI

        val projection = arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.THREAD_ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE,
            Telephony.Sms.TYPE,
            Telephony.Sms.READ,
            Telephony.Sms.SUBSCRIPTION_ID
        )

        var cursor: Cursor? = null
        try {
            cursor = contentResolver.query(
                uri,
                projection,
                null,
                null,
                "${Telephony.Sms.DATE} ASC"
            )
        } catch (e: SecurityException) {
            Log.w(TAG, "SecurityException querying Telephony.Sms content provider (Permission denied)", e)
            try {
                importLogDao.insertImportLog(
                    SmsImportLogEntity(
                        timestamp = System.currentTimeMillis(),
                        status = "PERMISSION_DENIED",
                        durationMs = System.currentTimeMillis() - startTime
                    )
                )
            } catch (_: Exception) {}
            return@withContext 0
        } catch (e: Exception) {
            Log.e(TAG, "Failed querying Telephony.Sms content provider during SMS import", e)
            try {
                importLogDao.insertImportLog(
                    SmsImportLogEntity(
                        timestamp = System.currentTimeMillis(),
                        status = "FAILED",
                        durationMs = System.currentTimeMillis() - startTime
                    )
                )
            } catch (_: Exception) {}
            return@withContext 0
        }

        if (cursor == null) return@withContext 0

        val totalCount = cursor.count
        if (totalCount == 0) {
            cursor.close()
            importLogDao.insertImportLog(
                SmsImportLogEntity(
                    timestamp = System.currentTimeMillis(),
                    totalSystemSms = 0,
                    newlyImportedCount = 0,
                    skippedDuplicatesCount = 0,
                    failedCount = 0,
                    status = "SUCCESS",
                    durationMs = System.currentTimeMillis() - startTime
                )
            )
            return@withContext 0
        }

        val threadIdIndex = cursor.getColumnIndex(Telephony.Sms.THREAD_ID)
        val addressIndex = cursor.getColumnIndex(Telephony.Sms.ADDRESS)
        val bodyIndex = cursor.getColumnIndex(Telephony.Sms.BODY)
        val dateIndex = cursor.getColumnIndex(Telephony.Sms.DATE)
        val typeIndex = cursor.getColumnIndex(Telephony.Sms.TYPE)
        val readIndex = cursor.getColumnIndex(Telephony.Sms.READ)
        val subIdIndex = cursor.getColumnIndex(Telephony.Sms.SUBSCRIPTION_ID)

        val batchList = mutableListOf<MessageEntity>()
        val unreadCountMap = mutableMapOf<Long, Int>()
        val contactCache = mutableMapOf<String, Pair<String?, String?>>()
        val hasContactsPermission = ContactManager.hasContactsPermission(context)

        var processed = 0
        var newlyImportedCount = 0
        var skippedDuplicatesCount = 0
        var failedCount = 0

        while (cursor.moveToNext()) {
            try {
                val address = if (addressIndex != -1) cursor.getString(addressIndex) ?: "Unknown" else "Unknown"
                val body = if (bodyIndex != -1) cursor.getString(bodyIndex) ?: "" else ""
                val timestamp = if (dateIndex != -1) cursor.getLong(dateIndex) else System.currentTimeMillis()
                val rawType = if (typeIndex != -1) cursor.getInt(typeIndex) else Telephony.Sms.MESSAGE_TYPE_INBOX
                val isRead = if (readIndex != -1) cursor.getInt(readIndex) == 1 else true
                val sysThreadId = if (threadIdIndex != -1) cursor.getLong(threadIdIndex) else 0L
                val subId = if (subIdIndex != -1) cursor.getInt(subIdIndex) else -1

                val threadId = if (sysThreadId > 0) sysThreadId else address.trim().lowercase().hashCode().toLong()
                val category = autoCategorizeMessage(address, body)

                val msgType = when (rawType) {
                    Telephony.Sms.MESSAGE_TYPE_INBOX -> MessageType.INBOX.code
                    Telephony.Sms.MESSAGE_TYPE_SENT -> MessageType.SENT.code
                    Telephony.Sms.MESSAGE_TYPE_DRAFT -> MessageType.DRAFT.code
                    Telephony.Sms.MESSAGE_TYPE_FAILED -> MessageType.FAILED.code
                    Telephony.Sms.MESSAGE_TYPE_OUTBOX -> MessageType.OUTBOX.code
                    else -> MessageType.INBOX.code
                }

                val signature = "${address.trim()}|$timestamp|${body.trim()}|$msgType"

                if (!existingSignatures.contains(signature)) {
                    existingSignatures.add(signature)
                    newlyImportedCount++

                    val messageEntity = MessageEntity(
                        threadId = threadId,
                        address = address,
                        body = body,
                        timestamp = timestamp,
                        type = msgType,
                        simSlot = if (subId > 0) 1 else 0,
                        isRead = isRead,
                        category = category,
                        subId = subId
                    )

                    batchList.add(messageEntity)
                } else {
                    skippedDuplicatesCount++
                }

                if (!isRead) {
                    unreadCountMap[threadId] = unreadCountMap.getOrDefault(threadId, 0) + 1
                }

                // Resolve contact name & avatar photo
                val (resolvedName, resolvedPhoto) = contactCache.getOrPut(address) {
                    if (hasContactsPermission) {
                        ContactManager.resolveContactNameAndPhoto(context, address)
                    } else {
                        Pair(null, null)
                    }
                }

                val existingConv = conversationEntityMap[threadId]
                if (existingConv == null) {
                    conversationEntityMap[threadId] = ConversationEntity(
                        threadId = threadId,
                        address = address,
                        contactName = resolvedName,
                        avatarUri = resolvedPhoto,
                        lastMessage = body,
                        lastTimestamp = timestamp,
                        unreadCount = unreadCountMap[threadId] ?: 0,
                        category = category
                    )
                } else {
                    if (timestamp >= existingConv.lastTimestamp) {
                        conversationEntityMap[threadId] = existingConv.copy(
                            lastMessage = body,
                            lastTimestamp = timestamp,
                            contactName = existingConv.contactName ?: resolvedName,
                            avatarUri = existingConv.avatarUri ?: resolvedPhoto,
                            unreadCount = unreadCountMap[threadId] ?: existingConv.unreadCount,
                            category = if (existingConv.category == MessageCategory.PERSONAL) category else existingConv.category
                        )
                    } else {
                        conversationEntityMap[threadId] = existingConv.copy(
                            contactName = existingConv.contactName ?: resolvedName,
                            avatarUri = existingConv.avatarUri ?: resolvedPhoto,
                            unreadCount = unreadCountMap[threadId] ?: existingConv.unreadCount
                        )
                    }
                }

                processed++

                if (batchList.size >= 150) {
                    val encryptedBatch = batchList.map { FieldEncryptionManager.encryptMessage(it) }
                    messageDao.insertMessagesBatch(encryptedBatch)
                    batchList.clear()
                    onProgress(processed.toFloat() / totalCount.toFloat(), processed, totalCount)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to import single SMS record from cursor", e)
                failedCount++
            }
        }

        cursor.close()

        if (batchList.isNotEmpty()) {
            val encryptedBatch = batchList.map { FieldEncryptionManager.encryptMessage(it) }
            messageDao.insertMessagesBatch(encryptedBatch)
            batchList.clear()
        }

        if (conversationEntityMap.isNotEmpty()) {
            val encryptedConvs = conversationEntityMap.values.map { FieldEncryptionManager.encryptConversation(it) }
            conversationDao.insertOrUpdateConversationsBatch(encryptedConvs)
        }

        val duration = System.currentTimeMillis() - startTime
        importLogDao.insertImportLog(
            SmsImportLogEntity(
                timestamp = System.currentTimeMillis(),
                totalSystemSms = totalCount,
                newlyImportedCount = newlyImportedCount,
                skippedDuplicatesCount = skippedDuplicatesCount,
                failedCount = failedCount,
                status = if (failedCount == 0) "SUCCESS" else "PARTIAL",
                durationMs = duration
            )
        )

        onProgress(1.0f, totalCount, totalCount)
        return@withContext if (newlyImportedCount > 0) newlyImportedCount else processed
    }

    private fun autoCategorizeMessage(sender: String, body: String): MessageCategory {
        val text = (sender + " " + body).lowercase()
        return when {
            text.contains("کد") || text.contains("رمز") || text.contains("otp") || text.contains("code") || text.contains("تایید") -> MessageCategory.OTP
            text.contains("تراکنش") || text.contains("واریز") || text.contains("برداشت") || text.contains("موجودی") || text.contains("بانک") || text.contains("حساب") -> MessageCategory.BANK
            text.contains("تخفیف") || text.contains("خرید") || text.contains("فروشگاه") || text.contains("دیجی‌کالا") || text.contains("اسنپ") -> MessageCategory.SHOPPING
            text.contains("ارسال شد") || text.contains("پست") || text.contains("تیپاکس") || text.contains("مرسوله") -> MessageCategory.DELIVERY
            else -> MessageCategory.PERSONAL
        }
    }
}
