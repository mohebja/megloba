package com.global.sms.core.ai.fraud

data class FraudEvaluation(
    val riskLevel: FraudRiskLevel, // SAFE, WARNING, DANGEROUS
    val riskScorePercentage: Int, // 0 to 100
    val isScamOrPhishing: Boolean,
    val riskReasons: List<String>,
    val detectedUrls: List<String>
)

/**
 * Advanced Fraud & Scam Detection Engine for Sprint 2.3.
 * Evaluates banking phishing, fake delivery notices, scam links, and suspicious senders.
 */
object FraudDetectionEngine {

    fun evaluateMessage(sender: String, body: String): FraudEvaluation {
        val result = SmartFraudDetector.analyzeMessage(sender, body)
        return FraudEvaluation(
            riskLevel = result.riskLevel,
            riskScorePercentage = (result.riskScore * 100).toInt().coerceIn(0, 100),
            isScamOrPhishing = result.isFraud,
            riskReasons = result.reasons,
            detectedUrls = result.detectedUrls
        )
    }
}
