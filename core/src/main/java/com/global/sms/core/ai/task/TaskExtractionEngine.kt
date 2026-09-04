package com.global.sms.core.ai.task

import com.global.sms.core.ai.copilot.CommunicationIntent
import com.global.sms.core.ai.copilot.ConversationUnderstandingEngine
import com.global.sms.core.ai.copilot.EntityExtractionEngine
import com.global.sms.data.entity.TaskEntity

object TaskExtractionEngine {

    fun extractTaskFromMessage(messageId: Long, messageText: String): TaskEntity? {
        val intentResult = ConversationUnderstandingEngine.analyzeIntent(messageText)
        val entities = EntityExtractionEngine.extractEntities(messageText)

        if (!intentResult.requiresAction) {
            return null
        }

        val title = when (intentResult.primaryIntent) {
            CommunicationIntent.APPOINTMENT -> {
                val loc = entities.locations.firstOrNull() ?: ""
                if (loc.isNotEmpty()) "جلسه در $loc" else "جلسه کاری"
            }
            CommunicationIntent.PAYMENT -> {
                val amt = entities.amounts.firstOrNull() ?: ""
                if (amt.isNotEmpty()) "پرداخت مبلغ $amt" else "پرداخت صورتحساب"
            }
            CommunicationIntent.REMINDER -> "یادآوری پیام"
            CommunicationIntent.REQUEST -> "انجام درخواست کاربر"
            else -> "اقدام روی پیام"
        }

        val description = messageText.take(120)
        val dueDateMillis = System.currentTimeMillis() + 86400000L // Default tomorrow

        return TaskEntity(
            messageId = messageId,
            title = title,
            description = description,
            dueDateMillis = dueDateMillis,
            isCompleted = false,
            priority = if (intentResult.primaryIntent == CommunicationIntent.PAYMENT) "HIGH" else "NORMAL"
        )
    }
}
