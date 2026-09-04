package com.global.sms.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "ai_settings")
data class AiSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val aiClassificationEnabled: Boolean = true,
    val smartReplyEnabled: Boolean = true,
    val fraudDetectionEnabled: Boolean = true,
    val summariesEnabled: Boolean = true,
    val voiceAssistantEnabled: Boolean = true,
    val localProcessingOnly: Boolean = true,
    val autoDeleteOtpDays: Int = 0, // 0 = Disabled, 1 = 24 hours, 7 = 7 days
    val urlProtectionEnabled: Boolean = true
)

@Entity(
    tableName = "ai_metadata",
    indices = [
        Index("classificationResult"),
        Index("fraudRiskScore")
    ]
)
data class AiMetadataEntity(
    @PrimaryKey val messageId: Long,
    val classificationResult: String,
    val confidenceScore: Float = 0.95f,
    val classificationDate: Long = System.currentTimeMillis(),
    val fraudRiskScore: Float = 0.0f,
    val fraudReasons: String? = null,
    val detectedOtpCode: String? = null,
    val summarySnippet: String? = null
)

@Entity(
    tableName = "financial_transactions",
    indices = [
        Index("messageId"),
        Index("bankName"),
        Index("transactionType"),
        Index("timestamp")
    ]
)
data class FinancialTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val messageId: Long,
    val bankName: String,
    val transactionType: String, // "EXPENSE", "INCOME", "TRANSFER"
    val amount: Double,
    val cardOrAccount: String? = null,
    val balanceAfter: Double? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val category: String = "GENERAL"
)

@Entity(
    tableName = "ai_feedback",
    indices = [
        Index("senderPattern"),
        Index("timestamp")
    ]
)
data class AiFeedbackEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val originalCategory: String,
    val userSelectedCategory: String,
    val senderPattern: String,
    val keywordSnippet: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "ai_message_analysis",
    indices = [
        Index("messageId"),
        Index("category")
    ]
)
data class AIMessageAnalysisEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val messageId: Long,
    val category: String,
    val confidence: Int,
    val riskScore: Int,
    val summary: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "otp_codes",
    indices = [
        Index("messageId"),
        Index("code"),
        Index("receivedTimestamp")
    ]
)
data class OtpEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val messageId: Long,
    val code: String,
    val serviceName: String,
    val address: String,
    val receivedTimestamp: Long = System.currentTimeMillis(),
    val expiresTimestamp: Long,
    val isUsed: Boolean = false,
    val securityLevel: String = "NORMAL"
)

@Entity(
    tableName = "smart_replies",
    indices = [
        Index("category"),
        Index("usageCount")
    ]
)
data class SmartReplyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val messageId: Long? = null,
    val replyText: String,
    val category: String = "GENERAL",
    val usageCount: Int = 0,
    val isCustom: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

