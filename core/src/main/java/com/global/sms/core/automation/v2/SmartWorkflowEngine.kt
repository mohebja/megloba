package com.global.sms.core.automation.v2

import com.global.sms.data.entity.MessageEntity
import com.global.sms.data.entity.WorkflowRuleEntity
import com.global.sms.data.entity.AiAgentActionEntity

object SmartWorkflowEngine {

    fun evaluateWorkflows(
        message: MessageEntity,
        activeRules: List<WorkflowRuleEntity>
    ): List<AiAgentActionEntity> {
        val suggestedActions = mutableListOf<AiAgentActionEntity>()

        for (rule in activeRules) {
            if (!rule.isEnabled) continue

            val isMatch = when (rule.triggerType) {
                "NEW_SMS" -> true
                "SENDER" -> message.address.contains(rule.triggerValue, ignoreCase = true)
                "CATEGORY" -> message.category?.name?.equals(rule.triggerValue, ignoreCase = true) == true
                "KEYWORD" -> message.body.contains(rule.triggerValue, ignoreCase = true)
                "AI_INTENT" -> {
                    val body = message.body
                    if (rule.triggerValue == "FINANCIAL") body.contains("واریز") || body.contains("برداشت")
                    else if (rule.triggerValue == "OTP") body.contains("کد") || body.contains("ورود")
                    else body.contains(rule.triggerValue, ignoreCase = true)
                }
                else -> false
            }

            if (isMatch) {
                val action = AiAgentActionEntity(
                    actionType = rule.actionType,
                    targetId = message.id.toString(),
                    description = "گردش‌کار [${rule.ruleName}]: ${rule.actionType} (${rule.actionValue})",
                    status = if (rule.requiresApproval) "PENDING_APPROVAL" else "SUGGESTED",
                    urgency = 70
                )
                suggestedActions.add(action)
            }
        }

        return suggestedActions
    }

    fun getDefaultSystemRules(): List<WorkflowRuleEntity> {
        return listOf(
            WorkflowRuleEntity(
                id = 1,
                ruleName = "مدیریت پیام‌های بانکی",
                triggerType = "AI_INTENT",
                triggerValue = "FINANCIAL",
                actionType = "CATEGORIZE",
                actionValue = "FINANCE",
                requiresApproval = true,
                isEnabled = true
            ),
            WorkflowRuleEntity(
                id = 2,
                ruleName = "تشخیص خودکار کد پویا",
                triggerType = "KEYWORD",
                triggerValue = "کد",
                actionType = "MARK_IMPORTANT",
                actionValue = "OTP",
                requiresApproval = true,
                isEnabled = true
            ),
            WorkflowRuleEntity(
                id = 3,
                ruleName = "یادآوری سررسید فاکتور",
                triggerType = "KEYWORD",
                triggerValue = "سررسید",
                actionType = "CREATE_REMINDER",
                actionValue = "DUE_DATE",
                requiresApproval = true,
                isEnabled = true
            )
        )
    }
}
