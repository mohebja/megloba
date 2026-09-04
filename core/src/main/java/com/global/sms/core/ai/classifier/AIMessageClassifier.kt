package com.global.sms.core.ai.classifier

import com.global.sms.data.entity.MessageCategory

data class ClassificationOutput(
    val category: MessageCategory,
    val confidencePercentage: Int, // e.g. 96 for 96%
    val confidenceScore: Float, // 0.0f to 1.0f
    val labelPersian: String,
    val labelEnglish: String,
    val matchedKeywords: List<String>,
    val explanation: String
)

object AIMessageClassifier {

    /**
     * Main classification entrypoint supporting Persian, English, and Arabic messages.
     * Analyzes sender number, message content, keywords, patterns, and sender reputation.
     */
    fun classifyMessage(
        sender: String,
        body: String,
        isKnownContact: Boolean = false,
        userReputationScore: Float? = null
    ): ClassificationOutput {
        val calculatedReputation = userReputationScore ?: if (isKnownContact) 0.9f else 0.5f

        val result = MessageClassificationEngine.classify(
            sender = sender,
            body = body,
            senderReputationScore = calculatedReputation
        )

        val percentage = (result.confidenceScore * 100).toInt().coerceIn(0, 100)

        return ClassificationOutput(
            category = result.category,
            confidencePercentage = percentage,
            confidenceScore = result.confidenceScore,
            labelPersian = result.categoryLabelPersian,
            labelEnglish = result.categoryLabelEnglish,
            matchedKeywords = result.detectedKeywords,
            explanation = result.explanation
        )
    }
}
