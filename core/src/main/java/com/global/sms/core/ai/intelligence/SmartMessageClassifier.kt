package com.global.sms.core.ai.intelligence

enum class MessageCategory {
    BANKING,
    OTP,
    PERSONAL,
    WORK,
    SHOPPING,
    DELIVERY,
    TRAVEL,
    MEDICAL,
    GOVERNMENT,
    SPAM
}

data class ClassificationResult(
    val category: MessageCategory,
    val confidence: Float,
    val detectedLanguage: String,
    val isOtp: Boolean,
    val isSpam: Boolean,
    val summary: String
)

class SmartMessageClassifier {

    fun classifyMessage(text: String): ClassificationResult {
        val normalized = normalizeText(text)
        val lang = detectLanguage(text)

        val isOtp = containsOtpKeywords(normalized)
        val isSpam = containsSpamKeywords(normalized)

        val category = when {
            isOtp -> MessageCategory.OTP
            isSpam -> MessageCategory.SPAM
            containsBankingKeywords(normalized) -> MessageCategory.BANKING
            containsDeliveryKeywords(normalized) -> MessageCategory.DELIVERY
            containsShoppingKeywords(normalized) -> MessageCategory.SHOPPING
            containsTravelKeywords(normalized) -> MessageCategory.TRAVEL
            containsMedicalKeywords(normalized) -> MessageCategory.MEDICAL
            containsGovernmentKeywords(normalized) -> MessageCategory.GOVERNMENT
            containsWorkKeywords(normalized) -> MessageCategory.WORK
            else -> MessageCategory.PERSONAL
        }

        val summary = when (category) {
            MessageCategory.BANKING -> "اطلاعیه مالی/بانکی"
            MessageCategory.OTP -> "کد تایید یک‌بارمصرف"
            MessageCategory.DELIVERY -> "مرسوله و تحویل سفارش"
            MessageCategory.SHOPPING -> "سفارش و خرید"
            MessageCategory.TRAVEL -> "سفر و بلیط"
            MessageCategory.MEDICAL -> "درمانی و پزشکی"
            MessageCategory.GOVERNMENT -> "اداری و دولتی"
            MessageCategory.WORK -> "کاری و سازمانی"
            MessageCategory.SPAM -> "تبلیغاتی / مشکوک"
            MessageCategory.PERSONAL -> "پیام شخصی"
        }

        return ClassificationResult(
            category = category,
            confidence = 0.95f,
            detectedLanguage = lang,
            isOtp = isOtp,
            isSpam = isSpam,
            summary = summary
        )
    }

    private fun containsOtpKeywords(text: String): Boolean {
        return text.contains("کد تایید") || text.contains("کد ورود") || text.contains("رمز یکبار مصرف") ||
                text.contains("otp") || text.contains("verification code") || text.contains("pin code") || text.contains("رمز پویا")
    }

    private fun containsSpamKeywords(text: String): Boolean {
        return text.contains("برنده شدید") || text.contains("لینک زیر را کلیک کنید") || text.contains("استخدام فوری") ||
                text.contains("کسب درآمد آنلاین") || text.contains("تخفیف ویژه امروز") || text.contains("قرعه کشی")
    }

    private fun containsBankingKeywords(text: String): Boolean {
        return text.contains("بانک") || text.contains("حساب") || text.contains("مبلغ") || text.contains("برداشت") ||
                text.contains("واریز") || text.contains("مانده") || text.contains("تومان") || text.contains("ریال") ||
                text.contains("کارت به کارت") || text.contains("bank") || text.contains("transfer")
    }

    private fun containsDeliveryKeywords(text: String): Boolean {
        return text.contains("پست") || text.contains("تیپاکس") || text.contains("مرسوله") || text.contains("کد رهگیری") ||
                text.contains("پیک") || text.contains("delivery") || text.contains("tracking")
    }

    private fun containsShoppingKeywords(text: String): Boolean {
        return text.contains("دیجی کالا") || text.contains("فاکتور") || text.contains("فروشگاه") || text.contains("سفارش") ||
                text.contains("خرید") || text.contains("order") || text.contains("invoice")
    }

    private fun containsTravelKeywords(text: String): Boolean {
        return text.contains("پرواز") || text.contains("بلیط") || text.contains("هتل") || text.contains("قطار") ||
                text.contains("اسنپ") || text.contains("تپسی") || text.contains("flight") || text.contains("ticket")
    }

    private fun containsMedicalKeywords(text: String): Boolean {
        return text.contains("داروخانه") || text.contains("مطب") || text.contains("دکتر") || text.contains("نوبت") ||
                text.contains("آزمایشگاه") || text.contains("بیمارستان") || text.contains("clinic")
    }

    private fun containsGovernmentKeywords(text: String): Boolean {
        return text.contains("سامانه") || text.contains("مالیات") || text.contains("عدل ایران") || text.contains("ثبت احوال") ||
                text.contains("تامین اجتماعی") || text.contains("ابلاغیه") || text.contains("ثبت اسناد")
    }

    private fun containsWorkKeywords(text: String): Boolean {
        return text.contains("جلسه") || text.contains("پروژه") || text.contains("مدیر") || text.contains("همکار") ||
                text.contains("گزارش") || text.contains("meeting") || text.contains("report")
    }

    private fun normalizeText(text: String): String {
        return text.lowercase()
            .replace('۰', '0').replace('۱', '1').replace('۲', '2').replace('۳', '3').replace('۴', '4')
            .replace('۵', '5').replace('۶', '6').replace('۷', '7').replace('۸', '8').replace('۹', '9')
            .replace("أ", "ا").replace("إ", "ا").replace("آ", "ا").replace("ك", "ک").replace("ي", "ی")
    }

    private fun detectLanguage(text: String): String {
        val containsPersian = text.any { it in '\u0600'..'\u06FF' }
        return if (containsPersian) "fa" else "en"
    }
}
