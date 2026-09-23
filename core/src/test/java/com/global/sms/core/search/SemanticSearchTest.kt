package com.global.sms.core.search

import com.global.sms.data.entity.MessageCategory
import com.global.sms.data.entity.MessageEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SemanticSearchTest {

    private val engine = DefaultSemanticSearchEngine()

    @Test
    fun synonymQueryRanksBankMessageAbovePersonal() {
        val now = System.currentTimeMillis()
        val messages = listOf(
            MessageEntity(
                id = 1,
                threadId = 1,
                address = "MELLAT",
                body = "واریز مبلغ 250000 تومان به حساب شما انجام شد",
                category = MessageCategory.BANK,
                timestamp = now
            ),
            MessageEntity(
                id = 2,
                threadId = 2,
                address = "09120000000",
                body = "سلام خوبی؟ فردا میای؟",
                category = MessageCategory.PERSONAL,
                timestamp = now
            )
        )

        val hits = engine.rankCorpus("تراکنش بانکی", messages, limit = 10)
        assertTrue(hits.isNotEmpty())
        assertEquals(1L, hits.first().message.id)
        assertTrue(hits.first().score > (hits.getOrNull(1)?.score ?: 0.0))
    }

    @Test
    fun otpSynonymsMatchVerificationCode() {
        val now = System.currentTimeMillis()
        val messages = listOf(
            MessageEntity(
                id = 10,
                address = "Digikala",
                body = "کد ورود شما 882134 است",
                category = MessageCategory.OTP,
                otpCode = "882134",
                timestamp = now
            ),
            MessageEntity(
                id = 11,
                address = "Friend",
                body = "کد پستی خانه را فرستادم",
                category = MessageCategory.PERSONAL,
                timestamp = now
            )
        )

        val hits = engine.rankCorpus("otp verification", messages)
        assertEquals(10L, hits.first().message.id)
    }

    @Test
    fun blankQueryReturnsEmpty() {
        val messages = listOf(
            MessageEntity(id = 1, address = "A", body = "hello")
        )
        assertTrue(engine.rankCorpus("   ", messages).isEmpty())
    }
}
