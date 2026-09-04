package com.global.sms.core.ai.otp

import com.global.sms.core.ai.nlp.LocalNlpEngine
import java.util.Locale

data class OtpDetectionResult(
    val isOtp: Boolean,
    val otpCode: String? = null,
    val serviceName: String? = null,
    val expiryMinutes: Int? = null,
    val confidence: Float = 0.0f
)

/**
 * Intelligent OTP Detector for Persian and English verification, authentication, and login messages.
 */
object OtpDetector {

    private val OTP_KEYWORDS_PERSIAN = listOf(
        "کد ورود", "کد تایید", "کد فعالسازی", "رمز پویا", "رمز یکبار مصرف", "کد احراز هویّت", "کد بازیابی"
    )

    private val OTP_KEYWORDS_ENGLISH = listOf(
        "verification code", "login code", "passcode", "one-time password", "otp", "auth code", "security code"
    )

    fun detect(body: String): OtpDetectionResult {
        val normalized = LocalNlpEngine.normalizeDigits(body)
        val lower = normalized.lowercase(Locale.ROOT)

        val hasPersianKw = OTP_KEYWORDS_PERSIAN.any { lower.contains(it) }
        val hasEnglishKw = OTP_KEYWORDS_ENGLISH.any { lower.contains(it) }

        if (!hasPersianKw && !hasEnglishKw) {
            return OtpDetectionResult(isOtp = false)
        }

        val code = OtpExtractor.extractCode(body)
        val service = extractServiceName(normalized)
        val expiry = extractExpiryMinutes(normalized)

        val confidence = if (code != null) 0.98f else 0.60f

        return OtpDetectionResult(
            isOtp = true,
            otpCode = code,
            serviceName = service,
            expiryMinutes = expiry,
            confidence = confidence
        )
    }

    private fun extractServiceName(body: String): String? {
        val knownServices = mapOf(
            "دیجی‌کالا" to "Digikala",
            "دیجیکالا" to "Digikala",
            "اسنپ" to "Snapp",
            "تپسی" to "Tapsi",
            "روابط عمومی" to "Gov",
            "بانک" to "Bank",
            "دیوار" to "Divar",
            "شیپور" to "Sheypoor",
            "بلوبانک" to "BluBank",
            "تلگرام" to "Telegram",
            "واتساپ" to "WhatsApp",
            "گوگل" to "Google",
            "ایتا" to "Eitaa",
            "بله" to "Bale",
            "روبیکا" to "Rubika"
        )
        for ((key, value) in knownServices) {
            if (body.contains(key, ignoreCase = true)) return value
        }
        return null
    }

    private fun extractExpiryMinutes(body: String): Int? {
        val regex = Regex("(\\d+)\\s*(دقیقه|minute|min)")
        val match = regex.find(body)
        return match?.groupValues?.get(1)?.toIntOrNull()
    }
}
