package com.global.sms.core.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Persian (Farsi) language, Jalali calendar, digits, and text utilities.
 */
object PersianUtils {

    private val PERSIAN_DIGITS = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    private val ENGLISH_DIGITS = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')

    /**
     * Converts English digits in a string to Persian digits if Persian locale/setting is enabled.
     */
    fun toPersianDigits(text: String): String {
        var result = text
        for (i in 0..9) {
            result = result.replace(ENGLISH_DIGITS[i], PERSIAN_DIGITS[i])
        }
        return result
    }

    /**
     * Converts Persian digits to English digits for normalization.
     */
    fun toEnglishDigits(text: String): String {
        var result = text
        for (i in 0..9) {
            result = result.replace(PERSIAN_DIGITS[i], ENGLISH_DIGITS[i])
        }
        return result
    }

    /**
     * Checks if text contains Persian or Arabic unicode characters.
     */
    fun containsPersian(text: String): Boolean {
        for (c in text.toCharArray()) {
            val block = Character.UnicodeBlock.of(c)
            if (block == Character.UnicodeBlock.ARABIC ||
                block == Character.UnicodeBlock.ARABIC_SUPPLEMENT ||
                block == Character.UnicodeBlock.ARABIC_PRESENTATION_FORMS_A ||
                block == Character.UnicodeBlock.ARABIC_PRESENTATION_FORMS_B
            ) {
                return true
            }
        }
        return false
    }

    /**
     * Formats timestamp to a human-readable date string (Jalali representation or standard).
     */
    fun formatTimestamp(timestamp: Long, usePersianCalendar: Boolean = true, usePersianDigits: Boolean = true): String {
        if (timestamp <= 0) return ""
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        val date = Date(timestamp)
        val formatted = when {
            diff < 60 * 1000 -> "هم‌اکنون" // Just now
            diff < 24 * 3600 * 1000 -> {
                val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                sdf.format(date)
            }
            diff < 7 * 24 * 3600 * 1000 -> {
                val sdf = SimpleDateFormat("EEE HH:mm", Locale.getDefault())
                sdf.format(date)
            }
            else -> {
                if (usePersianCalendar) {
                    toJalaliDate(timestamp)
                } else {
                    val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
                    sdf.format(date)
                }
            }
        }

        return if (usePersianDigits) toPersianDigits(formatted) else formatted
    }

    /**
     * Simple Jalali (Persian Calendar) conversion helper.
     */
    fun toJalaliDate(timestamp: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        val gYear = calendar.get(Calendar.YEAR)
        val gMonth = calendar.get(Calendar.MONTH) + 1
        val gDay = calendar.get(Calendar.DAY_OF_MONTH)

        // Jalali conversion algorithm
        val gDaysInMonth = intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        val jDaysInMonth = intArrayOf(31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29)

        var gy = gYear - 1600
        var gm = gMonth - 1
        var gd = gDay - 1

        var gDayNo = 365 * gy + (gy + 3) / 4 - (gy + 99) / 100 + (gy + 399) / 400
        for (i in 0 until gm) {
            gDayNo += gDaysInMonth[i]
        }
        if (gm > 1 && ((gy % 4 == 0 && gy % 100 != 0) || (gy % 400 == 0))) {
            gDayNo++
        }
        gDayNo += gd

        var jDayNo = gDayNo - 79
        val jNp = jDayNo / 12053
        jDayNo %= 12053

        var jy = 979 + 33 * jNp + 4 * (jDayNo / 1461)
        jDayNo %= 1461

        if (jDayNo >= 366) {
            jy += (jDayNo - 1) / 365
            jDayNo = (jDayNo - 1) % 365
        }

        var jm = 0
        var jd = 0
        for (i in 0..11) {
            val monthDays = jDaysInMonth[i]
            if (jDayNo < monthDays) {
                jm = i + 1
                jd = jDayNo + 1
                break
            }
            jDayNo -= monthDays
        }

        val jYear = jy
        val jMonthStr = String.format(Locale.US, "%02d", jm)
        val jDayStr = String.format(Locale.US, "%02d", jd)

        return "$jYear/$jMonthStr/$jDayStr"
    }
}
