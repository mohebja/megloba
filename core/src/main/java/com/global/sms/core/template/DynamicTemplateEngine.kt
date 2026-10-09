package com.global.sms.core.template

import com.global.sms.core.util.PersianUtils
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class TemplateVariable(
    val key: String,
    val displayName: String,
    val exampleValue: String
)

object DynamicTemplateEngine {

    val STANDARD_VARIABLES = listOf(
        TemplateVariable("{نام}", "نام مخاطب", "علی محمدی"),
        TemplateVariable("{مبلغ}", "مبلغ فاکتور/بدهی", "۲,۵۰۰,۰۰۰ تومان"),
        TemplateVariable("{سررسید}", "تاریخ سررسید", "۱۴۰۳/۰۸/۱۵"),
        TemplateVariable("{کد_پیگیری}", "کد رهگیری یا سند", "۹۸۷۶۵۴"),
        TemplateVariable("{بانک}", "بانک عامل", "بانک ملی"),
        TemplateVariable("{تاریخ_امروز}", "تاریخ جاری شمسی", PersianUtils.formatTimestamp(System.currentTimeMillis()))
    )

    /**
     * Resolves template variables in text using the provided variable map.
     * Missing variables are left as is or replaced with blank.
     */
    fun resolveTemplate(
        templateText: String,
        variables: Map<String, String>,
        usePersianDigits: Boolean = true
    ): String {
        var resolved = templateText

        // Auto-inject today's date if requested
        if (resolved.contains("{تاریخ_امروز}")) {
            val todayStr = PersianUtils.formatTimestamp(System.currentTimeMillis())
            resolved = resolved.replace("{تاریخ_امروز}", todayStr)
        }

        for ((key, value) in variables) {
            val formattedValue = if (usePersianDigits) PersianUtils.toPersianDigits(value) else value
            val cleanKey = if (key.startsWith("{") && key.endsWith("}")) key else "{$key}"
            resolved = resolved.replace(cleanKey, formattedValue)
        }

        return resolved
    }

    /**
     * Extracts list of variable placeholder keys present in a template string.
     */
    fun extractVariables(templateText: String): List<String> {
        val regex = "\\{([^{}]+)\\}".toRegex()
        return regex.findAll(templateText).map { it.value }.distinct().toList()
    }
}
