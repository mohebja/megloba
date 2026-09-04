package com.global.sms.core.ai.copilot

import com.global.sms.data.entity.MessageEntity

enum class CommunicationIntent {
    QUESTION,
    REQUEST,
    APPOINTMENT,
    PAYMENT,
    REMINDER,
    COMPLAINT,
    IMPORTANT_ANNOUNCEMENT,
    CASUAL_CHAT,
    UNKNOWN
}

data class IntentAnalysisResult(
    val primaryIntent: CommunicationIntent,
    val confidence: Float,
    val detectedIntents: List<CommunicationIntent>,
    val requiresAction: Boolean
)

data class DeepConversationSummary(
    val topicSummary: String,
    val userIntention: String,
    val detectedEmotion: String,
    val urgencyLevel: String,
    val importantDecisions: List<String>,
    val followUpActions: List<String>,
    val messageCountAnalyzed: Int
)

object ConversationUnderstandingEngine {

    fun analyzeIntent(messageText: String): IntentAnalysisResult {
        val text = messageText.trim()
        val detected = mutableListOf<CommunicationIntent>()

        // Question detection
        if (text.contains("?") || text.contains("؟") || text.contains("چیست") || 
            text.contains("کجا") || text.contains("کی") || text.contains("چند") || text.contains("چرا")) {
            detected.add(CommunicationIntent.QUESTION)
        }

        // Request detection
        if (text.contains("لطفاً") || text.contains("لطفا") || text.contains("ارسال کنید") || 
            text.contains("بفرست") || text.contains("اقدام کنید") || text.contains("پیگیری کنید")) {
            detected.add(CommunicationIntent.REQUEST)
        }

        // Appointment detection
        if (text.contains("جلسه") || text.contains("قرار") || text.contains("دیدار") || 
            text.contains("ملاقات") || text.contains("ساعت")) {
            detected.add(CommunicationIntent.APPOINTMENT)
        }

        // Payment detection
        if (text.contains("واریز") || text.contains("پرداخت") || text.contains("قبض") || 
            text.contains("تراکنش") || text.contains("مبلغ") || text.contains("موجودیم") || text.contains("خرید")) {
            detected.add(CommunicationIntent.PAYMENT)
        }

        // Reminder detection
        if (text.contains("یادآوری") || text.contains("مهلت") || text.contains("تا تاریخ") || 
            text.contains("فراموش نکن") || text.contains("تذکر")) {
            detected.add(CommunicationIntent.REMINDER)
        }

        // Complaint detection
        if (text.contains("شکایت") || text.contains("خراب") || text.contains("اشتباه") || 
            text.contains("مشکل") || text.contains("پشتیبانی") || text.contains("اعتراض")) {
            detected.add(CommunicationIntent.COMPLAINT)
        }

        // Important Announcement
        if (text.contains("توجه") || text.contains("مهم") || text.contains("اطلاعیه") || 
            text.contains("کد تایید") || text.contains("هشدار")) {
            detected.add(CommunicationIntent.IMPORTANT_ANNOUNCEMENT)
        }

        val primary = detected.firstOrNull() ?: CommunicationIntent.CASUAL_CHAT
        val requiresAction = primary in listOf(
            CommunicationIntent.REQUEST,
            CommunicationIntent.APPOINTMENT,
            CommunicationIntent.PAYMENT,
            CommunicationIntent.REMINDER,
            CommunicationIntent.QUESTION
        )

        return IntentAnalysisResult(
            primaryIntent = primary,
            confidence = if (detected.isNotEmpty()) 0.95f else 0.60f,
            detectedIntents = detected.distinct(),
            requiresAction = requiresAction
        )
    }

    fun analyzeLongConversation(messages: List<MessageEntity>): DeepConversationSummary {
        if (messages.isEmpty()) {
            return DeepConversationSummary(
                topicSummary = "هیچ پیامی برای بررسی یافت نشد",
                userIntention = "نامشخص",
                detectedEmotion = "NEUTRAL",
                urgencyLevel = "LOW",
                importantDecisions = emptyList(),
                followUpActions = emptyList(),
                messageCountAnalyzed = 0
            )
        }

        // Optimized sampling for long conversations (1000+ messages)
        val sample = if (messages.size > 100) {
            messages.takeLast(50) + messages.take(20)
        } else {
            messages
        }

        val combinedText = sample.joinToString(" ") { it.body }

        // Topic detection
        val topic = when {
            combinedText.contains("خودرو") || combinedText.contains("ماشین") -> "این گفتگو درباره خرید و هماهنگی خودرو است"
            combinedText.contains("وام") || combinedText.contains("بانک") || combinedText.contains("واریز") -> "این گفتگو درباره امور مالی و بانکی است"
            combinedText.contains("جلسه") || combinedText.contains("قرار") -> "این گفتگو درباره هماهنگی جلسات و دیدارهای کاری است"
            combinedText.contains("پروژه") || combinedText.contains("کار") -> "این گفتگو درباره مدیریت پروژه و پیگیری کارهای جاری است"
            else -> "این گفتگو شامل گپ و گفتگوهای عمومی و روزمره است"
        }

        // Decision & Action detection
        val decisions = mutableListOf<String>()
        val actions = mutableListOf<String>()

        sample.forEach { msg ->
            val b = msg.body
            if (b.contains("توافق") || b.contains("قبول") || b.contains("نهایی شد") || b.contains("اوکی شد")) {
                decisions.add(b)
            }
            if (b.contains("لطفاً") || b.contains("یادت نره") || b.contains("پیگیری") || b.contains("بفرست")) {
                actions.add(b)
            }
        }

        val isUrgent = combinedText.contains("فوری") || combinedText.contains("سریع") || combinedText.contains("مشکل")
        val isAngry = combinedText.contains("شکایت") || combinedText.contains("ناراضی") || combinedText.contains("اعتراض")

        val emotion = when {
            isAngry -> "CONCERNED_OR_ANGRY"
            combinedText.contains("ممنون") || combinedText.contains("تشکر") || combinedText.contains("عالی") -> "SATISFIED"
            else -> "NEUTRAL"
        }

        return DeepConversationSummary(
            topicSummary = topic,
            userIntention = if (actions.isNotEmpty()) "درخواست پیگیری و اقدام" else "تبادل اطلاعات",
            detectedEmotion = emotion,
            urgencyLevel = if (isUrgent) "HIGH" else "NORMAL",
            importantDecisions = decisions.take(3),
            followUpActions = actions.take(3),
            messageCountAnalyzed = messages.size
        )
    }
}
