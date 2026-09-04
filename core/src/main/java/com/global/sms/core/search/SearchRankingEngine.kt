package com.global.sms.core.search

import com.global.sms.data.entity.MessageEntity

data class SearchResultItem(
    val message: MessageEntity,
    val score: Double,
    val highlightedBody: String,
    val matchedTokens: List<String>
)

class SearchRankingEngine {

    fun rankResults(query: String, messages: List<MessageEntity>): List<SearchResultItem> {
        return rank(query, messages)
    }

    fun rankResults(messagesList: List<MessageEntity>, queryString: String): List<SearchResultItem> {
        return rank(queryString, messagesList)
    }

    private fun rank(query: String, messages: List<MessageEntity>): List<SearchResultItem> {
        val parsed = SearchQueryParser.parse(query)
        val normalizedQuery = parsed.normalizedText.lowercase()

        val results = messages.map { msg ->
            val normalizedBody = SearchQueryParser.normalizeText(msg.body).lowercase()
            val normalizedAddress = SearchQueryParser.normalizeText(msg.address).lowercase()

            var score = 0.0

            // Exact query match
            if (normalizedBody == normalizedQuery) {
                score += 100.0
            } else if (normalizedBody.startsWith(normalizedQuery)) {
                score += 50.0
            } else if (normalizedBody.contains(normalizedQuery)) {
                score += 30.0
            }

            // Keyword matches
            val matchedTokens = mutableListOf<String>()
            parsed.keywords.forEach { token ->
                val lowerToken = token.lowercase()
                if (lowerToken.length > 1 && normalizedBody.contains(lowerToken)) {
                    score += 10.0
                    matchedTokens.add(token)
                }
            }

            // Address match bonus
            if (normalizedAddress.contains(normalizedQuery)) {
                score += 25.0
                matchedTokens.add(msg.address)
            }

            // Category match bonus
            if (parsed.detectedCategory != null && msg.category == parsed.detectedCategory) {
                score += 15.0
            }

            // Recency boost (decay over 30 days)
            val daysOld = (System.currentTimeMillis() - msg.timestamp) / (1000.0 * 60 * 60 * 24)
            val recencyBoost = maxOf(0.0, 20.0 - (daysOld * 0.5))
            score += recencyBoost

            val highlighted = highlightQueryTokens(msg.body, parsed.keywords.ifEmpty { listOf(query) })

            SearchResultItem(
                message = msg,
                score = score,
                highlightedBody = highlighted,
                matchedTokens = matchedTokens.distinct()
            )
        }

        return results.sortedByDescending { it.score }
    }

    private fun highlightQueryTokens(body: String, tokens: List<String>): String {
        var result = body
        tokens.filter { it.isNotBlank() && it.length > 1 }.forEach { token ->
            result = result.replace(token, "**$token**", ignoreCase = true)
        }
        return result
    }
}
