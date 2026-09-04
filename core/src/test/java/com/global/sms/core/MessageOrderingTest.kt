package com.global.sms.core

import com.global.sms.data.entity.MessageEntity
import com.global.sms.data.entity.MessageType
import org.junit.Assert.assertEquals
import org.junit.Test

class MessageOrderingTest {

    @Test
    fun testMessagesSortedDescendingForReverseLayout() {
        val now = System.currentTimeMillis()
        val m1 = MessageEntity(id = 1L, threadId = 100L, address = "1000", body = "Oldest message", timestamp = now - 10000, type = MessageType.INBOX.code)
        val m2 = MessageEntity(id = 2L, threadId = 100L, address = "1000", body = "Middle message", timestamp = now - 5000, type = MessageType.SENT.code)
        val m3 = MessageEntity(id = 3L, threadId = 100L, address = "1000", body = "Newest message", timestamp = now, type = MessageType.INBOX.code)

        val rawList = listOf(m1, m2, m3)
        val sortedDesc = rawList.sortedByDescending { it.timestamp }

        // In reverseLayout = true, index 0 is at bottom of LazyColumn (Newest message)
        assertEquals(3L, sortedDesc[0].id)
        assertEquals("Newest message", sortedDesc[0].body)
        assertEquals(1L, sortedDesc[2].id)
        assertEquals("Oldest message", sortedDesc[2].body)
    }
}
