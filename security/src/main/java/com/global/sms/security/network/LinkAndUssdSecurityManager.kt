package com.global.sms.security.network

import java.net.URI
import java.util.regex.Pattern

enum class LinkRiskLevel {
    SAFE,
    SUSPICIOUS,
    HIGH_RISK
}

data class LinkScanResult(
    val url: String,
    val riskLevel: LinkRiskLevel,
    val warnings: List<String>
)

data class UssdScanResult(
    val isUssdCode: Boolean,
    val rawCode: String,
    val description: String,
    val isDangerous: Boolean
)

/**
 * Link Security & USSD Code Inspector.
 * Protects users against phishing links, drive-by APK downloads, and malicious carrier USSD code executions.
 */
object LinkAndUssdSecurityManager {

    private val IP_HOST_PATTERN = Pattern.compile("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$")
    private val SUSPICIOUS_TLDS = setOf("xyz", "top", "zip", "kim", "work", "fit", "country", "tk", "ml", "ga", "cf", "gq")
    private val URL_SHORTENERS = setOf("bit.ly", "tinyurl.com", "goo.gl", "is.gd", "buff.ly", "t.co", "ow.ly", "cutt.ly")
    private val EXECUTABLE_EXTENSIONS = setOf("apk", "exe", "bat", "scr", "vbs", "cmd", "msi", "jar")

    private val USSD_PATTERNS = listOf(
        Pattern.compile("^\\*\\*?21\\*.*#$"),   // Unconditional Call Forwarding
        Pattern.compile("^\\*\\*?61\\*.*#$"),   // Call Forwarding No Answer
        Pattern.compile("^\\*\\*?62\\*.*#$"),   // Call Forwarding Not Reachable
        Pattern.compile("^\\*\\*?67\\*.*#$"),   // Call Forwarding Busy
        Pattern.compile("^##002#$"),            // Cancel All Forwarding
        Pattern.compile("^\\*#06#$"),           // IMEI lookup
        Pattern.compile("^\\*#\\*#4636#\\*#\\*$") // Testing menu
    )

    /**
     * Scans a URL string for phishing, raw IP hosts, suspicious TLDs, and executable files.
     */
    fun scanUrl(urlString: String): LinkScanResult {
        val warnings = mutableListOf<String>()
        var risk = LinkRiskLevel.SAFE

        try {
            val formattedUrl = if (!urlString.startsWith("http://") && !urlString.startsWith("https://")) {
                "http://$urlString"
            } else urlString

            val uri = URI(formattedUrl)
            val host = uri.host ?: ""

            // 1. IP Host check
            if (IP_HOST_PATTERN.matcher(host).matches()) {
                warnings.add("آدرس وب‌سایت به صورت IP عددی مستقیم است (احتمال فیشینگ بالا).")
                risk = LinkRiskLevel.HIGH_RISK
            }

            // 2. HTTP unencrypted check
            if (formattedUrl.startsWith("http://")) {
                warnings.add("ارتباط ناامن بدون رمزنگاری SSL/TLS (HTTP).")
                if (risk < LinkRiskLevel.SUSPICIOUS) risk = LinkRiskLevel.SUSPICIOUS
            }

            // 3. Executable file download check
            val path = uri.path ?: ""
            val extension = path.substringAfterLast('.', "").lowercase()
            if (EXECUTABLE_EXTENSIONS.contains(extension)) {
                warnings.add("لینک حاوی فایل قابل اجرا ($extension) است.")
                risk = LinkRiskLevel.HIGH_RISK
            }

            // 4. Shortened URL check
            if (URL_SHORTENERS.contains(host.lowercase())) {
                warnings.add("لینک کوتاه شده است و مقصد اصلی مشخص نیست.")
                if (risk < LinkRiskLevel.SUSPICIOUS) risk = LinkRiskLevel.SUSPICIOUS
            }

            // 5. Suspicious TLD check
            val tld = host.substringAfterLast('.', "").lowercase()
            if (SUSPICIOUS_TLDS.contains(tld)) {
                warnings.add("پسوند دامنه ($tld) در لیست پسوندهای پرخطر قرار دارد.")
                if (risk < LinkRiskLevel.SUSPICIOUS) risk = LinkRiskLevel.SUSPICIOUS
            }

        } catch (e: Exception) {
            warnings.add("ساختار آدرس اینترنتی نامعتبر است.")
            risk = LinkRiskLevel.SUSPICIOUS
        }

        return LinkScanResult(url = urlString, riskLevel = risk, warnings = warnings)
    }

    /**
     * Inspects text for carrier USSD codes and detects malicious execution attempts.
     */
    fun inspectUssdCode(text: String): UssdScanResult {
        val trimmed = text.trim()
        val isUssd = (trimmed.startsWith("*") && trimmed.endsWith("#")) || (trimmed.startsWith("#") && trimmed.endsWith("#"))

        if (!isUssd) {
            return UssdScanResult(isUssdCode = false, rawCode = trimmed, description = "", isDangerous = false)
        }

        var isDangerous = false
        var description = "کد USSD عمومی اپراتور"

        for (pattern in USSD_PATTERNS) {
            if (pattern.matcher(trimmed).matches()) {
                isDangerous = true
                description = "کد حساس انحراف مکالمات/تنظیمات سیم‌کارت (Call Forwarding/Carrier MMI)"
                break
            }
        }

        return UssdScanResult(
            isUssdCode = true,
            rawCode = trimmed,
            description = description,
            isDangerous = isDangerous
        )
    }
}
