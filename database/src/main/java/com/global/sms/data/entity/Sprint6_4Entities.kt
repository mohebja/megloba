package com.global.sms.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ai_agent_actions",
    indices = [
        Index(value = ["status"]),
        Index(value = ["targetId"])
    ]
)
data class AiAgentActionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val actionType: String, // e.g., "CREATE_REMINDER", "REPLY_TEMPLATE", "CATEGORIZE", "TRACK_PACKAGE"
    val targetId: String = "",
    val description: String,
    val status: String = "SUGGESTED", // SUGGESTED, PENDING_APPROVAL, APPROVED, EXECUTED, REJECTED, BLOCKED
    val urgency: Int = 50,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "workflow_rules",
    indices = [
        Index(value = ["triggerType"]),
        Index(value = ["isEnabled"])
    ]
)
data class WorkflowRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ruleName: String,
    val triggerType: String, // NEW_SMS, SENDER, CATEGORY, TIME, KEYWORD, AI_INTENT
    val triggerValue: String,
    val actionType: String, // CREATE_REMINDER, CATEGORIZE, SUGGEST_REPLY, ARCHIVE, MARK_IMPORTANT, NOTIFY_USER
    val actionValue: String = "",
    val requiresApproval: Boolean = true,
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "communication_profiles",
    indices = [
        Index(value = ["contactAddress"], unique = true)
    ]
)
data class CommunicationProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val contactAddress: String,
    val communicationStyle: String = "FORMAL", // FORMAL, CASUAL, BRIEF, URGENT
    val priorityScore: Int = 50,
    val averageResponseTimeMinutes: Int = 30,
    val preferredChannel: String = "SMS",
    val workHoursOnly: Boolean = false,
    val lastAnalyzed: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "agent_approvals",
    indices = [
        Index(value = ["status"]),
        Index(value = ["actionId"])
    ]
)
data class AgentApprovalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val actionId: Long,
    val actionSummary: String,
    val requestedAt: Long = System.currentTimeMillis(),
    val decidedAt: Long? = null,
    val status: String = "PENDING", // PENDING, APPROVED, REJECTED
    val isKillSwitchActive: Boolean = false
)
