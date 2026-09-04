package com.global.sms.core.ai.search

import com.global.sms.core.ai.nlp.LocalNlpEngine
import com.global.sms.data.entity.MessageCategory
import java.util.Calendar
import java.util.Locale

data class SemanticSearchQuery(
    val rawQuery: String,
    val targetCategory: MessageCategory?,
    val keywordFilter: String?,
    val isBankOnly: Boolean,
    val isOtpOnly: Boolean,
    val startDate: Long?,
    val endDate: Long?
)

object SemanticSearchEngine {

    fun parseQuery(rawQuery: String): SemanticSearchQuery {
        val clean = LocalNlpEngine.normalizeDigits(rawQuery.lowercase(Locale.ROOT)).trim()

        var category: MessageCategory? = null
        var isBank = false
        var isOtp = false
        var startDate: Long? = null
        var endDate: Long? = null

        // Detect Bank intent
        if (clean.contains("بانکی") || clean.contains("بانک") || clean.contains("پرداخت") || clean.contains("تراکنش") || clean.contains("واریز") || clean.contains("برداشت")) {
            category = MessageCategory.BANK
            isBank = true
        }

        // Detect OTP intent
        if (clean.contains("کد تایید") || clean.contains("کد ورود") || clean.contains("رمز پویا") || clean.contains("رمز یکبار")) {
            category = MessageCategory.OTP
            isOtp = true
        }

        // Detect Delivery intent
        if (clean.contains("پست") || clean.contains("مرسوله") || clean.contains("تحویل") || clean.contains("تیپاکس")) {
            category = MessageCategory.DELIVERY
        }

        // Timeframe detection
        val cal = Calendar.getInstance()
        if (clean.contains("ماه قبل") || clean.contains("ماه گذشته")) {
            cal.add(Calendar.MONTH, -1)
            cal.set(Calendar.DAY_OF_MONTH, 1)
            startDate = cal.timeInMillis
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
            endDate = cal.timeInMillis
        } else if (clean.contains("امروز")) {
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            startDate = cal.timeInMillis
        } else if (clean.contains("دیروز")) {
            cal.add(Calendar.DAY_OF_YEAR, -1)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            startDate = cal.timeInMillis
            cal.set(Calendar.HOUR_OF_DAY, 23)
            cal.set(Calendar.MINUTE, 59)
            cal.set(Calendar.SECOND, 59)
            endDate = cal.timeInMillis
        }

        val cleanKeywords = clean
            .replace("پیامک‌های", "")
            .replace("پیامک های", "")
            .replace("ماه قبل", "")
            .replace("ماه گذشته", "")
            .replace("امروز", "")
            .replace("دیروز", "")
            .replace("بانکی", "")
            .replace("کد تایید", "")
            .trim()

        return SemanticSearchQuery(
            rawQuery = rawQuery,
            targetCategory = category,
            keywordFilter = cleanKeywords.ifBlank { null },
            isBankOnly = isBank,
            isOtpOnly = isOtp,
            startDate = startDate,
            endDate = endDate
        )
    }
}
