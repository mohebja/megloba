package com.global.sms.core.notification

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

enum class NotificationPriorityGroup {
    EMERGENCY_CRITICAL,
    FINANCIAL_OTP,
    MEETING_REMINDER,
    GENERAL_PROMOTIONAL,
    SILENT_INTELLIGENT
}

data class UnifiedNotificationItem(
    val notificationId: String = UUID.randomUUID().toString(),
    val senderAddress: String,
    val group: NotificationPriorityGroup,
    val publicTitleOnLockScreen: String, // Masked content for LockScreen privacy
    val privateBodyText: String,
    val isOtpCode: Boolean = false,
    val isSilentModeActive: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

class AIUnifiedNotificationManager {

    private val _activeNotifications = MutableStateFlow<List<UnifiedNotificationItem>>(
        listOf(
            UnifiedNotificationItem(
                senderAddress = "02191000000",
                group = NotificationPriorityGroup.FINANCIAL_OTP,
                publicTitleOnLockScreen = "اعلان بانکی جدید (محتوا روی صفحه‌قفل مخفی است)",
                privateBodyText = "کد تایید ورود: 987654 (اعتبار ۵ دقیقه)",
                isOtpCode = true
            ),
            UnifiedNotificationItem(
                senderAddress = "09123456789",
                group = NotificationPriorityGroup.MEETING_REMINDER,
                publicTitleOnLockScreen = "یادآور جلسه کاری (۱ پیامک جدید)",
                privateBodyText = "جلسه بررسی قرارداد فردا ساعت ۱۰ صبح برگزار می‌شود.",
                isOtpCode = false
            )
        )
    )
    val activeNotifications: StateFlow<List<UnifiedNotificationItem>> = _activeNotifications.asStateFlow()

    fun processIncomingSmsNotification(
        senderAddress: String,
        bodyText: String,
        isLockScreenVisible: Boolean = true
    ): UnifiedNotificationItem {
        val lower = bodyText.lowercase()
        val (group, lockScreenTitle) = when {
            lower.contains("فوری") || lower.contains("هشدار") || lower.contains("امنیتی") -> {
                NotificationPriorityGroup.EMERGENCY_CRITICAL to "هشدار امنیتی فوری"
            }
            lower.contains("کد تایید") || lower.contains("رمز پویا") || lower.contains("otp") || lower.contains("واریز") -> {
                NotificationPriorityGroup.FINANCIAL_OTP to "پیامک تراکنش مالی / OTP"
            }
            lower.contains("جلسه") || lower.contains("قرار") || lower.contains("ساعت") -> {
                NotificationPriorityGroup.MEETING_REMINDER to "یادآور رویداد و جلسه"
            }
            else -> {
                NotificationPriorityGroup.GENERAL_PROMOTIONAL to "پیام جدید دریافتی"
            }
        }

        val item = UnifiedNotificationItem(
            senderAddress = senderAddress,
            group = group,
            publicTitleOnLockScreen = if (isLockScreenVisible) "$lockScreenTitle (اطلاعات حساس پنهان است)" else bodyText.take(20),
            privateBodyText = bodyText,
            isOtpCode = lower.contains("کد تایید") || lower.contains("otp"),
            isSilentModeActive = group == NotificationPriorityGroup.GENERAL_PROMOTIONAL
        )

        _activeNotifications.value = listOf(item) + _activeNotifications.value
        return item
    }

    fun clearNotification(notificationId: String): Boolean {
        _activeNotifications.value = _activeNotifications.value.filter { it.notificationId != notificationId }
        return true
    }

    fun clearAllNotifications(): Boolean {
        _activeNotifications.value = emptyList()
        return true
    }
}
