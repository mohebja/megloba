package com.global.sms.core.ai.copilot

import com.global.sms.core.util.PersianUtils
import java.util.regex.Pattern

data class ExtractedEntities(
    val personNames: List<String> = emptyList(),
    val dates: List<String> = emptyList(),
    val times: List<String> = emptyList(),
    val locations: List<String> = emptyList(),
    val amounts: List<String> = emptyList(),
    val phoneNumbers: List<String> = emptyList(),
    val trackingCodes: List<String> = emptyList()
)

object EntityExtractionEngine {

    private val PHONE_PATTERN = Pattern.compile("(?:\\+?98|0)?9\\d{9}")
    private val TIME_PATTERN = Pattern.compile("(?:ساعت\\s*)?(\\d{1,2}(?::\\d{2})?\\s*(?:صبح|عصر|شب|ظهر)?)")
    private val DATE_PATTERNS = listOf(
        Pattern.compile("(امروز|فردا|پس‌فردا|دوشنبه|سه‌شنبه|چهارشنبه|پنج‌شنبه|جمعه|شنبه|یکشنبه)"),
        Pattern.compile("\\d{1,4}[/\\.-]\\d{1,2}[/\\.-]\\d{1,4}"),
        Pattern.compile("\\d{1,2}\\s*(مرداد|شهریور|مهر|آبان|آذر|دی|بهمن|اسفند|فروردین|اردیبهشت|خرداد)")
    )
    private val AMOUNT_PATTERN = Pattern.compile("([\\d,٫]+)\\s*(?:ریال|تومان|تومان|Rials?|USD|\\$)?")
    private val TRACKING_PATTERN = Pattern.compile("(?:TRK-[A-Za-z0-9]+|(?:کد\\s*پیگیری(?:\\s*مرسوله(?:\\s*شما)?)?|کد\\s*رهگیری|شماره\\s*پیگیری|کد\\s*مرسوله|شماره\\s*سفارش|کد)[\\s:]*([A-Za-z0-9_-]+))")

    private val COMMON_PERSIAN_NAMES = setOf(
        "رضا", "محمد", "علی", "حسین", "مهدی", "امیر", "سارا", "مریم", "زهرا", "نرگس", "سعید", "فاطمه", "احمد", "حسن", "ارسلان", "نیما"
    )
    private val LOCATION_KEYWORDS = listOf("خیابان", "میدان", "پلاک", "کوچه", "دفتر", "شعبه", "تهران", "اصفهان", "مشهد", "شیراز", "تبریز")

    fun extractEntities(text: String): ExtractedEntities {
        val normalizedText = PersianUtils.toPersianDigits(text)
        val asciiText = PersianUtils.toEnglishDigits(text)

        // Extract Phone Numbers
        val phoneNumbers = mutableListOf<String>()
        val phoneMatcher = PHONE_PATTERN.matcher(asciiText)
        while (phoneMatcher.find()) {
            val phone = phoneMatcher.group()
            phoneNumbers.add(phone)
            phoneNumbers.add(PersianUtils.toPersianDigits(phone))
        }

        // Extract Times
        val times = mutableListOf<String>()
        val timeMatcher = TIME_PATTERN.matcher(text)
        while (timeMatcher.find()) {
            val found = timeMatcher.group().trim()
            if (found.contains("ساعت") || found.contains(":") || found.contains("صبح") || found.contains("عصر")) {
                times.add(found)
            }
        }

        // Extract Dates
        val dates = mutableListOf<String>()
        for (pattern in DATE_PATTERNS) {
            val dateMatcher = pattern.matcher(text)
            while (dateMatcher.find()) {
                dates.add(dateMatcher.group().trim())
            }
        }

        // Extract Amounts
        val amounts = mutableListOf<String>()
        val amountMatcher = AMOUNT_PATTERN.matcher(text)
        while (amountMatcher.find()) {
            val raw = amountMatcher.group().trim()
            if (raw.contains("ریال") || raw.contains("تومان") || raw.contains(",") || raw.contains("٫")) {
                amounts.add(raw)
            }
        }

        // Extract Tracking Codes
        val trackingCodes = mutableListOf<String>()
        val trackMatcher = TRACKING_PATTERN.matcher(text)
        while (trackMatcher.find()) {
            val code = trackMatcher.group(1) ?: trackMatcher.group(0)
            val cleaned = code.replace(Regex("^(کد\\s*پیگیری(?:\\s*مرسوله(?:\\s*شما)?)?|کد\\s*رهگیری|شماره\\s*پیگیری|کد\\s*مرسوله|شماره\\s*سفارش|کد)[\\s:]*"), "").trim()
            if (cleaned.isNotBlank() && cleaned.length >= 4) {
                trackingCodes.add(cleaned)
            }
        }
        val trkMatcher = Pattern.compile("TRK-[A-Za-z0-9_-]+").matcher(text)
        while (trkMatcher.find()) {
            val trk = trkMatcher.group().trim()
            if (!trackingCodes.contains(trk)) {
                trackingCodes.add(trk)
            }
        }

        // Extract Person Names
        val names = mutableListOf<String>()
        for (name in COMMON_PERSIAN_NAMES) {
            if (text.contains(name)) {
                names.add(name)
            }
        }

        // Extract Locations
        val locations = mutableListOf<String>()
        for (loc in LOCATION_KEYWORDS) {
            if (text.contains(loc)) {
                val idx = text.indexOf(loc)
                val sub = text.substring(idx, minOf(text.length, idx + 25))
                locations.add(sub.trim())
            }
        }

        return ExtractedEntities(
            personNames = names.distinct(),
            dates = dates.distinct(),
            times = times.distinct(),
            locations = locations.distinct(),
            amounts = amounts.distinct(),
            phoneNumbers = phoneNumbers.distinct(),
            trackingCodes = trackingCodes.distinct()
        )
    }

    private fun String?.isNull_or_blank(): Boolean = this == null || this.trim().isEmpty()
}
