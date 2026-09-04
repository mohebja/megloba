package com.global.sms.core.localization

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

enum class AppLanguage(
    val code: String,
    val displayName: String,
    val nativeName: String,
    val isRtl: Boolean,
    val currencyCode: String,
    val currencySymbol: String
) {
    PERSIAN("fa", "Persian", "فارسی", true, "IRR", "تومان"),
    ENGLISH("en", "English", "English (US)", false, "USD", "$"),
    ARABIC("ar", "Arabic", "العربية", true, "SAR", "ر.س"),
    TURKISH("tr", "Turkish", "Türkçe", false, "TRY", "₺"),
    SPANISH("es", "Spanish", "Español", false, "EUR", "€"),
    FRENCH("fr", "French", "Français", false, "EUR", "€")
}

data class FormattedCurrencyResult(
    val formattedString: String,
    val rawAmount: Long,
    val symbol: String
)

class LocalizationEngine {

    private val _currentLanguage = MutableStateFlow(AppLanguage.PERSIAN)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    fun switchLanguage(language: AppLanguage) {
        _currentLanguage.value = language
    }

    fun formatNumber(number: Long, lang: AppLanguage = _currentLanguage.value): String {
        return when (lang) {
            AppLanguage.PERSIAN -> {
                val faDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
                number.toString().map { char ->
                    if (char.isDigit()) faDigits[char - '0'] else char
                }.joinToString("")
            }
            AppLanguage.ARABIC -> {
                val arDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
                number.toString().map { char ->
                    if (char.isDigit()) arDigits[char - '0'] else char
                }.joinToString("")
            }
            else -> {
                NumberFormat.getInstance(Locale.forLanguageTag(lang.code)).format(number)
            }
        }
    }

    fun formatCurrency(amount: Long, lang: AppLanguage = _currentLanguage.value): FormattedCurrencyResult {
        val numStr = formatNumber(amount, lang)
        val formatted = when (lang) {
            AppLanguage.PERSIAN -> "$numStr ${lang.currencySymbol}"
            AppLanguage.ARABIC -> "$numStr ${lang.currencySymbol}"
            AppLanguage.ENGLISH -> "${lang.currencySymbol}$numStr"
            else -> "$numStr ${lang.currencySymbol}"
        }
        return FormattedCurrencyResult(
            formattedString = formatted,
            rawAmount = amount,
            symbol = lang.currencySymbol
        )
    }

    fun formatDate(timestampMs: Long, lang: AppLanguage = _currentLanguage.value): String {
        val date = Date(timestampMs)
        val pattern = when (lang) {
            AppLanguage.PERSIAN -> "yyyy/MM/dd HH:mm"
            AppLanguage.ARABIC -> "dd/MM/yyyy HH:mm"
            AppLanguage.ENGLISH -> "MMM dd, yyyy h:mm a"
            AppLanguage.TURKISH -> "dd.MM.yyyy HH:mm"
            AppLanguage.SPANISH -> "dd/MM/yyyy HH:mm"
            AppLanguage.FRENCH -> "dd/MM/yyyy HH:mm"
        }
        val sdf = SimpleDateFormat(pattern, Locale.forLanguageTag(lang.code))
        return sdf.format(date)
    }

    fun isRtlLayout(lang: AppLanguage = _currentLanguage.value): Boolean {
        return lang.isRtl
    }
}
