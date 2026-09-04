package com.global.sms.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ai_memories",
    indices = [
        Index(value = ["address"]),
        Index(value = ["category"])
    ]
)
data class AiMemoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val address: String,
    val category: String, // "RELATIONSHIP", "PREFERENCE", "DECISION", "BUSINESS_ROLE"
    val memoryKey: String,
    val memoryValue: String,
    val confidence: Float = 0.9f,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "conversation_insights",
    indices = [
        Index(value = ["threadId"]),
        Index(value = ["urgencyLevel"])
    ]
)
data class ConversationInsightEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val threadId: Long,
    val topicSummary: String,
    val userIntention: String,
    val emotion: String,
    val urgencyLevel: String,
    val decisionsCount: Int = 0,
    val actionsCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "semantic_indices",
    indices = [
        Index(value = ["messageId"]),
        Index(value = ["keyword"])
    ]
)
data class SemanticIndexEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val messageId: Long,
    val keyword: String,
    val weight: Float = 1.0f,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "emotion_analyses",
    indices = [
        Index(value = ["messageId"]),
        Index(value = ["primaryEmotion"])
    ]
)
data class EmotionAnalysisEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val messageId: Long,
    val primaryEmotion: String,
    val intensityScore: Int,
    val priorityBoost: Int,
    val createdAt: Long = System.currentTimeMillis()
)
