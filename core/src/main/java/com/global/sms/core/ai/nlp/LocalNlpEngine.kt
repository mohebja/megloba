package com.global.sms.core.ai.nlp

import java.util.Locale

object LocalNlpEngine {

    private val PERSIAN_DIGITS = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    private val ARABIC_DIGITS = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    private val LATIN_DIGITS = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')

    private val PERSIAN_STOP_WORDS = setOf(
        "از", "به", "با", "در", "بر", "برای", "که", "این", "آن", "شد", "است", "بود", "می", "را", "و", "یا"
    )

    /**
     * Converts Persian and Arabic digits to Latin standard digits.
     */
    fun normalizeDigits(text: String): String {
        var result = text
        for (i in 0..9) {
            result = result.replace(PERSIAN_DIGITS[i], LATIN_DIGITS[i])
            result = result.replace(ARABIC_DIGITS[i], LATIN_DIGITS[i])
        }
        return result
    }

    /**
     * Tokenizes Persian & English text into clean stemmed tokens.
     */
    fun tokenizeAndClean(text: String): List<String> {
        val normalized = normalizeDigits(text.lowercase(Locale.ROOT))
            .replace(Regex("[^a-zA-Z0-9آ-ی\\s]"), " ")
        return normalized.split(Regex("\\s+"))
            .map { it.trim() }
            .filter { it.length > 1 && !PERSIAN_STOP_WORDS.contains(it) }
    }

    /**
     * Calculates Jaccard & Cosine text similarity score (0.0f to 1.0f).
     */
    fun calculateSemanticSimilarity(textA: String, textB: String): Float {
        val tokensA = tokenizeAndClean(textA).toSet()
        val tokensB = tokenizeAndClean(textB).toSet()

        if (tokensA.isEmpty() || tokensB.isEmpty()) return 0.0f

        val intersection = tokensA.intersect(tokensB).size
        val union = tokensA.union(tokensB).size

        return intersection.toFloat() / union.toFloat()
    }

    /**
     * Extracts numerical monetary values (e.g., 500,000 تومان -> 500000.0).
     */
    fun extractAmounts(text: String): List<Double> {
        val normalized = normalizeDigits(text).replace(",", "").replace("،", "")
        val regex = Regex("(\\d{3,12})\\s*(تومان|ریال|Rials|Toman)?")
        val matches = regex.findAll(normalized)
        val results = mutableListOf<Double>()
        for (match in matches) {
            val numStr = match.groupValues[1]
            val unit = match.groupValues.getOrNull(2) ?: ""
            var amount = numStr.toDoubleOrNull() ?: continue
            if (unit == "ریال" || unit.equals("Rials", ignoreCase = true)) {
                amount /= 10.0 // convert Rials to Toman
            }
            results.add(amount)
        }
        return results
    }
}
