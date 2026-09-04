package com.global.sms.core.security

import java.util.regex.Pattern

data class PhishingScanResult(
    val isSpamOrPhishing: Boolean,
    val threatLevel: ThreatLevel = ThreatLevel.LOW,
    val detectedUrls: List<String> = emptyList(),
    val warningReason: String? = null
)

enum class ThreatLevel {
    LOW, MEDIUM, HIGH, CRITICAL
}

object PhishingDetector {

    private val URL_PATTERN = Pattern.compile(
        "(https?://[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}(?:/[^\\s]*)?)",
        Pattern.CASE_INSENSITIVE
    )

    private val SUSPICIOUS_DOMAINS = listOf(
        "bit.ly", "tinyurl.com", "goo.gl", "cutt.ly", "t.co", "is.gd",
        "xyz", "top", "online", "site", "vip", "club", "info", "work"
    )

    private val SPAM_KEYWORDS = listOf(
        "برنده", "جایزه", "قرعه کشی", "وام بدون ضامن", "شارژ رایگان", "پیشنهاد ویژه",
        "کسب درآمد", "لینک زیر", "ثبت نام فوری", "ارز دیجیتال", "سهام عدالت",
        "Winner", "Free Gift", "Lottery", "Claim Now", "Click Here", "Urgent"
    )

    fun scanMessage(sender: String, body: String): PhishingScanResult {
        val detectedUrls = mutableListOf<String>()
        val matcher = URL_PATTERN.matcher(body)
        while (matcher.find()) {
            matcher.group(1)?.let { detectedUrls.add(it) }
        }

        val hasSuspiciousUrl = detectedUrls.any { url ->
            SUSPICIOUS_DOMAINS.any { domain -> url.lowercase().contains(domain) }
        }

        val containsSpamKeyword = SPAM_KEYWORDS.any { body.contains(it, ignoreCase = true) }
        val isNumericUnknownSender = sender.length in 4..6 && !sender.startsWith("+")

        return when {
            hasSuspiciousUrl && containsSpamKeyword -> PhishingScanResult(
                isSpamOrPhishing = true,
                threatLevel = ThreatLevel.CRITICAL,
                detectedUrls = detectedUrls,
                warningReason = "هشدار: پیام حاوی لینک مشکوک و عبارات تبلیغاتی مکرر است!"
            )
            hasSuspiciousUrl -> PhishingScanResult(
                isSpamOrPhishing = true,
                threatLevel = ThreatLevel.HIGH,
                detectedUrls = detectedUrls,
                warningReason = "هشدار: لینک موجود در پیام مشکوک است. قبل از باز کردن بررسی کنید."
            )
            containsSpamKeyword || isNumericUnknownSender -> PhishingScanResult(
                isSpamOrPhishing = true,
                threatLevel = ThreatLevel.MEDIUM,
                detectedUrls = detectedUrls,
                warningReason = "شناسایی شده به عنوان پیام تبلیغاتی یا ناشناس"
            )
            else -> PhishingScanResult(
                isSpamOrPhishing = false,
                threatLevel = ThreatLevel.LOW,
                detectedUrls = detectedUrls,
                warningReason = null
            )
        }
    }
}
