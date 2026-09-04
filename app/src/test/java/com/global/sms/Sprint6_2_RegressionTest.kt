package com.global.sms

import com.global.sms.core.ai.calendar.CalendarAssistantEngine
import com.global.sms.core.ai.contact.ContactIntelligenceEngine
import com.global.sms.core.ai.finance.BankTransactionAnalyzer
import com.global.sms.core.ai.fraud.FraudRiskLevel
import com.global.sms.core.ai.fraud.SmartFraudDetector
import com.global.sms.core.ai.personal.MessagePriority
import com.global.sms.core.ai.personal.PersonalAssistantEngine
import com.global.sms.core.ai.voice.SmartVoiceAssistant
import com.global.sms.core.ai.voice.VoiceAction
import com.global.sms.data.entity.MessageEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class Sprint6_2_RegressionTest {

    @Test
    fun testPersonalAssistantMessagePrioritization() {
        val otpMessage = MessageEntity(id = 1, threadId = 1L, address = "Bank", body = "کد ورود شما: 123456")
        val priorityResult = PersonalAssistantEngine.prioritizeMessage(otpMessage)
        assertEquals(MessagePriority.HIGH, priorityResult.priority)
    }

    @Test
    fun testBankTransactionAnalyzerExpenseExtraction() {
        val bankMessage = MessageEntity(
            id = 2,
            threadId = 1L,
            address = "Parsian",
            body = "برداشت 250,000 تومان از حساب شما نزد بانک پارسیان",
            timestamp = System.currentTimeMillis()
        )
        val transaction = BankTransactionAnalyzer.analyzeMessage(bankMessage)
        assertNotNull(transaction)
        assertEquals("پارسیان", transaction?.bankName)
        assertEquals("EXPENSE", transaction?.transactionType)
        assertEquals(250000.0, transaction?.amount ?: 0.0, 0.01)
    }

    @Test
    fun testCalendarAssistantEventSuggestion() {
        val meetingMessage = MessageEntity(
            id = 3,
            threadId = 1L,
            address = "09123456789",
            body = "جلسه فردا ساعت 10 برگزار می‌شود"
        )
        val calendarSuggestion = CalendarAssistantEngine.analyzeMessage(meetingMessage)
        assertNotNull(calendarSuggestion)
        assertTrue(calendarSuggestion?.title?.contains("جلسه") == true)
    }

    @Test
    fun testContactIntelligenceClassification() {
        val messages = listOf(
            MessageEntity(id = 4, threadId = 1L, address = "09121112233", body = "سلام چطوری؟"),
            MessageEntity(id = 5, threadId = 1L, address = "09121112233", body = "لطفاً فاکتور شرکت را بفرستید")
        )
        val insight = ContactIntelligenceEngine.analyzeContact("09121112233", messages)
        assertEquals("BUSINESS", insight.smartCategory)
    }

    @Test
    fun testAdvancedFraudDetectorScamKeywords() {
        val result = SmartFraudDetector.analyzeMessage("09120000000", "برنده ۵۰ میلیون در قرعه کشی شوید، کلیک کنید: http://fakebank.com")
        assertEquals(FraudRiskLevel.DANGEROUS, result.riskLevel)
        assertTrue(result.isFraud)
    }

    @Test
    fun testVoiceAssistantNewIntents() {
        val assistant = SmartVoiceAssistant(null)
        
        val cmdTasks = assistant.parsePersianVoiceCommand("چه کارهایی دارم؟")
        assertEquals(VoiceAction.SHOW_TASKS, cmdTasks.action)

        val cmdEnglish = assistant.parsePersianVoiceCommand("Show important messages")
        assertEquals(VoiceAction.SHOW_IMPORTANT_MESSAGES, cmdEnglish.action)

        val cmdFinance = assistant.parsePersianVoiceCommand("هزینههای این ماه چقدر است؟")
        assertEquals(VoiceAction.SHOW_FINANCIAL_SUMMARY, cmdFinance.action)
    }
}
