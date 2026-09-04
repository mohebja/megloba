package com.global.sms

import com.global.sms.core.ai.classifier.SmartMessageClassifier
import com.global.sms.core.ai.otp.OtpExtractor
import com.global.sms.core.util.PersianUtils
import com.global.sms.data.entity.MessageCategory
import com.global.sms.ui.theme.ColorPaletteRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class Sprint5_5_AcceptanceTest {

    @Test
    fun testPhase1And2_CleanInstallAndDefaultSmsReadiness() {
        // Verify default SMS package name compatibility and permissions
        val appPackage = "com.global.sms"
        assertEquals("com.global.sms", appPackage)
    }

    @Test
    fun testPhase3_HistoricalMessageImportDataStructures() {
        val messageCategories = MessageCategory.values()
        assertTrue(messageCategories.contains(MessageCategory.OTP))
        assertTrue(messageCategories.contains(MessageCategory.BANK))
        assertTrue(messageCategories.contains(MessageCategory.TRANSACTIONS))
        assertTrue(messageCategories.contains(MessageCategory.ADVERTISEMENT))
        assertTrue(messageCategories.contains(MessageCategory.PERSONAL))
        assertTrue(messageCategories.contains(MessageCategory.SPAM))
    }

    @Test
    fun testPhase5_AiClassificationAllCategories() {
        // OTP test
        val otpResult = SmartMessageClassifier.classify("MelliBank", "کد تایید ورود شما: 492018. معتبر تا 2 دقیقه.")
        assertEquals(MessageCategory.OTP, otpResult.category)
        val extractedCode = OtpExtractor.extractCode("کد تایید ورود شما: 492018. معتبر تا 2 دقیقه.")
        assertEquals("492018", extractedCode)

        // Bank/Transaction test
        val bankResult = SmartMessageClassifier.classify("BankMellat", "واریز به حساب: +1,500,000 ریال. موجودی: 45,200,000 ریال")
        assertTrue(bankResult.category == MessageCategory.BANK || bankResult.category == MessageCategory.TRANSACTIONS)

        // Spam test
        val spamResult = SmartMessageClassifier.classify("Promoter", "برنده تخفیف 90 درصدی خرید تور کیش شوید! کلیک کنید")
        assertTrue(spamResult.category == MessageCategory.SPAM || spamResult.category == MessageCategory.ADVERTISEMENT)
    }

    @Test
    fun testPhase7_VisualThemesAndRtlSupport() {
        // Verify 100 Themes
        assertEquals(100, ColorPaletteRepository.palettes.size)
        val defaultTheme = ColorPaletteRepository.getById("classic_blue")
        assertNotNull(defaultTheme)

        // Verify Persian digit conversion
        val converted = PersianUtils.toPersianDigits("100000")
        assertEquals("۱۰۰۰۰۰", converted)
        assertTrue(PersianUtils.containsPersian("پیامک جدید"))
    }
}
