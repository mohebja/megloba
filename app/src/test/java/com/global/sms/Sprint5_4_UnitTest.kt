package com.global.sms

import com.global.sms.core.ai.classifier.SmartMessageClassifier
import com.global.sms.core.ai.otp.OtpExtractor
import com.global.sms.core.util.PersianUtils
import com.global.sms.data.entity.MessageCategory
import com.global.sms.ui.theme.ColorPaletteRepository
import org.junit.Assert.assertEquals
import org.junit.Test

class Sprint5_4_UnitTest {

    @Test
    fun testAiMessageClassificationEngine() {
        val otpResult = SmartMessageClassifier.classify("HamrahAvval", "کد تایید ورود شما: 983104. معتبر تا 3 دقیقه.")
        assertEquals(MessageCategory.OTP, otpResult.category)

        val code = OtpExtractor.extractCode("کد تایید ورود شما: 983104. معتبر تا 3 دقیقه.")
        assertEquals("983104", code)
    }

    @Test
    fun testPersianNumeralsAndRtlUtils() {
        val formatted = PersianUtils.toPersianDigits("1234567890")
        assertEquals("۱۲۳۴۵۶۷۸۹۰", formatted)

        val contains = PersianUtils.containsPersian("سلام")
        assertEquals(true, contains)
    }

    @Test
    fun testThemeRepository100Palettes() {
        assertEquals(100, ColorPaletteRepository.palettes.size)
        val classicBlue = ColorPaletteRepository.getById("classic_blue")
        assertEquals("classic_blue", classicBlue.id)
    }
}
