package com.global.sms

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.global.sms.core.ai.brain.AiQueryEngine
import com.global.sms.data.db.GlobalSmsDatabase
import com.global.sms.data.entity.MessageCategory
import com.global.sms.data.entity.MessageEntity
import com.global.sms.data.entity.OtpEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AiChatAssistantRealDataTest {

    private lateinit var context: Context
    private lateinit var database: GlobalSmsDatabase

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        database = GlobalSmsDatabase.getInstance(context)
        runBlocking {
            database.otpDao().clearAllOtps()
            database.messageDao().deleteAllSpamMessages()
        }
    }

    @After
    fun tearDown() {
        runBlocking {
            database.otpDao().clearAllOtps()
            database.messageDao().deleteAllSpamMessages()
        }
    }

    @Test
    fun `otp query returns honest empty state when no OTP exists`() = runBlocking {
        val response = AiQueryEngine.processUserQuery(context, "کد تایید من چیست؟")
        assertTrue(response.contains("هیچ کد تایید (OTP) فعالی در سیستم ثبت نشده است"))
        assertFalse(response.contains("۱۲۴۵۷۸"))
    }

    @Test
    fun `otp query returns real inserted OTP data`() = runBlocking {
        val testOtp = OtpEntity(
            messageId = 101L,
            code = "987654",
            serviceName = "Digikala",
            address = "Digikala",
            receivedTimestamp = System.currentTimeMillis(),
            expiresTimestamp = System.currentTimeMillis() + 120_000L,
            isUsed = false
        )
        database.otpDao().insertOtp(testOtp)

        val response = AiQueryEngine.processUserQuery(context, "کد تایید OTP من رو بگو")
        assertTrue(response.contains("987654"))
        assertTrue(response.contains("Digikala"))
        assertFalse(response.contains("۱۲۴۵۷۸"))
    }

    @Test
    fun `bank query returns real inserted bank transactions`() = runBlocking {
        val bankMsg = MessageEntity(
            id = 202L,
            address = "MELLAT",
            body = "بانک ملت\nبرداشت: 850,000 ریال\nمانده: 12,500,000 ریال",
            category = MessageCategory.BANK,
            timestamp = System.currentTimeMillis()
        )
        database.messageDao().insertMessage(bankMsg)

        val response = AiQueryEngine.processUserQuery(context, "تراکنش‌های بانکی من چطوره؟")
        assertTrue(response.contains("بانک ملت"))
        assertTrue(response.contains("برداشت"))
        assertFalse(response.contains("۵۰۰,۰۰۰ ریال"))
    }

    @Test
    fun `security query reflects actual spam count and sender`() = runBlocking {
        val spamMsg = MessageEntity(
            id = 303L,
            address = "+989991234567",
            body = "برنده جایزه ۱۰۰ میلیونی شدید! وارد لینک فیشینگ شوید",
            category = MessageCategory.SPAM,
            timestamp = System.currentTimeMillis()
        )
        database.messageDao().insertMessage(spamMsg)

        val response = AiQueryEngine.processUserQuery(context, "آیا پیام اسپم یا مشکوک دارم؟")
        assertTrue(response.contains("هشدار امنیتی"))
        assertTrue(response.contains("+989991234567"))
        assertFalse(response.contains("وضعیت امنیتی سبز است"))
    }

    @Test
    fun `fallback search query returns real matched message text and sender`() = runBlocking {
        val aliMsg = MessageEntity(
            id = 404L,
            address = "AliReza",
            body = "سلام پروژه نهایی تحویل داده شد و مدارک ارسال گردید",
            category = MessageCategory.PERSONAL,
            timestamp = System.currentTimeMillis()
        )
        database.messageDao().insertMessage(aliMsg)

        val response = AiQueryEngine.processUserQuery(context, "آیا پیامی درباره مدارک دارم؟")
        assertTrue(response.contains("AliReza"))
        assertTrue(response.contains("مدارک ارسال گردید"))
        assertFalse(response.contains("خلاصه: آیا پیامی درباره مدارک دارم؟"))
    }

    @Test
    fun `fallback search query returns honest empty state when no message matches`() = runBlocking {
        val response = AiQueryEngine.processUserQuery(context, "پیام‌های مربوط به بیمه سینا چیست؟")
        assertTrue(response.contains("هیچ پیام یا سابقه مرتبطی با «پیام‌های مربوط به بیمه سینا چیست؟» در پیامک‌های دستگاه یافت نشد"))
        assertFalse(response.contains("خلاصه: پیام‌های مربوط به بیمه سینا چیست؟"))
    }

    @Test
    fun `automation intent parser accurately extracts triggers and actions`() {
        val blockRule = com.global.sms.core.automation.AutomationIntentParser.parsePromptToRule(
            "اگر پیامک حاوی 'تخفیف' بود مسدود کن"
        )
        org.junit.Assert.assertEquals(com.global.sms.core.automation.AutomationTriggerType.BODY_CONTAINS, blockRule.triggerType)
        org.junit.Assert.assertEquals("تخفیف", blockRule.triggerValue)
        org.junit.Assert.assertEquals(com.global.sms.core.automation.AutomationActionType.BLOCK_SENDER, blockRule.actionType)

        val bankRule = com.global.sms.core.automation.AutomationIntentParser.parsePromptToRule(
            "تراکنش‌های واریز بانک را در امور مالی ثبت کن"
        )
        org.junit.Assert.assertEquals(com.global.sms.core.automation.AutomationTriggerType.CATEGORY_IS, bankRule.triggerType)
        org.junit.Assert.assertEquals("BANKING", bankRule.triggerValue)
        org.junit.Assert.assertEquals(com.global.sms.core.automation.AutomationActionType.CREATE_FINANCE_RECORD, bankRule.actionType)

        val forwardRule = com.global.sms.core.automation.AutomationIntentParser.parsePromptToRule(
            "پیامک‌های حاوی 'فوری' را به شماره 09123456789 ارسال کن"
        )
        org.junit.Assert.assertEquals(com.global.sms.core.automation.AutomationTriggerType.BODY_CONTAINS, forwardRule.triggerType)
        org.junit.Assert.assertEquals("فوری", forwardRule.triggerValue)
        org.junit.Assert.assertEquals(com.global.sms.core.automation.AutomationActionType.FORWARD_SMS, forwardRule.actionType)
        org.junit.Assert.assertEquals("09123456789", forwardRule.actionValue)
    }
}
