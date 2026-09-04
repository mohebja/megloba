package com.global.sms.core.ai.classifier

import com.global.sms.data.entity.MessageCategory
import java.util.Locale

/**
 * Normalization utilities for Persian, Arabic, and English SMS text analysis.
 */
object TextNormalizer {

    private val PERSIAN_DIGITS = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    private val ARABIC_DIGITS = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    private val LATIN_DIGITS = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')

    /**
     * Normalizes digits and unifies Arabic/Persian character variants ('ي' -> 'ی', 'ك' -> 'ک').
     */
    fun normalizeText(text: String): String {
        var result = text
        for (i in 0..9) {
            result = result.replace(PERSIAN_DIGITS[i], LATIN_DIGITS[i])
            result = result.replace(ARABIC_DIGITS[i], LATIN_DIGITS[i])
        }
        return result
            .replace('ي', 'ی')
            .replace('ك', 'ک')
            .replace('ة', 'ه')
            .replace('إ', 'ا')
            .replace('أ', 'ا')
            .replace('آ', 'ا')
            .trim()
    }
}

data class DetailedClassificationResult(
    val category: MessageCategory,
    val confidenceScore: Float, // 0.0f to 1.0f (e.g. 0.96f = 96%)
    val categoryLabelPersian: String,
    val categoryLabelEnglish: String,
    val detectedKeywords: List<String>,
    val explanation: String
)

/**
 * Core Engine for Rule-based and Heuristic Statistical AI Classification.
 * Fully on-device, offline-first, supporting Persian, English, and Arabic messages.
 */
object MessageClassificationEngine {

    // Common Known Sender Reputation Mapping
    private val KNOWN_BANK_SENDERS = setOf(
        "melli", "mellat", "saman", "blu", "blubank", "tejarat", "saderat", "pasargad",
        "parsian", "refah", "keshavarzi", "bms", "maskan", "khavarmianeh", "sinabank"
    )

    private val KNOWN_DELIVERY_SENDERS = setOf(
        "tipax", "post", "post_ir", "alopeyk", "snappbox", "mpexpress", "chapar"
    )

    private val KNOWN_SHOPPING_SENDERS = setOf(
        "digikala", "snappfood", "torob", "basalam", "okala", "technolife", "mobit"
    )

    fun classify(
        sender: String,
        body: String,
        senderReputationScore: Float = 0.5f // 0.0 (Spam/Blocked) to 1.0 (Trusted/Saved Contact)
    ): DetailedClassificationResult {
        val normalizedBody = TextNormalizer.normalizeText(body).lowercase(Locale.ROOT)
        val normalizedSender = TextNormalizer.normalizeText(sender).lowercase(Locale.ROOT)

        // 1. OTP Check (Highest Priority)
        val otpScore = evaluateOtpScore(normalizedSender, normalizedBody)
        if (otpScore > 0.70f) {
            return DetailedClassificationResult(
                category = MessageCategory.OTP,
                confidenceScore = (otpScore * 100).coerceAtMost(99f) / 100f,
                categoryLabelPersian = "کد تایید و ورود",
                categoryLabelEnglish = "OTP / Verification Code",
                detectedKeywords = listOf("کد تایید", "رمز پویا", "otp", "code"),
                explanation = "کد تایید یا رمز ورود یکبار مصرف شناسایی شد."
            )
        }

        // 2. Banking & Financial Check
        val bankScore = evaluateBankScore(normalizedSender, normalizedBody)
        if (bankScore > 0.65f) {
            return DetailedClassificationResult(
                category = MessageCategory.BANK,
                confidenceScore = (bankScore * 100).coerceAtMost(98f) / 100f,
                categoryLabelPersian = "تراکنش و امور بانکی",
                categoryLabelEnglish = "Banking / Financial",
                detectedKeywords = listOf("واریز", "برداشت", "موجودی", "حساب", "بانک"),
                explanation = "تراکنش یا اطلاعیه حساب بانکی تشخيص داده شد."
            )
        }

        // 3. Spam Check
        val spamScore = evaluateSpamScore(normalizedSender, normalizedBody, senderReputationScore)
        if (spamScore > 0.75f) {
            return DetailedClassificationResult(
                category = MessageCategory.SPAM,
                confidenceScore = (spamScore * 100).coerceAtMost(99f) / 100f,
                categoryLabelPersian = "اسپم و مشکوک",
                categoryLabelEnglish = "Spam / Fraud",
                detectedKeywords = listOf("برنده", "قرعه کشی", "وام بدون ضامن", "کلیک کنید"),
                explanation = "پیامک حاوی الگوهای مشکوک یا تبلیغات کلاهبردارانه است."
            )
        }

        // 4. Delivery & Post Check
        val deliveryScore = evaluateDeliveryScore(normalizedSender, normalizedBody)
        if (deliveryScore > 0.60f) {
            return DetailedClassificationResult(
                category = MessageCategory.DELIVERY,
                confidenceScore = (deliveryScore * 100).coerceAtMost(95f) / 100f,
                categoryLabelPersian = "ارسال و مرسولات پستی",
                categoryLabelEnglish = "Delivery / Shipping",
                detectedKeywords = listOf("پست", "مرسوله", "کد رهگیری", "تیپاکس"),
                explanation = "اطلاعیه تحویل یا کد رهگیری مرسوله پستی."
            )
        }

        // 5. Shopping & E-Commerce Check
        val shoppingScore = evaluateShoppingScore(normalizedSender, normalizedBody)
        if (shoppingScore > 0.60f) {
            return DetailedClassificationResult(
                category = MessageCategory.SHOPPING,
                confidenceScore = (shoppingScore * 100).coerceAtMost(95f) / 100f,
                categoryLabelPersian = "خرید و سفارش‌ها",
                categoryLabelEnglish = "Shopping / Orders",
                detectedKeywords = listOf("سفارش", "فاکتور", "سبد خرید", "دیجی‌کالا"),
                explanation = "ثبت یا پیگیری سفارش خرید اینترنتی."
            )
        }

        // 6. Advertisement Check
        val adScore = evaluateAdScore(normalizedSender, normalizedBody)
        if (adScore > 0.60f) {
            return DetailedClassificationResult(
                category = MessageCategory.ADVERTISEMENT,
                confidenceScore = (adScore * 100).coerceAtMost(95f) / 100f,
                categoryLabelPersian = "تبلیغات و حراج",
                categoryLabelEnglish = "Advertisement",
                detectedKeywords = listOf("تخفیف", "حراج", "فروش ویژه", "جشنواره"),
                explanation = "پیامک تبلیغاتی یا پیشنهاد ویژه فروش."
            )
        }

        // 7. Work / Business Check
        val workScore = evaluateWorkScore(normalizedSender, normalizedBody)
        if (workScore > 0.60f) {
            return DetailedClassificationResult(
                category = MessageCategory.WORK,
                confidenceScore = (workScore * 100).coerceAtMost(92f) / 100f,
                categoryLabelPersian = "کاری و اداری",
                categoryLabelEnglish = "Work / Business",
                detectedKeywords = listOf("جلسه", "پروژه", "صورتجلسه", "قرار داد"),
                explanation = "محتوای مرتبط با امور شغلی و هماهنگی کاری."
            )
        }

        // 8. Important Check
        val importantScore = evaluateImportantScore(normalizedSender, normalizedBody)
        if (importantScore > 0.60f) {
            return DetailedClassificationResult(
                category = MessageCategory.IMPORTANT,
                confidenceScore = (importantScore * 100).coerceAtMost(90f) / 100f,
                categoryLabelPersian = "مهم و ضروری",
                categoryLabelEnglish = "Important",
                detectedKeywords = listOf("فوری", "مهلت", "اخطار", "قوه قضاییه"),
                explanation = "پیامک نیازمند توجه فوری و اقدام کاربر."
            )
        }

        // 9. Personal Fallback (Saved contact or standard mobile number)
        val isStandardMobile = normalizedSender.startsWith("+989") || normalizedSender.startsWith("09") ||
                (normalizedSender.length >= 10 && normalizedSender.all { it.isDigit() || it == '+' })

        val personalScore = if (isStandardMobile || senderReputationScore > 0.7f) 0.85f else 0.50f

        return DetailedClassificationResult(
            category = MessageCategory.PERSONAL,
            confidenceScore = personalScore,
            categoryLabelPersian = "شخصی",
            categoryLabelEnglish = "Personal",
            detectedKeywords = emptyList(),
            explanation = "گفتگوی عمومی یا پیامک مخاطبین شخصی."
        )
    }

    private fun evaluateOtpScore(sender: String, body: String): Float {
        var score = 0f
        val keywords = listOf("کد ورود", "کد تایید", "رمز یکبار", "رمز پویا", "کد فعالسازی", "احراز هویت", "کد احراز", "otp", "passcode", "code is", "verification code", "auth code")
        keywords.forEach { kw ->
            if (body.contains(kw)) score += 0.40f
        }
        if (body.matches(Regex(".*\\b\\d{4,8}\\b.*"))) score += 0.35f
        if (body.contains("لغو") || body.contains("اشتراک")) score -= 0.20f // reduce ad overlap
        return score.coerceIn(0f, 1f)
    }

    private fun evaluateBankScore(sender: String, body: String): Float {
        var score = 0f
        if (KNOWN_BANK_SENDERS.any { sender.contains(it) }) score += 0.50f
        val keywords = listOf("واریز", "برداشت", "مانده", "موجودی", "حساب", "تراکنش", "کارت به کارت", "بانک", "transfer", "balance", "deposit", "withdraw")
        keywords.forEach { kw ->
            if (body.contains(kw)) score += 0.30f
        }
        return score.coerceIn(0f, 1f)
    }

    private fun evaluateSpamScore(sender: String, body: String, reputation: Float): Float {
        var score = (1.0f - reputation) * 0.3f
        val keywords = listOf("برنده", "جایزه", "قرعه کشی", "شارژ رایگان", "درآمد میلیون", "وام فوری بدون ضامن", "کلیک کنید", "ثبت نام رایگان")
        keywords.forEach { kw ->
            if (body.contains(kw)) score += 0.35f
        }
        if (body.contains("http://") || body.contains("bit.ly") || body.contains(".tk") || body.contains("t.me")) {
            score += 0.30f
        }
        return score.coerceIn(0f, 1f)
    }

    private fun evaluateDeliveryScore(sender: String, body: String): Float {
        var score = 0f
        if (KNOWN_DELIVERY_SENDERS.any { sender.contains(it) }) score += 0.50f
        val keywords = listOf("تیپاکس", "پست", "مرسوله", "کد رهگیری", "مامور پست", "تحویل سفارش", "توزیع", "tracking code", "express")
        keywords.forEach { kw ->
            if (body.contains(kw)) score += 0.25f
        }
        return score.coerceIn(0f, 1f)
    }

    private fun evaluateShoppingScore(sender: String, body: String): Float {
        var score = 0f
        if (KNOWN_SHOPPING_SENDERS.any { sender.contains(it) }) score += 0.50f
        val keywords = listOf("سفارش", "فاکتور", "دیجی‌کالا", "ترب", "بااسلام", "اسنپ فود", "order", "invoice", "shopping")
        keywords.forEach { kw ->
            if (body.contains(kw)) score += 0.25f
        }
        return score.coerceIn(0f, 1f)
    }

    private fun evaluateAdScore(sender: String, body: String): Float {
        var score = 0f
        val keywords = listOf("تخفیف", "حراج", "فروش ویژه", "ارسال رایگان", "جشنواره", "کد تخفیف", "پیشنهاد ویژه", "discount", "off")
        keywords.forEach { kw ->
            if (body.contains(kw)) score += 0.30f
        }
        if (sender.startsWith("983000") || sender.startsWith("981000") || sender.startsWith("3000") || sender.startsWith("1000")) {
            score += 0.20f
        }
        return score.coerceIn(0f, 1f)
    }

    private fun evaluateWorkScore(sender: String, body: String): Float {
        var score = 0f
        val keywords = listOf("جلسه", "پروژه", "صورتجلسه", "قرارداد", "پشتیبانی", "هماهنگی کاری", "meeting", "project", "deadline")
        keywords.forEach { kw ->
            if (body.contains(kw)) score += 0.30f
        }
        return score.coerceIn(0f, 1f)
    }

    private fun evaluateImportantScore(sender: String, body: String): Float {
        var score = 0f
        val keywords = listOf("فوری", "مهلت", "اخطار", "قوه قضاییه", "ثنا", "مالیات", "عدل ایران", "urgent", "notice")
        keywords.forEach { kw ->
            if (body.contains(kw)) score += 0.35f
        }
        return score.coerceIn(0f, 1f)
    }
}
