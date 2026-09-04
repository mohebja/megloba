package com.global.sms.core.ai.agent

import com.global.sms.data.entity.CommunicationProfileEntity
import com.global.sms.data.entity.MessageEntity

object CommunicationProfileEngine {

    fun analyzeContactCommunicationProfile(
        contactAddress: String,
        messages: List<MessageEntity>
    ): CommunicationProfileEntity {
        if (messages.isEmpty()) {
            return CommunicationProfileEntity(
                contactAddress = contactAddress,
                communicationStyle = "FORMAL",
                priorityScore = 50,
                averageResponseTimeMinutes = 30
            )
        }

        var totalChars = 0
        var formalCount = 0
        var casualCount = 0
        var urgentCount = 0

        for (msg in messages) {
            totalChars += msg.body.length
            val body = msg.body
            if (body.contains("جناب") || body.contains("احتراماً") || body.contains("با سلام") || body.contains("عزیز")) {
                formalCount++
            }
            if (body.contains("سلام") || body.contains("چطوری") || body.contains("ممنون") || body.contains("دمت گرم")) {
                casualCount++
            }
            if (body.contains("فوری") || body.contains("کد") || body.contains("اخطار") || body.contains("سررسید")) {
                urgentCount++
            }
        }

        val style = when {
            urgentCount > messages.size / 3 -> "URGENT"
            casualCount > formalCount -> "CASUAL"
            else -> "FORMAL"
        }

        val priorityScore = ((messages.size * 5) + (urgentCount * 15)).coerceIn(10, 100)

        return CommunicationProfileEntity(
            contactAddress = contactAddress,
            communicationStyle = style,
            priorityScore = priorityScore,
            averageResponseTimeMinutes = if (priorityScore > 70) 10 else 45,
            preferredChannel = "SMS",
            workHoursOnly = style == "FORMAL",
            lastAnalyzed = System.currentTimeMillis()
        )
    }
}
