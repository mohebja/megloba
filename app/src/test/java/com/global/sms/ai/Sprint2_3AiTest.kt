package com.global.sms.ai

import com.global.sms.core.ai.classifier.SmartMessageClassifier
import com.global.sms.core.ai.fraud.FraudDetectionEngine
import com.global.sms.core.ai.fraud.FraudRiskLevel
import com.global.sms.core.ai.otp.OtpExtractor
import com.global.sms.core.ai.smartreply.SmartReplyEngine
import com.global.sms.core.ai.summarizer.ConversationSummaryEngine
import com.global.sms.data.entity.MessageCategory
import com.global.sms.data.entity.MessageEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class Sprint2_3AiTest {

    @Test
    fun testOtpExtractionAndClassification() {
        val otpMessage = "کد ورود شما به همراه کارت: 849201. معتبر تا 5 دقیقه."
        val result = SmartMessageClassifier.classify("MelliBank", otpMessage)
        assertEquals(MessageCategory.OTP, result.category)

        val code = OtpExtractor.extractCode(otpMessage)
        assertEquals("849201", code)
    }

    @Test
    fun testFraudDetectionEngine() {
        val scamMessage = "حساب کاربری شما مسدود شد. برای فعال‌سازی مجدد وارد لینک زیر شوید: http://bank-melli.fake-phishing.com/login"
        val result = FraudDetectionEngine.evaluateMessage("09121112233", scamMessage)

        assertEquals(FraudRiskLevel.DANGEROUS, result.riskLevel)
        assertTrue(result.isScamOrPhishing)
        assertTrue(result.riskScorePercentage >= 65)
        assertTrue(result.detectedUrls.isNotEmpty())
    }

    @Test
    fun testSmartReplyGeneration() {
        val incomingMsg = "سلام، فردا برای جلسه وقت داری؟"
        val replies = SmartReplyEngine.generateSmartReplies(incomingMsg)

        assertTrue(replies.isNotEmpty())
        assertTrue(replies.size >= 3)
    }

    @Test
    fun testConversationSummarization() {
        val messages = listOf(
            MessageEntity(id = 1, threadId = 100, address = "09120000000", body = "سلام وقت بخیر", timestamp = 1000L),
            MessageEntity(id = 2, threadId = 100, address = "09120000000", body = "کد تایید ورود شما 4589 است.", timestamp = 2000L)
        )

        val summary = ConversationSummaryEngine.generateSummary(messages)
        assertNotNull(summary)
        assertTrue(summary.contains("کد"))
    }
}
