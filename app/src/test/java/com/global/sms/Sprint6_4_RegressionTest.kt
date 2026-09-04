package com.global.sms

import com.global.sms.core.ai.agent.*
import com.global.sms.core.automation.v2.SmartWorkflowEngine
import com.global.sms.data.entity.MessageEntity
import com.global.sms.data.entity.WorkflowRuleEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class Sprint6_4_RegressionTest {

    @Test
    fun testKillSwitchPreventsAgentExecution() = runBlocking {
        CommunicationAgent.setKillSwitch(true)
        assertTrue(CommunicationAgent.isKillSwitchActive.value)

        val msg = MessageEntity(id = 1, address = "10002000", body = "قسط وام شما فردا سررسید می‌شود.", timestamp = System.currentTimeMillis())
        var actionSuggested = false

        val reasoning = CommunicationAgent.processIncomingMessage(msg) {
            actionSuggested = true
        }

        assertNull(reasoning)
        assertFalse(actionSuggested)

        // Reset kill switch
        CommunicationAgent.setKillSwitch(false)
        assertFalse(CommunicationAgent.isKillSwitchActive.value)
    }

    @Test
    fun testActionRecommendationEngineBankSms() {
        val msg = MessageEntity(id = 10, address = "20003000", body = "قسط وام شماره ۱۲۳۴ فردا سررسید میشود.", timestamp = System.currentTimeMillis())
        val reasoning = ActionRecommendationEngine.reasonAboutMessage(msg.body)
        assertEquals("FINANCIAL_DUE_DATE", reasoning.detectedIntent)
        assertTrue(reasoning.urgencyScore >= 80)

        val suggestions = ActionRecommendationEngine.generateRecommendations(msg)
        assertTrue(suggestions.isNotEmpty())
        assertEquals("CREATE_REMINDER", suggestions.first().actionType)
    }

    @Test
    fun testActionRecommendationEngineCustomerInquiry() {
        val msg = MessageEntity(id = 11, address = "+989123456789", body = "سلام، قیمت محصول جدید شما چقدر است؟", timestamp = System.currentTimeMillis())
        val reasoning = ActionRecommendationEngine.reasonAboutMessage(msg.body)
        assertEquals("CUSTOMER_INQUIRY", reasoning.detectedIntent)

        val suggestions = ActionRecommendationEngine.generateRecommendations(msg)
        assertTrue(suggestions.any { it.actionType == "REPLY_TEMPLATE" })
    }

    @Test
    fun testActionRecommendationEngineDeliveryPackage() {
        val msg = MessageEntity(id = 12, address = "POST", body = "مرسوله شما آماده تحویل است. کد رهگیری: 98765", timestamp = System.currentTimeMillis())
        val suggestions = ActionRecommendationEngine.generateRecommendations(msg)
        assertTrue(suggestions.any { it.actionType == "TRACK_PACKAGE" })
    }

    @Test
    fun testSmartWorkflowEngineEvaluation() {
        val msg = MessageEntity(id = 20, address = "BANK", body = "واریز مبلغ ۵,۰۰۰,۰۰۰ ریال به حساب", timestamp = System.currentTimeMillis())
        val rules = SmartWorkflowEngine.getDefaultSystemRules()

        val actions = SmartWorkflowEngine.evaluateWorkflows(msg, rules)
        assertTrue(actions.isNotEmpty())
        assertTrue(actions.any { it.actionType == "CATEGORIZE" })
    }

    @Test
    fun testCommunicationProfileEngine() {
        val messages = listOf(
            MessageEntity(id = 1, address = "+989120000000", body = "جناب آقای حسینی با سلام و احترام", timestamp = System.currentTimeMillis()),
            MessageEntity(id = 2, address = "+989120000000", body = "احتراماً صورتحساب خدمتتان ارسال گردید.", timestamp = System.currentTimeMillis())
        )

        val profile = CommunicationProfileEngine.analyzeContactCommunicationProfile("+989120000000", messages)
        assertEquals("FORMAL", profile.communicationStyle)
        assertTrue(profile.priorityScore >= 10)
    }

    @Test
    fun testAIInboxManagerClassification() {
        val urgentMsg = MessageEntity(id = 1, address = "ALERT", body = "فوری: اخطار امنیتی حساب کاربری شما!", timestamp = System.currentTimeMillis())
        val finMsg = MessageEntity(id = 2, address = "BANK", body = "واریز ۵۰۰,۰۰۰ ریال", timestamp = System.currentTimeMillis())

        val urgentItem = AIInboxManager.classifyMessageToCategory(urgentMsg)
        val finItem = AIInboxManager.classifyMessageToCategory(finMsg)

        assertEquals(AIInboxCategory.CRITICAL, urgentItem.category)
        assertEquals(AIInboxCategory.FINANCE, finItem.category)
    }

    @Test
    fun testBusinessAgentEngineMetrics() {
        val messages = listOf(
            MessageEntity(id = 1, address = "CUST1", body = "قیمت سفارش ما چقدر می‌شود؟", type = 1, timestamp = System.currentTimeMillis()),
            MessageEntity(id = 2, address = "CUST2", body = "کاتالوگ جدید محصولات را ارسال کنید", type = 1, timestamp = System.currentTimeMillis())
        )

        val metrics = BusinessAgentEngine.computeEnterpriseMetrics(messages)
        assertEquals(2, metrics.pendingRepliesCount)
        assertEquals(2, metrics.salesOpportunitiesCount)
        assertTrue(metrics.totalCustomerAttentionScore >= 70)
    }

    @Test
    fun testVoiceIntentRouterCommands() {
        val r1 = VoiceIntentRouter.routeSpokenCommand("پیامهای مهم را بررسی کن")
        assertEquals(VoiceAgentCommandType.CHECK_IMPORTANT_MESSAGES, r1.commandType)

        val r2 = VoiceIntentRouter.routeSpokenCommand("چه جوابهایی پیشنهاد میکنی؟")
        assertEquals(VoiceAgentCommandType.SUGGEST_REPLIES, r2.commandType)

        val r3 = VoiceIntentRouter.routeSpokenCommand("کارهای عقب افتاده را نشان بده")
        assertEquals(VoiceAgentCommandType.SHOW_OVERDUE_TASKS, r3.commandType)

        val r4 = VoiceIntentRouter.routeSpokenCommand("پیامهای مشتریان را بررسی کن")
        assertEquals(VoiceAgentCommandType.CHECK_CUSTOMER_MESSAGES, r4.commandType)
    }
}
