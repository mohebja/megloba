package com.global.sms.core.ai.agent

import com.global.sms.data.entity.MessageEntity
import com.global.sms.data.entity.AiAgentActionEntity

object ActionRecommendationEngine {

    fun reasonAboutMessage(text: String): AgentReasoning {
        val lower = text.lowercase()
        return when {
            text.contains("قسط") || text.contains("سررسید") || text.contains("بدهی") || lower.contains("due") -> {
                AgentReasoning(
                    detectedIntent = "FINANCIAL_DUE_DATE",
                    urgencyScore = 85,
                    reasoningText = "پیام حاوی سررسید مالی یا قسط وام است.",
                    needsApproval = true
                )
            }
            text.contains("قیمت") || text.contains("سفارش") || text.contains("خرید") || lower.contains("price") -> {
                AgentReasoning(
                    detectedIntent = "CUSTOMER_INQUIRY",
                    urgencyScore = 70,
                    reasoningText = "استعلام قیمت یا خدمات توسط مشتری.",
                    needsApproval = true
                )
            }
            text.contains("مرسوله") || text.contains("کد رهگیری") || text.contains("پست") || lower.contains("tracking") -> {
                AgentReasoning(
                    detectedIntent = "DELIVERY_TRACKING",
                    urgencyScore = 60,
                    reasoningText = "اطلاعیه تحویل یا رهگیری مرسوله پستی.",
                    needsApproval = true
                )
            }
            else -> {
                AgentReasoning(
                    detectedIntent = "GENERAL_MESSAGE",
                    urgencyScore = 30,
                    reasoningText = "پیام عمومی بدون نیاز به اقدام اضطراری.",
                    needsApproval = true
                )
            }
        }
    }

    fun generateRecommendations(message: MessageEntity): List<AiAgentActionEntity> {
        val text = message.body
        val lower = text.lowercase()
        val actions = mutableListOf<AiAgentActionEntity>()

        // 1. Bank / Installment SMS
        if (text.contains("قسط") || text.contains("سررسید") || text.contains("بدهی") || lower.contains("due")) {
            actions.add(
                AiAgentActionEntity(
                    actionType = "CREATE_REMINDER",
                    targetId = message.id.toString(),
                    description = "افزودن یادآوری سررسید قسط به تقویم",
                    status = "SUGGESTED",
                    urgency = 85
                )
            )
        }

        // 2. Customer Product / Price Inquiry
        if (text.contains("قیمت") || text.contains("محصول") || text.contains("سفارش") || lower.contains("price")) {
            actions.add(
                AiAgentActionEntity(
                    actionType = "REPLY_TEMPLATE",
                    targetId = message.id.toString(),
                    description = "ارسال قالب پاسخ سریع: لیست قیمت و کاتالوگ",
                    status = "SUGGESTED",
                    urgency = 75
                )
            )
        }

        // 3. Delivery / Postal Package Tracking
        if (text.contains("مرسوله") || text.contains("تحویل") || text.contains("رهگیری") || lower.contains("tracking")) {
            actions.add(
                AiAgentActionEntity(
                    actionType = "TRACK_PACKAGE",
                    targetId = message.id.toString(),
                    description = "پیگیری خودکار وضعیت مرسوله پستی",
                    status = "SUGGESTED",
                    urgency = 65
                )
            )
        }

        // 4. Meeting / Appointment
        if (text.contains("جلسه") || text.contains("قرار") || text.contains("ساعت")) {
            actions.add(
                AiAgentActionEntity(
                    actionType = "CALENDAR_EVENT",
                    targetId = message.id.toString(),
                    description = "ثبت رویداد جدید در تقویم رویدادها",
                    status = "SUGGESTED",
                    urgency = 70
                )
            )
        }

        return actions
    }
}
