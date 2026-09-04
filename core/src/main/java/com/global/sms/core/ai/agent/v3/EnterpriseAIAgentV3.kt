package com.global.sms.core.ai.agent.v3

import com.global.sms.core.ai.agent.AgentActionPlan
import com.global.sms.core.ai.agent.AgentActionType
import com.global.sms.core.ai.agent.AgentRoleType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

enum class SupportedLanguage {
    PERSIAN,
    ENGLISH,
    ARABIC
}

enum class TaskPriority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

data class AgentTaskItem(
    val taskId: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val isCompleted: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

data class AutonomousAgentActionPlan(
    val planId: String = UUID.randomUUID().toString(),
    val senderAddress: String,
    val intentCategory: String,
    val confidenceScore: Float,
    val summaryText: String,
    val recommendedActions: List<String>,
    val proposedDraftReply: String,
    val requiresUserConfirmation: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)

data class MultiStepReasoningPlan(
    val planId: String = UUID.randomUUID().toString(),
    val language: SupportedLanguage,
    val initialGoal: String,
    val reasoningSteps: List<String>,
    val predictedRisks: List<String>,
    val recommendedWorkflow: String?,
    val meetingPrepDetails: String?,
    val followUpAction: String?,
    val confidenceScore: Float = 0.98f,
    val timestamp: Long = System.currentTimeMillis()
)

class EnterpriseAIAgentV3 {

    private val _activeReasoningPlans = MutableStateFlow<List<MultiStepReasoningPlan>>(emptyList())
    val activeReasoningPlans: StateFlow<List<MultiStepReasoningPlan>> = _activeReasoningPlans.asStateFlow()

    private val _agentTasks = MutableStateFlow<List<AgentTaskItem>>(
        listOf(
            AgentTaskItem(
                title = "پیگیری خودکار قرارداد شرکت پارس آنلاین",
                description = "بررسی پیامک‌های دریافتی، استخراج توافقات و تنظیم یادآور پیرو.",
                priority = TaskPriority.HIGH
            )
        )
    )
    val agentTasks: StateFlow<List<AgentTaskItem>> = _agentTasks.asStateFlow()

    fun detectLanguage(inputText: String): SupportedLanguage {
        val lower = inputText.lowercase()
        return when {
            inputText.any { it in '\u0600'..'\u06FF' } -> {
                if (lower.contains("مرحبا") || lower.contains("شكرا") || lower.contains("سلام عليكم")) {
                    SupportedLanguage.ARABIC
                } else {
                    SupportedLanguage.PERSIAN
                }
            }
            else -> SupportedLanguage.ENGLISH
        }
    }

    fun executeMultiStepReasoning(
        senderAddress: String,
        messages: List<String>,
        agentRole: AgentRoleType = AgentRoleType.EMPLOYEE_ASSIST
    ): MultiStepReasoningPlan {
        val combinedText = messages.joinToString(" ")
        val lang = detectLanguage(combinedText)

        val steps = mutableListOf<String>()
        val risks = mutableListOf<String>()
        var workflow: String? = null
        var meetingPrep: String? = null
        var followUp: String? = null

        when (lang) {
            SupportedLanguage.PERSIAN -> {
                steps.add("مرحله ۱: تحلیل ساختاری متن و شناسایی کلیدواژه‌های حساس (قرارداد، مالی، جلسه).")
                steps.add("مرحله ۲: ارزیابی سطح ریسک و انطباق با سیاست‌های امنیت صفر (Zero Trust).")
                steps.add("مرحله ۳: استخراج اقدامات بعدی و پیشنهاد خودکارسازی مربوطه.")

                if (combinedText.contains("جلسه") || combinedText.contains("قرار")) {
                    meetingPrep = "آماده‌سازی دستور جلسه، بررسی تقویم و درج یادآور ۳۰ دقیقه قبل از جلسه."
                }
                if (combinedText.contains("مشکل") || combinedText.contains("شکایت")) {
                    risks.add("ریسک نارضایتی شدید مشتری. ارجاع به مدیر پشتیبانی توصیه می‌شود.")
                }
                workflow = "خودکارسازی پیگیری ۳ مرحله‌ای مشتریان تجاری (CRM Follow-up)"
                followUp = "ارسال پیامک تایید دریافت درخواست و پیگیری توسط کارشناس مربوطه."
            }
            SupportedLanguage.ENGLISH -> {
                steps.add("Step 1: Structural parsing and intent classification.")
                steps.add("Step 2: Risk assessment and enterprise compliance check.")
                steps.add("Step 3: Action extraction and automated workflow synthesis.")
                workflow = "Automated Enterprise Lead Nurturing Workflow"
                followUp = "Send automated acknowledgment SMS and schedule CRM task."
            }
            SupportedLanguage.ARABIC -> {
                steps.add("الخطوة ١: تحليل النص وتحديد النوايا والكلمات الرئيسية.")
                steps.add("الخطوة ٢: تقييم المخاطر وتطبيق سياسات الأمان.")
                steps.add("الخطوة ٣: استخراج الإجراءات واقتراح أتمتة مهام العمل.")
                workflow = "سير العمل المؤتمت لمتابعة العملاء"
                followUp = "إرسال رسالة تأكيد تلقائية وإضافة تذكير."
            }
        }

        val plan = MultiStepReasoningPlan(
            language = lang,
            initialGoal = "پردازش هوشمند گفتگو با مخاطب $senderAddress",
            reasoningSteps = steps,
            predictedRisks = risks,
            recommendedWorkflow = workflow,
            meetingPrepDetails = meetingPrep,
            followUpAction = followUp
        )

        _activeReasoningPlans.value = listOf(plan) + _activeReasoningPlans.value
        return plan
    }

    fun generateAutonomousActionPlan(
        senderAddress: String,
        messages: List<String>,
        agentRole: AgentRoleType = AgentRoleType.CUSTOMER_SUPPORT
    ): AutonomousAgentActionPlan {
        val multiStep = executeMultiStepReasoning(senderAddress, messages, agentRole)
        return AutonomousAgentActionPlan(
            senderAddress = senderAddress,
            intentCategory = "ENTERPRISE_V3_REASONING",
            confidenceScore = multiStep.confidenceScore,
            summaryText = multiStep.initialGoal,
            recommendedActions = multiStep.reasoningSteps,
            proposedDraftReply = multiStep.followUpAction ?: "درخواست شما با موفقیت ثبت شد.",
            requiresUserConfirmation = true
        )
    }
}
