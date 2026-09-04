package com.global.sms.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reliability_logs")
data class ReliabilityLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val eventType: String,
    val details: String,
    val healthScore: Int
)

@Entity(tableName = "device_profiles")
data class DeviceProfileEntity(
    @PrimaryKey val id: Int = 1,
    val manufacturer: String,
    val brand: String,
    val isAutoStartEnabled: Boolean,
    val isBatteryOptimized: Boolean
)

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val activeProfileName: String,
    val isAutoReplyActive: Boolean,
    val autoReplyMessage: String,
    val isMuteActive: Boolean
)

@Entity(tableName = "analytics_records")
data class AnalyticsEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val totalMessages: Int,
    val totalSpamBlocked: Int,
    val avgResponseTimeMinutes: Int
)

@Entity(tableName = "backup_history")
data class BackupHistoryEntity(
    @PrimaryKey val backupId: String,
    val timestamp: Long,
    val formattedDate: String,
    val messageCount: Int,
    val contactCount: Int,
    val sizeBytes: Long,
    val isEncrypted: Boolean = true
)
