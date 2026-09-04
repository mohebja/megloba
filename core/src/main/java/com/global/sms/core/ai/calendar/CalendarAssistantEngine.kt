package com.global.sms.core.ai.calendar

import com.global.sms.core.ai.copilot.EntityExtractionEngine
import com.global.sms.data.entity.CalendarSuggestionEntity
import com.global.sms.data.entity.MessageEntity

object CalendarAssistantEngine {

    fun analyzeMessage(message: MessageEntity): CalendarSuggestionEntity? {
        val body = message.body
        val normalized = body.lowercase()

        val isCalendarEvent = normalized.contains("جلسه") ||
                normalized.contains("قرار") ||
                normalized.contains("مراسم") ||
                normalized.contains("همایش") ||
                normalized.contains("ساعت") && (normalized.contains("فردا") || normalized.contains("شنبه") || normalized.contains("یکشنبه") || normalized.contains("دوشنبه") || normalized.contains("سه‌شنبه") || normalized.contains("چهارشنبه") || normalized.contains("پنج‌شنبه") || normalized.contains("جمعه"))

        if (!isCalendarEvent) return null

        val extractedDates = EntityExtractionEngine.extractEntities(body).dates
        val extractedTimes = EntityExtractionEngine.extractEntities(body).times

        val dateStr = extractedDates.firstOrNull() ?: "روز کاری آینده"
        val timeStr = extractedTimes.firstOrNull() ?: "ساعت مشخص‌شده"

        val title = when {
            normalized.contains("جلسه") -> "جلسه: " + body.take(25) + "..."
            normalized.contains("قرار") -> "قرار ملاقات: " + body.take(25) + "..."
            else -> "رویداد: " + body.take(25) + "..."
        }

        return CalendarSuggestionEntity(
            messageId = message.id,
            title = title,
            eventDateMillis = System.currentTimeMillis() + 86400000L,
            timeString = "$dateStr - $timeStr",
            location = if (body.contains("دفتر") || body.contains("سالن")) "محل مشخص‌شده" else "آنلاین/حضوری",
            isAccepted = false,
            createdAt = System.currentTimeMillis()
        )
    }
}
