package com.global.sms.core.ai.otp

import com.global.sms.core.ai.nlp.LocalNlpEngine
import java.util.Locale

object OtpIntelligenceEngine {

    private val OTP_PATTERNS = listOf(
        Regex("کد\\s*(تایید|ورود|فعالسازی|پویا)\\s*[:\\-]?\\s*(\\d{4,8})", RegexOption.IGNORE_CASE),
        Regex("رمز\\s*(پویا|یکبار\\s*مصرف)\\s*[:\\-]?\\s*(\\d{4,8})", RegexOption.IGNORE_CASE),
        Regex("(passcode|otp|verification code|code)\\s*is\\s*[:\\-]?\\s*(\\d{4,8})", RegexOption.IGNORE_CASE),
        Regex("\\b(\\d{4,8})\\b")
    )

    /**
     * Detects and extracts OTP verification code from Persian or English SMS.
     */
    fun extractOtpCode(body: String): String? {
        val cleanBody = LocalNlpEngine.normalizeDigits(body)
        val lowerBody = cleanBody.lowercase(Locale.ROOT)

        val isOtpContext = lowerBody.contains("کد") || lowerBody.contains("رمز") ||
                lowerBody.contains("ورود") || lowerBody.contains("تایید") ||
                lowerBody.contains("otp") || lowerBody.contains("code") ||
                lowerBody.contains("passcode") || lowerBody.contains("verification")

        if (!isOtpContext) return null

        for (pattern in OTP_PATTERNS) {
            val match = pattern.find(cleanBody)
            if (match != null) {
                val code = if (match.groupValues.size > 1) match.groupValues.last() else match.value
                if (code.length in 4..8 && code.all { it.isDigit() }) {
                    return code
                }
            }
        }
        return null
    }
}
