package com.global.sms.engine.receiver

import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import com.global.sms.data.db.GlobalSmsDatabase
import com.global.sms.data.entity.SpamRuleEntity
import com.global.sms.engine.dispatcher.MessageDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            try {
                val db = GlobalSmsDatabase.getInstance(context)
                val threadId = intent.getLongExtra(EXTRA_THREAD_ID, -1L)
                val messageId = intent.getLongExtra(EXTRA_MESSAGE_ID, -1L)
                val address = intent.getStringExtra(EXTRA_ADDRESS) ?: ""

                when (action) {
                    ACTION_REPLY -> {
                        val replyBundle = RemoteInput.getResultsFromIntent(intent)
                        val replyText = replyBundle?.getCharSequence(KEY_TEXT_REPLY)?.toString()
                        if (!replyText.isNullOrBlank() && address.isNotBlank()) {
                            MessageDispatcher.dispatchSendMessage(
                                context = context,
                                address = address,
                                body = replyText,
                                threadId = if (threadId != -1L) threadId else null
                            )
                            if (threadId != -1L) {
                                db.messageDao().markThreadAsRead(threadId)
                                db.conversationDao().markConversationRead(threadId)
                            }
                        }
                    }

                    ACTION_MARK_READ -> {
                        if (threadId != -1L) {
                            db.messageDao().markThreadAsRead(threadId)
                            db.conversationDao().markConversationRead(threadId)
                        } else if (messageId != -1L) {
                            val msg = db.messageDao().getAllMessagesSync().find { it.id == messageId }
                            if (msg != null) {
                                db.messageDao().markThreadAsRead(msg.threadId)
                                db.conversationDao().markConversationRead(msg.threadId)
                            }
                        }
                    }

                    ACTION_ARCHIVE -> {
                        if (threadId != -1L) {
                            db.conversationDao().setConversationArchived(threadId, true)
                            db.messageDao().markThreadAsRead(threadId)
                            db.conversationDao().markConversationRead(threadId)
                        }
                    }

                    ACTION_DELETE -> {
                        if (threadId != -1L) {
                            db.messageDao().deleteThreadMessages(threadId)
                            db.conversationDao().deleteConversation(threadId)
                        } else if (messageId != -1L) {
                            db.messageDao().deleteMessage(messageId)
                        }
                    }

                    ACTION_COPY_OTP -> {
                        val otpCode = intent.getStringExtra(EXTRA_OTP_CODE) ?: ""
                        if (otpCode.isNotBlank()) {
                            Handler(Looper.getMainLooper()).post {
                                try {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("OTP Code", otpCode)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "کد تایید کپی شد: $otpCode", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Log.e("NotificationActionRcvr", "Failed to copy OTP to clipboard", e)
                                }
                            }
                        }
                    }

                    ACTION_BLOCK -> {
                        if (address.isNotBlank()) {
                            db.spamRuleDao().insertSpamRule(
                                SpamRuleEntity(pattern = address, ruleType = "SENDER")
                            )
                            if (threadId != -1L) {
                                db.conversationDao().setConversationHidden(threadId, true)
                            }
                        }
                    }

                    ACTION_MUTE -> {
                        if (threadId != -1L) {
                            db.conversationDao().setConversationMuted(threadId, true)
                        }
                    }
                }

                // Dismiss individual notification
                val notifId = if (threadId != -1L) threadId.toInt() else messageId.toInt()
                if (notifId != -1) {
                    val notifManager = NotificationManagerCompat.from(context)
                    notifManager.cancel(notifId)
                }

                // Refresh summary grouped notification
                MessageDispatcher.updateSummaryNotification(context)

            } catch (e: Exception) {
                Log.e("NotificationActionRcvr", "Error processing notification action", e)
            } finally {
                pendingResult?.finish()
            }
        }
    }

    companion object {
        const val ACTION_REPLY = "com.global.sms.engine.ACTION_NOTIF_REPLY"
        const val ACTION_MARK_READ = "com.global.sms.engine.ACTION_NOTIF_MARK_READ"
        const val ACTION_ARCHIVE = "com.global.sms.engine.ACTION_NOTIF_ARCHIVE"
        const val ACTION_DELETE = "com.global.sms.engine.ACTION_NOTIF_DELETE"
        const val ACTION_COPY_OTP = "com.global.sms.engine.ACTION_NOTIF_COPY_OTP"
        const val ACTION_BLOCK = "com.global.sms.engine.ACTION_NOTIF_BLOCK"
        const val ACTION_MUTE = "com.global.sms.engine.ACTION_NOTIF_MUTE"

        const val EXTRA_THREAD_ID = "extra_thread_id"
        const val EXTRA_MESSAGE_ID = "extra_message_id"
        const val EXTRA_ADDRESS = "extra_address"
        const val EXTRA_OTP_CODE = "extra_otp_code"
        const val KEY_TEXT_REPLY = "key_text_reply"
    }
}
