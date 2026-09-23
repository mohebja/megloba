package com.global.sms.core.search

import com.global.sms.data.dao.ContactDao
import com.global.sms.data.dao.MessageDao
import com.global.sms.data.dao.SearchHistoryDao
import com.global.sms.data.entity.MessageCategory
import com.global.sms.data.entity.MessageEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class SearchEngine(
    private val messageDao: MessageDao,
    private val contactDao: ContactDao,
    private val searchHistoryDao: SearchHistoryDao,
    semanticSearchEngine: SemanticSearchEngine = DefaultSemanticSearchEngine()
) {
    val repository = SearchRepository(messageDao)
    val filterEngine = SearchFilterEngine()
    val rankingEngine = SearchRankingEngine()
    val historyManager = SearchHistoryManager(searchHistoryDao)
    val suggestionEngine = SearchSuggestionEngine(contactDao, searchHistoryDao)
    val semanticEngine: SemanticSearchEngine = semanticSearchEngine

    fun search(criteria: SearchFilterCriteria): Flow<List<SearchResultItem>> {
        val parsed = SearchQueryParser.parse(criteria.query)

        val targetCategory = criteria.categories.firstOrNull() ?: parsed.detectedCategory
        val startDate = criteria.startDate ?: parsed.startDate
        val endDate = criteria.endDate ?: parsed.endDate

        val exactFlow = repository.executeSearch(
            query = parsed.normalizedText.ifBlank { null },
            category = targetCategory,
            isOtpOnly = criteria.isOtpOnly || targetCategory == MessageCategory.OTP,
            hasAttachmentOnly = criteria.hasAttachmentOnly,
            isUnreadOnly = criteria.isUnreadOnly,
            isPinnedOnly = criteria.isPinnedOnly,
            isBankOnly = criteria.isBankOnly || targetCategory == MessageCategory.BANK,
            senderFilter = criteria.senderFilter ?: parsed.phoneNumber,
            startDate = startDate,
            endDate = endDate,
            includeHidden = criteria.isHiddenOnly
        )

        val broadFlow = if (criteria.query.isNotBlank()) {
            repository.executeSearch(
                query = null,
                category = targetCategory,
                isOtpOnly = criteria.isOtpOnly || targetCategory == MessageCategory.OTP,
                hasAttachmentOnly = criteria.hasAttachmentOnly,
                isUnreadOnly = criteria.isUnreadOnly,
                isPinnedOnly = criteria.isPinnedOnly,
                isBankOnly = criteria.isBankOnly || targetCategory == MessageCategory.BANK,
                senderFilter = criteria.senderFilter ?: parsed.phoneNumber,
                startDate = startDate,
                endDate = endDate,
                includeHidden = criteria.isHiddenOnly
            )
        } else {
            flowOf(emptyList())
        }

        return combine(exactFlow, broadFlow) { exactMessages, broadMessages ->
            val corpus = (exactMessages + broadMessages).distinctBy { it.id }
            val filtered = filterEngine.filterMessages(corpus, criteria.copy(query = ""))
            val lexical = rankingEngine.rankResults(criteria.query, filtered)
            if (criteria.query.isBlank()) {
                lexical
            } else {
                mergeWithSemantic(criteria.query, filtered, lexical)
            }
        }
    }

    private fun mergeWithSemantic(
        query: String,
        corpus: List<MessageEntity>,
        lexical: List<SearchResultItem>
    ): List<SearchResultItem> {
        val semanticHits = semanticEngine.rankCorpus(query, corpus, limit = corpus.size.coerceAtLeast(1))
        if (semanticHits.isEmpty()) return lexical

        val lexicalById = lexical.associateBy { it.message.id }
        val semanticById = semanticHits.associateBy { it.message.id }
        val mergedIds = LinkedHashSet<Long>()
        lexical.forEach { mergedIds += it.message.id }
        semanticHits.forEach { mergedIds += it.message.id }

        return mergedIds.mapNotNull { id ->
            val lexicalItem = lexicalById[id]
            val semanticHit = semanticById[id]
            val message = lexicalItem?.message ?: semanticHit?.message ?: return@mapNotNull null
            val combinedScore = (lexicalItem?.score ?: 0.0) + (semanticHit?.score ?: 0.0) * 1.4
            SearchResultItem(
                message = message,
                score = combinedScore,
                highlightedBody = lexicalItem?.highlightedBody ?: message.body,
                matchedTokens = ((lexicalItem?.matchedTokens ?: emptyList()) + (semanticHit?.matchedTokens ?: emptyList())).distinct()
            )
        }.sortedByDescending { it.score }
    }
}
