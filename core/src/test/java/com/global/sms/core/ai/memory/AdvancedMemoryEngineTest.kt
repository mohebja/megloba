package com.global.sms.core.ai.memory

import com.global.sms.core.ai.privacy.AIPrivacyController
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdvancedMemoryEngineTest {

    @Test
    fun testInitialMemoriesAreEmpty() {
        val engine = AdvancedMemoryEngine()
        assertTrue("Initial memory engine must not have hardcoded fake data", engine.memories.value.isEmpty())
    }

    @Test
    fun testStoreAndRetrieveMemory() {
        val engine = AdvancedMemoryEngine()
        val record = engine.storeMemory(
            type = MemoryType.USER_PREFERENCE,
            subjectKey = "language",
            content = "فارسی",
            importanceScore = 0.9f
        )

        assertEquals(1, engine.memories.value.size)
        assertEquals("فارسی", engine.memories.value.first().memoryContent)
        assertEquals("language", engine.memories.value.first().subjectKey)
    }

    @Test
    fun testUpdateAndDeleteMemory() {
        val engine = AdvancedMemoryEngine()
        val record = engine.storeMemory(
            type = MemoryType.CONTACT_RELATIONSHIP,
            subjectKey = "09121111111",
            content = "همکار پروژه",
            importanceScore = 0.8f
        )

        engine.updateMemory(record.memoryId, "مدیر پروژه ارتباطات")
        assertEquals("مدیر پروژه ارتباطات", engine.memories.value.first().memoryContent)

        engine.deleteMemory(record.memoryId)
        assertTrue(engine.memories.value.isEmpty())
    }

    @Test
    fun testPurgeExpiredMemories() {
        val engine = AdvancedMemoryEngine()
        // Storing expired memory
        engine.storeMemory(
            type = MemoryType.SHORT_TERM,
            subjectKey = "otp_context",
            content = "کد ورود",
            ttlMinutes = -1L // Expired 1 minute ago
        )
        // Storing valid memory
        engine.storeMemory(
            type = MemoryType.LONG_TERM,
            subjectKey = "banking",
            content = "حساب بانکی",
            ttlMinutes = 60L
        )

        assertEquals(2, engine.memories.value.size)
        val purged = engine.purgeExpiredMemories()
        assertEquals(1, purged)
        assertEquals(1, engine.memories.value.size)
        assertEquals("banking", engine.memories.value.first().subjectKey)
    }

    @Test
    fun testResetAllMemories() {
        val engine = AdvancedMemoryEngine()
        engine.storeMemory(type = MemoryType.USER_PREFERENCE, subjectKey = "k1", content = "v1")
        engine.storeMemory(type = MemoryType.USER_PREFERENCE, subjectKey = "k2", content = "v2")
        assertEquals(2, engine.memories.value.size)

        engine.resetAllAiMemories()
        assertTrue(engine.memories.value.isEmpty())
    }

    @Test
    fun testAIPrivacyControllerSettings() {
        val controller = AIPrivacyController()
        assertEquals(30, controller.settings.value.memoryRetentionDays)
        assertTrue(controller.settings.value.autoRemoveOtpFacts)

        controller.setRetentionPeriod(45)
        assertEquals(45, controller.settings.value.memoryRetentionDays)

        controller.setAutoRemoveOtpFacts(false)
        assertFalse(controller.settings.value.autoRemoveOtpFacts)

        controller.toggleExcludeAddress("09123456789")
        assertFalse(controller.isAddressAllowedForAi("09123456789"))

        controller.toggleExcludeAddress("09123456789")
        assertTrue(controller.isAddressAllowedForAi("09123456789"))
    }
}
