package com.global.sms.core.ai.assistant

enum class MessageUrgency {
    HIGH,
    MEDIUM,
    LOW
}

enum class CustomerIntent {
    PRICE_INQUIRY,
    SUPPORT_REQUEST,
    ORDER_STATUS,
    COMPLAINT,
    GENERAL_GREETING,
    UNKNOWN
}

data class ConversationInsight(
    val conversationId: String,
    val summary: String,
    val urgency: MessageUrgency,
    val detectedIntent: CustomerIntent,
    val keyPoints: List<String>,
    val actionRequired: String?,
    val timestamp: Long = System.currentTimeMillis()
)

class ConversationInsightEngine {

    fun analyzeConversation(conversationId: String, messages: List<String>): ConversationInsight {
        val combinedText = messages.joinToString(" ")
        val isUrgent = combinedText.contains("فوری") || combinedText.contains("پیگیری") || combinedText.contains("مشکل") || combinedText.contains("urgent")

        val urgency = if (isUrgent) MessageUrgency.HIGH else MessageUrgency.MEDIUM

        val intent = when {
            combinedText.contains("قیمت") || combinedText.contains("فاکتور") || combinedText.contains("هزینه") -> CustomerIntent.PRICE_INQUIRY
            combinedText.contains("پشتیبانی") || combinedText.contains("خراب") || combinedText.contains("قطع") -> CustomerIntent.SUPPORT_REQUEST
            combinedText.contains("ارسال") || combinedText.contains("کد رهگیری") || combinedText.contains("سفارش") -> CustomerIntent.ORDER_STATUS
            combinedText.contains("شکایت") || combinedText.contains("ناراضی") -> CustomerIntent.COMPLAINT
            else -> CustomerIntent.GENERAL_GREETING
        }

        val keyPoints = mutableListOf<String>()
        if (intent == CustomerIntent.PRICE_INQUIRY) keyPoints.add("درخواست استعلام قیمت/فاکتور")
        if (intent == CustomerIntent.SUPPORT_REQUEST) keyPoints.add("درخواست پشتیبانی فنی")
        if (intent == CustomerIntent.ORDER_STATUS) keyPoints.add("پیگیری وضعیت ارسال مرسوله")

        val actionRequired = when (intent) {
            CustomerIntent.PRICE_INQUIRY -> "ارسال پیش‌فاکتور رسمی به مشتری"
            CustomerIntent.SUPPORT_REQUEST -> "ارجاع به واحد پشتیبانی فنی"
            CustomerIntent.ORDER_STATUS -> "بررسی سامانه پستی و ارسال کد رهگیری"
            CustomerIntent.COMPLAINT -> "تماس فوری اپراتور با مشتری"
            else -> null
        }

        val summary = "گفتگو شامل ${messages.size} پیام. موضوع اصلی: ${intent.name}. سطح اهمیت: ${urgency.name}"

        return ConversationInsight(
            conversationId = conversationId,
            summary = summary,
            urgency = urgency,
            detectedIntent = intent,
            keyPoints = keyPoints,
            actionRequired = actionRequired
        )
    }
}
