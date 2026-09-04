package com.global.sms.core.ai.fraud

import com.global.sms.core.ai.nlp.LocalNlpEngine
import com.global.sms.core.ai.url.SmartUrlSecurityAnalyzer
import java.util.Locale

enum class SpamAction {
    NONE,
    WARN_USER,
    MOVE_TO_SPAM
}

data class AdvancedSpamReport(
    val spamScore: Int, // 0 to 100
    val isSpam: Boolean,
    val action: SpamAction,
    val factors: List<String>,
    val canUserOverride: Boolean = true
)

/**
 * Advanced Spam Detector with multi-factor scoring (0 to 100).
 */
object AdvancedSpamDetector {

    private val SPAM_PATTERNS = listOf(
        "برنده", "قرعه کشی", "جایزه میلیون", "شارژ رایگان", "وام بدون ضامن",
        "کسب درآمد", "سود تضمینی", "کلیک کنید", "درگاه پرداخت", "ثبت نام رایگان",
        "تخفیف ویژه امروز", "ارسال عدد 1", "ارسال 1"
    )

    fun evaluateSpam(
        sender: String,
        body: String,
        isKnownContact: Boolean = false,
        messageRepeatCount: Int = 1,
        userReportedSpamCount: Int = 0
    ): AdvancedSpamReport {
        var score = 0
        val factors = mutableListOf<String>()

        val cleanSender = sender.trim().lowercase(Locale.ROOT)
        val cleanBody = LocalNlpEngine.normalizeDigits(body).lowercase(Locale.ROOT)

        // 1. Unknown / Non-contact Sender Factor
        if (!isKnownContact) {
            score += 15
            factors.add("فرستنده ناشناس و خارج از مخاطبین")
        }

        // 2. Repeated Message Factor
        if (messageRepeatCount > 2) {
            val add = (messageRepeatCount * 10).coerceAtMost(25)
            score += add
            factors.add("پیامک تکراری به تعداد $messageRepeatCount بار")
        }

        // 3. User Reports Factor
        if (userReportedSpamCount > 0) {
            score += 30
            factors.add("گزارش قبلی اسپم توسط کاربر")
        }

        // 4. Suspicious Links Factor
        val urls = SmartUrlSecurityAnalyzer.extractUrls(body)
        if (urls.isNotEmpty()) {
            var linkRisk = false
            for (url in urls) {
                val report = SmartUrlSecurityAnalyzer.analyzeUrl(url)
                if (report.isSuspicious) {
                    linkRisk = true
                    score += 35
                    factors.add("لینک غیرمجاز یا مشکوک (${report.domain})")
                }
            }
            if (!linkRisk) {
                score += 15
                factors.add("حاوی لینک اینترنتی")
            }
        }

        // 5. Advertisement / Phishing Patterns
        var patternMatches = 0
        for (pattern in SPAM_PATTERNS) {
            if (cleanBody.contains(pattern)) {
                patternMatches++
            }
        }
        if (patternMatches > 0) {
            val patternScore = (patternMatches * 20).coerceAtMost(40)
            score += patternScore
            factors.add("الگوهای تبلیغاتی و مشکوک ($patternMatches مورد)")
        }

        val finalScore = score.coerceIn(0, 100)

        val action = when {
            finalScore >= 70 -> SpamAction.MOVE_TO_SPAM
            finalScore >= 45 -> SpamAction.WARN_USER
            else -> SpamAction.NONE
        }

        return AdvancedSpamReport(
            spamScore = finalScore,
            isSpam = finalScore >= 70,
            action = action,
            factors = factors.ifEmpty { listOf("پیامک طبیعی و بدون ریسک") },
            canUserOverride = true
        )
    }
}
