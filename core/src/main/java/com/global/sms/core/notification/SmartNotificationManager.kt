package com.global.sms.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.global.sms.core.ai.intelligence.MessageCategory
import com.global.sms.core.ai.intelligence.UrgencyLevel

data class FormattedNotification(
    val title: String,
    val contentText: String,
    val summaryText: String,
    val priority: UrgencyLevel,
    val channelId: String,
    val groupKey: String
)

class SmartNotificationManager(private val context: Context) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val criticalChannel = NotificationChannel(
                CHANNEL_CRITICAL,
                "پیام‌های حیاتی و بانکی",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "کدهای تایید یک‌بارمصرف و هشدارهای مهم بانکی"
                enableVibration(true)
            }

            val normalChannel = NotificationChannel(
                CHANNEL_NORMAL,
                "پیام‌های عمومی",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "اطلاعیه‌ها و پیام‌های عمومی"
            }

            val silentChannel = NotificationChannel(
                CHANNEL_SILENT,
                "پیام‌های تبلیغاتی و کم‌اهمیت",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "پیام‌های غیرضروری و اسپم"
            }

            notificationManager.createNotificationChannels(
                listOf(criticalChannel, normalChannel, silentChannel)
            )
        }
    }

    fun formatNotification(
        sender: String,
        body: String,
        category: MessageCategory,
        urgency: UrgencyLevel,
        isLockScreenPrivate: Boolean = false
    ): FormattedNotification {

        val channelId = when (urgency) {
            UrgencyLevel.CRITICAL -> CHANNEL_CRITICAL
            UrgencyLevel.HIGH -> CHANNEL_CRITICAL
            UrgencyLevel.NORMAL -> CHANNEL_NORMAL
            UrgencyLevel.SILENT -> CHANNEL_SILENT
        }

        val groupKey = "com.global.sms.GROUP_${category.name}"

        if (isLockScreenPrivate) {
            return FormattedNotification(
                title = "Global SMS",
                contentText = "پیام جدید دریافت شد",
                summaryText = "پیام محرمانه",
                priority = urgency,
                channelId = channelId,
                groupKey = groupKey
            )
        }

        val (title, contentText) = when (category) {
            MessageCategory.BANKING -> "تراکنش بانکی ($sender)" to body
            MessageCategory.OTP -> "کد ورود شما آماده است" to "کد تایید: ${extractCode(body)} ($sender)"
            MessageCategory.DELIVERY -> "اطلاعیه مرسوله پستی" to body
            MessageCategory.SHOPPING -> "جزئیات خرید" to body
            MessageCategory.SPAM -> "پیام تبلیغاتی" to "محتوای کم‌اهمیت"
            else -> sender to body
        }

        return FormattedNotification(
            title = title,
            contentText = contentText,
            summaryText = "دسته‌بندی: ${category.name}",
            priority = urgency,
            channelId = channelId,
            groupKey = groupKey
        )
    }

    private fun extractCode(body: String): String {
        val match = "\\b\\d{4,8}\\b".toRegex().find(body)
        return match?.value ?: body.take(10)
    }

    companion object {
        const val CHANNEL_CRITICAL = "channel_critical_sms"
        const val CHANNEL_NORMAL = "channel_normal_sms"
        const val CHANNEL_SILENT = "channel_silent_sms"
    }
}
