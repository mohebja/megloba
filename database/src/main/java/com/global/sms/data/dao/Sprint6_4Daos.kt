package com.global.sms.data.dao

import androidx.room.*
import com.global.sms.data.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AiAgentActionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAction(action: AiAgentActionEntity): Long

    @Query("SELECT * FROM ai_agent_actions WHERE id = :id")
    suspend fun getActionById(id: Long): AiAgentActionEntity?

    @Query("SELECT * FROM ai_agent_actions WHERE status = :status ORDER BY timestamp DESC")
    fun getActionsByStatusFlow(status: String): Flow<List<AiAgentActionEntity>>

    @Query("SELECT * FROM ai_agent_actions ORDER BY timestamp DESC LIMIT :limit")
    fun getAllActionsFlow(limit: Int = 100): Flow<List<AiAgentActionEntity>>

    @Query("UPDATE ai_agent_actions SET status = :status WHERE id = :id")
    suspend fun updateActionStatus(id: Long, status: String)

    @Query("DELETE FROM ai_agent_actions WHERE id = :id")
    suspend fun deleteAction(id: Long)
}

@Dao
interface WorkflowRuleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: WorkflowRuleEntity): Long

    @Query("SELECT * FROM workflow_rules WHERE isEnabled = 1")
    suspend fun getActiveRules(): List<WorkflowRuleEntity>

    @Query("SELECT * FROM workflow_rules ORDER BY createdAt DESC")
    fun getAllRulesFlow(): Flow<List<WorkflowRuleEntity>>

    @Query("UPDATE workflow_rules SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun setRuleEnabled(id: Long, isEnabled: Boolean)

    @Query("DELETE FROM workflow_rules WHERE id = :id")
    suspend fun deleteRule(id: Long)
}

@Dao
interface CommunicationProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: CommunicationProfileEntity): Long

    @Query("SELECT * FROM communication_profiles WHERE contactAddress = :address LIMIT 1")
    suspend fun getProfileByAddress(address: String): CommunicationProfileEntity?

    @Query("SELECT * FROM communication_profiles ORDER BY priorityScore DESC")
    fun getAllProfilesFlow(): Flow<List<CommunicationProfileEntity>>
}

@Dao
interface AgentApprovalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApproval(approval: AgentApprovalEntity): Long

    @Query("SELECT * FROM agent_approvals WHERE status = 'PENDING' ORDER BY requestedAt DESC")
    fun getPendingApprovalsFlow(): Flow<List<AgentApprovalEntity>>

    @Query("SELECT * FROM agent_approvals ORDER BY requestedAt DESC LIMIT :limit")
    fun getAllApprovalsFlow(limit: Int = 100): Flow<List<AgentApprovalEntity>>

    @Query("UPDATE agent_approvals SET status = :status, decidedAt = :decidedAt WHERE id = :id")
    suspend fun updateApprovalDecision(id: Long, status: String, decidedAt: Long = System.currentTimeMillis())
}
