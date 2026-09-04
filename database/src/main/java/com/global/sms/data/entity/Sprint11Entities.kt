package com.global.sms.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ai_agents")
data class AiAgentEntity(
    @PrimaryKey val id: String,
    val agentName: String,
    val roleType: String,
    val departmentId: String,
    val executionMode: String = "HUMAN_CONFIRMED",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "workflows")
data class WorkflowEntity(
    @PrimaryKey val id: String,
    val name: String,
    val triggerType: String,
    val conditionJson: String,
    val actionJson: String,
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "workflow_executions")
data class WorkflowExecutionEntity(
    @PrimaryKey val id: String,
    val workflowId: String,
    val triggerEvent: String,
    val status: String,
    val logDetails: String,
    val executedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "enterprise_reports")
data class EnterpriseReportEntity(
    @PrimaryKey val id: String,
    val reportTitle: String,
    val reportType: String,
    val format: String,
    val contentPayload: String,
    val generatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "api_access_logs")
data class ApiAccessLogEntity(
    @PrimaryKey val id: String,
    val apiKeyId: String,
    val endpoint: String,
    val httpMethod: String,
    val responseStatus: Int,
    val clientIp: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "security_audits")
data class SecurityAuditEntity(
    @PrimaryKey val id: String,
    val eventType: String,
    val severity: String,
    val sourceModule: String,
    val description: String,
    val signatureHash: String,
    val timestamp: Long = System.currentTimeMillis()
)
