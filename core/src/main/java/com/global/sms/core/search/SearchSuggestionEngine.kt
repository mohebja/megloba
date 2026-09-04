package com.global.sms.core.search

import com.global.sms.data.dao.ContactDao
import com.global.sms.data.dao.SearchHistoryDao
import com.global.sms.data.entity.ContactEntity
import com.global.sms.data.entity.MessageCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf

sealed class SearchSuggestion {
    data class ContactSuggestion(val contact: ContactEntity) : SearchSuggestion()
    data class CategorySuggestion(val category: MessageCategory, val displayName: String) : SearchSuggestion()
    data class KeywordSuggestion(val keyword: String) : SearchSuggestion()
    data class HistorySuggestion(val query: String, val id: Long) : SearchSuggestion()
}

class SearchSuggestionEngine(
    private val contactDao: ContactDao,
    private val searchHistoryDao: SearchHistoryDao
) {

    fun generateSuggestions(query: String): Flow<List<SearchSuggestion>> {
        val normalized = SearchQueryParser.normalizeText(query).lowercase()

        val defaultCategorySuggestions = listOf(
            SearchSuggestion.CategorySuggestion(MessageCategory.BANK, "بانک‌ها و تراکنش‌ها"),
            SearchSuggestion.CategorySuggestion(MessageCategory.OTP, "کد‌های تایید (OTP)"),
            SearchSuggestion.CategorySuggestion(MessageCategory.SPAM, "پیام‌های اسپم"),
            SearchSuggestion.CategorySuggestion(MessageCategory.WORK, "ارتباطات کاری")
        )

        val historyFlow = searchHistoryDao.getRecentSearches()
        val contactsFlow = contactDao.getAllContactsFlow()

        return combine(historyFlow, contactsFlow) { historyList, contacts ->
            val suggestions = mutableListOf<SearchSuggestion>()

            if (normalized.isBlank()) {
                // Return recent history + categories when query is empty
                historyList.take(5).forEach {
                    suggestions.add(SearchSuggestion.HistorySuggestion(it.query, it.id))
                }
                suggestions.addAll(defaultCategorySuggestions)
            } else {
                // Filter matching history
                historyList.filter { it.query.lowercase().contains(normalized) }.take(3).forEach {
                    suggestions.add(SearchSuggestion.HistorySuggestion(it.query, it.id))
                }

                // Filter matching contacts
                contacts.filter {
                    it.name.lowercase().contains(normalized) || it.phoneNumber.contains(normalized)
                }.take(3).forEach {
                    suggestions.add(SearchSuggestion.ContactSuggestion(it))
                }

                // Matching common Persian banking/messaging keywords
                val commonKeywords = listOf("رمزدوم", "تراکنش", "موجودی", "امروز", "دیروز", "فاکتور", "سفارش", "کد ورود")
                commonKeywords.filter { it.contains(normalized) }.forEach {
                    suggestions.add(SearchSuggestion.KeywordSuggestion(it))
                }
            }

            suggestions
        }
    }
}
