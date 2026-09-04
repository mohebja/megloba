package com.global.sms.core.ai.agent

enum class VoiceAgentCommandType(val faDescription: String) {
    CHECK_IMPORTANT_MESSAGES("بررسی پیام‌های مهم"),
    SUGGEST_REPLIES("پیشنهاد پاسخ‌های هوشمند"),
    SHOW_OVERDUE_TASKS("نمایش کارهای عقب افتاده"),
    CHECK_CUSTOMER_MESSAGES("بررسی پیام‌های مشتریان"),
    UNKNOWN_COMMAND("دستور ناشناخته")
}

data class VoiceAgentResult(
    val commandType: VoiceAgentCommandType,
    val spokenQuery: String,
    val responseSummary: String,
    val actionPayload: Map<String, String> = emptyMap()
)

object VoiceIntentRouter {

    fun routeSpokenCommand(spokenText: String): VoiceAgentResult {
        val text = spokenText.trim()
        val lower = text.lowercase()

        return when {
            text.contains("مهم") || lower.contains("important") -> {
                VoiceAgentResult(
                    commandType = VoiceAgentCommandType.CHECK_IMPORTANT_MESSAGES,
                    spokenQuery = text,
                    responseSummary = "در حال بازخوانی پیام‌های مهم و اولویت‌دار سیستم..."
                )
            }
            text.contains("جواب") || text.contains("پاسخ") || lower.contains("reply") || lower.contains("suggest") -> {
                VoiceAgentResult(
                    commandType = VoiceAgentCommandType.SUGGEST_REPLIES,
                    spokenQuery = text,
                    responseSummary = "پاسخ‌های پیشنهادی بر اساس متن آخرین پیام دریافت شده تولید شد."
                )
            }
            text.contains("کار") || text.contains("عقب افتاده") || text.contains("وظیفه") || lower.contains("task") -> {
                VoiceAgentResult(
                    commandType = VoiceAgentCommandType.SHOW_OVERDUE_TASKS,
                    spokenQuery = text,
                    responseSummary = "فهرست کارهای عقب‌افتاده و سررسیدهای گذشته آماده نمایش است."
                )
            }
            text.contains("مشتری") || text.contains("مشتریان") || text.contains("استعلام") || lower.contains("customer") -> {
                VoiceAgentResult(
                    commandType = VoiceAgentCommandType.CHECK_CUSTOMER_MESSAGES,
                    spokenQuery = text,
                    responseSummary = "پیام‌های استعلام و سفارشات مشتریان بازخوانی شد."
                )
            }
            else -> {
                VoiceAgentResult(
                    commandType = VoiceAgentCommandType.UNKNOWN_COMMAND,
                    spokenQuery = text,
                    responseSummary = "دستور صوتی متوجه نشد. متغیرهای فرمان: پیامهای مهم، پیشنهاد جواب، کارهای عقب افتاده، پیامهای مشتریان."
                )
            }
        }
    }
}
