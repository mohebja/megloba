package com.global.sms.core.ai.fraud

import com.global.sms.core.ai.nlp.LocalNlpEngine
import com.global.sms.core.ai.url.SmartUrlSecurityAnalyzer
import java.util.Locale

enum class FraudRiskLevel {
    SAFE,
    WARNING,
    DANGEROUS
}

data class FraudAnalysisResult(
    val isFraud: Boolean,
    val riskScore: Float, // 0.0f (safe) to 1.0f (high risk)
    val riskLevel: FraudRiskLevel,
    val reasons: List<String>,
    val detectedUrls: List<String>
)

object SmartFraudDetector {

    private val SUSPICIOUS_KEYWORDS = listOf(
        "مسدود شد", "حساب شما مسدود", "قطع یارانه", "برنده ۵۰ میلیون", "برنده ۵۰", "قرعه کشی بانک",
        "امنیتی حساب", "رمز دوم شما تغییر کرد", "توقیف خودرو", "ابلاغیه الکترونیکی", "ثنا شکایت",
        "درآمد روزانه ۵ میلیون", "وام بدون ضامن", "کلیک کنید", "درگاه پرداخت", "احراز هویت بانک",
        "سود تضمینی", "پانزی", "سرمایه‌گذاری دیجیتال", "ارز دیجیتال رایگان", "استخراج بیت‌کوین",
        "رمز یکبار مصرف شما در اختیار دیگری", "کد ورود را ارسال کنید"
    )

    private val FAKE_BANKING_PATTERNS = listOf(
        "وارد لینک شوید", "حساب کاربری مسدود", "به روزرسانی اطلاعات حساب", "کارت شما مسدود"
    )

    fun analyzeMessage(sender: String, body: String): FraudAnalysisResult {
        val cleanBody = LocalNlpEngine.normalizeDigits(body.lowercase(Locale.ROOT))
        val cleanSender = sender.trim().lowercase(Locale.ROOT)

        var score = 0.0f
        val reasons = mutableListOf<String>()

        // 1. Unknown / Commercial numeric sender check
        val isNumericOnlySender = cleanSender.matches(Regex("\\+?\\d+"))
        val isShortCode = isNumericOnlySender && cleanSender.length in 4..6
        val isPersonalPhone = isNumericOnlySender && cleanSender.length >= 10

        // 2. URL Analysis
        val extractedUrls = SmartUrlSecurityAnalyzer.extractUrls(body)
        var dangerousUrlFound = false
        for (url in extractedUrls) {
            val urlReport = SmartUrlSecurityAnalyzer.analyzeUrl(url)
            if (urlReport.isSuspicious) {
                dangerousUrlFound = true
                score += 0.45f
                reasons.add("لینک مشکوک یا هدایت‌کننده (${urlReport.domain})")
            }
        }

        // 3. Banking Impersonation Check
        val containsBankingTerms = cleanBody.contains("بانک") || cleanBody.contains("حساب") || cleanBody.contains("کارت")
        if (containsBankingTerms && isPersonalPhone && extractedUrls.isNotEmpty()) {
            score += 0.40f
            reasons.add("ارسال پیامک بانکی همراه با لینک از شماره شخصی")
        }

        // 4. Phishing Keywords
        for (kw in SUSPICIOUS_KEYWORDS) {
            if (cleanBody.contains(kw)) {
                score += 0.30f
                reasons.add("محتوای مشکوک به کلاهبرداری ($kw)")
                break
            }
        }

        // 5. Fake Banking Patterns
        for (pattern in FAKE_BANKING_PATTERNS) {
            if (cleanBody.contains(pattern)) {
                score += 0.35f
                reasons.add("الگوی جعلی به‌روزرسانی یا مسدودی حساب")
                break
            }
        }

        val clampedScore = score.coerceIn(0.0f, 1.0f)
        val riskLevel = when {
            clampedScore >= 0.65f -> FraudRiskLevel.DANGEROUS
            clampedScore >= 0.35f -> FraudRiskLevel.WARNING
            else -> FraudRiskLevel.SAFE
        }

        return FraudAnalysisResult(
            isFraud = riskLevel == FraudRiskLevel.DANGEROUS,
            riskScore = clampedScore,
            riskLevel = riskLevel,
            reasons = reasons.ifEmpty { listOf("پیامک امن به‌نظر می‌رسد.") },
            detectedUrls = extractedUrls
        )
    }
}
