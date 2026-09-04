package com.global.sms.core.analytics

import com.global.sms.data.entity.MessageCategory
import com.global.sms.data.entity.MessageEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class MessageAnalyticsEngineTest {

    @Test
    fun testEmptyMessagesReturnsZeroMetrics() {
        val summary = MessageAnalyticsEngine.calculateSummary(emptyList())

        assertEquals(0, summary.totalMessagesCount)
        assertEquals(0, summary.incomingMessagesCount)
        assertEquals(0, summary.outgoingMessagesCount)
        assertEquals(0, summary.totalSpamBlocked)
        assertEquals(0, summary.averageResponseTimeMinutes)
        assertEquals("-", summary.topSenderName)
        assertEquals("-", summary.peakHourOfDay)
        assertTrue(summary.hourlyDistribution.all { it.second == 0 })
    }

    @Test
    fun testMessageCountsAndSpamCalculation() {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 14)
        cal.set(Calendar.MINUTE, 30)
        val time1 = cal.timeInMillis

        cal.set(Calendar.HOUR_OF_DAY, 14)
        cal.set(Calendar.MINUTE, 45)
        val time2 = cal.timeInMillis

        val messages = listOf(
            MessageEntity(
                id = 1L,
                threadId = 10L,
                address = "+989123456789",
                body = "سلام",
                timestamp = time1,
                type = 1, // Incoming
                category = MessageCategory.PERSONAL
            ),
            MessageEntity(
                id = 2L,
                threadId = 10L,
                address = "+989123456789",
                body = "علیک سلام",
                timestamp = time2,
                type = 2, // Outgoing (Reply 15 minutes later)
                category = MessageCategory.PERSONAL
            ),
            MessageEntity(
                id = 3L,
                threadId = 11L,
                address = "10009999",
                body = "برنده جایزه شدید",
                timestamp = time1,
                type = 1, // Incoming
                category = MessageCategory.SPAM
            )
        )

        val summary = MessageAnalyticsEngine.calculateSummary(messages)

        assertEquals(3, summary.totalMessagesCount)
        assertEquals(2, summary.incomingMessagesCount)
        assertEquals(1, summary.outgoingMessagesCount)
        assertEquals(1, summary.totalSpamBlocked)
        assertEquals(15, summary.averageResponseTimeMinutes)
        assertEquals("14:00 - 15:00", summary.peakHourOfDay)

        // 14:00 bucket is "۱۴-۱۶"
        val bucket14to16 = summary.hourlyDistribution.find { it.first == "۱۴-۱۶" }
        assertEquals(3, bucket14to16?.second)
    }
}
