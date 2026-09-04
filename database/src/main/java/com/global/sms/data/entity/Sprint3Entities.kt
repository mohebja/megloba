package com.global.sms.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "contact_profiles",
    indices = [
        Index("primaryPhoneNumber"),
        Index("category"),
        Index("isVip")
    ]
)
data class ContactProfileEntity(
    @PrimaryKey val id: String,
    val fullName: String,
    val primaryPhoneNumber: String,
    val phoneNumbersJson: String, // Comma or JSON separated
    val email: String? = null,
    val company: String? = null,
    val jobTitle: String? = null,
    val avatarUri: String? = null,
    val category: String = "مشتریان",
    val tagsJson: String = "",
    val notesJson: String = "",
    val isVip: Boolean = false,
    val isFavorite: Boolean = false,
    val createdTimestamp: Long = System.currentTimeMillis(),
    val lastContactTimestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "campaigns",
    indices = [
        Index("status"),
        Index("createdTimestamp")
    ]
)
data class CampaignEntity(
    @PrimaryKey val id: String,
    val name: String,
    val templateBody: String,
    val scheduledTime: Long = System.currentTimeMillis(),
    val simSlot: Int = 0,
    val status: String = "QUEUED", // QUEUED, RUNNING, COMPLETED, PAUSED
    val totalRecipients: Int = 0,
    val deliveredCount: Int = 0,
    val failedCount: Int = 0,
    val pendingCount: Int = 0,
    val createdTimestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "campaign_recipients",
    indices = [
        Index("campaignId"),
        Index("recipientPhone"),
        Index("deliveryStatus")
    ]
)
data class CampaignRecipientEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val campaignId: String,
    val recipientPhone: String,
    val recipientName: String = "",
    val deliveryStatus: String = "PENDING", // PENDING, SENT, DELIVERED, FAILED
    val sentTimestamp: Long = 0,
    val errorMessage: String? = null
)

@Entity(
    tableName = "ai_insights",
    indices = [
        Index("conversationId"),
        Index("urgency"),
        Index("detectedIntent")
    ]
)
data class AiInsightEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val summary: String,
    val urgency: String, // HIGH, MEDIUM, LOW
    val detectedIntent: String,
    val keyPointsJson: String = "",
    val actionRequired: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "backups",
    indices = [
        Index("timestamp")
    ]
)
data class BackupEntity(
    @PrimaryKey val id: String,
    val fileName: String,
    val filePath: String,
    val fileSizeByte: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val messageCount: Int = 0,
    val contactCount: Int = 0,
    val integrityHashSha256: String,
    val cloudTarget: String = "LOCAL_STORAGE"
)
