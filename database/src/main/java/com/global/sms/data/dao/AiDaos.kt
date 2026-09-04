package com.global.sms.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.global.sms.data.entity.AIMessageAnalysisEntity
import com.global.sms.data.entity.AiFeedbackEntity
import com.global.sms.data.entity.AiMetadataEntity
import com.global.sms.data.entity.AiSettingsEntity
import com.global.sms.data.entity.FinancialTransactionEntity
import com.global.sms.data.entity.OtpEntity
import com.global.sms.data.entity.SmartReplyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AiSettingsDao {
    @Query("SELECT * FROM ai_settings WHERE id = 1")
    fun getAiSettingsFlow(): Flow<AiSettingsEntity?>

    @Query("SELECT * FROM ai_settings WHERE id = 1")
    suspend fun getAiSettingsOnce(): AiSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAiSettings(settings: AiSettingsEntity)
}

@Dao
interface AiMetadataDao {
    @Query("SELECT * FROM ai_metadata WHERE messageId = :messageId")
    suspend fun getMetadataForMessage(messageId: Long): AiMetadataEntity?

    @Query("SELECT * FROM ai_metadata WHERE messageId = :messageId")
    fun getMetadataFlowForMessage(messageId: Long): Flow<AiMetadataEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMetadata(metadata: AiMetadataEntity)

    @Query("DELETE FROM ai_metadata WHERE messageId = :messageId")
    suspend fun deleteMetadataForMessage(messageId: Long)

    @Query("DELETE FROM ai_metadata")
    suspend fun clearAllMetadata()
}

@Dao
interface FinancialTransactionDao {
    @Query("SELECT * FROM financial_transactions ORDER BY timestamp DESC")
    fun getAllTransactionsFlow(): Flow<List<FinancialTransactionEntity>>

    @Query("SELECT * FROM financial_transactions ORDER BY timestamp DESC")
    suspend fun getAllTransactionsOnce(): List<FinancialTransactionEntity>

    @Query("SELECT * FROM financial_transactions WHERE timestamp >= :startTime ORDER BY timestamp DESC")
    fun getTransactionsSinceFlow(startTime: Long): Flow<List<FinancialTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: FinancialTransactionEntity)

    @Query("DELETE FROM financial_transactions WHERE messageId = :messageId")
    suspend fun deleteTransactionByMessageId(messageId: Long)

    @Query("DELETE FROM financial_transactions")
    suspend fun clearAllTransactions()
}

@Dao
interface AiFeedbackDao {
    @Query("SELECT * FROM ai_feedback ORDER BY timestamp DESC")
    fun getAllFeedbackFlow(): Flow<List<AiFeedbackEntity>>

    @Query("SELECT * FROM ai_feedback ORDER BY timestamp DESC")
    suspend fun getAllFeedbackOnce(): List<AiFeedbackEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeedback(feedback: AiFeedbackEntity)

    @Query("DELETE FROM ai_feedback")
    suspend fun clearAllFeedback()
}

@Dao
interface AiAnalysisDao {
    @Query("SELECT * FROM ai_message_analysis WHERE messageId = :messageId")
    suspend fun getAnalysisForMessage(messageId: Long): AIMessageAnalysisEntity?

    @Query("SELECT * FROM ai_message_analysis WHERE messageId = :messageId")
    fun getAnalysisFlowForMessage(messageId: Long): Flow<AIMessageAnalysisEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnalysis(analysis: AIMessageAnalysisEntity)

    @Query("DELETE FROM ai_message_analysis WHERE messageId = :messageId")
    suspend fun deleteAnalysisForMessage(messageId: Long)

    @Query("DELETE FROM ai_message_analysis")
    suspend fun clearAllAnalysis()
}

@Dao
interface OtpDao {
    @Query("SELECT * FROM otp_codes ORDER BY receivedTimestamp DESC")
    fun getAllOtpsFlow(): Flow<List<OtpEntity>>

    @Query("SELECT * FROM otp_codes WHERE isUsed = 0 ORDER BY receivedTimestamp DESC")
    fun getActiveOtpsFlow(): Flow<List<OtpEntity>>

    @Query("SELECT * FROM otp_codes WHERE messageId = :messageId LIMIT 1")
    suspend fun getOtpForMessage(messageId: Long): OtpEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOtp(otp: OtpEntity): Long

    @Query("UPDATE otp_codes SET isUsed = 1 WHERE id = :id")
    suspend fun markAsUsed(id: Long)

    @Query("DELETE FROM otp_codes WHERE id = :id")
    suspend fun deleteOtp(id: Long)

    @Query("DELETE FROM otp_codes WHERE receivedTimestamp < :threshold")
    suspend fun deleteOtpsOlderThan(threshold: Long)

    @Query("DELETE FROM otp_codes")
    suspend fun clearAllOtps()
}

@Dao
interface SmartReplyDao {
    @Query("SELECT * FROM smart_replies WHERE category = :category OR category = 'GENERAL' ORDER BY usageCount DESC")
    fun getRepliesForCategoryFlow(category: String): Flow<List<SmartReplyEntity>>

    @Query("SELECT * FROM smart_replies ORDER BY usageCount DESC LIMIT :limit")
    suspend fun getTopReplies(limit: Int = 10): List<SmartReplyEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReply(reply: SmartReplyEntity): Long

    @Query("UPDATE smart_replies SET usageCount = usageCount + 1 WHERE id = :id")
    suspend fun incrementUsage(id: Long)

    @Query("DELETE FROM smart_replies WHERE id = :id")
    suspend fun deleteReply(id: Long)

    @Query("DELETE FROM smart_replies WHERE isCustom = 0")
    suspend fun clearDefaultReplies()
}

