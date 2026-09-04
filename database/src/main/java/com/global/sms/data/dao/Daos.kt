package com.global.sms.data.dao

import com.global.sms.data.entity.*

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Query("SELECT * FROM messages WHERE threadId = :threadId AND isHidden = 0 ORDER BY timestamp DESC")
    fun getMessagesForThread(threadId: Long): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE threadId = :threadId AND isHidden = 0 ORDER BY timestamp DESC")
    fun getMessagesForThreadPagingSource(threadId: Long): PagingSource<Int, MessageEntity>

    @Query("SELECT * FROM messages WHERE threadId = :threadId AND isHidden = 0 ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
    suspend fun getMessagesForThreadPaged(threadId: Long, limit: Int, offset: Int): List<MessageEntity>

    @Query("SELECT * FROM messages WHERE isHidden = 1 ORDER BY timestamp DESC")
    fun getHiddenMessages(): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE category = 'SPAM' ORDER BY timestamp DESC")
    fun getSpamMessages(): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE body LIKE '%' || :query || '%' OR address LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchMessages(query: String): Flow<List<MessageEntity>>

    @Query("""
        SELECT messages.* FROM messages 
        JOIN messages_fts ON messages.id = messages_fts.docid 
        WHERE messages_fts MATCH :query AND messages.isHidden = 0 
        ORDER BY messages.timestamp DESC
    """)
    fun searchMessagesFts(query: String): Flow<List<MessageEntity>>

    @Query("""
        SELECT messages.* FROM messages 
        JOIN messages_fts ON messages.id = messages_fts.docid 
        WHERE messages_fts MATCH :query AND messages.isHidden = 0 
        ORDER BY messages.timestamp DESC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun searchMessagesFtsPaged(query: String, limit: Int, offset: Int): List<MessageEntity>

    @Query("""
        SELECT DISTINCT messages.* FROM messages 
        LEFT JOIN messages_fts ON messages.id = messages_fts.docid 
        WHERE (:query IS NULL OR :query = '' OR messages_fts MATCH :query OR messages.body LIKE '%' || :query || '%' OR messages.address LIKE '%' || :query || '%')
        AND (:category IS NULL OR :category = '' OR messages.category = :category)
        AND (:isOtpOnly = 0 OR (messages.otpCode IS NOT NULL AND messages.otpCode != '') OR messages.body LIKE '%کد%' OR messages.body LIKE '%رمز%')
        AND (:hasAttachmentOnly = 0 OR (messages.attachmentUri IS NOT NULL AND messages.attachmentUri != '') OR messages.isMms = 1)
        AND (:isUnreadOnly = 0 OR messages.isRead = 0)
        AND (:isPinnedOnly = 0 OR messages.isPinned = 1)
        AND (:isBankOnly = 0 OR messages.category = 'BANK' OR messages.body LIKE '%تراکنش%' OR messages.body LIKE '%واریز%' OR messages.body LIKE '%برداشت%' OR messages.body LIKE '%موجودی%')
        AND (:senderFilter IS NULL OR :senderFilter = '' OR messages.address LIKE '%' || :senderFilter || '%')
        AND (:startDate IS NULL OR messages.timestamp >= :startDate)
        AND (:endDate IS NULL OR messages.timestamp <= :endDate)
        AND (:includeHidden = 1 OR messages.isHidden = 0)
        ORDER BY messages.timestamp DESC
        LIMIT 200
    """)
    fun searchMessagesAdvanced(
        query: String?,
        category: String?,
        isOtpOnly: Boolean,
        hasAttachmentOnly: Boolean,
        isUnreadOnly: Boolean,
        isPinnedOnly: Boolean,
        isBankOnly: Boolean,
        senderFilter: String?,
        startDate: Long?,
        endDate: Long?,
        includeHidden: Boolean
    ): Flow<List<MessageEntity>>


    @Query("SELECT * FROM messages ORDER BY timestamp DESC")
    fun getAllMessagesFlow(): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages")
    suspend fun getAllMessagesSync(): List<MessageEntity>

    @Update
    suspend fun updateMessage(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessagesBatch(messages: List<MessageEntity>): List<Long>

    @Transaction
    suspend fun insertMessagesInTransaction(messages: List<MessageEntity>): List<Long> {
        return insertMessagesBatch(messages)
    }

    @Query("UPDATE messages SET isRead = 1 WHERE threadId = :threadId")
    suspend fun markThreadAsRead(threadId: Long)

    @Query("UPDATE messages SET isHidden = :isHidden WHERE id = :messageId")
    suspend fun setMessageHidden(messageId: Long, isHidden: Boolean)

    @Query("UPDATE messages SET category = :category WHERE id = :messageId")
    suspend fun setMessageCategory(messageId: Long, category: MessageCategory)

    @Query("UPDATE messages SET category = :category WHERE threadId = :threadId")
    suspend fun setThreadCategory(threadId: Long, category: MessageCategory)

    @Query("DELETE FROM messages WHERE id = :messageId")
    suspend fun deleteMessage(messageId: Long)

    @Query("DELETE FROM messages WHERE threadId = :threadId")
    suspend fun deleteThreadMessages(threadId: Long)

    @Query("DELETE FROM messages WHERE category = 'SPAM'")
    suspend fun deleteAllSpamMessages(): Int

    @Query("DELETE FROM messages WHERE category = 'SPAM' AND timestamp < :beforeTimestamp")
    suspend fun deleteOldSpamMessages(beforeTimestamp: Long): Int

    @Query("DELETE FROM messages WHERE isHidden = 1 AND timestamp < :beforeTimestamp")
    suspend fun deleteOldHiddenMessages(beforeTimestamp: Long): Int

    @Query("SELECT * FROM messages WHERE id = :id")
    suspend fun getMessageById(id: Long): MessageEntity?

    @Query("SELECT * FROM messages WHERE deliveryStatus = 1 OR type = 4 ORDER BY timestamp ASC")
    fun getPendingMessages(): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE deliveryStatus = 1 OR type = 4 ORDER BY timestamp ASC")
    suspend fun getPendingMessagesOnce(): List<MessageEntity>

    @Query("SELECT * FROM messages WHERE deliveryStatus = 1 OR type = 4 ORDER BY timestamp ASC LIMIT 1")
    suspend fun getNextPendingMessage(): MessageEntity?

    @Query("SELECT * FROM messages WHERE deliveryStatus = 5 ORDER BY timestamp ASC")
    fun getRetryMessages(): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE deliveryStatus = 5 ORDER BY timestamp ASC")
    suspend fun getRetryMessagesOnce(): List<MessageEntity>

    @Query("SELECT * FROM messages WHERE deliveryStatus = 4 OR type = 5 ORDER BY timestamp DESC")
    fun getFailedMessages(): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE deliveryStatus = 4 OR type = 5 ORDER BY timestamp DESC")
    suspend fun getFailedMessagesOnce(): List<MessageEntity>

    @Query("UPDATE messages SET deliveryStatus = :status, type = :type WHERE id = :messageId")
    suspend fun updateMessageDeliveryAndType(messageId: Long, status: Int, type: Int)

    @Query("UPDATE messages SET deliveryStatus = :status WHERE id = :messageId")
    suspend fun updateDeliveryStatus(messageId: Long, status: Int)

    @Query("UPDATE messages SET retryCount = :retryCount WHERE id = :messageId")
    suspend fun updateRetryCount(messageId: Long, retryCount: Int)

    @Query("UPDATE messages SET isRead = 1 WHERE id = :messageId")
    suspend fun markMessageAsRead(messageId: Long)

    @Query("SELECT COUNT(*) FROM messages")
    fun getTotalMessageCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM messages")
    suspend fun getTotalMessageCountOnce(): Int

    @Query("SELECT COUNT(*) FROM messages WHERE type = 2")
    fun getSentMessageCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM messages WHERE type = 1")
    fun getReceivedMessageCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM messages WHERE category = 'SPAM'")
    fun getSpamCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM messages WHERE category = 'BANK'")
    fun getBankCount(): Flow<Int>

    @Query("SELECT * FROM messages WHERE category = 'BANK' OR body LIKE '%تراکنش%' OR body LIKE '%واریز%' OR body LIKE '%برداشت%' OR body LIKE '%موجودی%' OR body LIKE '%رمز پویا%' OR body LIKE '%کد تایید%' ORDER BY timestamp DESC")
    fun getBankMessages(): Flow<List<MessageEntity>>
}

@Dao
interface ConversationDao {

    @Query("SELECT * FROM conversations WHERE isHidden = 0 ORDER BY isPinned DESC, lastTimestamp DESC")
    fun getAllConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE isHidden = 0 ORDER BY isPinned DESC, lastTimestamp DESC")
    fun getAllConversationsPagingSource(): PagingSource<Int, ConversationEntity>

    @Query("SELECT * FROM conversations WHERE isHidden = 0 AND category = :category ORDER BY isPinned DESC, lastTimestamp DESC")
    fun getConversationsByCategoryPagingSource(category: MessageCategory): PagingSource<Int, ConversationEntity>

    @Query("SELECT * FROM conversations")
    suspend fun getAllConversationsSync(): List<ConversationEntity>

    @Query("SELECT * FROM conversations WHERE isHidden = 0 ORDER BY isPinned DESC, lastTimestamp DESC LIMIT :limit OFFSET :offset")
    suspend fun getConversationsPaged(limit: Int, offset: Int): List<ConversationEntity>

    @Query("SELECT * FROM conversations WHERE isHidden = 0 AND category = :category ORDER BY isPinned DESC, lastTimestamp DESC")
    fun getConversationsByCategory(category: MessageCategory): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE isHidden = 1 ORDER BY lastTimestamp DESC")
    fun getHiddenConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE address LIKE '%' || :query || '%' OR contactName LIKE '%' || :query || '%' OR lastMessage LIKE '%' || :query || '%' ORDER BY lastTimestamp DESC")
    fun searchConversations(query: String): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE threadId = :threadId")
    suspend fun getConversationByThreadId(threadId: Long): ConversationEntity?

    @Query("SELECT * FROM conversations WHERE address = :address")
    suspend fun getConversationByAddress(address: String): ConversationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateConversation(conversation: ConversationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateConversationsBatch(conversations: List<ConversationEntity>)

    @Transaction
    suspend fun insertOrUpdateConversationsInTransaction(conversations: List<ConversationEntity>) {
        insertOrUpdateConversationsBatch(conversations)
    }

    @Query("UPDATE conversations SET isHidden = :isHidden WHERE threadId = :threadId")
    suspend fun setConversationHidden(threadId: Long, isHidden: Boolean)

    @Query("UPDATE conversations SET isPinned = :isPinned, pinnedTimestamp = :pinnedTimestamp WHERE threadId = :threadId")
    suspend fun setConversationPinnedWithTimestamp(threadId: Long, isPinned: Boolean, pinnedTimestamp: Long = System.currentTimeMillis())

    @Query("UPDATE conversations SET isPinned = :isPinned WHERE threadId = :threadId")
    suspend fun setConversationPinned(threadId: Long, isPinned: Boolean)

    @Query("UPDATE conversations SET isFavorite = :isFavorite WHERE threadId = :threadId")
    suspend fun setConversationFavorite(threadId: Long, isFavorite: Boolean)

    @Query("UPDATE conversations SET muteUntil = :muteUntil, isMuted = CASE WHEN :muteUntil > 0 THEN 1 ELSE 0 END WHERE threadId = :threadId")
    suspend fun setConversationMuteUntil(threadId: Long, muteUntil: Long)

    @Query("UPDATE conversations SET isArchived = :isArchived WHERE threadId = :threadId")
    suspend fun setConversationArchived(threadId: Long, isArchived: Boolean)

    @Query("UPDATE conversations SET category = :category WHERE threadId = :threadId")
    suspend fun setConversationCategory(threadId: Long, category: MessageCategory)

    @Query("SELECT * FROM conversations WHERE isFavorite = 1 AND isHidden = 0 ORDER BY isPinned DESC, pinnedTimestamp DESC, lastTimestamp DESC")
    fun getFavoriteConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE isPinned = 1 AND isHidden = 0 ORDER BY pinnedTimestamp DESC, lastTimestamp DESC")
    fun getPinnedConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE unreadCount > 0 AND isHidden = 0 ORDER BY isPinned DESC, lastTimestamp DESC")
    fun getUnreadConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE isArchived = 1 AND isHidden = 0 ORDER BY lastTimestamp DESC")
    fun getArchivedConversations(): Flow<List<ConversationEntity>>

    @Query("UPDATE conversations SET unreadCount = 0 WHERE threadId = :threadId")
    suspend fun markConversationRead(threadId: Long)

    @Query("UPDATE conversations SET unreadCount = 1 WHERE threadId = :threadId")
    suspend fun markConversationUnread(threadId: Long)

    @Query("UPDATE conversations SET isMuted = :isMuted WHERE threadId = :threadId")
    suspend fun setConversationMuted(threadId: Long, isMuted: Boolean)

    @Query("SELECT * FROM conversations WHERE unreadCount > 0 AND isHidden = 0 ORDER BY lastTimestamp DESC")
    suspend fun getUnreadConversationsSync(): List<ConversationEntity>

    @Query("DELETE FROM conversations WHERE threadId = :threadId")
    suspend fun deleteConversation(threadId: Long)

    @Query("SELECT COUNT(*) FROM conversations WHERE isHidden = 0")
    fun getTotalConversationsCount(): Flow<Int>
}

@Dao
interface ScheduledMessageDao {

    @Query("SELECT * FROM scheduled_messages ORDER BY scheduledTimestamp ASC")
    fun getAllScheduledMessages(): Flow<List<ScheduledMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheduledMessage(message: ScheduledMessageEntity): Long

    @Query("UPDATE scheduled_messages SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)

    @Query("DELETE FROM scheduled_messages WHERE id = :id")
    suspend fun deleteScheduledMessage(id: Long)
}

@Dao
interface SpamRuleDao {

    @Query("SELECT * FROM spam_rules")
    fun getAllSpamRules(): Flow<List<SpamRuleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpamRule(rule: SpamRuleEntity)

    @Query("DELETE FROM spam_rules WHERE id = :id")
    suspend fun deleteSpamRule(id: Long)
}

@Dao
interface QuickReplyDao {

    @Query("SELECT * FROM quick_replies")
    fun getAllQuickReplies(): Flow<List<QuickReplyEntity>>

    @Query("SELECT * FROM quick_replies")
    suspend fun getAllQuickRepliesSync(): List<QuickReplyEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuickReply(quickReply: QuickReplyEntity)

    @Query("DELETE FROM quick_replies WHERE id = :id")
    suspend fun deleteQuickReply(id: Long)
}

@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories ORDER BY priority DESC, id ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories ORDER BY priority DESC, id ASC")
    suspend fun getAllCategoriesOnce(): List<CategoryEntity>

    @Query("SELECT * FROM categories ORDER BY priority DESC, id ASC")
    suspend fun getAllCategoriesSync(): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: Long): CategoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteCategoryById(id: Long)
}

@Dao
interface SettingsDao {

    @Query("SELECT * FROM settings WHERE id = 1")
    fun getSettings(): Flow<SettingsEntity?>

    @Query("SELECT * FROM settings WHERE id = 1")
    suspend fun getSettingsOnce(): SettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSettings(settings: SettingsEntity)
}

@Dao
interface SearchHistoryDao {

    @Query("SELECT * FROM search_history ORDER BY timestamp DESC LIMIT 20")
    fun getRecentSearches(): Flow<List<SearchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearch(searchHistory: SearchHistoryEntity)

    @Query("DELETE FROM search_history WHERE id = :id")
    suspend fun deleteSearchById(id: Long)

    @Query("DELETE FROM search_history")
    suspend fun clearSearchHistory()
}

@Dao
interface ClassificationRuleDao {

    @Query("SELECT * FROM classification_rules ORDER BY priority DESC, id ASC")
    fun getAllRulesFlow(): Flow<List<ClassificationRuleEntity>>

    @Query("SELECT * FROM classification_rules ORDER BY priority DESC, id ASC")
    suspend fun getAllRulesSync(): List<ClassificationRuleEntity>

    @Query("SELECT * FROM classification_rules WHERE isEnabled = 1 ORDER BY priority DESC, id ASC")
    suspend fun getEnabledRulesSync(): List<ClassificationRuleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: ClassificationRuleEntity): Long

    @Update
    suspend fun updateRule(rule: ClassificationRuleEntity)

    @Query("DELETE FROM classification_rules WHERE id = :id")
    suspend fun deleteRuleById(id: Long)

    @Query("UPDATE classification_rules SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun setRuleEnabled(id: Long, isEnabled: Boolean)

    @Query("SELECT COUNT(*) FROM classification_rules")
    suspend fun getRulesCount(): Int
}

@Dao
interface ContactGroupDao {

    @Query("SELECT * FROM contact_groups ORDER BY name ASC")
    fun getAllGroupsFlow(): Flow<List<ContactGroupEntity>>

    @Query("SELECT * FROM contact_groups ORDER BY name ASC")
    suspend fun getAllGroupsSync(): List<ContactGroupEntity>

    @Query("SELECT * FROM contact_groups WHERE id = :id")
    suspend fun getGroupById(id: Long): ContactGroupEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: ContactGroupEntity): Long

    @Update
    suspend fun updateGroup(group: ContactGroupEntity)

    @Query("DELETE FROM contact_groups WHERE id = :id")
    suspend fun deleteGroupById(id: Long)
}

@Dao
interface ContactDao {

    @Query("SELECT * FROM contacts ORDER BY name ASC")
    fun getAllContactsFlow(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts ORDER BY name ASC")
    suspend fun getAllContactsSync(): List<ContactEntity>

    @Query("SELECT * FROM contacts WHERE favorite = 1 ORDER BY name ASC")
    fun getFavoriteContactsFlow(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE id = :id")
    suspend fun getContactById(id: Long): ContactEntity?

    @Query("SELECT * FROM contacts WHERE phoneNumber = :phoneNumber OR normalizedNumber = :phoneNumber LIMIT 1")
    suspend fun getContactByPhone(phoneNumber: String): ContactEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: ContactEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContactsBatch(contacts: List<ContactEntity>): List<Long>

    @Update
    suspend fun updateContact(contact: ContactEntity)

    @Query("DELETE FROM contacts WHERE id = :id")
    suspend fun deleteContactById(id: Long)

    @Query("DELETE FROM contacts")
    suspend fun clearAllContacts()
}

@Dao
interface ContactGroupMemberDao {

    @Query("SELECT * FROM contact_group_members WHERE groupId = :groupId")
    suspend fun getMembersByGroupId(groupId: Long): List<ContactGroupMemberEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: ContactGroupMemberEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMembersBatch(members: List<ContactGroupMemberEntity>)

    @Query("DELETE FROM contact_group_members WHERE groupId = :groupId AND contactId = :contactId")
    suspend fun removeMember(groupId: Long, contactId: Long)

    @Query("DELETE FROM contact_group_members WHERE groupId = :groupId")
    suspend fun removeAllMembersByGroupId(groupId: Long)
}

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders WHERE threadId = :threadId ORDER BY scheduledTime ASC")
    fun getRemindersForThread(threadId: Long): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE isCompleted = 0 ORDER BY scheduledTime ASC")
    fun getAllActiveReminders(): Flow<List<ReminderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity): Long

    @Update
    suspend fun updateReminder(reminder: ReminderEntity)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteReminder(id: Long)
}

@Dao
interface ConversationTagDao {
    @Query("SELECT * FROM conversation_tags WHERE threadId = :threadId")
    fun getTagsForThread(threadId: Long): Flow<List<ConversationTagEntity>>

    @Query("SELECT threadId FROM conversation_tags WHERE tag = :tag")
    fun getThreadsForTag(tag: String): Flow<List<Long>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: ConversationTagEntity): Long

    @Query("DELETE FROM conversation_tags WHERE threadId = :threadId AND tag = :tag")
    suspend fun deleteTag(threadId: Long, tag: String)

    @Query("SELECT DISTINCT tag FROM conversation_tags ORDER BY tag ASC")
    fun getAllTags(): Flow<List<String>>
}

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks WHERE threadId = :threadId ORDER BY timestamp DESC")
    fun getBookmarksForThread(threadId: Long): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks ORDER BY timestamp DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Query("SELECT messages.* FROM messages JOIN bookmarks ON messages.id = bookmarks.messageId ORDER BY bookmarks.timestamp DESC")
    fun getBookmarkedMessages(): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity): Long

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteBookmark(id: Long)

    @Query("DELETE FROM bookmarks WHERE messageId = :messageId")
    suspend fun deleteBookmarkByMessageId(messageId: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE messageId = :messageId)")
    fun isMessageBookmarked(messageId: Long): Flow<Boolean>
}

@Dao
interface SmsImportLogDao {
    @Query("SELECT * FROM sms_import_logs ORDER BY timestamp DESC")
    fun getAllImportLogs(): Flow<List<SmsImportLogEntity>>

    @Query("SELECT * FROM sms_import_logs ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestImportLog(): SmsImportLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImportLog(log: SmsImportLogEntity): Long

    @Query("DELETE FROM sms_import_logs")
    suspend fun clearImportLogs()
}





