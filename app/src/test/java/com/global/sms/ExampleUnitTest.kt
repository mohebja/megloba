package com.global.sms

import com.global.sms.core.parser.BankTransactionParser
import com.global.sms.core.security.PhishingDetector
import com.global.sms.core.util.PersianUtils
import com.global.sms.core.util.SmsSegmenter
import com.global.sms.security.crypto.CryptoManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testPersianDigitsConversion() {
        val english = "1234567890"
        val persian = PersianUtils.toPersianDigits(english)
        assertEquals("۱۲۳۴۵۶۷۸۹۰", persian)
    }

    @Test
    fun testSmsSegmenterUnicode() {
        val persianText = "سلام، این یک پیامک آزمایشی به زبان فارسی است."
        val segmentInfo = SmsSegmenter.calculateSegments(persianText)
        assertTrue(segmentInfo.isUnicode)
        assertEquals(1, segmentInfo.segmentCount)
    }

    @Test
    fun testBankTransactionParser() {
        val smsBody = "بانک ملی: واریز مبلغ 500,000 ریال. رمز پویا: 849201"
        val analysis = BankTransactionParser.analyzeMessage("MELLI", smsBody)
        assertTrue(analysis.isBankMessage)
        assertEquals("849201", analysis.otpCode)
    }

    @Test
    fun testPhishingDetector() {
        val spamBody = "برنده ۵۰ میلیون تومان شدید! جهت دریافت لینک زیر را باز کنید: http://bit.ly/free-gift-xyz"
        val scan = PhishingDetector.scanMessage("09120000000", spamBody)
        assertTrue(scan.isSpamOrPhishing)
        assertNotNull(scan.warningReason)
    }

    @Test
    fun testCryptoManagerEncryptionDecryption() {
        val plainText = "Secret Vault Message"
        val pass = "MySecretPass123"
        val encrypted = CryptoManager.encrypt(plainText, pass)
        val decrypted = CryptoManager.decrypt(encrypted, pass)
        assertEquals(plainText, decrypted)
    }

    @Test
    fun testLongSmsSegmentation() {
        val longEnglishText = "A".repeat(300)
        val segmentInfo = SmsSegmenter.calculateSegments(longEnglishText)
        assertEquals(2, segmentInfo.segmentCount)
    }

}
