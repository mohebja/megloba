package com.global.sms.core.search

import com.global.sms.data.entity.MessageCategory
import org.junit.Assert.*
import org.junit.Test

class SearchParserTest {

    @Test
    fun testTextNormalization() {
        val input = "كد ورود به حساب ۱۲۳۴۵۶"
        val normalized = SearchQueryParser.normalizeText(input)
        assertEquals("کد ورود به حساب 123456", normalized)
    }

    @Test
    fun testCategoryIntentDetection() {
        val bankQuery = "پیام های واریز بانک ملت"
        val parsedBank = SearchQueryParser.parse(bankQuery)
        assertEquals(MessageCategory.BANK, parsedBank.detectedCategory)

        val otpQuery = "رمز پویا صادرات"
        val parsedOtp = SearchQueryParser.parse(otpQuery)
        assertEquals(MessageCategory.OTP, parsedOtp.detectedCategory)
    }

    @Test
    fun testDateIntentDetection() {
        val query = "پیام های تراکنش امروز"
        val parsed = SearchQueryParser.parse(query)
        assertNotNull(parsed.startDate)
        assertNotNull(parsed.endDate)
    }

    @Test
    fun testPhoneNumberParsing() {
        val query = "پیام های 09121234567"
        val parsed = SearchQueryParser.parse(query)
        assertEquals("09121234567", parsed.phoneNumber)
    }
}
