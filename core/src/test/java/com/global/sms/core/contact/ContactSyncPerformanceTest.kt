package com.global.sms.core.contact

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.system.measureTimeMillis

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ContactSyncPerformanceTest {

    private val persianFirstNames = listOf("علی", "محمد", "حسین", "رضا", "مهدی", "سارا", "مریم", "زهرا", "امیر", "فاطمه")
    private val persianLastNames = listOf("محمدی", "حسینی", "احمدی", "رضایی", "کاظمی", "کریمی", "موسوی", "حیدری")

    /**
     * Generates 10,000 synthetic contacts with Persian names, multiple phone numbers, and deliberate duplicates.
     */
    private fun generate10kContacts(): List<ContactInfo> {
        val contacts = ArrayList<ContactInfo>(10000)
        
        for (i in 1..10000) {
            val firstName = persianFirstNames[i % persianFirstNames.size]
            val lastName = persianLastNames[i % persianLastNames.size]
            val rawName = "$firstName $lastName $i"
            val persianFormattedName = PersianContactUtils.toPersianDigits(rawName)

            // Primary number with varied formats (Persian digits, Latin digits, E.164, local format)
            val baseNum = 9120000000L + (i % 8000) // Deliberate duplicate numbers for i >= 8001
            val rawNumber = if (i % 3 == 0) {
                "۰۹۱۲${(i % 8000).toString().padStart(7, '۰')}"
            } else if (i % 2 == 0) {
                "+98$baseNum"
            } else {
                "0$baseNum"
            }

            val normalizedNumber = PhoneNumberNormalizer.normalize(rawNumber)

            // Secondary alternate number for multiple phone numbers per contact
            val altNum = "+98935${(1000000 + i).toString().padStart(7, '0')}"

            contacts.add(
                ContactInfo(
                    id = "contact_$i",
                    name = persianFormattedName,
                    rawName = rawName,
                    phoneNumber = normalizedNumber,
                    normalizedNumber = normalizedNumber,
                    photoUri = if (i % 5 == 0) "content://com.android.contacts/display_photo/$i" else null,
                    lookupKey = "lookup_key_$i",
                    groupNames = if (i % 4 == 0) listOf("همکاران", "VIP") else listOf("دوستان"),
                    isDuplicate = false,
                    duplicateNumbers = listOf(altNum)
                )
            )
        }
        return contacts
    }

    @Test
    fun test10kContactsIndexingAndCachePerformance() {
        val contacts10k = generate10kContacts()
        assertEquals(10000, contacts10k.size)

        val cacheManager = ContactCacheManager.getInstance()
        cacheManager.invalidateAll()

        // Measure time to index 10,000 contacts into fast lookup maps
        val indexingTimeMs = measureTimeMillis {
            cacheManager.updateContacts(contacts10k)
        }

        println("BENCHMARK: 10,000 contacts indexed in $indexingTimeMs ms")
        assertTrue("Indexing 10,000 contacts should complete under 1000ms", indexingTimeMs < 1000)

        // Verify cached retrieve
        val cachedAll = cacheManager.getAllContacts()
        assertNotNull(cachedAll)
        assertEquals(10000, cachedAll?.size)

        // Measure O(1) phone lookup speed across 1,000 queries
        val lookupTimeMs = measureTimeMillis {
            for (k in 1..1000) {
                val targetNum = "+98912000${(k % 100).toString().padStart(4, '0')}"
                val resolved = cacheManager.lookupContact(targetNum)
                assertNotNull("Should resolve contact for number $targetNum", resolved)
            }
        }

        println("BENCHMARK: 1,000 O(1) lookups performed in $lookupTimeMs ms")
        assertTrue("1,000 O(1) lookups should complete under 100ms", lookupTimeMs < 100)
    }

    @Test
    fun testPersianNameSorting10kContacts() {
        val contacts10k = generate10kContacts()
        
        val sortTimeMs = measureTimeMillis {
            contacts10k.sortedWith { c1, c2 ->
                PersianContactUtils.persianNameComparator.compare(c1.name, c2.name)
            }
        }

        println("BENCHMARK: 10,000 Persian names sorted in $sortTimeMs ms")
        assertTrue("Persian sorting of 10,000 contacts should complete under 1500ms", sortTimeMs < 1500)
    }

    @Test
    fun testDuplicateGroupDetection10kContacts() {
        val contacts10k = generate10kContacts()

        var duplicateGroups: List<ContactDuplicateGroup>
        val dupDetectionTimeMs = measureTimeMillis {
            duplicateGroups = ContactManager.findDuplicateContactGroups(contacts10k)
        }

        println("BENCHMARK: Duplicate detection on 10,000 contacts completed in $dupDetectionTimeMs ms. Found ${duplicateGroups.size} duplicate groups.")
        assertTrue("Duplicate detection on 10,000 contacts should take under 1000ms", dupDetectionTimeMs < 1000)
        assertTrue("Should detect duplicate phone groups", duplicateGroups.isNotEmpty())
    }

    @Test
    fun testDiffCalculationOn10kContacts() {
        val original10k = generate10kContacts()
        val cacheManager = ContactCacheManager.getInstance()
        cacheManager.updateContacts(original10k)

        // Simulate contact modifications: 50 added, 30 removed, 100 name changed, 50 phone changed
        val modifiedList = ArrayList<ContactInfo>()

        // Keep first 9,970 (removes 30)
        for (i in 0 until 9970) {
            val contact = original10k[i]
            if (i < 100) {
                // Name changed
                modifiedList.add(contact.copy(name = "${contact.name} (ویرایش شده)"))
            } else if (i in 100..149) {
                // Phone changed
                modifiedList.add(contact.copy(phoneNumber = "+989999999999"))
            } else {
                modifiedList.add(contact)
            }
        }

        // Add 50 new contacts
        for (newIdx in 10001..10050) {
            modifiedList.add(
                ContactInfo(
                    id = "contact_$newIdx",
                    name = "مخاطب جدید $newIdx",
                    rawName = "New Contact $newIdx",
                    phoneNumber = "+98912999${newIdx % 1000}",
                    normalizedNumber = "+98912999${newIdx % 1000}"
                )
            )
        }

        val oldMap = original10k.associateBy { it.id }
        var addedCount = 0
        var removedCount = 0
        var updatedCount = 0
        var nameChangedCount = 0
        var phoneChangedCount = 0

        val diffTimeMs = measureTimeMillis {
            val newMap = modifiedList.associateBy { it.id }
            for (newContact in modifiedList) {
                val oldContact = oldMap[newContact.id]
                if (oldContact == null) {
                    addedCount++
                } else {
                    var isUpdated = false
                    if (oldContact.name != newContact.name) {
                        nameChangedCount++
                        isUpdated = true
                    }
                    if (oldContact.phoneNumber != newContact.phoneNumber) {
                        phoneChangedCount++
                        isUpdated = true
                    }
                    if (isUpdated) {
                        updatedCount++
                    }
                }
            }
            for (oldContact in original10k) {
                if (!newMap.containsKey(oldContact.id)) {
                    removedCount++
                }
            }
        }

        println("BENCHMARK: Diff calculation on 10,000 contacts took $diffTimeMs ms.")
        println("Diff Result -> Added: $addedCount, Removed: $removedCount, Name Changed: $nameChangedCount, Phone Changed: $phoneChangedCount")

        assertEquals(50, addedCount)
        assertEquals(30, removedCount)
        assertEquals(100, nameChangedCount)
        assertEquals(50, phoneChangedCount)
        assertTrue("Diff calculation should execute under 200ms", diffTimeMs < 200)
    }
}
