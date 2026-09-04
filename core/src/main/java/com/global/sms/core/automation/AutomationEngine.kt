package com.global.sms.core.automation

import com.global.sms.core.ai.intelligence.MessageCategory
import com.global.sms.core.ai.intelligence.SmartMessageClassifier

enum class AutomationTriggerType {
    SENDER_CONTAINS,
    CATEGORY_IS,
    BODY_CONTAINS,
    OTP_RECEIVED,
    SPAM_DETECTED,
    NEW_CUSTOMER_MESSAGE,
    PAYMENT_REMINDER,
    KEYWORD_DETECTED,
    COMPLAINT_DETECTED,
    VIP_MESSAGE
}

enum class AutomationActionType {
    AUTO_CATEGORIZE,
    SHOW_NOTIFY,
    ARCHIVE,
    COPY_OTP,
    BLOCK_SENDER,
    FORWARD_SMS,
    AUTO_REPLY,
    CREATE_FINANCE_RECORD,
    CREATE_TASK,
    NOTIFY_MANAGER,
    GENERATE_REPLY_SUGGESTION,
    CATEGORIZE_CUSTOMER
}

data class AutomationRule(
    val id: String,
    val name: String,
    val triggerType: AutomationTriggerType,
    val triggerValue: String,
    val actionType: AutomationActionType,
    val actionValue: String? = null,
    val isEnabled: Boolean = true
)

data class AutomationExecutionResult(
    val ruleId: String,
    val ruleName: String,
    val actionTaken: AutomationActionType,
    val messageHandled: Boolean,
    val extractedData: String? = null
)

class AutomationEngine {

    private val classifier = SmartMessageClassifier()
    private val rulesList = mutableListOf<AutomationRule>()

    init {
        // Built-in default rules
        rulesList.add(
            AutomationRule(
                id = "rule_bank_auto",
                name = "دسته‌بندی و بایگانی خودکار بانک",
                triggerType = AutomationTriggerType.CATEGORY_IS,
                triggerValue = MessageCategory.BANKING.name,
                actionType = AutomationActionType.AUTO_CATEGORIZE
            )
        )
        rulesList.add(
            AutomationRule(
                id = "rule_otp_copy",
                name = "استخراج سریع کد تایید OTP",
                triggerType = AutomationTriggerType.OTP_RECEIVED,
                triggerValue = "OTP",
                actionType = AutomationActionType.COPY_OTP
            )
        )
        rulesList.add(
            AutomationRule(
                id = "rule_spam_block",
                name = "مسدودسازی هوشمند پیامک‌های اسپم",
                triggerType = AutomationTriggerType.SPAM_DETECTED,
                triggerValue = "SPAM",
                actionType = AutomationActionType.BLOCK_SENDER
            )
        )
        rulesList.add(
            AutomationRule(
                id = "rule_vip_task",
                name = "ایجاد وظیفه فوری برای مشتریان VIP",
                triggerType = AutomationTriggerType.VIP_MESSAGE,
                triggerValue = "VIP",
                actionType = AutomationActionType.CREATE_TASK,
                actionValue = "پیگیری سریع مشتری VIP"
            )
        )
        rulesList.add(
            AutomationRule(
                id = "rule_complaint_notify",
                name = "اعلام شکایت به مدیر دپارتمان",
                triggerType = AutomationTriggerType.COMPLAINT_DETECTED,
                triggerValue = "شکایت",
                actionType = AutomationActionType.NOTIFY_MANAGER,
                actionValue = "بررسی شکایت مشتری"
            )
        )
    }

    fun addCustomRule(rule: AutomationRule) {
        rulesList.add(rule)
    }

    fun getActiveRules(): List<AutomationRule> = rulesList.filter { it.isEnabled }

    fun processIncomingMessage(
        sender: String,
        body: String
    ): List<AutomationExecutionResult> {
        val classification = classifier.classifyMessage(body)
        val results = mutableListOf<AutomationExecutionResult>()

        for (rule in getActiveRules()) {
            var matched = false

            when (rule.triggerType) {
                AutomationTriggerType.SENDER_CONTAINS -> {
                    if (sender.contains(rule.triggerValue, ignoreCase = true)) matched = true
                }
                AutomationTriggerType.CATEGORY_IS -> {
                    if (classification.category.name.equals(rule.triggerValue, ignoreCase = true)) matched = true
                }
                AutomationTriggerType.BODY_CONTAINS -> {
                    if (body.contains(rule.triggerValue, ignoreCase = true)) matched = true
                }
                AutomationTriggerType.OTP_RECEIVED -> {
                    if (classification.isOtp) matched = true
                }
                AutomationTriggerType.SPAM_DETECTED -> {
                    if (classification.isSpam) matched = true
                }
                AutomationTriggerType.NEW_CUSTOMER_MESSAGE -> {
                    if (body.contains("سلام") || body.contains("شروع")) matched = true
                }
                AutomationTriggerType.PAYMENT_REMINDER -> {
                    if (body.contains("فاکتور") || body.contains("بدهی") || body.contains("پرداخت")) matched = true
                }
                AutomationTriggerType.KEYWORD_DETECTED -> {
                    if (body.contains(rule.triggerValue, ignoreCase = true)) matched = true
                }
                AutomationTriggerType.COMPLAINT_DETECTED -> {
                    if (body.contains("شکایت") || body.contains("خراب") || body.contains("پشتیبانی")) matched = true
                }
                AutomationTriggerType.VIP_MESSAGE -> {
                    if (sender.contains("VIP") || body.contains("ویژه")) matched = true
                }
            }

            if (matched) {
                val extracted = if (rule.actionType == AutomationActionType.COPY_OTP) {
                    "\\b\\d{4,8}\\b".toRegex().find(body)?.value ?: body.take(6)
                } else null

                results.add(
                    AutomationExecutionResult(
                        ruleId = rule.id,
                        ruleName = rule.name,
                        actionTaken = rule.actionType,
                        messageHandled = true,
                        extractedData = extracted
                    )
                )
            }
        }

        return results
    }
}
