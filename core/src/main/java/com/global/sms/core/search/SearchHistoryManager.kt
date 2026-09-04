package com.global.sms.core.search

import com.global.sms.data.dao.SearchHistoryDao
import com.global.sms.data.entity.SearchHistoryEntity
import kotlinx.coroutines.flow.Flow

class SearchHistoryManager(
    private val searchHistoryDao: SearchHistoryDao
) {
    fun getRecentSearches(): Flow<List<SearchHistoryEntity>> =
        searchHistoryDao.getRecentSearches()

    suspend fun recordSearch(query: String) {
        if (query.isBlank()) return
        val normalized = SearchQueryParser.normalizeText(query)
        searchHistoryDao.insertSearch(
            SearchHistoryEntity(
                query = normalized,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteSearch(id: Long) {
        searchHistoryDao.deleteSearchById(id)
    }

    suspend fun clearHistory() {
        searchHistoryDao.clearSearchHistory()
    }
}
