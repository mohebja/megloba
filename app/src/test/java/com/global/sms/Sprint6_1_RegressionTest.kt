package com.global.sms

import com.global.sms.core.ai.copilot.AiCopilotEngine
import com.global.sms.core.ai.copilot.CommunicationIntent
import com.global.sms.core.ai.copilot.ConversationUnderstandingEngine
import com.global.sms.core.ai.copilot.EntityExtractionEngine
import com.global.sms.core.ai.smartreply.SmartReplyEngine
import com.global.sms.core.ai.summary.DailyCommunicationSummaryEngine
import com.global.sms.core.ai.task.TaskExtractionEngine
import com.global.sms.core.ai.voice.SmartVoiceAssistant
import com.global.sms.core.ai.voice.VoiceAction
import com.global.sms.core.automation.AutomationEngine
import com.global.sms.data.entity.MessageCategory
import com.global.sms.data.entity.MessageEntity
import com.global.sms.data.entity.TaskPriority
import com.global.sms.data.entity.TaskSource
import com.global.sms.data.entity.TaskStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class Sprint6_1_RegressionTest {

    @Test
    fun testPersianEntityExtractionAndNormalization() {
        val sampleText = "مبلغ 500,000 تومان از حساب شما برداشت شد. کد پیگیری: 123456"
        val entities = EntityExtractionEngine.extractEntities(sampleText)

        assertTrue(entities.amounts.isNotEmpty())
        assertTrue(entities.trackingCodes.contains("123456"))
    }

    @Test
    fun testIntentUnderstanding() {
        val intentResult = ConversationUnderstandingEngine.analyzeIntent("جلسه فردا ساعت 10 برگزار میشود")
        assertEquals(CommunicationIntent.APPOINTMENT, intentResult.primaryIntent)
        assertTrue(intentResult.requiresAction)
    }

    @Test
    fun testTaskEnumsAndCreation() {
        val priority = TaskPriority.HIGH.name
        val status = TaskStatus.NEW.name
        val source = TaskSource.AI_SUGGESTED.name

        assertEquals("HIGH", priority)
        assertEquals("NEW", status)
        assertEquals("AI_SUGGESTED", source)
    }

    @Test
    fun testVoiceAssistantCommands() {
        val assistant = SmartVoiceAssistant(null)
        val parsedCmd = assistant.parsePersianVoiceCommand("آخرین پیام بانک را نشان بده")
        assertEquals(VoiceAction.SHOW_LATEST_BANK_MESSAGE, parsedCmd.action)
    }

    @Test
    fun testSmartReplyTones() {
        val replies = SmartReplyEngine.generateContextualReplies("کجا هستید؟")
        assertTrue(replies.friendly.isNotEmpty())
        assertTrue(replies.business.isNotEmpty())
    }

    @Test
    fun testAutomationEngineRules() {
        val engine = AutomationEngine()
        val actions = engine.processIncomingMessage("Bank", "کد ورود: 445566")
        assertTrue(actions.isNotEmpty())
    }
}
