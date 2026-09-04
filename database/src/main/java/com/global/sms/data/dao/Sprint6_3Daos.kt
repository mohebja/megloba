package com.global.sms.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.global.sms.data.entity.AiMemoryEntity
import com.global.sms.data.entity.ConversationInsightEntity
import com.global.sms.data.entity.EmotionAnalysisEntity
import com.global.sms.data.entity.SemanticIndexEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AiMemoryDao {
    @Query("SELECT * FROM ai_memories ORDER BY updatedAt DESC")
    fun getAllMemoriesFlow(): Flow<List<AiMemoryEntity>>

    @Query("SELECT * FROM ai_memories WHERE address = :address ORDER BY updatedAt DESC")
    fun getMemoriesByAddress(address: String): Flow<List<AiMemoryEntity>>

    @Query("SELECT * FROM ai_memories WHERE address = :address ORDER BY updatedAt DESC")
    suspend fun getMemoriesByAddressList(address: String): List<AiMemoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateMemory(memory: AiMemoryEntity): Long

    @Query("DELETE FROM ai_memories WHERE id = :id")
    suspend fun deleteMemory(id: Long)

    @Query("DELETE FROM ai_memories")
    suspend fun deleteAllMemories()
}

@Dao
interface ConversationInsightDao {
    @Query("SELECT * FROM conversation_insights WHERE threadId = :threadId ORDER BY createdAt DESC LIMIT 1")
    suspend fun getInsightForThread(threadId: Long): ConversationInsightEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInsight(insight: ConversationInsightEntity): Long
}

@Dao
interface SemanticIndexDao {
    @Query("SELECT * FROM semantic_indices WHERE keyword LIKE '%' || :keyword || '%'")
    suspend fun findByKeyword(keyword: String): List<SemanticIndexEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIndex(index: SemanticIndexEntity): Long
}

@Dao
interface EmotionAnalysisDao {
    @Query("SELECT * FROM emotion_analyses WHERE messageId = :messageId LIMIT 1")
    suspend fun getEmotionByMessageId(messageId: Long): EmotionAnalysisEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmotion(emotion: EmotionAnalysisEntity): Long
}
