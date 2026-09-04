package com.global.sms.core.ai.agent

import com.global.sms.data.entity.MessageEntity

enum class AIInboxCategory(val displayNameFa: String) {
    CRITICAL("حساس و اضطراری"),
    IMPORTANT("پیام‌های مهم"),
    WAITING_RESPONSE("منتظر پاسخ"),
    TASKS("وظایف و پیگیری"),
    FINANCE("مالی و بانکی"),
    PERSONAL("شخصی و تعاملات")
}

data class CategorizedInboxItem(
    val message: MessageEntity,
    val category: AIInboxCategory,
    val urgencyScore: Int
)

object AIInboxManager {

    fun classifyMessageToCategory(message: MessageEntity): CategorizedInboxItem {
        val body = message.body
        val lower = body.lowercase()

        val category = when {
            body.contains("فوری") || body.contains("اخطار") || body.contains("امنیتی") || body.contains("کد ورود") || lower.contains("urgent") || lower.contains("security") -> {
                AIInboxCategory.CRITICAL
            }
            body.contains("واریز") || body.contains("برداشت") || body.contains("مبلغ") || body.contains("بانک") || body.contains("قسط") -> {
                AIInboxCategory.FINANCE
            }
            body.contains("جلسه") || body.contains("وظیفه") || body.contains("یادآوری") || body.contains("انجام بده") || body.contains("تا تاریخ") -> {
                AIInboxCategory.TASKS
            }
            body.contains("سوال") || body.contains("لطفاً پاسخ دهید") || body.contains("نظر شما") || body.contains("؟") -> {
                AIInboxCategory.WAITING_RESPONSE
            }
            body.contains("جلسه کاری") || body.contains("قرارداد") || body.contains("شرکت") || body.contains("فاکتور") -> {
                AIInboxCategory.IMPORTANT
            }
            else -> {
                AIInboxCategory.PERSONAL
            }
        }

        val urgencyScore = when (category) {
            AIInboxCategory.CRITICAL -> 95
            AIInboxCategory.FINANCE -> 85
            AIInboxCategory.TASKS -> 75
            AIInboxCategory.IMPORTANT -> 70
            AIInboxCategory.WAITING_RESPONSE -> 60
            AIInboxCategory.PERSONAL -> 40
        }

        return CategorizedInboxItem(
            message = message,
            category = category,
            urgencyScore = urgencyScore
        )
    }

    fun organizeInbox(messages: List<MessageEntity>): Map<AIInboxCategory, List<MessageEntity>> {
        val grouped = mutableMapOf<AIInboxCategory, MutableList<MessageEntity>>()
        for (cat in AIInboxCategory.values()) {
            grouped[cat] = mutableListOf()
        }

        for (msg in messages) {
            val item = classifyMessageToCategory(msg)
            grouped[item.category]?.add(msg)
        }

        return grouped
    }
}
