package com.global.sms.core.ai.assistant

data class SmartReplySuggestion(
    val text: String,
    val language: String = "fa", // "fa" or "en"
    val confidence: Float = 0.95f
)

class SmartReplyLearningEngine {

    private val userCustomReplies = mutableListOf<String>()

    fun getSuggestedReplies(
        intent: CustomerIntent,
        lastMessageBody: String
    ): List<SmartReplySuggestion> {
        val suggestions = mutableListOf<SmartReplySuggestion>()

        when (intent) {
            CustomerIntent.PRICE_INQUIRY -> {
                suggestions.add(SmartReplySuggestion("سلام، پیش‌فاکتور تا دقایقی دیگر برای شما ارسال می‌شود.", "fa"))
                suggestions.add(SmartReplySuggestion("سلام، جهت دریافت قیمت عمده لطفاً با شماره پشتیبانی تماس بگیرید.", "fa"))
                suggestions.add(SmartReplySuggestion("Your price quote is being generated and will be sent shortly.", "en"))
            }
            CustomerIntent.SUPPORT_REQUEST -> {
                suggestions.add(SmartReplySuggestion("سلام، پیام شما دریافت شد. در اسرع وقت بررسی می‌شود.", "fa"))
                suggestions.add(SmartReplySuggestion("کارشناسان پشتیبانی در حال پیگیری مشکل شما هستند.", "fa"))
                suggestions.add(SmartReplySuggestion("Your message has been received. Our team is investigating.", "en"))
            }
            CustomerIntent.ORDER_STATUS -> {
                suggestions.add(SmartReplySuggestion("سلام، سفارش شما تحویل اداره پست گردید.", "fa"))
                suggestions.add(SmartReplySuggestion("کد رهگیری پستی شما به زودی پیامک خواهد شد.", "fa"))
                suggestions.add(SmartReplySuggestion("Your order has been dispatched successfully.", "en"))
            }
            else -> {
                suggestions.add(SmartReplySuggestion("سلام، پیام شما دریافت شد.", "fa"))
                suggestions.add(SmartReplySuggestion("در اولین فرصت پاسخ داده می‌شود.", "fa"))
                suggestions.add(SmartReplySuggestion("Thank you, we received your message.", "en"))
            }
        }

        userCustomReplies.forEach {
            suggestions.add(SmartReplySuggestion(it, "fa", 0.99f))
        }

        return suggestions
    }

    fun learnUserReply(replyText: String) {
        if (replyText.isNotBlank() && !userCustomReplies.contains(replyText)) {
            userCustomReplies.add(0, replyText)
            if (userCustomReplies.size > 20) {
                userCustomReplies.removeAt(userCustomReplies.size - 1)
            }
        }
    }
}
