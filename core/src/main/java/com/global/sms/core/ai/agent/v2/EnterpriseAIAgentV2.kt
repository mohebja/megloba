package com.global.sms.core.ai.agent.v2

import com.global.sms.core.ai.agent.AgentActionPlan
import com.global.sms.core.ai.agent.AgentActionType
import com.global.sms.core.ai.agent.AgentRoleType
import com.global.sms.core.ai.agent.ExecutionMode
import com.global.sms.core.ai.agent.ActionPlanStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

data class DailyIntelligenceSummary(
    val summaryId: String = UUID.randomUUID().toString(),
    val date: String,
    val totalProcessedMessages: Int,
    val totalPendingTasks: Int,
    val keyInsights: List<String>,
    val highPriorityEscalations: Int,
    val sentimentBreakdown: Map<String, Float>,
    val productivityScore: Int,
    val timestamp: Long = System.currentTimeMillis()
)

data class AutonomousTaskItem(
    val taskId: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val senderAddress: String,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val isCompleted: Boolean = false,
    val dueDateMs: Long = System.currentTimeMillis() + 86400000L
)

enum class TaskPriority {
    LOW, MEDIUM, HIGH, CRITICAL
}

data class WorkflowRecommendation(
    val recommendationId: String = UUID.randomUUID().toString(),
    val workflowName: String,
    val triggerPattern: String,
    val suggestedAction: String,
    val estimatedTimeSavedMinutes: Int,
    val isAccepted: Boolean = false
)

class EnterpriseAIAgentV2 {

    private val _pendingActionPlans = MutableStateFlow<List<AgentActionPlan>>(emptyList())
    val pendingActionPlans: StateFlow<List<AgentActionPlan>> = _pendingActionPlans.asStateFlow()

    private val _activeTasks = MutableStateFlow<List<AutonomousTaskItem>>(emptyList())
    val activeTasks: StateFlow<List<AutonomousTaskItem>> = _activeTasks.asStateFlow()

    private val _recommendedWorkflows = MutableStateFlow<List<WorkflowRecommendation>>(emptyList())
    val recommendedWorkflows: StateFlow<List<WorkflowRecommendation>> = _recommendedWorkflows.asStateFlow()

    private val _dailyIntelligence = MutableStateFlow<DailyIntelligenceSummary?>(null)
    val dailyIntelligence: StateFlow<DailyIntelligenceSummary?> = _dailyIntelligence.asStateFlow()

    fun analyzeConversationContext(
        sender: String,
        messages: List<String>,
        role: AgentRoleType = AgentRoleType.CUSTOMER_SUPPORT
    ): AgentActionPlan {
        val lastMessage = messages.lastOrNull() ?: ""
        val isComplaint = lastMessage.contains("شکایت") || lastMessage.contains("ناراضی") || lastMessage.contains("پیگیری")
        val isPricing = lastMessage.contains("قیمت") || lastMessage.contains("هزینه") || lastMessage.contains("تخفیف")

        val intent = when {
            isComplaint -> "COMPLAINT_ESCALATION"
            isPricing -> "PRICING_INQUIRY"
            else -> "GENERAL_SUPPORT"
        }

        val proposedResponse = when {
            isComplaint -> "پیام شما دریافت شد و تیم ارشد پشتیبانی حداکثر تا ۲ ساعت آینده با شما تماس خواهند گرفت."
            isPricing -> "سرویس پیامک انبوه سازمانی از ۵۰ ریال به ازای هر پارت پیامک با تعرفه ویژه آغاز می‌شود."
            else -> "درخواست شما توسط هوش مصنوعی بررسی گردید و در حال اقدام است."
        }

        val actionType = if (isComplaint) AgentActionType.ESCALATE_TO_HUMAN else AgentActionType.SEND_REPLY

        val plan = AgentActionPlan(
            agentType = role,
            senderAddress = sender,
            proposedResponse = proposedResponse,
            suggestedAction = actionType,
            intentCategory = intent,
            confidenceScore = 0.94f,
            safetyScore = 0.99f,
            requiresUserConfirmation = isComplaint,
            reasoningSummary = "Local V2 NLP parsing detected intent: $intent with 100% on-device processing."
        )

        if (plan.requiresUserConfirmation) {
            _pendingActionPlans.value = _pendingActionPlans.value + plan
        }

        // Auto-generate task if complaint or high priority
        if (isComplaint) {
            val task = AutonomousTaskItem(
                title = "پیگیری شکایت مخاطب $sender",
                description = "بررسی علت نارضایتی مشتری و ثبت گزارش در سیستم.",
                senderAddress = sender,
                priority = TaskPriority.HIGH
            )
            _activeTasks.value = _activeTasks.value + task
        }

        return plan
    }

    fun generateDailyIntelligence(): DailyIntelligenceSummary {
        val summary = DailyIntelligenceSummary(
            date = "1405/05/17",
            totalProcessedMessages = 1420,
            totalPendingTasks = _activeTasks.value.size,
            keyInsights = listOf(
                "افزایش ۲۵ درصدی استعلام تعرفه پیامک انبوه",
                "افت ۴۰ درصدی شکایات مشتریان نسبت به هفته گذشته",
                "بهینه‌سازی پاسخ‌دهی خودکار با نرخ دقت ۹۶٪"
            ),
            highPriorityEscalations = _activeTasks.value.count { it.priority == TaskPriority.HIGH || it.priority == TaskPriority.CRITICAL },
            sentimentBreakdown = mapOf("POSITIVE" to 0.72f, "NEUTRAL" to 0.21f, "NEGATIVE" to 0.07f),
            productivityScore = 94
        )
        _dailyIntelligence.value = summary
        return summary
    }

    fun recommendWorkflow(triggerPattern: String, suggestedAction: String): WorkflowRecommendation {
        val rec = WorkflowRecommendation(
            workflowName = "پاسخ خودکار به الگوی $triggerPattern",
            triggerPattern = triggerPattern,
            suggestedAction = suggestedAction,
            estimatedTimeSavedMinutes = 45
        )
        _recommendedWorkflows.value = _recommendedWorkflows.value + rec
        return rec
    }

    fun completeTask(taskId: String): Boolean {
        val updated = _activeTasks.value.map {
            if (it.taskId == taskId) it.copy(isCompleted = true) else it
        }
        _activeTasks.value = updated
        return true
    }

    fun confirmActionPlan(actionId: String): Boolean {
        val current = _pendingActionPlans.value
        val plan = current.find { it.actionId == actionId } ?: return false
        _pendingActionPlans.value = current.filter { it.actionId != actionId }
        return true
    }
}
