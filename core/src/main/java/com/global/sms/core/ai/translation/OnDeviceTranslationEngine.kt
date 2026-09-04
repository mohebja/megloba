package com.global.sms.core.ai.translation

enum class SupportedLanguage(val code: String, val displayNameFa: String) {
    PERSIAN("fa", "فارسی"),
    ENGLISH("en", "انگلیسی"),
    ARABIC("ar", "عربی")
}

data class TranslationResult(
    val originalText: String,
    val translatedText: String,
    val sourceLanguage: SupportedLanguage,
    val targetLanguage: SupportedLanguage,
    val isOfflineProcessed: Boolean = true
)

class OnDeviceTranslationEngine {

    fun translateMessage(
        text: String,
        targetLang: SupportedLanguage
    ): TranslationResult {
        val sourceLang = detectLanguage(text)
        if (sourceLang == targetLang) {
            return TranslationResult(text, text, sourceLang, targetLang)
        }

        val translated = when {
            sourceLang == SupportedLanguage.ENGLISH && targetLang == SupportedLanguage.PERSIAN -> {
                translateEnglishToPersian(text)
            }
            sourceLang == SupportedLanguage.PERSIAN && targetLang == SupportedLanguage.ENGLISH -> {
                translatePersianToEnglish(text)
            }
            sourceLang == SupportedLanguage.ARABIC && targetLang == SupportedLanguage.PERSIAN -> {
                translateArabicToPersian(text)
            }
            sourceLang == SupportedLanguage.PERSIAN && targetLang == SupportedLanguage.ARABIC -> {
                translatePersianToArabic(text)
            }
            else -> text
        }

        return TranslationResult(
            originalText = text,
            translatedText = translated,
            sourceLanguage = sourceLang,
            targetLanguage = targetLang
        )
    }

    private fun translateEnglishToPersian(text: String): String {
        var result = text
        val dictionary = mapOf(
            "your verification code is" to "کد تایید شما عبارت است از:",
            "your password is" to "رمز عبور شما عبارت است از:",
            "payment successful" to "پرداخت با موفقیت انجام شد",
            "order confirmed" to "سفارش شما تایید شد",
            "thank you for your purchase" to "از خرید شما متشکریم",
            "delivery status" to "وضعیت تحویل مرسوله",
            "account balance" to "موجودی حساب",
            "transferred to" to "انتقال داده شد به",
            "meeting scheduled" to "جلسه تنظیم شد",
            "urgent" to "فوری",
            "hello" to "سلام",
            "welcome" to "خوش آمدید"
        )

        val lower = text.lowercase()
        dictionary.forEach { (en, fa) ->
            if (lower.contains(en)) {
                result = result.replace(en, fa, ignoreCase = true)
            }
        }
        return result
    }

    private fun translatePersianToEnglish(text: String): String {
        var result = text
        val dictionary = mapOf(
            "کد تایید" to "Verification code",
            "رمز پویا" to "OTP Code",
            "مبلغ" to "Amount",
            "تومان" to "Toman",
            "ریال" to "Rial",
            "پرداخت شد" to "Paid successfully",
            "برداشت شد" to "Debited",
            "واریز شد" to "Credited",
            "سلام" to "Hello",
            "ممنون" to "Thank you",
            "جلسه" to "Meeting"
        )

        dictionary.forEach { (fa, en) ->
            if (result.contains(fa)) {
                result = result.replace(fa, en)
            }
        }
        return result
    }

    private fun translateArabicToPersian(text: String): String {
        return text.replace("تم الدفع بنجاح", "پرداخت با موفقیت انجام شد")
            .replace("شكرا لك", "با تشکر از شما")
            .replace("رمز التحقق", "کد تایید")
            .replace("موعد", "نوبت / جلسه")
    }

    private fun translatePersianToArabic(text: String): String {
        return text.replace("کد تایید", "رمز التحقق")
            .replace("پرداخت شد", "تم الدفع")
            .replace("تشکر", "شكرا لك")
    }

    private fun detectLanguage(text: String): SupportedLanguage {
        val containsPersian = text.any { it in '\u0600'..'\u06FF' }
        val containsArabicSpecific = text.contains("لم") || text.contains("على") || text.contains("هذا")
        return when {
            containsArabicSpecific -> SupportedLanguage.ARABIC
            containsPersian -> SupportedLanguage.PERSIAN
            else -> SupportedLanguage.ENGLISH
        }
    }
}
