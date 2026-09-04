package com.global.sms.core.wear

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class WearNotificationMirror(
    val id: String,
    val senderName: String,
    val messageText: String,
    val isImportant: Boolean = false,
    val aiSummaryText: String? = null
)

class WearCompanionManager {

    private val _notifications = MutableStateFlow<List<WearNotificationMirror>>(
        listOf(
            WearNotificationMirror("w1", "بانک ملی", "کد تایید ورود: 987123", isImportant = true, aiSummaryText = "کد OTP امنیتی"),
            WearNotificationMirror("w2", "مهندس علیرضا رضایی", "جلسه ساعت ۱۴ تایید شد.", isImportant = false, aiSummaryText = "تایید زمان جلسه")
        )
    )
    val notifications: StateFlow<List<WearNotificationMirror>> = _notifications.asStateFlow()

    fun mirrorNotification(sender: String, message: String, isImportant: Boolean, aiSummary: String? = null) {
        val newNotification = WearNotificationMirror(
            id = "w_${System.currentTimeMillis()}",
            senderName = sender,
            messageText = message,
            isImportant = isImportant,
            aiSummaryText = aiSummary
        )
        _notifications.value = listOf(newNotification) + _notifications.value
    }

    fun sendQuickReplyFromWear(notificationId: String, replyText: String): Boolean {
        // Dispatch quick reply back through main SMS Engine
        return true
    }

    companion object {
        fun buildWearSyncPayload(
            address: String,
            senderName: String,
            body: String,
            isOtp: Boolean
        ): String {
            return """{"type":"SMS_INCOMING","address":"$address","senderName":"$senderName","body":"$body","isOtp":$isOtp,"timestamp":${System.currentTimeMillis()}}"""
        }
    }
}
