package com.global.sms.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.global.sms.data.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AiAgentDao {
    @Query("SELECT * FROM ai_agents ORDER BY createdAt DESC")
    fun getAllAgents(): Flow<List<AiAgentEntity>>

    @Query("SELECT * FROM ai_agents WHERE id = :id")
    suspend fun getAgentById(id: String): AiAgentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAgent(agent: AiAgentEntity)

    @Query("DELETE FROM ai_agents WHERE id = :id")
    suspend fun deleteAgent(id: String)
}

@Dao
interface WorkflowDao {
    @Query("SELECT * FROM workflows ORDER BY createdAt DESC")
    fun getAllWorkflows(): Flow<List<WorkflowEntity>>

    @Query("SELECT * FROM workflows WHERE isEnabled = 1")
    suspend fun getEnabledWorkflows(): List<WorkflowEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkflow(workflow: WorkflowEntity)

    @Query("DELETE FROM workflows WHERE id = :id")
    suspend fun deleteWorkflow(id: String)
}

@Dao
interface WorkflowExecutionDao {
    @Query("SELECT * FROM workflow_executions ORDER BY executedAt DESC")
    fun getAllExecutions(): Flow<List<WorkflowExecutionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExecution(execution: WorkflowExecutionEntity)
}

@Dao
interface EnterpriseReportDao {
    @Query("SELECT * FROM enterprise_reports ORDER BY generatedAt DESC")
    fun getAllReports(): Flow<List<EnterpriseReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: EnterpriseReportEntity)

    @Query("DELETE FROM enterprise_reports WHERE id = :id")
    suspend fun deleteReport(id: String)
}

@Dao
interface ApiAccessLogDao {
    @Query("SELECT * FROM api_access_logs ORDER BY timestamp DESC LIMIT 100")
    fun getRecentApiLogs(): Flow<List<ApiAccessLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ApiAccessLogEntity)
}

@Dao
interface SecurityAuditDao {
    @Query("SELECT * FROM security_audits ORDER BY timestamp DESC")
    fun getAllSecurityAudits(): Flow<List<SecurityAuditEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAudit(audit: SecurityAuditEntity)
}
