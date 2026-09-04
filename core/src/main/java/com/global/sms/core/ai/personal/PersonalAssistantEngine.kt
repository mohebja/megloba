package com.global.sms.core.ai.personal

import com.global.sms.core.ai.copilot.AiCopilotEngine
import com.global.sms.core.ai.copilot.EntityExtractionEngine
import com.global.sms.data.entity.MessageEntity

data class PersonalActionDetected(
    val title: String,
    val description: String,
    val actionType: String, // "TASK", "FINANCIAL_REMINDER", "DEADLINE"
    val priority: MessagePriority,
    val deadlineString: String = ""
)

enum class MessagePriority {
    HIGH,
    MEDIUM,
    LOW
}

data class PriorityResult(
    val priority: MessagePriority,
    val reason: String
)

object PersonalAssistantEngine {

    fun prioritizeMessage(message: MessageEntity): PriorityResult {
        val body = message.body
        val address = message.address.lowercase()

        val isFinancial = body.contains("واریز") || body.contains("برداشت") || body.contains("کارت") || body.contains("حساب")
        val isOtp = body.contains("کد ورود") || body.contains("کد تایید") || body.contains("otp") || body.contains("code")
        val isUrgent = body.contains("فوری") || body.contains("حتماً") || body.contains("مهلت") || body.contains("تا امروز") || body.contains("اقدام کنید")

        return when {
            isOtp -> PriorityResult(MessagePriority.HIGH, "کد امنیتی / OTP")
            isFinancial -> PriorityResult(MessagePriority.HIGH, "تراکنش بانکی")
            isUrgent -> PriorityResult(MessagePriority.HIGH, "پیام فوری با مهلت اقدام")
            address.contains("bank") || address.contains("bazaar") -> PriorityResult(MessagePriority.MEDIUM, "اطلاعیه سرویس")
            body.length > 20 -> PriorityResult(MessagePriority.MEDIUM, "پیام شخصی/کاری")
            else -> PriorityResult(MessagePriority.LOW, "پیام معمولی")
        }
    }

    fun detectPendingActions(message: MessageEntity): List<PersonalActionDetected> {
        val actions = mutableListOf<PersonalActionDetected>()
        val body = message.body
        val normalizedBody = body.lowercase()

        // 1. Task Action Detection (e.g., "لطفاً فایل را ارسال کنید", "فردا تماس بگیرید")
        if (normalizedBody.contains("ارسال کنید") || normalizedBody.contains("ارسال کن") ||
            normalizedBody.contains("تماس بگیرید") || normalizedBody.contains("پیگیری کنید") ||
            normalizedBody.contains("بفرستید") || normalizedBody.contains("جلسه")
        ) {
            val dateStr = EntityExtractionEngine.extractEntities(body).dates.firstOrNull() ?: "به زودی"
            actions.add(
                PersonalActionDetected(
                    title = "اقدام معوقه: " + body.take(30) + "...",
                    description = "استخراج شده از پیام ${message.address}",
                    actionType = "TASK",
                    priority = MessagePriority.HIGH,
                    deadlineString = dateStr
                )
            )
        }

        // 2. Financial Reminder Detection (e.g., "قبض شما تا 15 مرداد پرداخت شود")
        if (normalizedBody.contains("قبض") || normalizedBody.contains("بدهی") ||
            normalizedBody.contains("سررسید") || normalizedBody.contains("پرداخت شود") ||
            normalizedBody.contains("مهلت پرداخت")
        ) {
            val dateStr = EntityExtractionEngine.extractEntities(body).dates.firstOrNull() ?: "تاریخ سررسید"
            val amounts = EntityExtractionEngine.extractEntities(body).amounts.firstOrNull() ?: ""
            actions.add(
                PersonalActionDetected(
                    title = "یادآوری مالی: $amounts ${titleFromText(body)}",
                    description = "یادآوری سررسید و پرداخت برای ${message.address}",
                    actionType = "FINANCIAL_REMINDER",
                    priority = MessagePriority.HIGH,
                    deadlineString = dateStr
                )
            )
        }

        return actions
    }

    private fun titleFromText(body: String): String {
        return if (body.length > 25) body.substring(0, 25) + "..." else body
    }
}
