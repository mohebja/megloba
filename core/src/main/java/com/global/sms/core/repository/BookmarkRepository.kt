package com.global.sms.core.repository

import com.global.sms.data.dao.BookmarkDao
import com.global.sms.data.entity.BookmarkEntity
import com.global.sms.data.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

class BookmarkRepository(
    private val bookmarkDao: BookmarkDao
) {
    fun getBookmarksForThread(threadId: Long): Flow<List<BookmarkEntity>> =
        bookmarkDao.getBookmarksForThread(threadId)

    fun getAllBookmarks(): Flow<List<BookmarkEntity>> =
        bookmarkDao.getAllBookmarks()

    fun getBookmarkedMessages(): Flow<List<MessageEntity>> =
        bookmarkDao.getBookmarkedMessages()

    suspend fun addBookmark(messageId: Long, threadId: Long, note: String? = null): Long {
        return bookmarkDao.insertBookmark(
            BookmarkEntity(
                messageId = messageId,
                threadId = threadId,
                note = note
            )
        )
    }

    suspend fun removeBookmark(id: Long) {
        bookmarkDao.deleteBookmark(id)
    }

    suspend fun removeBookmarkByMessageId(messageId: Long) {
        bookmarkDao.deleteBookmarkByMessageId(messageId)
    }

    fun isMessageBookmarked(messageId: Long): Flow<Boolean> =
        bookmarkDao.isMessageBookmarked(messageId)
}
