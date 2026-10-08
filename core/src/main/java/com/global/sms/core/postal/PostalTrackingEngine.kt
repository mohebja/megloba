package com.global.sms.core.postal

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.global.sms.core.util.PersianUtils
import java.util.regex.Pattern

data class PostalTrackingInfo(
    val trackingCode: String,
    val formattedCode: String,
    val trackingUrl: String,
    val carrierName: String = "شرکت ملی پست جمهوری اسلامی ایران"
)

object PostalTrackingEngine {

    // Regex for 20-24 digit standard postal codes, or codes following common keywords
    private val POSTAL_24_DIGIT_REGEX = Pattern.compile("\\b(\\d{20,24})\\b")
    private val POSTAL_KEYWORD_REGEX = Pattern.compile("(?:مرسوله|پست|رهگیری|رهگیری پستی|بارنامه)[^\\d]{0,15}(\\d{16,24})")

    /**
     * Extracts Iran Post 20-24 digit tracking code from SMS body text.
     */
    fun extractPostalTracking(body: String): PostalTrackingInfo? {
        val normalized = PersianUtils.toEnglishDigits(body)

        // 1. Try keyword pattern
        val kwMatcher = POSTAL_KEYWORD_REGEX.matcher(normalized)
        if (kwMatcher.find()) {
            val code = kwMatcher.group(1)
            if (code != null && code.length >= 16) {
                return buildPostalInfo(code)
            }
        }

        // 2. Try 20-24 digit barcode pattern if text relates to shipping/delivery
        val isShippingRelated = body.contains("پست") ||
                body.contains("مرسوله") ||
                body.contains("پیشتاز") ||
                body.contains("ارسال") ||
                body.contains("بسته") ||
                body.contains("تیپاکس") ||
                body.contains("رهگیری")

        if (isShippingRelated) {
            val digitMatcher = POSTAL_24_DIGIT_REGEX.matcher(normalized)
            if (digitMatcher.find()) {
                val code = digitMatcher.group(1)
                if (code != null) {
                    return buildPostalInfo(code)
                }
            }
        }

        return null
    }

    private fun buildPostalInfo(code: String): PostalTrackingInfo {
        val url = "https://tracking.post.ir/?id=$code"
        val formatted = PersianUtils.toPersianDigits(code)
        return PostalTrackingInfo(
            trackingCode = code,
            formattedCode = formatted,
            trackingUrl = url
        )
    }

    /**
     * Creates an Intent to open the tracking page in browser.
     */
    fun createTrackingIntent(trackingCode: String): Intent {
        val url = "https://tracking.post.ir/?id=$trackingCode"
        return Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
}
