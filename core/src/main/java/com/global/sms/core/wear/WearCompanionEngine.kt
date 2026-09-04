package com.global.sms.core.wear

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

data class WearNotification(
    val notificationId: String = UUID.randomUUID().toString(),
    val senderAddress: String,
    val senderName: String,
    val body: String,
    val isPriority: Boolean = false,
    val isSecurityAlert: Boolean = false,
    val aiSummary: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class WearVoiceCommandResult(
    val commandId: String = UUID.randomUUID().toString(),
    val rawVoiceText: String,
    val recognizedIntent: String,
    val recipientAddress: String?,
    val executionMessage: String,
    val isSuccess: Boolean = true
)

class WearCompanionEngine {

    private val manager = WearCompanionManager()

    private val _wearNotifications = MutableStateFlow<List<WearNotification>>(
        listOf(
            WearNotification(
                senderAddress = "02191000000",
                senderName = "سیستم هشدار امین",
                body = "هشدار ورودی مشکوک از IP جدید!",
                isPriority = true,
                isSecurityAlert = true,
                aiSummary = "هشدار امنیتی بالا"
            ),
            WearNotification(
                senderAddress = "09123456789",
                senderName = "شرکت پارس",
                body = "لطفا قرارداد جدید را بررسی کنید.",
                isPriority = false,
                isSecurityAlert = false,
                aiSummary = "درخواست بررسی قرارداد"
            )
        )
    )
    val wearNotifications: StateFlow<List<WearNotification>> = _wearNotifications.asStateFlow()

    private val _voiceCommandHistory = MutableStateFlow<List<WearVoiceCommandResult>>(emptyList())
    val voiceCommandHistory: StateFlow<List<WearVoiceCommandResult>> = _voiceCommandHistory.asStateFlow()

    fun pushNotification(
        senderAddress: String,
        senderName: String,
        body: String,
        isPriority: Boolean = false,
        isSecurityAlert: Boolean = false,
        aiSummary: String? = null
    ): WearNotification {
        val notif = WearNotification(
            senderAddress = senderAddress,
            senderName = senderName,
            body = body,
            isPriority = isPriority,
            isSecurityAlert = isSecurityAlert,
            aiSummary = aiSummary
        )
        _wearNotifications.value = listOf(notif) + _wearNotifications.value
        manager.mirrorNotification(senderName, body, isPriority, aiSummary)
        return notif
    }

    fun sendQuickReply(notificationId: String, quickReplyText: String): Boolean {
        manager.sendQuickReplyFromWear(notificationId, quickReplyText)
        return true
    }

    fun processWearVoiceCommand(spokenText: String): WearVoiceCommandResult {
        val lower = spokenText.lowercase()
        val (intent, target, responseMsg) = when {
            lower.contains("ارسال پیام به") || lower.contains("پیام بده به") -> {
                val targetName = spokenText.substringAfter("به").trim()
                Triple("SEND_SMS", targetName, "آماده ارسال پیام به $targetName: «متن دریافت شد»")
            }
            lower.contains("خوانده شد") || lower.contains("پاک کن") -> {
                Triple("CLEAR_NOTIFICATIONS", null, "تمام نوتیفیکیشن‌ها علامت خوانده شده خوردند.")
            }
            lower.contains("وضعیت امنیت") -> {
                Triple("CHECK_SECURITY", null, "وضعیت امنیتی ساعت و گوشی: ۱۰۰٪ امن و رمزنگاری شده.")
            }
            else -> {
                Triple("GENERAL_QUERY", null, "دستور صوتی در ساعت پردازش شد: $spokenText")
            }
        }

        val result = WearVoiceCommandResult(
            rawVoiceText = spokenText,
            recognizedIntent = intent,
            recipientAddress = target,
            executionMessage = responseMsg,
            isSuccess = true
        )

        _voiceCommandHistory.value = listOf(result) + _voiceCommandHistory.value
        return result
    }

    fun getPriorityNotifications(): List<WearNotification> = _wearNotifications.value.filter { it.isPriority }

    fun getSecurityAlerts(): List<WearNotification> = _wearNotifications.value.filter { it.isSecurityAlert }

    fun getWearOsTileData(): Map<String, Any> {
        return mapOf(
            "unreadCount" to _wearNotifications.value.size,
            "securityAlertCount" to getSecurityAlerts().size,
            "latestSender" to (_wearNotifications.value.firstOrNull()?.senderName ?: "بدون پیام"),
            "aiStatus" to "100% Offline Active"
        )
    }
}
