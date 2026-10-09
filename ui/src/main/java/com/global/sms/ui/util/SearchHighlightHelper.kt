package com.global.sms.ui.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight

object SearchHighlightHelper {

    /**
     * Extracts a focused snippet around the search query keyword.
     * Shows context words before and after the match.
     */
    fun extractSearchSnippet(fullText: String, query: String, maxCharsAround: Int = 40): String {
        if (query.isBlank() || fullText.isBlank()) return fullText.take(120)

        val index = fullText.indexOf(query, ignoreCase = true)
        if (index == -1) return fullText.take(120)

        val start = (index - maxCharsAround).coerceAtLeast(0)
        val end = (index + query.length + maxCharsAround).coerceAtMost(fullText.length)

        val snippet = fullText.substring(start, end).trim()
        val prefix = if (start > 0) "... " else ""
        val suffix = if (end < fullText.length) " ..." else ""

        return "$prefix$snippet$suffix"
    }

    /**
     * Builds an AnnotatedString with highlighted background color on all occurrences
     * of the search query in the text.
     */
    fun highlightSearchQuery(
        text: String,
        query: String,
        highlightColor: Color = Color(0xFFFFD54F),
        highlightTextColor: Color = Color(0xFF000000)
    ): AnnotatedString {
        if (query.isBlank() || text.isBlank()) {
            return AnnotatedString(text)
        }

        return buildAnnotatedString {
            append(text)
            val lowerText = text.lowercase()
            val lowerQuery = query.lowercase().trim()

            var startIndex = 0
            while (startIndex < lowerText.length) {
                val foundIndex = lowerText.indexOf(lowerQuery, startIndex)
                if (foundIndex == -1) break

                val endIndex = foundIndex + lowerQuery.length
                addStyle(
                    style = SpanStyle(
                        background = highlightColor,
                        color = highlightTextColor,
                        fontWeight = FontWeight.Bold
                    ),
                    start = foundIndex,
                    end = endIndex
                )
                startIndex = endIndex
            }
        }
    }
}
