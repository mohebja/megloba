package com.global.sms.core.repository

import com.global.sms.data.dao.ConversationDao
import com.global.sms.data.dao.ConversationTagDao
import com.global.sms.data.entity.ConversationEntity
import com.global.sms.data.entity.ConversationTagEntity
import kotlinx.coroutines.flow.Flow

class ConversationManagementRepository(
    private val conversationDao: ConversationDao,
    private val conversationTagDao: ConversationTagDao
) {
    fun getAllConversations(): Flow<List<ConversationEntity>> =
        conversationDao.getAllConversations()

    fun getFavoriteConversations(): Flow<List<ConversationEntity>> =
        conversationDao.getFavoriteConversations()

    fun getPinnedConversations(): Flow<List<ConversationEntity>> =
        conversationDao.getPinnedConversations()

    fun getUnreadConversations(): Flow<List<ConversationEntity>> =
        conversationDao.getUnreadConversations()

    fun getArchivedConversations(): Flow<List<ConversationEntity>> =
        conversationDao.getArchivedConversations()

    suspend fun setPinned(threadId: Long, isPinned: Boolean) {
        conversationDao.setConversationPinnedWithTimestamp(threadId, isPinned, System.currentTimeMillis())
    }

    suspend fun setFavorite(threadId: Long, isFavorite: Boolean) {
        conversationDao.setConversationFavorite(threadId, isFavorite)
    }

    suspend fun setMuteDuration(threadId: Long, durationMs: Long) {
        val muteUntil = if (durationMs > 0) System.currentTimeMillis() + durationMs else 0L
        conversationDao.setConversationMuteUntil(threadId, muteUntil)
    }

    suspend fun setArchived(threadId: Long, isArchived: Boolean) {
        conversationDao.setConversationArchived(threadId, isArchived)
    }

    fun getTagsForThread(threadId: Long): Flow<List<ConversationTagEntity>> =
        conversationTagDao.getTagsForThread(threadId)

    suspend fun addTag(threadId: Long, tag: String): Long {
        return conversationTagDao.insertTag(ConversationTagEntity(threadId = threadId, tag = tag))
    }

    suspend fun removeTag(threadId: Long, tag: String) {
        conversationTagDao.deleteTag(threadId, tag)
    }
}
