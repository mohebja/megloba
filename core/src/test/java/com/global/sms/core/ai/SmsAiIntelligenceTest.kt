package com.global.sms.core.ai

import com.global.sms.core.ai.banking.BankMessageParser
import com.global.sms.core.ai.classifier.AIMessageClassifier
import com.global.sms.core.ai.fraud.AdvancedSpamDetector
import com.global.sms.core.ai.otp.OtpDetector
import com.global.sms.core.ai.otp.OtpExtractor
import com.global.sms.core.ai.search.SearchCriteria
import com.global.sms.core.ai.search.SmartSearchEngine
import com.global.sms.core.ai.smartreply.SmartReplyEngine
import com.global.sms.data.entity.MessageCategory
import com.global.sms.data.entity.MessageEntity
import org.junit.Assert.*
import org.junit.Test

class SmsAiIntelligenceTest {

    @Test
    fun testOtpDetectionAndExtraction() {
        val sampleSms = "کد ورود شما به دیجی‌کالا: ۱۲۳۴۵۶ (اعتبار ۵ دقیقه)"
        val detection = OtpDetector.detect(sampleSms)
        assertTrue(detection.isOtp)
        assertEquals("123456", detection.otpCode)

        val codeOnly = OtpExtractor.extractCode(sampleSms)
        assertEquals("123456", codeOnly)
    }

    @Test
    fun testBankSmsParsing() {
        val bankSms = "برداشت مبلغ 500,000 ریال از حساب 6104****1234. مانده: 10,000,000 ریال. بانک ملت"
        val parsed = BankMessageParser.parse(
            sender = "MELLAT",
            body = bankSms,
            messageId = 101L
        )
        assertNotNull(parsed)
        assertEquals("بانک ملت", parsed?.bankName)
        assertEquals("WITHDRAWAL", parsed?.transactionType)
        assertEquals(50000L, parsed?.amountTomans)
    }

    @Test
    fun testAiMessageClassifier() {
        val otpMsg = AIMessageClassifier.classifyMessage(
            sender = "Digikala",
            body = "کد تایید شما 884123 می باشد."
        )
        assertEquals(MessageCategory.OTP, otpMsg.category)
        assertTrue(otpMsg.confidencePercentage >= 50)

        val bankMsg = AIMessageClassifier.classifyMessage(
            sender = "MELLI",
            body = "واریز 2,000,000 تومان به حساب شما."
        )
        assertEquals(MessageCategory.BANK, bankMsg.category)
    }

    @Test
    fun testSmartReplyGeneration() {
        val replies = SmartReplyEngine.generateSmartReplies("سلام فردا وقت داری برای جلسه؟")
        assertTrue(replies.isNotEmpty())
        assertTrue(replies.any { it.contains("سلام") || it.contains("تایید") || it.contains("ممنون") || it.contains("زمان") || it.contains("بله") })
    }

    @Test
    fun testAdvancedSpamDetector() {
        val spamBody = "برنده جایزه ۵۰ میلیونی شده‌اید! برای دریافت وام بدون ضامن همین حالا کلیک کنید: http://bit.ly/fake"
        val report = AdvancedSpamDetector.evaluateSpam(
            sender = "9830001234",
            body = spamBody,
            isKnownContact = false
        )
        assertTrue(report.isSpam)
        assertTrue(report.spamScore >= 70)
    }

    @Test
    fun testSmartSearchEngine() {
        val messages = listOf(
            MessageEntity(id = 1, threadId = 1, address = "MELLI", body = "واریز 100,000 تومان", category = MessageCategory.BANK),
            MessageEntity(id = 2, threadId = 2, address = "09123456789", body = "سلام چطوری؟", category = MessageCategory.PERSONAL),
            MessageEntity(id = 3, threadId = 3, address = "Digikala", body = "کد ورود: 55432", category = MessageCategory.OTP)
        )

        val results = SmartSearchEngine.searchMessages(
            messages = messages,
            criteria = SearchCriteria(isOtpOnly = true)
        )

        assertEquals(1, results.size)
        assertEquals("Digikala", results[0].address)
    }
}
