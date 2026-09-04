package com.global.sms.core.util

/**
 * SMS length, encoding, and concatenation segmenter.
 */
data class SmsSegmentInfo(
    val charCount: Int,
    val segmentCount: Int,
    val remainingInSegment: Int,
    val isUnicode: Boolean
)

object SmsSegmenter {

    private const val GSM_SINGLE_LIMIT = 160
    private const val GSM_CONCAT_LIMIT = 153
    private const val UNICODE_SINGLE_LIMIT = 70
    private const val UNICODE_CONCAT_LIMIT = 67

    /**
     * Calculates the segment count and remaining characters for a given SMS text.
     */
    fun calculateSegments(text: String): SmsSegmentInfo {
        val isUnicode = PersianUtils.containsPersian(text) || containsNonGsmChars(text)
        val len = text.length

        if (len == 0) {
            return SmsSegmentInfo(
                charCount = 0,
                segmentCount = 1,
                remainingInSegment = if (isUnicode) UNICODE_SINGLE_LIMIT else GSM_SINGLE_LIMIT,
                isUnicode = isUnicode
            )
        }

        val limitSingle = if (isUnicode) UNICODE_SINGLE_LIMIT else GSM_SINGLE_LIMIT
        val limitConcat = if (isUnicode) UNICODE_CONCAT_LIMIT else GSM_CONCAT_LIMIT

        return if (len <= limitSingle) {
            SmsSegmentInfo(
                charCount = len,
                segmentCount = 1,
                remainingInSegment = limitSingle - len,
                isUnicode = isUnicode
            )
        } else {
            val segments = (len + limitConcat - 1) / limitConcat
            val remaining = (segments * limitConcat) - len
            SmsSegmentInfo(
                charCount = len,
                segmentCount = segments,
                remainingInSegment = remaining,
                isUnicode = isUnicode
            )
        }
    }

    private fun containsNonGsmChars(text: String): Boolean {
        for (c in text.toCharArray()) {
            if (c.code > 127) return true
        }
        return false
    }
}
