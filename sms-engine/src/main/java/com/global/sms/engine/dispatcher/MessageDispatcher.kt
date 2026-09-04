package com.global.sms.engine.dispatcher

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import com.global.sms.core.parser.BankTransactionParser
import com.global.sms.core.rule.CategoryRuleEngine
import com.global.sms.core.security.FieldEncryptionManager
import com.global.sms.core.security.PhishingDetector
import com.global.sms.data.db.GlobalSmsDatabase
import com.global.sms.data.entity.*
import com.global.sms.engine.queue.SmsQueueManager
import com.global.sms.engine.receiver.NotificationActionReceiver
import com.global.sms.engine.tts.TtsManager
import com.global.sms.security.lock.AppLockManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object MessageDispatcher {

    private const val TAG = "MessageDispatcher"

    const val GROUP_KEY_SMS = "com.global.sms.NOTIF_GROUP"
    const val SUMMARY_NOTIFICATION_ID = 99999

    const val CHANNEL_PERSONAL = "channel_personal"
    const val CHANNEL_OTP_TRANSACTIONAL = "channel_otp_transactional"
    const val CHANNEL_PROMOTIONAL = "channel_promotional"
    const val CHANNEL_SPAM = "channel_spam"
    const val CHANNEL_GENERAL = "channel_general"

    suspend fun onIncomingSms(
        context: Context,
        address: String,
        body: String,
        timestamp: Long = System.currentTimeMillis(),
        simSlot: Int = 0,
        subId: Int = -1
    ) = withContext(Dispatchers.IO) {
        val db = GlobalSmsDatabase.getInstance(context)
        val messageDao = db.messageDao()
        val conversationDao = db.conversationDao()

        val activeThreadId = address.hashCode().toLong() and 0x7FFFFFFF

        // 1. Phishing & Spam detection (gracefully resilient to parser/regex issues)
        val finalCategory = try {
            val scanResult = PhishingDetector.scanMessage(address, body)
            val ruleResult = CategoryRuleEngine.classifyMessage(
                sender = address,
                body = body,
                customCategories = emptyList(),
                isSpamOrPhishing = scanResult.isSpamOrPhishing
            )
            ruleResult.categoryEnum
        } catch (e: Exception) {
            Log.e(TAG, "Error executing spam/phishing heuristics on incoming SMS", e)
            MessageCategory.PERSONAL
        }

        // 2. Bank Transaction parsing
        val otpCode = try {
            val bankAnalysis = BankTransactionParser.analyzeMessage(address, body)
            bankAnalysis.otpCode
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing bank transaction or OTP code", e)
            null
        }

        // 3. Save Message
        val isHidden = finalCategory == MessageCategory.SPAM
        val message = MessageEntity(
            threadId = activeThreadId,
            address = address,
            body = body,
            timestamp = timestamp,
            type = MessageType.INBOX.code,
            simSlot = simSlot,
            isRead = false,
            isHidden = isHidden,
            category = finalCategory,
            otpCode = otpCode,
            subId = subId
        )

        val encryptedMessage = FieldEncryptionManager.encryptMessage(message)
        val messageId = messageDao.insertMessage(encryptedMessage)

        // 4. Update Conversation
        val existingConv = conversationDao.getConversationByThreadId(activeThreadId)
        val newUnreadCount = (existingConv?.unreadCount ?: 0) + 1

        val updatedConv = ConversationEntity(
            threadId = activeThreadId,
            address = address,
            contactName = existingConv?.contactName,
            lastMessage = body,
            lastTimestamp = timestamp,
            unreadCount = newUnreadCount,
            category = finalCategory,
            isPinned = existingConv?.isPinned ?: false,
            isHidden = isHidden,
            isMuted = existingConv?.isMuted ?: false
        )
        val encryptedConv = FieldEncryptionManager.encryptConversation(updatedConv)
        conversationDao.insertOrUpdateConversation(encryptedConv)

        // 5. Speak via TTS if enabled
        try {
            val ttsManager = TtsManager(context)
            ttsManager.speakMessage(body, false)
        } catch (e: Exception) {
            Log.e(TAG, "TTS read error", e)
        }

        // 6. Dispatch Notification
        if (!isHidden && existingConv?.isMuted != true) {
            showIncomingNotification(
                context = context,
                address = address,
                body = body,
                threadId = activeThreadId,
                messageId = messageId,
                category = finalCategory,
                existingOtpCode = otpCode
            )
        }
    }

    suspend fun onIncomingMms(
        context: Context,
        address: String,
        body: String,
        attachmentUri: String? = null,
        mimeType: String? = null,
        timestamp: Long = System.currentTimeMillis(),
        simSlot: Int = 0
    ) = withContext(Dispatchers.IO) {
        val db = GlobalSmsDatabase.getInstance(context)
        val activeThreadId = address.hashCode().toLong() and 0x7FFFFFFF

        val message = MessageEntity(
            threadId = activeThreadId,
            address = address,
            body = body.ifBlank { "[MMS Media Attachment]" },
            timestamp = timestamp,
            type = MessageType.INBOX.code,
            simSlot = simSlot,
            isRead = false,
            category = MessageCategory.PERSONAL,
            attachmentUri = attachmentUri,
            mimeType = mimeType,
            isMms = true
        )

        val encryptedMessage = FieldEncryptionManager.encryptMessage(message)
        val messageId = db.messageDao().insertMessage(encryptedMessage)

        val existingConv = db.conversationDao().getConversationByThreadId(activeThreadId)
        val updatedConv = ConversationEntity(
            threadId = activeThreadId,
            address = address,
            contactName = existingConv?.contactName,
            lastMessage = "[MMS] $body",
            lastTimestamp = timestamp,
            unreadCount = (existingConv?.unreadCount ?: 0) + 1,
            category = MessageCategory.PERSONAL,
            isMuted = existingConv?.isMuted ?: false
        )
        val encryptedConv = FieldEncryptionManager.encryptConversation(updatedConv)
        db.conversationDao().insertOrUpdateConversation(encryptedConv)

        if (existingConv?.isMuted != true) {
            showIncomingNotification(
                context = context,
                address = address,
                body = body.ifBlank { "[MMS Attachment]" },
                threadId = activeThreadId,
                messageId = messageId,
                category = MessageCategory.PERSONAL,
                existingOtpCode = null
            )
        }
    }

    suspend fun dispatchSendMessage(
        context: Context,
        address: String,
        body: String,
        simSlot: Int = 0,
        subId: Int = -1,
        attachmentUri: String? = null,
        mimeType: String? = null,
        threadId: Long? = null
    ): Long {
        return SmsQueueManager.enqueueMessage(
            context = context,
            address = address,
            body = body,
            simSlot = simSlot,
            subId = subId,
            attachmentUri = attachmentUri,
            mimeType = mimeType,
            threadId = threadId
        )
    }

    suspend fun dispatchScheduledMessage(
        context: Context,
        address: String,
        body: String,
        scheduledTimestamp: Long,
        simSlot: Int = 0
    ): Long {
        return SmsQueueManager.enqueueScheduledMessage(
            context = context,
            address = address,
            body = body,
            scheduledTimestamp = scheduledTimestamp,
            simSlot = simSlot
        )
    }

    private fun setupNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val channels = listOf(
                NotificationChannel(
                    CHANNEL_PERSONAL,
                    "پیام‌های شخصی (Personal Messages)",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "هشدار پیام‌های شخصی مخاطبین و بستگان"
                    enableVibration(true)
                },
                NotificationChannel(
                    CHANNEL_OTP_TRANSACTIONAL,
                    "کد تایید و تراکنش‌ها (OTP & Transactions)",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "کدهای ورود، تراکنش‌های بانکی و اعتبارسنجی"
                    enableVibration(true)
                },
                NotificationChannel(
                    CHANNEL_PROMOTIONAL,
                    "تبلیغات و پیام‌های تجاری (Promotions)",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "پیامک‌های تبلیغاتی و اطلاع‌رسانی تخفیف‌ها"
                    enableVibration(false)
                },
                NotificationChannel(
                    CHANNEL_SPAM,
                    "اسپم و مسدودشده (Spam & Blocked)",
                    NotificationManager.IMPORTANCE_MIN
                ).apply {
                    description = "پیام‌های مشکوک به اسپم یا بلاک‌شده"
                    enableVibration(false)
                },
                NotificationChannel(
                    CHANNEL_GENERAL,
                    "اعلان‌های عمومی (General SMS)",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "سایر پیامک‌های دریافتی سیستم"
                    enableVibration(true)
                }
            )

            channels.forEach { channel ->
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

    private fun getChannelForCategory(category: MessageCategory): String {
        return when (category) {
            MessageCategory.OTP, MessageCategory.BANK, MessageCategory.TRANSACTIONS -> CHANNEL_OTP_TRANSACTIONAL
            MessageCategory.PERSONAL, MessageCategory.WORK, MessageCategory.IMPORTANT -> CHANNEL_PERSONAL
            MessageCategory.ADVERTISEMENT, MessageCategory.BUSINESS, MessageCategory.SHOPPING, MessageCategory.DELIVERY -> CHANNEL_PROMOTIONAL
            MessageCategory.SPAM -> CHANNEL_SPAM
            else -> CHANNEL_GENERAL
        }
    }

    private fun extractOtpCode(body: String, existingOtp: String?): String? {
        if (!existingOtp.isNullOrBlank()) return existingOtp
        val codeKeywords = listOf("کد", "رمز", "code", "otp", "passcode", "pin")
        if (codeKeywords.any { body.contains(it, ignoreCase = true) }) {
            val match = Regex("""\b\d{4,8}\b""").find(body)
            return match?.value
        }
        return null
    }

    private fun extractUrl(body: String): String? {
        val match = Regex("""https?://[^\s]+""").find(body)
        return match?.value
    }

    private suspend fun showIncomingNotification(
        context: Context,
        address: String,
        body: String,
        threadId: Long,
        messageId: Long,
        category: MessageCategory,
        existingOtpCode: String?
    ) {
        setupNotificationChannels(context)

        val appLockManager = AppLockManager(context)
        val isPrivateMode = appLockManager.isPrivateNotificationMode

        val channelId = getChannelForCategory(category)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Intent to open chat app
        val launchIntent = (context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?: Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                setPackage(context.packageName)
            }).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

        val pendingIntent = PendingIntent.getActivity(
            context,
            threadId.toInt(),
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (isPrivateMode) "Global SMS" else address
        val text = if (isPrivateMode) "پیام جدید دریافت شد" else body

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.sym_action_chat)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .setGroup(GROUP_KEY_SMS)

        if (!isPrivateMode) {
            // 1. Smart Action: Copy OTP
            val detectedOtp = extractOtpCode(body, existingOtpCode)
            if (!detectedOtp.isNullOrBlank()) {
                val copyOtpIntent = Intent(context, NotificationActionReceiver::class.java).apply {
                    action = NotificationActionReceiver.ACTION_COPY_OTP
                    putExtra(NotificationActionReceiver.EXTRA_OTP_CODE, detectedOtp)
                    putExtra(NotificationActionReceiver.EXTRA_THREAD_ID, threadId)
                }
                val copyOtpPendingIntent = PendingIntent.getBroadcast(
                    context,
                    (threadId.toInt() * 10) + 1,
                    copyOtpIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                builder.addAction(
                    android.R.drawable.ic_menu_save,
                    "کپی کد ($detectedOtp)",
                    copyOtpPendingIntent
                )
            }

            // 2. Smart Action: Open Link
            val detectedUrl = extractUrl(body)
            if (!detectedUrl.isNullOrBlank()) {
                try {
                    val linkIntent = Intent(Intent.ACTION_VIEW, Uri.parse(detectedUrl)).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    val linkPendingIntent = PendingIntent.getActivity(
                        context,
                        (threadId.toInt() * 10) + 2,
                        linkIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    builder.addAction(
                        android.R.drawable.ic_menu_compass,
                        "باز کردن لینک",
                        linkPendingIntent
                    )
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to attach action intent for link", e)
                }
            }

            // 3. Inline Reply Action
            val remoteInput = RemoteInput.Builder(NotificationActionReceiver.KEY_TEXT_REPLY)
                .setLabel("پاسخ به $address...")
                .build()

            val replyIntent = Intent(context, NotificationActionReceiver::class.java).apply {
                action = NotificationActionReceiver.ACTION_REPLY
                putExtra(NotificationActionReceiver.EXTRA_THREAD_ID, threadId)
                putExtra(NotificationActionReceiver.EXTRA_MESSAGE_ID, messageId)
                putExtra(NotificationActionReceiver.EXTRA_ADDRESS, address)
            }
            val replyPendingIntent = PendingIntent.getBroadcast(
                context,
                (threadId.toInt() * 10) + 3,
                replyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
            val replyAction = NotificationCompat.Action.Builder(
                android.R.drawable.ic_menu_send,
                "پاسخ",
                replyPendingIntent
            ).addRemoteInput(remoteInput).build()

            builder.addAction(replyAction)

            // 4. Mark as Read Action
            val markReadIntent = Intent(context, NotificationActionReceiver::class.java).apply {
                action = NotificationActionReceiver.ACTION_MARK_READ
                putExtra(NotificationActionReceiver.EXTRA_THREAD_ID, threadId)
                putExtra(NotificationActionReceiver.EXTRA_MESSAGE_ID, messageId)
            }
            val markReadPendingIntent = PendingIntent.getBroadcast(
                context,
                (threadId.toInt() * 10) + 4,
                markReadIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(
                android.R.drawable.ic_menu_agenda,
                "خوانده شد",
                markReadPendingIntent
            )

            // 5. Archive Action
            val archiveIntent = Intent(context, NotificationActionReceiver::class.java).apply {
                action = NotificationActionReceiver.ACTION_ARCHIVE
                putExtra(NotificationActionReceiver.EXTRA_THREAD_ID, threadId)
            }
            val archivePendingIntent = PendingIntent.getBroadcast(
                context,
                (threadId.toInt() * 10) + 5,
                archiveIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(
                android.R.drawable.ic_menu_delete,
                "آرشیو",
                archivePendingIntent
            )

            // 6. Delete Action
            val deleteIntent = Intent(context, NotificationActionReceiver::class.java).apply {
                action = NotificationActionReceiver.ACTION_DELETE
                putExtra(NotificationActionReceiver.EXTRA_THREAD_ID, threadId)
                putExtra(NotificationActionReceiver.EXTRA_MESSAGE_ID, messageId)
            }
            val deletePendingIntent = PendingIntent.getBroadcast(
                context,
                (threadId.toInt() * 10) + 6,
                deleteIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "حذف",
                deletePendingIntent
            )

            // 7. Block Sender Action
            val blockIntent = Intent(context, NotificationActionReceiver::class.java).apply {
                action = NotificationActionReceiver.ACTION_BLOCK
                putExtra(NotificationActionReceiver.EXTRA_THREAD_ID, threadId)
                putExtra(NotificationActionReceiver.EXTRA_ADDRESS, address)
            }
            val blockPendingIntent = PendingIntent.getBroadcast(
                context,
                (threadId.toInt() * 10) + 7,
                blockIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "بلاک",
                blockPendingIntent
            )

            // 8. Mute Action
            val muteIntent = Intent(context, NotificationActionReceiver::class.java).apply {
                action = NotificationActionReceiver.ACTION_MUTE
                putExtra(NotificationActionReceiver.EXTRA_THREAD_ID, threadId)
            }
            val mutePendingIntent = PendingIntent.getBroadcast(
                context,
                (threadId.toInt() * 10) + 8,
                muteIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(
                android.R.drawable.ic_lock_silent_mode,
                "بی‌صدا",
                mutePendingIntent
            )
        }

        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                notificationManager.notify(threadId.toInt(), builder.build())
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Notification permission missing on API 33+", e)
        } catch (e: Exception) {
            Log.e(TAG, "Error posting incoming notification", e)
        }

        // Post/Update Group Summary Notification
        updateSummaryNotification(context)
    }

    @android.annotation.SuppressLint("MissingPermission")
    suspend fun updateSummaryNotification(context: Context) {
        try {
            val db = GlobalSmsDatabase.getInstance(context)
            val unreadConvs = db.conversationDao().getUnreadConversationsSync()

            val notificationManager = NotificationManagerCompat.from(context)

            if (unreadConvs.isEmpty()) {
                notificationManager.cancel(SUMMARY_NOTIFICATION_ID)
                return
            }

            val appLockManager = AppLockManager(context)
            val isPrivateMode = appLockManager.isPrivateNotificationMode

            val launchIntent = (context.packageManager.getLaunchIntentForPackage(context.packageName)
                ?: Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                    setPackage(context.packageName)
                }).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }

            val pendingIntent = PendingIntent.getActivity(
                context,
                SUMMARY_NOTIFICATION_ID,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val inboxStyle = NotificationCompat.InboxStyle()
                .setBigContentTitle("پیام‌های خوانده نشده (${unreadConvs.size} گفتگو)")

            unreadConvs.take(5).forEach { conv ->
                val lineText = if (isPrivateMode) {
                    "پیام جدید دریافت شد"
                } else {
                    "${conv.contactName ?: conv.address}: ${conv.lastMessage}"
                }
                inboxStyle.addLine(lineText)
            }

            val summaryTitle = if (isPrivateMode) "Global SMS" else "پیام‌های جدید"
            val summaryText = if (isPrivateMode) "شما ${unreadConvs.size} گفتگو با پیام خوانده نشده دارید" else "${unreadConvs.size} گفتگوی خوانده نشده"

            val summaryNotification = NotificationCompat.Builder(context, CHANNEL_GENERAL)
                .setSmallIcon(android.R.drawable.sym_action_chat)
                .setContentTitle(summaryTitle)
                .setContentText(summaryText)
                .setStyle(inboxStyle)
                .setGroup(GROUP_KEY_SMS)
                .setGroupSummary(true)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                notificationManager.notify(SUMMARY_NOTIFICATION_ID, summaryNotification)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update summary grouped notification", e)
        }
    }
}
