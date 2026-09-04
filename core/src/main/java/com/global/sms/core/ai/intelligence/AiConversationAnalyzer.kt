package com.global.sms.core.ai.intelligence

enum class UrgencyLevel { CRITICAL, HIGH, NORMAL, SILENT }
enum class MessageSentiment { POSITIVE, NEUTRAL, NEGATIVE, URGENT }
enum class CustomerSatisfaction { EXCELLENT, SATISFIED, NEUTRAL, DISSATISFIED }

data class ConversationAnalysisResult(
    val conversationId: String,
    val primaryIntent: String,
    val category: String,
    val urgency: UrgencyLevel,
    val sentiment: MessageSentiment,
    val satisfaction: CustomerSatisfaction,
    val isImportant: Boolean,
    val recommendedAction: String,
    val extractedEntities: List<String> = emptyList()
)

class AiConversationAnalyzer {

    fun analyzeConversation(
        conversationId: String,
        messages: List<String>
    ): ConversationAnalysisResult {
        val fullText = messages.joinToString(" ")
        val normalized = normalizePersianDigits(fullText)

        // Urgency Detection
        val isCritical = normalized.contains("فوری") || normalized.contains("کد ورود") || normalized.contains("برداشت") || normalized.contains("مسدودی")
        val isHigh = normalized.contains("بدهی") || normalized.contains("سررسید") || normalized.contains("بیمه") || normalized.contains("شکایت")
        val urgency = when {
            isCritical -> UrgencyLevel.CRITICAL
            isHigh -> UrgencyLevel.HIGH
            else -> UrgencyLevel.NORMAL
        }

        // Category & Intent Detection
        val (category, intent) = when {
            normalized.contains("فاکتور") || normalized.contains("قیمت") || normalized.contains("خرید") || normalized.contains("سفارش") ->
                "Shopping" to "استعلام قیمت / خرید"
            normalized.contains("بانک") || normalized.contains("حساب") || normalized.contains("مبلغ") || normalized.contains("تومان") ->
                "Banking" to "اطلاع‌رسانی مالی / بانکی"
            normalized.contains("کد") || normalized.contains("تایید") || normalized.contains("رمز") ->
                "OTP" to "کد تایید یک‌بارمصرف"
            normalized.contains("ارسال") || normalized.contains("مرسوله") || normalized.contains("پست") || normalized.contains("کد رهگیری") ->
                "Delivery" to "پیگیری مرسوله پستی"
            normalized.contains("قیمت") || normalized.contains("فاکتور") || normalized.contains("خرید") ->
                "Shopping" to "استعلام قیمت / خرید"
            normalized.contains("شکایت") || normalized.contains("مشکل") || normalized.contains("خراب") ->
                "Support" to "پشتیبانی و پیگیری شکایت"
            else -> "Personal" to "گفتگوی عمومی"
        }

        // Sentiment & Satisfaction Detection
        val sentiment = when {
            normalized.contains("عالی") || normalized.contains("ممنون") || normalized.contains("تشکر") -> MessageSentiment.POSITIVE
            normalized.contains("اشتباه") || normalized.contains("شکایت") || normalized.contains("افتضاح") -> MessageSentiment.NEGATIVE
            isCritical || isHigh -> MessageSentiment.URGENT
            else -> MessageSentiment.NEUTRAL
        }

        val satisfaction = when (sentiment) {
            MessageSentiment.POSITIVE -> CustomerSatisfaction.EXCELLENT
            MessageSentiment.NEGATIVE -> CustomerSatisfaction.DISSATISFIED
            MessageSentiment.URGENT -> CustomerSatisfaction.NEUTRAL
            else -> CustomerSatisfaction.SATISFIED
        }

        val isImportant = urgency == UrgencyLevel.CRITICAL || urgency == UrgencyLevel.HIGH || category == "Banking"

        val recommendedAction = when (category) {
            "Banking" -> "مشاهده جزئیات تراکنش بانکی"
            "OTP" -> "کپی سریع کد تایید"
            "Delivery" -> "کپی کد رهگیری پستی"
            "Support" -> "پاسخ‌دهی سریع به مشتری"
            else -> "مشاهده گفتگو"
        }

        return ConversationAnalysisResult(
            conversationId = conversationId,
            primaryIntent = intent,
            category = category,
            urgency = urgency,
            sentiment = sentiment,
            satisfaction = satisfaction,
            isImportant = isImportant,
            recommendedAction = recommendedAction,
            extractedEntities = extractKeyTerms(normalized)
        )
    }

    private fun extractKeyTerms(text: String): List<String> {
        val terms = mutableListOf<String>()
        val words = text.split("\\s+".toRegex())
        words.forEach { w ->
            if (w.length > 4 && (w.startsWith("6037") || w.startsWith("IR") || w.contains("تومان"))) {
                terms.add(w)
            }
        }
        return terms.distinct()
    }

    private fun normalizePersianDigits(text: String): String {
        return text
            .replace('۰', '0').replace('۱', '1').replace('۲', '2').replace('۳', '3').replace('۴', '4')
            .replace('۵', '5').replace('۶', '6').replace('۷', '7').replace('۸', '8').replace('۹', '9')
    }
}
