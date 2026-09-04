package com.global.sms.core.search

import com.global.sms.data.entity.MessageCategory
import com.global.sms.data.entity.MessageEntity
import org.junit.Assert.*
import org.junit.Test

class FilterEngineTest {

    private val filterEngine = SearchFilterEngine()

    @Test
    fun testCategoryFiltering() {
        val messages = listOf(
            MessageEntity(id = 1, threadId = 1, address = "MELLAT", body = "واریز 10000", category = MessageCategory.BANK),
            MessageEntity(id = 2, threadId = 2, address = "983000", body = "کد 1234", category = MessageCategory.OTP),
            MessageEntity(id = 3, threadId = 3, address = "Friend", body = "سلام چطوری", category = MessageCategory.PERSONAL)
        )

        val criteria = SearchFilterCriteria(categories = setOf(MessageCategory.BANK))
        val filtered = filterEngine.filterMessages(messages, criteria)

        assertEquals(1, filtered.size)
        assertEquals("MELLAT", filtered.first().address)
    }

    @Test
    fun testHiddenVaultSafeguard() {
        val messages = listOf(
            MessageEntity(id = 1, threadId = 1, address = "Secret", body = "Private data", isHidden = true),
            MessageEntity(id = 2, threadId = 2, address = "Public", body = "Public data", isHidden = false)
        )

        val normalSearch = SearchFilterCriteria(query = "data", isHiddenOnly = false)
        val normalResults = filterEngine.filterMessages(messages, normalSearch)
        assertEquals(1, normalResults.size)
        assertEquals("Public", normalResults.first().address)

        val vaultSearch = SearchFilterCriteria(query = "data", isHiddenOnly = true)
        val vaultResults = filterEngine.filterMessages(messages, vaultSearch)
        assertEquals(1, vaultResults.size)
        assertEquals("Secret", vaultResults.first().address)
    }
}
