package com.global.sms.core.ai.contact

import com.global.sms.data.entity.ContactInsightEntity
import com.global.sms.data.entity.MessageEntity

object ContactIntelligenceEngine {

    fun analyzeContact(address: String, messages: List<MessageEntity>): ContactInsightEntity {
        val totalMessages = messages.size
        val lastMessageTime = messages.maxOfOrNull { it.timestamp } ?: System.currentTimeMillis()

        val isBusinessKeyword = messages.any { msg ->
            val b = msg.body.lowercase()
            b.contains("سفارش") || b.contains("فاکتور") || b.contains("شرکت") || b.contains("جلسه") || b.contains("قرارداد")
        }

        val isFamilyKeyword = messages.any { msg ->
            val b = msg.body.lowercase()
            b.contains("خونه") || b.contains("مامان") || b.contains("بابا") || b.contains("عزیز")
        }

        val (smartCat, score) = when {
            totalMessages > 20 -> Pair("VIP", 95)
            isBusinessKeyword -> Pair("BUSINESS", 80)
            isFamilyKeyword -> Pair("IMPORTANT", 85)
            totalMessages > 5 -> Pair("IMPORTANT", 70)
            else -> Pair("PERSONAL", 50)
        }

        return ContactInsightEntity(
            address = address,
            smartCategory = smartCat,
            interactionCount = totalMessages,
            lastContactMillis = lastMessageTime,
            priorityScore = score
        )
    }
}
