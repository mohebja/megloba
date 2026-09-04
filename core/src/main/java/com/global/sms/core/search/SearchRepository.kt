package com.global.sms.core.search

import com.global.sms.data.dao.MessageDao
import com.global.sms.data.entity.MessageCategory
import com.global.sms.data.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

class SearchRepository(
    private val messageDao: MessageDao
) {

    fun executeSearch(
        query: String?,
        category: MessageCategory?,
        isOtpOnly: Boolean = false,
        hasAttachmentOnly: Boolean = false,
        isUnreadOnly: Boolean = false,
        isPinnedOnly: Boolean = false,
        isBankOnly: Boolean = false,
        senderFilter: String? = null,
        startDate: Long? = null,
        endDate: Long? = null,
        includeHidden: Boolean = false
    ): Flow<List<MessageEntity>> {
        val normQuery = query?.let { SearchQueryParser.normalizeText(it) }
        val categoryStr = category?.name

        return messageDao.searchMessagesAdvanced(
            query = normQuery,
            category = categoryStr,
            isOtpOnly = isOtpOnly,
            hasAttachmentOnly = hasAttachmentOnly,
            isUnreadOnly = isUnreadOnly,
            isPinnedOnly = isPinnedOnly,
            isBankOnly = isBankOnly,
            senderFilter = senderFilter,
            startDate = startDate,
            endDate = endDate,
            includeHidden = includeHidden
        )
    }

    suspend fun executeFtsQuery(query: String): Flow<List<MessageEntity>> {
        val norm = SearchQueryParser.normalizeText(query)
        return messageDao.searchMessagesFts("*$norm*")
    }
}
