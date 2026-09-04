package com.global.sms.engine.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.global.sms.data.entity.MessageCategory

data class SmartNotificationPayload(
    val messageId: Long,
    val sender: String,
    val contactName: String? = null,
    val body: String,
    val category: MessageCategory,
    val isHidden: Boolean = false,
    val otpCode: String? = null,
    val spamScore: Int = 0
)

/**
 * Smart Notification Engine prioritizing privacy, OTP copying, and category badges.
 */
class SmartNotificationManager(private val context: Context) {

    companion object {
        const val CHANNEL_ID_DEFAULT = "sms_default_channel"
        const val CHANNEL_ID_OTP = "sms_otp_channel"
        const val CHANNEL_ID_BANK = "sms_bank_channel"
        const val CHANNEL_ID_SPAM = "sms_spam_channel"
        const val CHANNEL_ID_PRIVATE = "sms_private_channel"
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createChannels()
    }

    private fun createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val defaultChannel = NotificationChannel(
                CHANNEL_ID_DEFAULT,
                "پیام‌های عمومی (General SMS)",
                NotificationManager.IMPORTANCE_HIGH
            )
            val otpChannel = NotificationChannel(
                CHANNEL_ID_OTP,
                "کدهای تایید (OTP Codes)",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                enableVibration(true)
            }
            val bankChannel = NotificationChannel(
                CHANNEL_ID_BANK,
                "تراکنش‌های بانکی (Banking)",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val spamChannel = NotificationChannel(
                CHANNEL_ID_SPAM,
                "هشدار اسپم (Spam Warnings)",
                NotificationManager.IMPORTANCE_LOW
            )
            val privateChannel = NotificationChannel(
                CHANNEL_ID_PRIVATE,
                "صندوق شخصی (Private Vault)",
                NotificationManager.IMPORTANCE_HIGH
            )

            notificationManager.createNotificationChannels(
                listOf(defaultChannel, otpChannel, bankChannel, spamChannel, privateChannel)
            )
        }
    }

    fun postNotification(payload: SmartNotificationPayload) {
        val displayName = payload.contactName ?: payload.sender

        val (title, content, channelId) = when {
            // Private / Hidden Message
            payload.isHidden -> Triple(
                "پیامک جدید شخصی",
                "پیام شخصی جدید دریافت شد. برای دیدن، وارد برنامه شوید.",
                CHANNEL_ID_PRIVATE
            )

            // OTP Code Message
            payload.category == MessageCategory.OTP || payload.otpCode != null -> Triple(
                "کد تایید دریافت شد",
                "کد: ${payload.otpCode ?: "مشاهده در پیامک"} - ارسال شده از $displayName",
                CHANNEL_ID_OTP
            )

            // Banking Message
            payload.category == MessageCategory.BANK || payload.category == MessageCategory.TRANSACTIONS -> Triple(
                "تراکنش بانکی جدید ($displayName)",
                payload.body,
                CHANNEL_ID_BANK
            )

            // Spam Warning
            payload.category == MessageCategory.SPAM || payload.spamScore >= 70 -> Triple(
                "⚠️ پیامک مشکوک یا اسپم",
                "پیام از $displayName احتمالاً اسپم است (امتیاز: ${payload.spamScore}٪)",
                CHANNEL_ID_SPAM
            )

            // Default Normal Message
            else -> Triple(
                "پیام از $displayName",
                payload.body,
                CHANNEL_ID_DEFAULT
            )
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.sym_action_chat)
            .setContentTitle(title)
            .setContentText(content)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        // OTP Action Button
        if (payload.otpCode != null && !payload.isHidden) {
            val copyIntent = Intent("com.global.sms.ACTION_COPY_OTP").apply {
                putExtra("OTP_CODE", payload.otpCode)
            }
            val pendingCopy = PendingIntent.getBroadcast(
                context,
                payload.messageId.toInt(),
                copyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(android.R.drawable.ic_menu_save, "کپی کد تایید", pendingCopy)
        }

        notificationManager.notify(payload.messageId.toInt(), builder.build())
    }
}
