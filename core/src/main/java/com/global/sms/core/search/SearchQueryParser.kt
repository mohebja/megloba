package com.global.sms.core.search

import com.global.sms.data.entity.MessageCategory
import java.util.Calendar

data class ParsedSearchQuery(
    val rawQuery: String,
    val normalizedText: String,
    val detectedCategory: MessageCategory? = null,
    val startDate: Long? = null,
    val endDate: Long? = null,
    val phoneNumber: String? = null,
    val keywords: List<String> = emptyList()
)

object SearchQueryParser {

    fun normalizeText(text: String): String {
        var result = text.trim()
            .replace('ك', 'ک')
            .replace('ي', 'ی')
            .replace('ۀ', 'ه')
            .replace('أ', 'ا')
            .replace('إ', 'ا')
            .replace('آ', 'ا')

        // Convert Arabic/Persian digits to English
        val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
        val arabicDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
        for (i in 0..9) {
            result = result.replace(persianDigits[i], (i + '0'.code).toChar())
            result = result.replace(arabicDigits[i], (i + '0'.code).toChar())
        }
        return result
    }

    fun parse(input: String): ParsedSearchQuery {
        val normalized = normalizeText(input)
        val lower = normalized.lowercase()

        var detectedCategory: MessageCategory? = null
        var startDate: Long? = null
        var endDate: Long? = null
        var phoneNumber: String? = null

        // Detect Phone Number
        val phoneRegex = Regex("""(\+98|0)?9\d{9}""")
        val phoneMatch = phoneRegex.find(normalized)
        if (phoneMatch != null) {
            phoneNumber = phoneMatch.value
        }

        // Detect Category intent
        when {
            lower.contains("رمز") || lower.contains("کد") || lower.contains("تایید") ||
            lower.contains("otp") || lower.contains("فعالسازی") -> {
                detectedCategory = MessageCategory.OTP
            }
            lower.contains("بانک") || lower.contains("تراکنش") || lower.contains("واریز") ||
            lower.contains("برداشت") || lower.contains("حساب") || lower.contains("ملت") ||
            lower.contains("ملی") || lower.contains("صادرات") || lower.contains("تجارت") -> {
                detectedCategory = MessageCategory.BANK
            }
            lower.contains("اسپم") || lower.contains("تبلیغ") || lower.contains("spam") -> {
                detectedCategory = MessageCategory.SPAM
            }
            lower.contains("شخصی") || lower.contains("personal") -> {
                detectedCategory = MessageCategory.PERSONAL
            }
            lower.contains("کاری") || lower.contains("اداری") || lower.contains("work") -> {
                detectedCategory = MessageCategory.WORK
            }
        }

        // Detect Date Intents
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        when {
            lower.contains("امروز") || lower.contains("today") -> {
                startDate = cal.timeInMillis
                endDate = System.currentTimeMillis()
            }
            lower.contains("دیروز") || lower.contains("yesterday") -> {
                cal.add(Calendar.DAY_OF_YEAR, -1)
                startDate = cal.timeInMillis
                cal.add(Calendar.DAY_OF_YEAR, 1)
                endDate = cal.timeInMillis
            }
            lower.contains("هفته قبل") || lower.contains("هفته گذشته") || lower.contains("last week") -> {
                cal.add(Calendar.DAY_OF_YEAR, -7)
                startDate = cal.timeInMillis
                endDate = System.currentTimeMillis()
            }
            lower.contains("ماه قبل") || lower.contains("ماه گذشته") || lower.contains("last month") -> {
                cal.add(Calendar.MONTH, -1)
                startDate = cal.timeInMillis
                endDate = System.currentTimeMillis()
            }
        }

        // Clean query keywords by removing recognized intent words
        var cleaned = normalized
        val stopWords = listOf("پیام", "های", "امروز", "دیروز", "هفته قبل", "هفته گذشته", "ماه قبل", "ماه گذشته", "بانک", "رمز")
        stopWords.forEach { word ->
            cleaned = cleaned.replace(word, "", ignoreCase = true)
        }

        val keywords = cleaned.split("""\s+""".toRegex()).filter { it.isNotBlank() }

        return ParsedSearchQuery(
            rawQuery = input,
            normalizedText = normalized,
            detectedCategory = detectedCategory,
            startDate = startDate,
            endDate = endDate,
            phoneNumber = phoneNumber,
            keywords = keywords
        )
    }
}
