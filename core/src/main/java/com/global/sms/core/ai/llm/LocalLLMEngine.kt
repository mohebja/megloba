package com.global.sms.core.ai.llm

enum class LLMBackendType {
    LOCAL_RULE_TRANSFORMER,
    TENSORFLOW_LITE_MOBILE,
    ONNX_RUNTIME_MOBILE,
    MEDIAPIPE_LLM_INFERENCE
}

data class LLMInferenceConfig(
    val backend: LLMBackendType = LLMBackendType.LOCAL_RULE_TRANSFORMER,
    val maxTokens: Int = 256,
    val temperature: Float = 0.7f,
    val topK: Int = 40
)

object LocalLLMEngine {

    private var activeConfig = LLMInferenceConfig()

    fun configure(config: LLMInferenceConfig) {
        activeConfig = config
    }

    fun generateText(prompt: String, context: String = ""): String {
        // 100% local execution pipeline with fallbacks to rule-based transformer logic
        val cleanPrompt = prompt.trim()
        return when {
            cleanPrompt.contains("خلاصه") || cleanPrompt.contains("summarize", ignoreCase = true) -> {
                summarizeText(context.ifEmpty { cleanPrompt })
            }
            cleanPrompt.contains("پاسخ") || cleanPrompt.contains("reply", ignoreCase = true) -> {
                suggestReply(context.ifEmpty { cleanPrompt }).joinToString(" | ")
            }
            else -> {
                "پردازش درون‌دستگاهی: $cleanPrompt"
            }
        }
    }

    fun summarizeText(text: String): String {
        if (text.isBlank()) return "متن خالی است."
        val sentences = text.split(".", "؟", "!", "\n").filter { it.isNotBlank() }
        if (sentences.isEmpty()) return text
        if (sentences.size <= 2) return text.trim()
        
        // Pick first and most informative sentence locally
        val keySentence = sentences.firstOrNull { 
            it.contains("واریز") || it.contains("جلسه") || it.contains("کد") || it.contains("مهم") 
        } ?: sentences.first()

        return "خلاصه گفتگو: $keySentence"
    }

    fun detectIntent(text: String): String {
        val lower = text.lowercase()
        return when {
            lower.contains("واریز") || lower.contains("برداشت") || lower.contains("خرید") -> "FINANCIAL_TRANSACTION"
            lower.contains("کد") || lower.contains("ورود") || lower.contains("رمز") -> "SECURITY_OTP"
            lower.contains("جلسه") || lower.contains("قرار") || lower.contains("ساعت") -> "CALENDAR_EVENT"
            lower.contains("سلام") || lower.contains("چطوری") || lower.contains("ممنون") -> "CASUAL_GREETING"
            lower.contains("تخفیف") || lower.contains("برنده") || lower.contains("لینک") -> "PROMOTIONAL_OFFER"
            else -> "GENERAL_MESSAGE"
        }
    }

    fun suggestReply(text: String): List<String> {
        val intent = detectIntent(text)
        return when (intent) {
            "CASUAL_GREETING" -> listOf("سلام، ممنون!", "درود بر شما، خوبم سپاس", "سلام، در خدمتم")
            "CALENDAR_EVENT" -> listOf("حتماً، هماهنگ شد.", "ساعت مناسبی است.", "متأسفانه در این زمان امکانش نیست.")
            "FINANCIAL_TRANSACTION" -> listOf("دریافت شد، متشکرم.", "رسید بررسی شد.")
            else -> listOf("باشه متوجه شدم.", "سپاسگزارم.", "بررسی می‌کنم و اطلاع میدم.")
        }
    }
}
