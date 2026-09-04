package com.global.sms.contact

import com.global.sms.core.contact.ContactInfo
import com.global.sms.core.contact.PersianContactUtils
import com.global.sms.core.contact.PhoneNumberNormalizer
import org.junit.Assert.*
import org.junit.Test

class ContactSearchTest {

    @Test
    fun testPhoneNumberNormalizer_persianDigitsConversion() {
        val persianInput = "۰۹۱۲۳۴۵۶۷۸۹"
        val normalized = PhoneNumberNormalizer.normalize(persianInput)
        assertEquals("09123456789", normalized)
    }

    @Test
    fun testPhoneNumberNormalizer_noiseAndUnicodeFormatting() {
        val messyNumber = "+98 (912) 345-6789 \u200E"
        val normalized = PhoneNumberNormalizer.normalize(messyNumber)
        assertEquals("+989123456789", normalized)
    }

    @Test
    fun testPhoneNumberNormalizer_numberEquality() {
        val num1 = "+989123456789"
        val num2 = "09123456789"
        val num3 = "00989123456789"

        assertTrue(PhoneNumberNormalizer.areNumbersEqual(num1, num2))
        assertTrue(PhoneNumberNormalizer.areNumbersEqual(num2, num3))
        assertTrue(PhoneNumberNormalizer.areNumbersEqual(num1, num3))
    }

    @Test
    fun testPersianContactUtils_arabicToPersianNormalization() {
        val arabicText = "علي كاظمی"
        val normalized = PersianContactUtils.normalizePersianText(arabicText)
        assertEquals("علی کاظمی", normalized)
    }

    @Test
    fun testPersianContactUtils_searchMatching() {
        val contactName = "محمدحسین رضایی"
        
        // Search with Arabic Yeh & Kaf
        assertTrue(PersianContactUtils.matchesQuery(contactName, "رضايی"))
        
        // Search with Persian digits
        assertTrue(PersianContactUtils.matchesQuery("۰۹۱۲۳۴۵۶۷۸۹", "0912345"))
    }

    @Test
    fun testPersianContactUtils_alphabeticalSorting() {
        val names = listOf("رضا", "آرش", "علی", "بهرام")
        val sorted = names.sortedWith(PersianContactUtils.persianNameComparator)
        
        assertEquals(listOf("آرش", "بهرام", "رضا", "علی"), sorted)
    }
}
