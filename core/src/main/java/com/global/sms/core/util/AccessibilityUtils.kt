package com.global.sms.core.util

data class HighContrastColorScheme(
    val backgroundArgb: Long = 0xFF000000L,
    val surfaceArgb: Long = 0xFF121212L,
    val primaryArgb: Long = 0xFFFFFF00L, // High Contrast Yellow
    val onPrimaryArgb: Long = 0xFF000000L,
    val textPrimaryArgb: Long = 0xFFFFFFFFL,
    val textSecondaryArgb: Long = 0xFF00FFFFL // Cyan
)

object AccessibilityUtils {

    /**
     * Generate screen reader (TalkBack) localized description for SMS list item.
     */
    fun buildSmsTalkBackDescription(
        senderName: String,
        address: String,
        bodyText: String,
        isUnread: Boolean,
        category: String
    ): String {
        val statusStr = if (isUnread) "خوانده نشده" else "خوانده شده"
        val displayName = senderName.ifBlank { address }
        return "پیامک از طرف $displayName. دسته‌بندی $category. وضعیت: $statusStr. متن پیام: $bodyText"
    }

    /**
     * Interpret voice navigation commands in Persian or English for hands-free navigation.
     */
    fun parseVoiceNavigationCommand(voiceInput: String): String? {
        val normalized = voiceInput.lowercase().trim()

        return when {
            normalized.contains("پیام جدید") || normalized.contains("ارسال") || normalized.contains("compose") -> "compose"
            normalized.contains("جستجو") || normalized.contains("search") -> "search"
            normalized.contains("بانک") || normalized.contains("حساب") || normalized.contains("bank") -> "bank"
            normalized.contains("رمز پویا") || normalized.contains("کد پویا") || normalized.contains("otp") -> "otp_center"
            normalized.contains("تنظیمات") || normalized.contains("settings") -> "settings"
            normalized.contains("گاوصندوق") || normalized.contains("vault") -> "private_vault"
            else -> null
        }
    }
}
