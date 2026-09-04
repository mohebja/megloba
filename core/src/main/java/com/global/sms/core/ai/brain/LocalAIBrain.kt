package com.global.sms.core.ai.brain

import com.global.sms.core.ai.copilot.EntityExtractionEngine
import com.global.sms.core.ai.llm.LocalLLMEngine
import com.global.sms.data.entity.MessageEntity

enum class LanguageCode {
    PERSIAN,
    ENGLISH,
    ARABIC,
    UNKNOWN
}

data class BrainAnalysisResult(
    val detectedLanguage: LanguageCode,
    val primaryIntent: String,
    val urgencyScore: Int, // 0 to 100
    val extractedEntities: List<String>,
    val sentiment: String,
    val recommendedActions: List<String>,
    val reasoningSummary: String
)

data class ChatMessage(val sender: String, val body: String)

object LocalAIBrain {

    fun summarizeMessage(text: String): String {
        return if (text.isBlank()) "پیام خالی است."
        else if (text.length <= 50) "خلاصه: $text"
        else "خلاصه: ${text.take(45)}..."
    }

    fun summarizeConversation(messages: List<ChatMessage>): String {
        if (messages.isEmpty()) return "گفتگو خالی است."
        val count = messages.size
        val lastText = messages.last().body
        return "گفتگو شامل $count پیام است. آخرین موضوع: ${lastText.take(30)}..."
    }

    fun extractTask(text: String): String? {
        val keywords = listOf("یادآوری", "انجام", "جلسه", "تماس", "پرداخت", "ارسال")
        return if (keywords.any { text.contains(it) }) {
            "پیگیری موضوع: ${text.take(30)}"
        } else null
    }

    fun translateToPersian(text: String): String {
        val lang = detectLanguage(text)
        return if (lang == LanguageCode.PERSIAN) text
        else "[ترجمه فارسی]: $text"
    }

    fun detectLanguage(text: String): LanguageCode {
        var persianArabicChars = 0
        var englishChars = 0

        for (ch in text) {
            when (ch.code) {
                in 0x0600..0x06FF, in 0x0750..0x077F -> persianArabicChars++
                in 0x0041..0x005A, in 0x0061..0x007A -> englishChars++
            }
        }

        return when {
            persianArabicChars > englishChars -> {
                if (text.contains("چ") || text.contains("پ") || text.contains("گ") || text.contains("ژ")) {
                    LanguageCode.PERSIAN
                } else if (text.contains("ة") || text.contains("ئ") || text.contains("ى")) {
                    LanguageCode.ARABIC
                } else {
                    LanguageCode.PERSIAN // Default to Persian for regional SMS
                }
            }
            englishChars > 0 -> LanguageCode.ENGLISH
            else -> LanguageCode.PERSIAN
        }
    }

    fun analyzeMessage(message: MessageEntity): BrainAnalysisResult {
        val lang = detectLanguage(message.body)
        val entities = EntityExtractionEngine.extractEntities(message.body)

        val intent = LocalLLMEngine.detectIntent(message.body)
        val isUrgent = message.body.contains("فوری") ||
                message.body.contains("کد") ||
                message.body.contains("اخطار") ||
                message.body.contains("urgent") ||
                message.body.contains("emergency") ||
                message.body.contains("عاجل")

        val urgency = if (isUrgent) 90 else 30

        val actions = mutableListOf<String>()
        if (entities.dates.isNotEmpty() || entities.times.isNotEmpty()) {
            actions.add("افزودن به تقویم")
        }
        if (entities.amounts.isNotEmpty()) {
            actions.add("ثبت در مدیریت مالی")
        }
        if (isUrgent) {
            actions.add("پاسخ سریع")
        }

        val summary = when (lang) {
            LanguageCode.PERSIAN -> "تحلیل هوشمند پیام: قصد اصلی $intent، درجه اهمیت $urgency%"
            LanguageCode.ARABIC -> "تحليل الرسالة الذكية: القصد $intent، الأهمية $urgency%"
            LanguageCode.ENGLISH -> "Smart AI Analysis: Intent $intent, Urgency $urgency%"
            else -> "AI Message Analysis Complete"
        }

        val allExtracted = (entities.amounts + entities.dates + entities.times + entities.trackingCodes + entities.locations).distinct()

        return BrainAnalysisResult(
            detectedLanguage = lang,
            primaryIntent = intent,
            urgencyScore = urgency,
            extractedEntities = allExtracted,
            sentiment = if (isUrgent) "HIGH_URGENCY" else "NEUTRAL",
            recommendedActions = actions,
            reasoningSummary = summary
        )
    }

    fun retrieveKnowledgeContext(query: String, previousMessages: List<MessageEntity>): String {
        val lang = detectLanguage(query)
        val matches = previousMessages.filter { it.body.contains(query, ignoreCase = true) }
        return if (matches.isNotEmpty()) {
            when (lang) {
                LanguageCode.PERSIAN -> "یافته شد ${matches.size} پیام مرتبط در تاریخچه"
                LanguageCode.ARABIC -> "تم العثور على ${matches.size} رسائل ذات صلة"
                else -> "Found ${matches.size} relevant messages in history"
            }
        } else {
            "هیچ سابقه مستقیمی یافت نشد"
        }
    }
}
