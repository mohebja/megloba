package com.global.sms.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ai_agent_memories_v2")
data class AIAgentMemoryEntity(
    @PrimaryKey val id: String,
    val agentRole: String,
    val memoryKey: String,
    val memoryValue: String,
    val sensitivityLevel: Int = 1,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "plugins")
data class PluginEntity(
    @PrimaryKey val id: String,
    val pluginId: String,
    val name: String,
    val category: String,
    val isInstalled: Boolean = false,
    val permissionsJson: String,
    val installedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "companion_devices")
data class DeviceEntity(
    @PrimaryKey val id: String,
    val deviceId: String,
    val deviceName: String,
    val platform: String,
    val lastActiveMs: Long = System.currentTimeMillis(),
    val isTrusted: Boolean = true
)

@Entity(tableName = "sync_sessions")
data class SyncSessionEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val deviceId: String,
    val syncType: String,
    val status: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "automation_templates")
data class AutomationTemplateEntity(
    @PrimaryKey val id: String,
    val templateId: String,
    val title: String,
    val category: String,
    val triggerPattern: String,
    val actionSummary: String,
    val isActivated: Boolean = false,
    val executionCount: Int = 0
)
