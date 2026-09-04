package com.global.sms.core.ai.otp

import com.global.sms.core.ai.nlp.LocalNlpEngine

data class ExtractedOtp(
    val code: String,
    val startIndex: Int,
    val endIndex: Int,
    val isCopyReady: Boolean = true
)

/**
 * Extracts and highlights OTP verification digits from raw message content.
 * Guarantees zero auto-execution of embedded links or USSD strings.
 */
object OtpExtractor {

    private val PATTERNS = listOf(
        Regex("(?:کد|رمز|code|passcode|otp|pin)\\s*[:\\-=\\s]*([0-9]{4,8})", RegexOption.IGNORE_CASE),
        Regex("\\b([0-9]{4,8})\\b")
    )

    fun extractCode(body: String): String? {
        val result = extractDetails(body)
        return result?.code
    }

    fun extractDetails(body: String): ExtractedOtp? {
        val normalized = LocalNlpEngine.normalizeDigits(body)

        for (pattern in PATTERNS) {
            val match = pattern.find(normalized)
            if (match != null) {
                val group = if (match.groupValues.size > 1) match.groupValues[1] else match.value
                if (group.length in 4..8 && group.all { it.isDigit() }) {
                    val start = match.range.first
                    val end = match.range.last + 1
                    return ExtractedOtp(
                        code = group,
                        startIndex = start,
                        endIndex = end,
                        isCopyReady = true
                    )
                }
            }
        }
        return null
    }
}
