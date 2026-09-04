package com.global.sms

import com.global.sms.core.ai.brain.LanguageCode
import com.global.sms.core.ai.brain.LocalAIBrain
import com.global.sms.core.ai.copilot.ConversationUnderstandingEngine
import com.global.sms.core.ai.emotion.EmotionAnalysisEngine
import com.global.sms.core.ai.emotion.EmotionState
import com.global.sms.core.ai.llm.LocalLLMEngine
import com.global.sms.core.ai.search.SmartSearchEngine
import com.global.sms.core.ai.smartreply.ConversationContextCategory
import com.global.sms.core.ai.smartreply.ReplyTone
import com.global.sms.core.ai.smartreply.SmartReplyEngine
import com.global.sms.data.entity.MessageCategory
import com.global.sms.data.entity.MessageEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class Sprint6_3_RegressionTest {

    @Test
    fun testLocalAIBrainLanguageDetection() {
        val faLang = LocalAIBrain.detectLanguage("سلام چطوری پیامک خریدم")
        assertEquals(LanguageCode.PERSIAN, faLang)

        val enLang = LocalAIBrain.detectLanguage("Hello John how are you doing today?")
        assertEquals(LanguageCode.ENGLISH, enLang)
    }

    @Test
    fun testLocalLLMIntentAndSummarize() {
        val intent = LocalLLMEngine.detectIntent("مبلغ ۱,۵۰۰,۰۰۰ تومان واریز شد")
        assertEquals("FINANCIAL_TRANSACTION", intent)

        val summary = LocalLLMEngine.summarizeText("سلام. برای جلسه فردا هماهنگ باشیم. ساعت ۴ بعدازظهر مناسبه.")
        assertTrue(summary.contains("خلاصه گفتگو"))
    }

    @Test
    fun testConversationUnderstandingLongThread() {
        val messages = listOf(
            MessageEntity(id = 1, address = "09121111111", body = "سلام آقای دکتر درباره خودرو صحبت کنیم", timestamp = 1000L),
            MessageEntity(id = 2, address = "09121111111", body = "قبول توافق شد قیمت نهایی اوکی شد", timestamp = 2000L),
            MessageEntity(id = 3, address = "09121111111", body = "لطفاً بفرست پیگیری کن", timestamp = 3000L)
        )
        val deepSummary = ConversationUnderstandingEngine.analyzeLongConversation(messages)
        assertEquals(3, deepSummary.messageCountAnalyzed)
        assertTrue(deepSummary.topicSummary.contains("خودرو"))
    }

    @Test
    fun testSmartReplyV3ContextAndSafety() {
        val result = SmartReplyEngine.generateAdvancedRepliesV3("شماره کارت جهت واریز مبلغ بانک ملی")
        assertEquals(ConversationContextCategory.BANK, result.detectedContext)
        assertTrue(result.requiresUserConfirmation) // Safety rule enforced
        assertTrue(result.persianReplies.containsKey(ReplyTone.FORMAL))
    }

    @Test
    fun testEmotionAnalysisEngine() {
        val result = EmotionAnalysisEngine.analyzeMessage("این سرویس شما بسیار افتضاح است و شکایت دارم")
        assertEquals(EmotionState.ANGRY, result.primaryEmotion)
        assertTrue(result.priorityBoost >= 40)
    }

    @Test
    fun testSmartSearchEngineSemanticQuery() {
        val messages = listOf(
            MessageEntity(id = 10, address = "09120000000", body = "پیامک واریز وام بانک ملی", category = MessageCategory.BANK, timestamp = System.currentTimeMillis()),
            MessageEntity(id = 11, address = "09120000000", body = "سلام چطوری عیدت مبارک", category = MessageCategory.PERSONAL, timestamp = System.currentTimeMillis())
        )
        val searchResult = SmartSearchEngine.executeSemanticSearch("پیام بانک درباره وام", messages)
        assertEquals(1, searchResult.messages.size)
        assertEquals(10L, searchResult.messages.first().id)
    }
}
