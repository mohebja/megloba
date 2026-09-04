package com.global.sms.core.ai.url

import java.net.URI
import java.util.Locale

data class UrlSecurityCardReport(
    val originalUrl: String,
    val domain: String,
    val riskLevel: String, // "LOW", "MEDIUM", "HIGH"
    val isShortened: Boolean,
    val isSuspicious: Boolean,
    val reason: String
)

object SmartUrlSecurityAnalyzer {

    private val SHORTENER_DOMAINS = setOf(
        "bit.ly", "t.co", "goo.gl", "tinyurl.com", "is.gd", "buff.ly", "adf.ly", "rebrand.ly", "cutt.ly"
    )

    private val SUSPICIOUS_DOMAIN_KEYWORDS = listOf(
        "bank", "melli", "mellat", "saman", "shaparak", "sana", "pishkhan", "shaparak-pay", "login", "verify"
    )

    fun extractUrls(text: String): List<String> {
        val urlRegex = Regex("(https?://[\\w\\d.-]+(?:/[\\w\\d./?%&=-]*)?)", RegexOption.IGNORE_CASE)
        return urlRegex.findAll(text).map { it.value }.toList()
    }

    fun analyzeUrl(rawUrl: String): UrlSecurityCardReport {
        val cleanUrl = if (!rawUrl.startsWith("http://") && !rawUrl.startsWith("https://")) {
            "http://$rawUrl"
        } else rawUrl

        val domain = try {
            val uri = URI(cleanUrl)
            val host = uri.host ?: cleanUrl
            host.lowercase(Locale.ROOT)
        } catch (e: Exception) {
            cleanUrl.lowercase(Locale.ROOT)
        }

        val isShortened = SHORTENER_DOMAINS.any { domain.contains(it) }

        var isSuspicious = false
        var reason = "دامنه رسمی و بدون مشکلی مشاهده شد."
        var riskLevel = "LOW"

        // Check if shortened domain
        if (isShortened) {
            isSuspicious = true
            riskLevel = "MEDIUM"
            reason = "استفاده از لینک کوتاه‌شده (مقصد نهایی پنهان است)."
        }

        // Check suspicious keywords in domain that mimic official government or bank sites
        for (kw in SUSPICIOUS_DOMAIN_KEYWORDS) {
            if (domain.contains(kw) && !domain.endsWith(".ir") && !domain.endsWith("shaparak.ir") && !domain.endsWith("bmi.ir") && !domain.endsWith("bankmellat.ir")) {
                isSuspicious = true
                riskLevel = "HIGH"
                reason = "شباهت جعلی دامنه با وب‌سایت‌های رسمی/بانکی (احتمال فیشینگ بالا)."
                break
            }
        }

        // Check IP address domain
        if (domain.matches(Regex("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}"))) {
            isSuspicious = true
            riskLevel = "HIGH"
            reason = "استفاده مستقیماً از آدرس IP به جای آدرس دامنه معتبر."
        }

        return UrlSecurityCardReport(
            originalUrl = rawUrl,
            domain = domain,
            riskLevel = riskLevel,
            isShortened = isShortened,
            isSuspicious = isSuspicious,
            reason = reason
        )
    }
}
