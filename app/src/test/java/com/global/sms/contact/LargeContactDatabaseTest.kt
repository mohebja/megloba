package com.global.sms.contact

import com.global.sms.core.contact.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class LargeContactDatabaseTest {

    private lateinit var cacheManager: ContactCacheManager
    private val largeContactList = mutableListOf<ContactInfo>()

    @Before
    fun setUp() {
        cacheManager = ContactCacheManager.getInstance()
        cacheManager.invalidateAll()

        // Generate 5,000 synthetic contacts
        val sampleNames = listOf("علي احمدی", "محمد رضایی", "سارا حسینی", "رضا کاظمی", "مریم بهرامی")
        for (i in 1..5000) {
            val baseName = sampleNames[i % sampleNames.size]
            val name = "$baseName $i"
            val phone = "0912" + String.format("%07d", i)
            val normalized = PhoneNumberNormalizer.normalize(phone)
            
            largeContactList.add(
                ContactInfo(
                    id = "contact_$i",
                    name = name,
                    rawName = name,
                    phoneNumber = normalized,
                    normalizedNumber = normalized,
                    lookupKey = "key_$i",
                    isDuplicate = (i % 100 == 0),
                    duplicateNumbers = if (i % 100 == 0) listOf("0935" + String.format("%07d", i)) else emptyList()
                )
            )
        }
    }

    @Test
    fun testLargeContactList_cacheUpdateAndFastLookupPerformance() {
        val startTime = System.currentTimeMillis()
        cacheManager.updateContacts(largeContactList)
        val updateTime = System.currentTimeMillis() - startTime

        // Cache update for 5,000 contacts should complete fast
        assertTrue("Cache update should take under 500ms, took $updateTime ms", updateTime < 500)

        // Test O(1) instant lookup for contact #2500
        val targetPhone = "0912" + String.format("%07d", 2500)
        
        val lookupStart = System.nanoTime()
        val foundContact = cacheManager.lookupContact(targetPhone)
        val lookupTimeMs = (System.nanoTime() - lookupStart) / 1000000.0

        assertNotNull(foundContact)
        assertEquals("contact_2500", foundContact?.id)
        assertTrue("O(1) lookup should take under 5ms, took $lookupTimeMs ms", lookupTimeMs < 5.0)
    }

    @Test
    fun testLargeContactList_searchResponseTime() {
        cacheManager.updateContacts(largeContactList)

        val query = "احمدی"
        val searchStart = System.currentTimeMillis()
        val results = largeContactList.filter { contact ->
            PersianContactUtils.matchesQuery(contact.name, query)
        }
        val searchTime = System.currentTimeMillis() - searchStart

        assertEquals(1000, results.size)
        assertTrue("Searching 5000 contacts should take under 100ms, took $searchTime ms", searchTime < 100)
    }

    @Test
    fun testLargeContactList_duplicateDetectionPerformance() {
        val duplicates = ContactManager.findDuplicateContactGroups(largeContactList)
        assertNotNull(duplicates)
    }

    @Test
    fun testLargeContactList_cacheInvalidationAndTrimMemory() {
        cacheManager.updateContacts(largeContactList)
        assertNotNull(cacheManager.getAllContacts())

        cacheManager.trimMemory()
        assertNotNull(cacheManager.getAllContacts())

        cacheManager.invalidateAll()
        assertNull(cacheManager.getAllContacts())
    }
}
