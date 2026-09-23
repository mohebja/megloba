package com.global.sms.core.search

import com.global.sms.core.ai.nlp.LocalNlpEngine
import com.global.sms.data.entity.MessageCategory
import com.global.sms.data.entity.MessageEntity
import java.util.Locale
import kotlin.math.ln

data class SemanticHit(
    val message: MessageEntity,
    val score: Double,
    val matchedTokens: List<String> = emptyList()
)

interface SemanticSearchEngine {
    suspend fun semanticSearch(query: String, limit: Int = 20): List<MessageEntity>
    suspend fun indexMessage(message: MessageEntity)
    fun rankCorpus(query: String, corpus: List<MessageEntity>, limit: Int = 20): List<SearchResultItem> = emptyList()
}

class DefaultSemanticSearchEngine : SemanticSearchEngine {
    override suspend fun semanticSearch(query: String, limit: Int): List<MessageEntity> {
        // Fallback placeholder for local vector search / LLM embeddings
        return emptyList()
    }

    override suspend fun indexMessage(message: MessageEntity) {
        // No-op for base engine
    }

    override fun rankCorpus(query: String, corpus: List<MessageEntity>, limit: Int): List<SearchResultItem> {
        val cleanQuery = SearchQueryParser.normalizeText(query).trim().lowercase(Locale.ROOT)
        if (cleanQuery.isBlank() || corpus.isEmpty()) return emptyList()

        val isOtpQuery = cleanQuery.contains("otp") || cleanQuery.contains("verification") ||
                cleanQuery.contains("رمز") || cleanQuery.contains("کد ورود") ||
                cleanQuery.contains("کد تایید") || cleanQuery.contains("فعالسازی")

        val isBankQuery = cleanQuery.contains("بانک") || cleanQuery.contains("وام") ||
                cleanQuery.contains("تراکنش") || cleanQuery.contains("واریز") ||
                cleanQuery.contains("برداشت") || cleanQuery.contains("حساب") ||
                cleanQuery.contains("bank") || cleanQuery.contains("transaction")

        val isSpamQuery = cleanQuery.contains("اسپم") || cleanQuery.contains("تبلیغ") || cleanQuery.contains("spam")

        val isPackageQuery = cleanQuery.contains("پست") || cleanQuery.contains("مرسوله") ||
                cleanQuery.contains("رهگیری") || cleanQuery.contains("تیپاکس") ||
                cleanQuery.contains("ارسال شد") || cleanQuery.contains("package") || cleanQuery.contains("delivery")

        val isWorkQuery = cleanQuery.contains("جلسه") || cleanQuery.contains("کاری") ||
                cleanQuery.contains("قرارداد") || cleanQuery.contains("meeting") || cleanQuery.contains("business")

        val queryTokens = LocalNlpEngine.tokenizeAndClean(cleanQuery).ifEmpty {
            cleanQuery.split("\\s+".toRegex()).filter { it.length > 1 }
        }

        // 3. Corpus-wide token document frequencies for light TF-IDF
        val corpusSize = corpus.size.toDouble().coerceAtLeast(1.0)
        val tokenDocFreq = mutableMapOf<String, Int>()
        corpus.forEach { msg ->
            val msgTokens = LocalNlpEngine.tokenizeAndClean(msg.body).toSet()
            msgTokens.forEach { token ->
                tokenDocFreq[token] = (tokenDocFreq[token] ?: 0) + 1
            }
        }

        val now = System.currentTimeMillis()

        return corpus.mapNotNull { msg ->
            val normBody = SearchQueryParser.normalizeText(msg.body).lowercase(Locale.ROOT)
            val normAddress = SearchQueryParser.normalizeText(msg.address).lowercase(Locale.ROOT)
            var score = 0.0
            val matchedTokens = mutableListOf<String>()

            // 1. Semantic category and synonym intent matching
            if (isBankQuery) {
                if (msg.category == MessageCategory.BANK || msg.category == MessageCategory.TRANSACTIONS) {
                    score += 50.0
                    matchedTokens.add("بانک/امور مالی")
                }
                val bankKeywords = listOf("واریز", "برداشت", "تراکنش", "حساب", "مبلغ", "تومان", "ریال", "بانک")
                for (kw in bankKeywords) {
                    if (normBody.contains(kw)) {
                        score += 10.0
                        matchedTokens.add(kw)
                    }
                }
            }

            if (isOtpQuery) {
                if (msg.category == MessageCategory.OTP || msg.otpCode != null) {
                    score += 50.0
                    matchedTokens.add("کد ورود/اعتبارسنجی (OTP)")
                }
                if (normBody.contains("کد ورود") || normBody.contains("کد تایید") || normBody.contains("رمز پویا")) {
                    score += 25.0
                    matchedTokens.add("کد تایید")
                }
            }

            if (isSpamQuery) {
                if (msg.category == MessageCategory.SPAM) {
                    score += 50.0
                    matchedTokens.add("اسپم/تبلیغات")
                }
            }

            if (isPackageQuery) {
                val packageKeywords = listOf("پست", "مرسوله", "کد رهگیری", "تیپاکس", "ارسال شد", "بسته")
                for (kw in packageKeywords) {
                    if (normBody.contains(kw)) {
                        score += 20.0
                        matchedTokens.add(kw)
                    }
                }
            }

            if (isWorkQuery) {
                val workKeywords = listOf("جلسه", "کاری", "قرارداد", "ملاقات", "دفتر", "ساعت")
                for (kw in workKeywords) {
                    if (normBody.contains(kw)) {
                        score += 15.0
                        matchedTokens.add(kw)
                    }
                }
            }

            // 2. Direct lexical match and TF-IDF token scoring
            val msgTokens = LocalNlpEngine.tokenizeAndClean(msg.body)
            for (token in queryTokens) {
                if (normBody.contains(token)) {
                    val df = tokenDocFreq[token] ?: 1
                    val idf = ln(corpusSize / df.toDouble()).coerceAtLeast(0.5)
                    val tf = msgTokens.count { it == token }.coerceAtLeast(1)
                    score += 15.0 + (tf * idf * 8.0)
                    matchedTokens.add(token)
                }
                if (normAddress.contains(token)) {
                    score += 15.0
                    matchedTokens.add(token)
                }
            }

            // 4. Exact phrase match boost
            if (normBody.contains(cleanQuery)) {
                score += 40.0
            }

            // 5. Jaccard & Cosine similarity from LocalNlpEngine
            val jaccardSim = LocalNlpEngine.calculateSemanticSimilarity(cleanQuery, msg.body)
            if (jaccardSim > 0.1f) {
                score += (jaccardSim * 30.0)
            }

            // 6. Recency Boost (daily decay)
            val ageDays = ((now - msg.timestamp) / (24 * 3600 * 1000L)).coerceAtLeast(0)
            val recencyMultiplier = 1.0 / (1.0 + ageDays * 0.01)
            score *= recencyMultiplier

            if (score > 0.0) {
                SearchResultItem(
                    message = msg,
                    score = score,
                    highlightedBody = msg.body,
                    matchedTokens = matchedTokens.distinct()
                )
            } else {
                null
            }
        }.sortedByDescending { it.score }
        .take(limit)
    }
}

