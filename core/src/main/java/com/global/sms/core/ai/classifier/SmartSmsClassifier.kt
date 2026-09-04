package com.global.sms.core.ai.classifier

import com.global.sms.core.ai.banking.SmartBankingAiEngine
import com.global.sms.core.ai.fraud.SmartFraudDetector
import com.global.sms.core.ai.otp.OtpIntelligenceEngine
import com.global.sms.core.classifier.ClassificationResult
import com.global.sms.core.classifier.SmsClassifierEngine
import com.global.sms.data.entity.AiMetadataEntity
import com.global.sms.data.entity.ClassificationRuleEntity
import com.global.sms.data.entity.FinancialTransactionEntity

data class FullAiAnalysisResult(
    val classification: ClassificationResult,
    val metadata: AiMetadataEntity,
    val financialTransaction: FinancialTransactionEntity?
)

object SmartSmsClassifier {

    fun analyzeAndClassify(
        messageId: Long,
        sender: String,
        body: String,
        timestamp: Long = System.currentTimeMillis(),
        customRules: List<ClassificationRuleEntity> = emptyList()
    ): FullAiAnalysisResult {
        // 1. Classification
        val classification = SmsClassifierEngine.classifyMessage(sender, body, customRules)

        // 2. Fraud & Spam Check
        val fraudResult = SmartFraudDetector.analyzeMessage(sender, body)

        // 3. OTP Code Extraction
        val otpCode = OtpIntelligenceEngine.extractOtpCode(body)

        // 4. Banking Intelligence Analysis
        val finTx = SmartBankingAiEngine.parseBankingSms(sender, body, messageId, timestamp)

        // Combine into AiMetadataEntity
        val metadata = AiMetadataEntity(
            messageId = messageId,
            classificationResult = classification.category.name,
            confidenceScore = classification.confidenceScore,
            classificationDate = timestamp,
            fraudRiskScore = fraudResult.riskScore,
            fraudReasons = if (fraudResult.reasons.isNotEmpty()) fraudResult.reasons.joinToString(" • ") else null,
            detectedOtpCode = otpCode,
            summarySnippet = body.take(90).replace("\n", " ")
        )

        return FullAiAnalysisResult(
            classification = classification,
            metadata = metadata,
            financialTransaction = finTx
        )
    }
}
