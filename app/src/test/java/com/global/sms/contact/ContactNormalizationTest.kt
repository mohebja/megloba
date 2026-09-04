package com.global.sms.contact

import com.global.sms.core.contact.PersianContactUtils
import com.global.sms.core.contact.PhoneNumberNormalizer
import org.junit.Assert.*
import org.junit.Test

class ContactNormalizationTest {

    @Test
    fun testPersianAndArabicDigitConversion() {
        val persianInput = "۰۹۱۲۳۴۵۶۷۸۹"
        val arabicInput = "٠٩١٢٣٤٥٦٧٨٩"
        val asciiInput = "09123456789"

        assertEquals(asciiInput, PhoneNumberNormalizer.convertDigitsToAscii(persianInput))
        assertEquals(asciiInput, PhoneNumberNormalizer.convertDigitsToAscii(arabicInput))
    }

    @Test
    fun testCharacterNormalization_KafAndYeh() {
        val inputWithArabicKafAndYeh = "كیوان امیری"
        val normalized = PersianContactUtils.normalizePersianText(inputWithArabicKafAndYeh)
        assertEquals("کیوان امیری", normalized)
    }

    @Test
    fun testPhoneNumberNormalizer_fullNormalization() {
        val input = " +۹۸ (۹۱۲) ۳۴۵-۶۷۸۹ "
        val normalized = PhoneNumberNormalizer.normalize(input)
        assertEquals("+989123456789", normalized)
    }

    @Test
    fun testPhoneNumberNormalizer_extractMatchableDigits() {
        val input = "+98-912-345-6789"
        val matchable = PhoneNumberNormalizer.extractMatchableDigits(input)
        assertEquals("123456789", matchable)
    }
}
