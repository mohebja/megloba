package com.global.sms.core.classifier

import com.global.sms.data.entity.ClassificationRuleEntity
import com.global.sms.data.entity.MessageCategory
import java.util.Locale
import java.util.regex.Pattern

data class ClassificationResult(
    val category: MessageCategory,
    val confidenceScore: Float, // 0.0f to 1.0f
    val matchedRuleName: String? = null,
    val matchedRulePriority: Int = 0,
    val detectedFeatures: List<String> = emptyList(),
    val featureWeightsMap: Map<MessageCategory, Float> = emptyMap()
)

object SmsClassifierEngine {

    /**
     * Default pre-populated Persian SMS classification rules with priority order.
     */
    val DEFAULT_RULES = listOf(
        ClassificationRuleEntity(
            id = 1,
            name = "کدهای تایید و ورود (OTP)",
            targetCategory = "OTP",
            keywords = "کد ورود, کد تایید, رمز یکبار مصرف, رمز پویا, otp, passcode, کد فعالسازی",
            senderPattern = "",
            ruleType = "KEYWORD",
            priority = 100
        ),
        ClassificationRuleEntity(
            id = 2,
            name = "تراکنش‌های واریز و برداشت بانکی",
            targetCategory = "TRANSACTIONS",
            keywords = "واریز, برداشت, مانده, موجودی, تراکنش, کارت به کارت, صورتحساب",
            senderPattern = "",
            ruleType = "KEYWORD",
            priority = 95
        ),
        ClassificationRuleEntity(
            id = 3,
            name = "اطلاعیه‌های بانک و مؤسسات مالی",
            targetCategory = "BANK",
            keywords = "بانک, حساب, صادرات, ملت, ملی, تجارت, سامان, پاسارگاد, پارسیان, بلوبانک, مهر ایران, رفاه, کشاورزی",
            senderPattern = "Melli, Mellat, Saman, Blu, Tejarat, Saderat, Pasargad",
            ruleType = "COMBINED",
            priority = 90
        ),
        ClassificationRuleEntity(
            id = 4,
            name = "پیامک‌های کلاهبرداری و اسپم",
            targetCategory = "SPAM",
            keywords = "برنده, جایزه, قرعه کشی, شارژ رایگان, درآمد میلیون, وام فوری بدون ضامن, کلیک کنید",
            senderPattern = "983000*, 981000*",
            ruleType = "KEYWORD",
            priority = 85
        ),
        ClassificationRuleEntity(
            id = 5,
            name = "تخفیف و پیشنهادات تبلیغاتی",
            targetCategory = "ADVERTISEMENT",
            keywords = "تخفیف, حراج, فروش ویژه, ارسال رایگان, جشنواره, کد تخفیف, پیشنهاد ویژه",
            senderPattern = "",
            ruleType = "KEYWORD",
            priority = 80
        ),
        ClassificationRuleEntity(
            id = 6,
            name = "خرید اینترنتی و فروشگاه‌ها",
            targetCategory = "SHOPPING",
            keywords = "سفارش, دیجی‌کالا, دیجیکالا, ترب, بااسلام, اسنپ فود, سبد خرید, فاکتور خرید",
            senderPattern = "Digikala, Torob, SnappFood",
            ruleType = "COMBINED",
            priority = 75
        ),
        ClassificationRuleEntity(
            id = 7,
            name = "مرسولات پستی و پیک delivery",
            targetCategory = "DELIVERY",
            keywords = "تیپاکس, پست, مرسوله, کد رهگیری, مامور پست, توزیع پست, پیک پیشتاز, تحویل سفارش",
            senderPattern = "Tipax, Post",
            ruleType = "COMBINED",
            priority = 70
        ),
        ClassificationRuleEntity(
            id = 8,
            name = "سامانه‌های دولتی و قضایی",
            targetCategory = "GOVERNMENT",
            keywords = "ثنا, عدل ایران, قوه قضاییه, پلیس, خلافی, مالیات, تامین اجتماعی, سامانه, راهور",
            senderPattern = "SANA, POLICE, RAHVAR",
            ruleType = "COMBINED",
            priority = 65
        ),
        ClassificationRuleEntity(
            id = 9,
            name = "پیامک‌های کاری و اداری",
            targetCategory = "BUSINESS",
            keywords = "جلسه, قرارداد, شرکت, پروژه, صورتجلسه, پشتیبانی, هماهنگی کاری",
            senderPattern = "",
            ruleType = "KEYWORD",
            priority = 60
        )
    )

    /**
     * Main Classifier Entrypoint:
     * Evaluates incoming message against active priority rules first, then uses
     * the ML feature weight vector engine if no priority rule matches.
     */
    fun classifyMessage(
        sender: String,
        body: String,
        customRules: List<ClassificationRuleEntity> = emptyList()
    ): ClassificationResult {
        val rulesToEvaluate = if (customRules.isNotEmpty()) {
            customRules.filter { it.isEnabled }.sortedByDescending { it.priority }
        } else {
            DEFAULT_RULES.sortedByDescending { it.priority }
        }

        val cleanSender = sender.trim().lowercase(Locale.ROOT)
        val cleanBody = body.trim().lowercase(Locale.ROOT)

        // -------------------------------------------------------------
        // LAYER 1: Priority Rule Engine (Custom & System Rules)
        // -------------------------------------------------------------
        for (rule in rulesToEvaluate) {
            val matched = evaluateRule(rule, cleanSender, cleanBody)
            if (matched) {
                val targetCat = parseCategoryString(rule.targetCategory)
                val matchedFeatures = extractMatchedKeywords(rule.keywords, cleanBody)
                return ClassificationResult(
                    category = targetCat,
                    confidenceScore = 0.98f,
                    matchedRuleName = rule.name,
                    matchedRulePriority = rule.priority,
                    detectedFeatures = matchedFeatures.ifEmpty { listOf("قانون اولویت‌دار: ${rule.name}") }
                )
            }
        }

        // -------------------------------------------------------------
        // LAYER 2: ML-Ready Statistical Feature Scoring Engine
        // -------------------------------------------------------------
        val featureWeights = computeMlFeatureWeights(cleanSender, cleanBody)
        val bestCategoryEntry = featureWeights.maxByOrNull { it.value }

        val bestCategory = bestCategoryEntry?.key ?: MessageCategory.UNKNOWN
        val topScore = bestCategoryEntry?.value ?: 0.0f

        val detectedFeaturesList = extractDetectedFeatureTokens(cleanBody, cleanSender)

        // Calculate normalized confidence score (0.0 to 1.0)
        val confidence = when {
            topScore >= 2.5f -> 0.95f
            topScore >= 1.5f -> 0.82f
            topScore >= 0.8f -> 0.65f
            topScore > 0.0f -> 0.45f
            else -> 0.10f
        }

        val finalCategory = if (confidence < 0.30f) {
            if (cleanBody.length < 50 && !cleanSender.contains("1000") && !cleanSender.contains("3000")) {
                MessageCategory.PERSONAL
            } else {
                MessageCategory.UNKNOWN
            }
        } else {
            bestCategory
        }

        return ClassificationResult(
            category = finalCategory,
            confidenceScore = confidence,
            matchedRuleName = null,
            matchedRulePriority = 0,
            detectedFeatures = detectedFeaturesList,
            featureWeightsMap = featureWeights
        )
    }

    private fun evaluateRule(
        rule: ClassificationRuleEntity,
        cleanSender: String,
        cleanBody: String
    ): Boolean {
        val keywords = rule.keywords.split(",")
            .map { it.trim().lowercase(Locale.ROOT) }
            .filter { it.isNotEmpty() }

        val senderPatterns = rule.senderPattern.split(",")
            .map { it.trim().lowercase(Locale.ROOT) }
            .filter { it.isNotEmpty() }

        val keywordMatched = keywords.isNotEmpty() && keywords.any { kw ->
            if (rule.ruleType == "REGEX") {
                try { Pattern.compile(kw).matcher(cleanBody).find() } catch (e: Exception) { cleanBody.contains(kw) }
            } else {
                cleanBody.contains(kw)
            }
        }

        val senderMatched = senderPatterns.isNotEmpty() && senderPatterns.any { sp ->
            if (sp.endsWith("*")) {
                cleanSender.startsWith(sp.removeSuffix("*"))
            } else {
                cleanSender.contains(sp)
            }
        }

        return when (rule.ruleType) {
            "SENDER" -> senderMatched
            "KEYWORD" -> keywordMatched
            "REGEX" -> keywordMatched
            "COMBINED" -> keywordMatched || senderMatched
            else -> keywordMatched || senderMatched
        }
    }

    private fun computeMlFeatureWeights(
        cleanSender: String,
        cleanBody: String
    ): Map<MessageCategory, Float> {
        val scores = mutableMapOf<MessageCategory, Float>()

        // Initialize all 11 categories with baseline prior
        MessageCategory.entries.forEach { cat -> scores[cat] = 0.0f }

        // 1. OTP Feature Tokens
        val otpKeywords = listOf("کد ورود", "کد تایید", "رمز یکبار", "رمز پویا", "کد فعالسازی", "otp", "passcode", "رمز شما")
        otpKeywords.forEach { kw ->
            if (cleanBody.contains(kw)) scores[MessageCategory.OTP] = (scores[MessageCategory.OTP] ?: 0f) + 1.2f
        }
        if (cleanBody.matches(Regex(".*\\b\\d{4,6}\\b.*")) && (cleanBody.contains("کد") || cleanBody.contains("رمز"))) {
            scores[MessageCategory.OTP] = (scores[MessageCategory.OTP] ?: 0f) + 1.5f
        }

        // 2. Transaction Feature Tokens
        val txKeywords = listOf("واریز", "برداشت", "مانده", "موجودی", "تراکنش", "کارت به کارت", "انتقال", "صورتحساب")
        txKeywords.forEach { kw ->
            if (cleanBody.contains(kw)) scores[MessageCategory.TRANSACTIONS] = (scores[MessageCategory.TRANSACTIONS] ?: 0f) + 1.2f
        }

        // 3. Bank Feature Tokens
        val bankKeywords = listOf("بانک", "حساب", "صادرات", "ملت", "ملی", "تجارت", "سامان", "پاسارگاد", "پارسیان", "بلوبانک", "مهر ایران", "رفاه", "کشاورزی")
        bankKeywords.forEach { kw ->
            if (cleanBody.contains(kw) || cleanSender.contains(kw)) {
                scores[MessageCategory.BANK] = (scores[MessageCategory.BANK] ?: 0f) + 1.0f
            }
        }

        // 4. Spam Feature Tokens
        val spamKeywords = listOf("برنده", "جایزه", "قرعه کشی", "شارژ رایگان", "درآمد میلیون", "وام فوری بدون ضامن", "کلیک کنید", "ثبت نام مجانی")
        spamKeywords.forEach { kw ->
            if (cleanBody.contains(kw)) scores[MessageCategory.SPAM] = (scores[MessageCategory.SPAM] ?: 0f) + 1.5f
        }

        // 5. Advertisement Feature Tokens
        val adKeywords = listOf("تخفیف", "حراج", "فروش ویژه", "ارسال رایگان", "جشنواره", "کد تخفیف", "پیشنهاد ویژه", "تمدید شد")
        adKeywords.forEach { kw ->
            if (cleanBody.contains(kw)) scores[MessageCategory.ADVERTISEMENT] = (scores[MessageCategory.ADVERTISEMENT] ?: 0f) + 1.1f
        }

        // 6. Shopping Feature Tokens
        val shopKeywords = listOf("سفارش", "دیجی‌کالا", "دیجیکالا", "ترب", "بااسلام", "اسنپ فود", "سبد خرید", "فاکتور خرید")
        shopKeywords.forEach { kw ->
            if (cleanBody.contains(kw) || cleanSender.contains(kw)) {
                scores[MessageCategory.SHOPPING] = (scores[MessageCategory.SHOPPING] ?: 0f) + 1.2f
            }
        }

        // 7. Delivery Feature Tokens
        val deliveryKeywords = listOf("تیپاکس", "پست", "مرسوله", "کد رهگیری", "مامور پست", "توزیع پست", "پیک پیشتاز", "تحویل سفارش")
        deliveryKeywords.forEach { kw ->
            if (cleanBody.contains(kw) || cleanSender.contains(kw)) {
                scores[MessageCategory.DELIVERY] = (scores[MessageCategory.DELIVERY] ?: 0f) + 1.3f
            }
        }

        // 8. Government Feature Tokens
        val govKeywords = listOf("ثنا", "عدل ایران", "قوه قضاییه", "پلیس", "خلافی", "مالیات", "تامین اجتماعی", "سامانه", "راهور")
        govKeywords.forEach { kw ->
            if (cleanBody.contains(kw) || cleanSender.contains(kw)) {
                scores[MessageCategory.GOVERNMENT] = (scores[MessageCategory.GOVERNMENT] ?: 0f) + 1.4f
            }
        }

        // 9. Business Feature Tokens
        val bizKeywords = listOf("جلسه", "قرارداد", "شرکت", "پروژه", "صورتجلسه", "پشتیبانی", "هماهنگی کاری")
        bizKeywords.forEach { kw ->
            if (cleanBody.contains(kw)) scores[MessageCategory.BUSINESS] = (scores[MessageCategory.BUSINESS] ?: 0f) + 1.0f
        }

        return scores
    }

    private fun extractDetectedFeatureTokens(cleanBody: String, cleanSender: String): List<String> {
        val tokens = mutableListOf<String>()
        val vocabulary = listOf(
            "کد ورود", "کد تایید", "رمز پویا", "رمز یکبار", "واریز", "برداشت", "موجودی", "مانده",
            "بانک", "حساب", "صادرات", "ملت", "ملی", "تجارت", "سامان", "بلوبانک",
            "برنده", "جایزه", "قرعه کشی", "شارژ رایگان", "تخفیف", "حراج", "فروش ویژه",
            "دیجی‌کالا", "ترب", "اسنپ", "تیپاکس", "پست", "مرسوله", "کد رهگیری",
            "ثنا", "عدل ایران", "قوه قضاییه", "پلیس", "خلافی", "مالیات"
        )
        vocabulary.forEach { word ->
            if (cleanBody.contains(word) || cleanSender.contains(word)) {
                tokens.add(word)
            }
        }
        return tokens
    }

    private fun extractMatchedKeywords(keywordsStr: String, cleanBody: String): List<String> {
        return keywordsStr.split(",")
            .map { it.trim().lowercase(Locale.ROOT) }
            .filter { it.isNotEmpty() && cleanBody.contains(it) }
    }

    fun parseCategoryString(catStr: String): MessageCategory {
        return try {
            MessageCategory.valueOf(catStr.uppercase(Locale.ROOT))
        } catch (e: Exception) {
            when (catStr.uppercase(Locale.ROOT)) {
                "BANK", "بانک" -> MessageCategory.BANK
                "TRANSACTIONS", "تراکنش" -> MessageCategory.TRANSACTIONS
                "OTP", "رمز" -> MessageCategory.OTP
                "SPAM", "اسپم" -> MessageCategory.SPAM
                "ADVERTISEMENT", "تبلیغات" -> MessageCategory.ADVERTISEMENT
                "BUSINESS", "تجاری" -> MessageCategory.BUSINESS
                "SHOPPING", "خرید" -> MessageCategory.SHOPPING
                "DELIVERY", "پست" -> MessageCategory.DELIVERY
                "GOVERNMENT", "دولتی" -> MessageCategory.GOVERNMENT
                "PERSONAL", "شخصی" -> MessageCategory.PERSONAL
                else -> MessageCategory.UNKNOWN
            }
        }
    }
}
