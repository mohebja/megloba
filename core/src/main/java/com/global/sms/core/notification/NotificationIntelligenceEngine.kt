package com.global.sms.core.notification

import android.content.Context
import java.util.Calendar

enum class NotificationPriority {
    URGENT_HIGH,
    NORMAL,
    LOW_SILENT
}

enum class PrivacyPreviewMode {
    SHOW_FULL_CONTENT,
    HIDE_SENSITIVE_TEXT,
    HIDE_SENDER_AND_CONTENT
}

data class NotificationProfile(
    val profileName: String = "پیش‌فرض",
    val quietHoursStartHour: Int = 23,
    val quietHoursEndHour: Int = 7,
    val privacyPreviewMode: PrivacyPreviewMode = PrivacyPreviewMode.SHOW_FULL_CONTENT,
    val isImportantSenderPriorityEnabled: Boolean = true
)

class NotificationIntelligenceEngine(private val context: Context) {

    private var profile = NotificationProfile()

    fun updateProfile(newProfile: NotificationProfile) {
        profile = newProfile
    }

    fun calculatePriority(senderAddress: String, messageBody: String): NotificationPriority {
        if (isInQuietHours()) {
            return if (isImportantSender(senderAddress) || messageBody.contains("رمز") || messageBody.contains("فوری")) {
                NotificationPriority.NORMAL
            } else {
                NotificationPriority.LOW_SILENT
            }
        }

        return when {
            messageBody.contains("کد") || messageBody.contains("رمز") || messageBody.contains("بانک") -> NotificationPriority.URGENT_HIGH
            isImportantSender(senderAddress) -> NotificationPriority.URGENT_HIGH
            messageBody.contains("تبلیغات") || messageBody.contains("تخفیف") -> NotificationPriority.LOW_SILENT
            else -> NotificationPriority.NORMAL
        }
    }

    fun formatPreviewText(sender: String, body: String): Pair<String, String> {
        return when (profile.privacyPreviewMode) {
            PrivacyPreviewMode.SHOW_FULL_CONTENT -> Pair(sender, body)
            PrivacyPreviewMode.HIDE_SENSITIVE_TEXT -> Pair(sender, "پیام جدید دریافت شد (محتوا پنهان است)")
            PrivacyPreviewMode.HIDE_SENDER_AND_CONTENT -> Pair("فرستنده ناشناس", "پیام جدید دریافت شد")
        }
    }

    private fun isImportantSender(address: String): Boolean {
        return address.contains("1000") || address.contains("2000") || address.contains("بانک")
    }

    private fun isInQuietHours(): Boolean {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val start = profile.quietHoursStartHour
        val end = profile.quietHoursEndHour

        return if (start > end) {
            currentHour >= start || currentHour < end
        } else {
            currentHour in start until end
        }
    }
}
