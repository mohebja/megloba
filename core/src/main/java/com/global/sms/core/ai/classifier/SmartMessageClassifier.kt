package com.global.sms.core.ai.classifier

import com.global.sms.data.entity.MessageCategory

data class SmartClassificationResult(
    val category: MessageCategory,
    val categoryNamePersian: String,
    val categoryNameEnglish: String,
    val confidenceScore: Int, // 0 to 100
    val matchedKeywords: List<String>,
    val explanation: String
)

/**
 * High-performance, on-device Smart Message Classifier for Sprint 2.3.
 * Supports Persian, English, and Arabic messages with hybrid keyword + heuristic AI score evaluation.
 */
object SmartMessageClassifier {

    fun classify(
        sender: String,
        body: String,
        isContact: Boolean = false
    ): SmartClassificationResult {
        val output = AIMessageClassifier.classifyMessage(
            sender = sender,
            body = body,
            isKnownContact = isContact
        )

        return SmartClassificationResult(
            category = output.category,
            categoryNamePersian = output.labelPersian,
            categoryNameEnglish = output.labelEnglish,
            confidenceScore = output.confidencePercentage,
            matchedKeywords = output.matchedKeywords,
            explanation = output.explanation
        )
    }
}
