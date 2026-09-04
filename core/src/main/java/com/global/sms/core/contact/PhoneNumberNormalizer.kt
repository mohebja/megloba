package com.global.sms.core.contact

object PhoneNumberNormalizer {

    /**
     * Converts Persian (۰-۹) and Arabic (٠-٩) digits to ASCII standard digits (0-9).
     */
    fun convertDigitsToAscii(input: String): String {
        if (input.isBlank()) return ""
        val builder = StringBuilder(input.length)
        for (ch in input) {
            when (ch) {
                in '۰'..'۹' -> builder.append((ch - '۰' + '0'.code).toChar())
                in '٠'..'٩' -> builder.append((ch - '٠' + '0'.code).toChar())
                else -> builder.append(ch)
            }
        }
        return builder.toString()
    }

    /**
     * Cleans and normalizes phone numbers by removing spaces, dashes,
     * parentheses, invisible Unicode markers, and converting digits.
     */
    fun normalize(rawNumber: String): String {
        if (rawNumber.isBlank()) return ""
        
        // 1. Convert digits first
        val asciiString = convertDigitsToAscii(rawNumber)
        
        // 2. Strip noise & non-phone characters except leading +
        val isPlusLeading = asciiString.trimStart().startsWith("+")
        val digitsOnly = asciiString.filter { it.isDigit() }
        
        if (digitsOnly.isEmpty()) return ""
        
        // 3. Handle Iranian country code standardizations
        return when {
            // e.g. 00989123456789 -> +989123456789
            digitsOnly.startsWith("0098") -> "+98${digitsOnly.substring(4)}"
            // e.g. 989123456789 without plus -> +989123456789
            digitsOnly.startsWith("989") && digitsOnly.length == 12 -> "+98${digitsOnly.substring(2)}"
            // e.g. 09123456789 -> 09123456789 (national format)
            digitsOnly.startsWith("09") && digitsOnly.length == 11 -> digitsOnly
            // e.g. +989123456789 -> +989123456789
            isPlusLeading -> "+$digitsOnly"
            else -> digitsOnly
        }
    }

    /**
     * Formats phone number to standard E.164 (+98...) format if Iranian number.
     */
    fun toE164Format(rawNumber: String): String {
        val normalized = normalize(rawNumber)
        return when {
            normalized.startsWith("+") -> normalized
            normalized.startsWith("09") && normalized.length == 11 -> "+98${normalized.substring(1)}"
            else -> normalized
        }
    }

    /**
     * Extracts the significant matching digits (usually last 9-10 digits)
     * to perform fast, accurate matching across +98 and 09 prefix variations.
     */
    fun extractMatchableDigits(rawNumber: String): String {
        val digitsOnly = convertDigitsToAscii(rawNumber).filter { it.isDigit() }
        return if (digitsOnly.length >= 9) {
            digitsOnly.takeLast(9)
        } else {
            digitsOnly
        }
    }

    /**
     * Compares two phone numbers for equality, accounting for prefix variations (+98 vs 09 vs 0098).
     */
    fun areNumbersEqual(num1: String?, num2: String?): Boolean {
        if (num1.isNull_or_blank() || num2.isNull_or_blank()) return false
        val n1 = num1 ?: return false
        val n2 = num2 ?: return false
        val norm1 = normalize(n1)
        val norm2 = normalize(n2)
        if (norm1 == norm2) return true
        
        val matchable1 = extractMatchableDigits(norm1)
        val matchable2 = extractMatchableDigits(norm2)
        
        return matchable1.isNotEmpty() && matchable1 == matchable2
    }

    private fun String?.isNull_or_blank(): Boolean = this == null || this.trim().isEmpty()
}
