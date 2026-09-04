package com.global.sms

import com.global.sms.core.ai.copilot.AiCopilotEngine
import com.global.sms.core.ai.copilot.CommunicationIntent
import com.global.sms.core.ai.copilot.ConversationUnderstandingEngine
import com.global.sms.core.ai.copilot.EntityExtractionEngine
import com.global.sms.core.ai.smartreply.SmartReplyEngine
import com.global.sms.core.ai.summary.DailyCommunicationSummaryEngine
import com.global.sms.core.ai.task.TaskExtractionEngine
import com.global.sms.core.ai.voice.VoiceAction
import com.global.sms.core.automation.AutomationEngine
import com.global.sms.data.entity.MessageCategory
import com.global.sms.data.entity.MessageEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class Sprint6_0_AICopilotTest {

    @Test
    fun testEntityExtractionEngine() {
        val sampleText = "رضا جان، فردا ساعت 10:30 در خیابان آزادی جلسه داریم. مبلغ 50,000 تومان واریز شد. کد پیگیری: 884192"
        val entities = EntityExtractionEngine.extractEntities(sampleText)

        assertTrue(entities.personNames.contains("رضا"))
        assertTrue(entities.dates.contains("فردا"))
        assertTrue(entities.times.any { it.contains("10:30") || it.contains("۱۰:۳۰") })
        assertTrue(entities.amounts.any { it.contains("50,000") || it.contains("۵۰,۰۰۰") })
        assertTrue(entities.trackingCodes.contains("884192"))
    }

    @Test
    fun testConversationUnderstandingEngine() {
        val questionResult = ConversationUnderstandingEngine.analyzeIntent("کجا هستید؟")
        assertEquals(CommunicationIntent.QUESTION, questionResult.primaryIntent)

        val appointmentResult = ConversationUnderstandingEngine.analyzeIntent("فردا ساعت 10 جلسه داریم")
        assertEquals(CommunicationIntent.APPOINTMENT, appointmentResult.primaryIntent)
        assertTrue(appointmentResult.requiresAction)

        val paymentResult = ConversationUnderstandingEngine.analyzeIntent("لطفاً مبلغ قبض را واریز کنید")
        assertEquals(CommunicationIntent.REQUEST, paymentResult.primaryIntent)
    }

    @Test
    fun testAiCopilotEngine() {
        val insight = AiCopilotEngine.analyzeMessage(
            conversationId = 1L,
            senderAddress = "09123456789",
            senderName = "علی",
            messageText = "فردا ساعت 14 جلسه کاری داریم در دفتر"
        )

        assertEquals(CommunicationIntent.APPOINTMENT, insight.intent)
        assertNotNull(insight.suggestedTaskTitle)
        assertTrue(insight.suggestedActions.isNotEmpty())
    }

    @Test
    fun testTaskExtractionEngine() {
        val task = TaskExtractionEngine.extractTaskFromMessage(
            messageId = 101L,
            messageText = "جلسه در شرکت فردا ساعت 9 صبح"
        )

        assertNotNull(task)
        assertEquals(101L, task?.messageId)
        assertTrue(task?.title?.contains("جلسه") == true)
    }

    @Test
    fun testDailyCommunicationSummaryEngine() {
        val messages = listOf(
            MessageEntity(id = 1, threadId = 1, address = "Bank", body = "واریز 1,000,000 ریال", category = MessageCategory.BANK),
            MessageEntity(id = 2, threadId = 1, address = "SMS", body = "کد ورود: 1234", category = MessageCategory.OTP),
            MessageEntity(id = 3, threadId = 1, address = "Work", body = "جلسه فردا", category = MessageCategory.BUSINESS)
        )

        val summary = DailyCommunicationSummaryEngine.generateDailySummary("امروز", messages)

        assertEquals(3, summary.totalReceived)
        assertEquals(1, summary.bankCount)
        assertEquals(1, summary.otpCount)
        assertEquals(1, summary.businessCount)
        assertTrue(summary.summaryPersian.contains("3 پیام"))
    }

    @Test
    fun testSmartReplyEngineTones() {
        val replies = SmartReplyEngine.generateContextualReplies("فردا ساعت 10 جلسه داریم")

        assertTrue(replies.business.isNotEmpty())
        assertTrue(replies.friendly.isNotEmpty())
        assertTrue(replies.short.isNotEmpty())
    }

    @Test
    fun testAutomationEngineRules() {
        val engine = AutomationEngine()
        val results = engine.processIncomingMessage("BankMelli", "کد تایید ورود شما: 983104")

        assertTrue(results.isNotEmpty())
        assertTrue(results.any { it.extractedData == "983104" })
    }
}
