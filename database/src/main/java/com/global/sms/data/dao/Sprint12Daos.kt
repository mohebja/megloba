package com.global.sms.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.global.sms.data.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AIAgentMemoryV2Dao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: AIAgentMemoryEntity)

    @Query("SELECT * FROM ai_agent_memories_v2 WHERE agentRole = :role")
    fun getMemoriesByRole(role: String): Flow<List<AIAgentMemoryEntity>>
}

@Dao
interface PluginDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlugin(plugin: PluginEntity)

    @Query("SELECT * FROM plugins")
    fun getAllPlugins(): Flow<List<PluginEntity>>
}

@Dao
interface DeviceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevice(device: DeviceEntity)

    @Query("SELECT * FROM companion_devices")
    fun getAllDevices(): Flow<List<DeviceEntity>>
}

@Dao
interface SyncSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncSession(session: SyncSessionEntity)

    @Query("SELECT * FROM sync_sessions ORDER BY timestamp DESC")
    fun getAllSyncSessions(): Flow<List<SyncSessionEntity>>
}

@Dao
interface AutomationTemplateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: AutomationTemplateEntity)

    @Query("SELECT * FROM automation_templates")
    fun getAllTemplates(): Flow<List<AutomationTemplateEntity>>

    @Query("UPDATE automation_templates SET isActivated = :isActivated WHERE templateId = :templateId")
    suspend fun updateTemplateActivation(templateId: String, isActivated: Boolean)

    @Query("DELETE FROM automation_templates WHERE templateId = :templateId")
    suspend fun deleteTemplate(templateId: String)
}
