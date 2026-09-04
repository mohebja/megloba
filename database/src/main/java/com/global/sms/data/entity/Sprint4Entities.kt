package com.global.sms.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ai_conversation_insights",
    indices = [
        Index("conversationId"),
        Index("category"),
        Index("urgency")
    ]
)
data class AiConversationInsightEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val primaryIntent: String,
    val category: String,
    val urgency: String,
    val sentiment: String,
    val satisfaction: String,
    val isImportant: Boolean,
    val recommendedAction: String,
    val keyTermsJson: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "themes",
    indices = [
        Index("style")
    ]
)
data class ThemeEntity(
    @PrimaryKey val id: String,
    val name: String,
    val style: String,
    val primaryColorHex: String,
    val secondaryColorHex: String,
    val backgroundColorHex: String,
    val surfaceColorHex: String,
    val isDark: Boolean,
    val isAmoled: Boolean = false,
    val isSelected: Boolean = false
)

@Entity(
    tableName = "notification_rules",
    indices = [
        Index("category"),
        Index("priority")
    ]
)
data class NotificationRuleEntity(
    @PrimaryKey val id: String,
    val category: String,
    val priority: String,
    val channelId: String,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val lockScreenPrivate: Boolean = false
)

@Entity(
    tableName = "voice_commands",
    indices = [
        Index("action")
    ]
)
data class VoiceCommandEntity(
    @PrimaryKey val id: String,
    val spokenPhrase: String,
    val action: String,
    val targetRecipient: String? = null,
    val executedTimestamp: Long = System.currentTimeMillis()
)
