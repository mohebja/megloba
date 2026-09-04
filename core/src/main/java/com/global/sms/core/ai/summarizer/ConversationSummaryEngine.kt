package com.global.sms.core.ai.summarizer

import com.global.sms.data.entity.MessageEntity

/**
 * AI Conversation Summary Engine for Sprint 2.3.
 * Generates offline, privacy-preserving summaries for SMS threads.
 */
object ConversationSummaryEngine {

    fun generateSummary(messages: List<MessageEntity>): String {
        return ConversationSummarizerEngine.summarizeThread(messages)
    }

    fun summarizeText(text: String): String {
        if (text.isBlank()) return "متن خالی است."
        val snippet = text.take(120).replace("\n", " ")
        return "خلاصه: $snippet"
    }
}
