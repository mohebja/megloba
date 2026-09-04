package com.global.sms.core.search

import com.global.sms.data.dao.ContactDao
import com.global.sms.data.dao.MessageDao
import com.global.sms.data.dao.SearchHistoryDao
import com.global.sms.data.entity.MessageEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SearchEngine(
    private val messageDao: MessageDao,
    private val contactDao: ContactDao,
    private val searchHistoryDao: SearchHistoryDao
) {
    val repository = SearchRepository(messageDao)
    val filterEngine = SearchFilterEngine()
    val rankingEngine = SearchRankingEngine()
    val historyManager = SearchHistoryManager(searchHistoryDao)
    val suggestionEngine = SearchSuggestionEngine(contactDao, searchHistoryDao)
    val semanticEngine: SemanticSearchEngine = DefaultSemanticSearchEngine()

    fun search(criteria: SearchFilterCriteria): Flow<List<SearchResultItem>> {
        val parsed = SearchQueryParser.parse(criteria.query)

        val targetCategory = criteria.categories.firstOrNull() ?: parsed.detectedCategory
        val startDate = criteria.startDate ?: parsed.startDate
        val endDate = criteria.endDate ?: parsed.endDate

        return repository.executeSearch(
            query = parsed.normalizedText.ifBlank { null },
            category = targetCategory,
            isOtpOnly = criteria.isOtpOnly || targetCategory == com.global.sms.data.entity.MessageCategory.OTP,
            hasAttachmentOnly = criteria.hasAttachmentOnly,
            isUnreadOnly = criteria.isUnreadOnly,
            isPinnedOnly = criteria.isPinnedOnly,
            isBankOnly = criteria.isBankOnly || targetCategory == com.global.sms.data.entity.MessageCategory.BANK,
            senderFilter = criteria.senderFilter ?: parsed.phoneNumber,
            startDate = startDate,
            endDate = endDate,
            includeHidden = criteria.isHiddenOnly
        ).map { messages ->
            val filtered = filterEngine.filterMessages(messages, criteria)
            rankingEngine.rankResults(criteria.query, filtered)
        }
    }
}
