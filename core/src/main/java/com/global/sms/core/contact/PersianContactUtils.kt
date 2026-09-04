package com.global.sms.core.contact

import java.text.Collator
import java.util.Locale

object PersianContactUtils {

    private val persianLocale = Locale.forLanguageTag("fa-IR")
    private val collator: Collator by lazy {
        Collator.getInstance(persianLocale).apply {
            strength = Collator.PRIMARY
        }
    }

    /**
     * Normalizes Persian and Arabic letters to a canonical form for search & comparison.
     * E.g., 'ي' -> 'ی', 'ك' -> 'ک', 'آ'/'أ'/'إ' -> 'ا', 'ة' -> 'ه'.
     */
    fun normalizePersianText(input: String?): String {
        if (input.isNullOrBlank()) return ""
        
        val builder = StringBuilder(input.length)
        for (ch in input) {
            when (ch) {
                'ي', 'ى' -> builder.append('ی')
                'ك' -> builder.append('ک')
                'آ', 'أ', 'إ', 'ٱ' -> builder.append('ا')
                'ة' -> builder.append('ه')
                'ؤ' -> builder.append('و')
                'ئ' -> builder.append('ی')
                // Remove Arabic diacritics / Tashkeel
                '\u064B', '\u064C', '\u064D', '\u064E', '\u064F', '\u0650', '\u0651', '\u0652' -> { /* Skip diacritic */ }
                else -> builder.append(ch)
            }
        }
        return builder.toString().lowercase(persianLocale).trim()
    }

    /**
     * Converts ASCII standard digits to Persian digits.
     */
    fun toPersianDigits(input: String): String {
        if (input.isBlank()) return ""
        val builder = StringBuilder(input.length)
        for (ch in input) {
            when (ch) {
                in '0'..'9' -> builder.append((ch - '0' + '۰'.code).toChar())
                else -> builder.append(ch)
            }
        }
        return builder.toString()
    }

    /**
     * Checks if target string matches query, taking into account Persian/Arabic letter
     * variations and digit variations.
     */
    fun matchesQuery(target: String?, query: String): Boolean {
        if (target.isNullOrBlank()) return false
        if (query.isBlank()) return true

        val normTarget = normalizePersianText(target)
        val normQuery = normalizePersianText(query)

        // Also check with converted digits
        val asciiTarget = PhoneNumberNormalizer.convertDigitsToAscii(normTarget)
        val asciiQuery = PhoneNumberNormalizer.convertDigitsToAscii(normQuery)

        return normTarget.contains(normQuery) || asciiTarget.contains(asciiQuery)
    }

    /**
     * Comparator for sorting contact names according to Persian alphabetical order.
     */
    val persianNameComparator = Comparator<String> { name1, name2 ->
        val n1 = normalizePersianText(name1)
        val n2 = normalizePersianText(name2)
        collator.compare(n1, n2)
    }
}
