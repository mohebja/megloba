package com.global.sms.core.ai.copilot

import com.global.sms.core.ai.classifier.SmartMessageClassifier
import com.global.sms.data.entity.MessageCategory

data class CopilotInsight(
    val conversationId: Long,
    val senderAddress: String,
    val senderName: String,
    val messageText: String,
    val category: MessageCategory,
    val intent: CommunicationIntent,
    val extractedEntities: ExtractedEntities,
    val suggestedTaskTitle: String?,
    val suggestedDeadline: String?,
    val suggestedActions: List<String>
)

object AiCopilotEngine {

    fun analyzeMessage(
        conversationId: Long,
        senderAddress: String,
        senderName: String,
        messageText: String
    ): CopilotInsight {
        val classification = SmartMessageClassifier.classify(senderAddress, messageText)
        val intentResult = ConversationUnderstandingEngine.analyzeIntent(messageText)
        val entities = EntityExtractionEngine.extractEntities(messageText)

        var taskTitle: String? = null
        var deadline: String? = null
        val actions = mutableListOf<String>()

        if (intentResult.primaryIntent == CommunicationIntent.APPOINTMENT) {
            val timeStr = entities.times.firstOrNull() ?: ""
            val dateStr = entities.dates.firstOrNull() ?: "امروز"
            taskTitle = "جلسه با $senderName"
            deadline = "$dateStr $timeStr".trim()
            actions.add("ثبت جلسه در تقویم")
            actions.add("ارسال تاییدیه")
        } else if (intentResult.primaryIntent == CommunicationIntent.PAYMENT) {
            val amountStr = entities.amounts.firstOrNull() ?: ""
            taskTitle = "پرداخت مبلغ $amountStr به $senderName".trim()
            deadline = entities.dates.firstOrNull() ?: "امروز"
            actions.add("پرداخت آنلاین")
            actions.add("ثبت در تراکنش‌ها")
        } else if (intentResult.primaryIntent == CommunicationIntent.REMINDER) {
            taskTitle = "یادآوری: $senderName"
            deadline = entities.dates.firstOrNull() ?: "امروز"
            actions.add("ایجاد یادآوری")
        } else if (intentResult.primaryIntent == CommunicationIntent.QUESTION) {
            actions.add("پاسخ سریع")
        }

        if (entities.trackingCodes.isNotEmpty()) {
            actions.add("پیگیری مرسوله (${entities.trackingCodes.first()})")
        }

        if (classification.category == MessageCategory.OTP) {
            actions.add("کپی کد تایید")
        }

        return CopilotInsight(
            conversationId = conversationId,
            senderAddress = senderAddress,
            senderName = senderName,
            messageText = messageText,
            category = classification.category,
            intent = intentResult.primaryIntent,
            extractedEntities = entities,
            suggestedTaskTitle = taskTitle,
            suggestedDeadline = deadline,
            suggestedActions = actions.distinct()
        )
    }
}
