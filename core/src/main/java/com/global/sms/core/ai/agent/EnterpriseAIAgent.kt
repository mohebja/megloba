package com.global.sms.core.ai.agent

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

enum class AgentRoleType {
    CUSTOMER_SUPPORT,
    EMPLOYEE_ASSIST,
    CAMPAIGN_RESPONDER,
    COMPLAINT_HANDLER,
    SERVICE_REQUEST
}

enum class AgentActionType {
    SEND_REPLY,
    ESCALATE_TO_HUMAN,
    SCHEDULE_TASK,
    UPDATE_CRM,
    TRIGGER_WORKFLOW
}

enum class ExecutionMode {
    AUTONOMOUS,
    HUMAN_CONFIRMED
}

data class AgentActionPlan(
    val actionId: String = UUID.randomUUID().toString(),
    val agentType: AgentRoleType,
    val senderAddress: String,
    val proposedResponse: String,
    val suggestedAction: AgentActionType,
    val intentCategory: String,
    val confidenceScore: Float,
    val safetyScore: Float,
    val requiresUserConfirmation: Boolean,
    val reasoningSummary: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: ActionPlanStatus = ActionPlanStatus.PENDING
)

enum class ActionPlanStatus {
    PENDING,
    APPROVED,
    REJECTED,
    EXECUTED
}

class EnterpriseAIAgent {

    private val _pendingActionPlans = MutableStateFlow<List<AgentActionPlan>>(emptyList())
    val pendingActionPlans: StateFlow<List<AgentActionPlan>> = _pendingActionPlans.asStateFlow()

    private val _executionHistory = MutableStateFlow<List<AgentActionPlan>>(emptyList())
    val executionHistory: StateFlow<List<AgentActionPlan>> = _executionHistory.asStateFlow()

    fun processMessage(
        sender: String,
        content: String,
        role: AgentRoleType,
        mode: ExecutionMode = ExecutionMode.HUMAN_CONFIRMED
    ): AgentActionPlan {
        val lowerContent = content.lowercase()

        val (intent, actionType, responseText, reasoning) = when {
            lowerContent.contains("complaint") || lowerContent.contains("angry") || lowerContent.contains("shikayat") -> {
                Quadruple(
                    "COMPLAINT_ESCALATION",
                    AgentActionType.ESCALATE_TO_HUMAN,
                    "Dear Customer, we apologize for any inconvenience. Your request has been escalated to our senior support manager.",
                    "High negative sentiment detected. Escalating to human representative."
                )
            }
            lowerContent.contains("price") || lowerContent.contains("cost") || lowerContent.contains("ghaymat") -> {
                Quadruple(
                    "PRICING_INQUIRY",
                    AgentActionType.SEND_REPLY,
                    "Hello! Our Enterprise plan starts at $49/mo with unlimited local SMS and AI automation. Would you like a brochure?",
                    "Standard pricing inquiry matched with business catalog."
                )
            }
            lowerContent.contains("order") || lowerContent.contains("status") || lowerContent.contains("rahgiri") -> {
                Quadruple(
                    "SERVICE_REQUEST",
                    AgentActionType.UPDATE_CRM,
                    "Your service request #8821 is currently active and processing.",
                    "Service query matched with CRM database record."
                )
            }
            lowerContent.contains("task") || lowerContent.contains("schedule") -> {
                Quadruple(
                    "INTERNAL_TASK",
                    AgentActionType.SCHEDULE_TASK,
                    "Internal Task created for team follow-up.",
                    "Employee request for task scheduling."
                )
            }
            else -> {
                Quadruple(
                    "GENERAL_INQUIRY",
                    AgentActionType.SEND_REPLY,
                    "Thank you for contacting Global SMS. How can our AI assistant assist you today?",
                    "General inquiry handled with polite automated assistance."
                )
            }
        }

        val requiresConfirmation = mode == ExecutionMode.HUMAN_CONFIRMED ||
                actionType == AgentActionType.ESCALATE_TO_HUMAN ||
                actionType == AgentActionType.TRIGGER_WORKFLOW ||
                role == AgentRoleType.COMPLAINT_HANDLER

        val plan = AgentActionPlan(
            agentType = role,
            senderAddress = sender,
            proposedResponse = responseText,
            suggestedAction = actionType,
            intentCategory = intent,
            confidenceScore = 0.92f,
            safetyScore = 0.98f,
            requiresUserConfirmation = requiresConfirmation,
            reasoningSummary = reasoning,
            status = if (requiresConfirmation) ActionPlanStatus.PENDING else ActionPlanStatus.EXECUTED
        )

        if (requiresConfirmation) {
            _pendingActionPlans.value = _pendingActionPlans.value + plan
        } else {
            _executionHistory.value = _executionHistory.value + plan
        }

        return plan
    }

    fun confirmActionPlan(actionId: String): Boolean {
        val currentPending = _pendingActionPlans.value
        val plan = currentPending.find { it.actionId == actionId } ?: return false

        val approvedPlan = plan.copy(status = ActionPlanStatus.APPROVED)
        _pendingActionPlans.value = currentPending.filterNot { it.actionId == actionId }
        _executionHistory.value = _executionHistory.value + approvedPlan.copy(status = ActionPlanStatus.EXECUTED)
        return true
    }

    fun rejectActionPlan(actionId: String, reason: String): Boolean {
        val currentPending = _pendingActionPlans.value
        val plan = currentPending.find { it.actionId == actionId } ?: return false

        val rejectedPlan = plan.copy(
            status = ActionPlanStatus.REJECTED,
            reasoningSummary = "Rejected by user: $reason"
        )
        _pendingActionPlans.value = currentPending.filterNot { it.actionId == actionId }
        _executionHistory.value = _executionHistory.value + rejectedPlan
        return true
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
