package com.global.sms.data.dao

import androidx.room.*
import com.global.sms.data.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AiConversationInsightDao {
    @Query("SELECT * FROM ai_conversation_insights WHERE conversationId = :conversationId LIMIT 1")
    suspend fun getInsightByConversationId(conversationId: String): AiConversationInsightEntity?

    @Query("SELECT * FROM ai_conversation_insights ORDER BY timestamp DESC")
    fun getAllInsights(): Flow<List<AiConversationInsightEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInsight(insight: AiConversationInsightEntity)
}

@Dao
interface ThemeDao {
    @Query("SELECT * FROM themes")
    fun getAllThemes(): Flow<List<ThemeEntity>>

    @Query("SELECT * FROM themes WHERE isSelected = 1 LIMIT 1")
    suspend fun getSelectedTheme(): ThemeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTheme(theme: ThemeEntity)
}

@Dao
interface NotificationRuleDao {
    @Query("SELECT * FROM notification_rules WHERE category = :category LIMIT 1")
    suspend fun getRuleForCategory(category: String): NotificationRuleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotificationRule(rule: NotificationRuleEntity)
}

@Dao
interface VoiceCommandDao {
    @Query("SELECT * FROM voice_commands ORDER BY executedTimestamp DESC")
    fun getRecentVoiceCommands(): Flow<List<VoiceCommandEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVoiceCommand(command: VoiceCommandEntity)
}
