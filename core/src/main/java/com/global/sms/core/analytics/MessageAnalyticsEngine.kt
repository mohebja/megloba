package com.global.sms.core.analytics

import com.global.sms.data.entity.MessageCategory
import com.global.sms.data.entity.MessageEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar
import java.util.Locale

data class LocalAnalyticsSummary(
    val totalMessagesCount: Int = 0,
    val incomingMessagesCount: Int = 0,
    val outgoingMessagesCount: Int = 0,
    val totalSpamBlocked: Int = 0,
    val averageResponseTimeMinutes: Int = 0,
    val topSenderName: String = "-",
    val peakHourOfDay: String = "-",
    val hourlyDistribution: List<Pair<String, Int>> = listOf(
        Pair("۰-۴", 0),
        Pair("۴-۸", 0),
        Pair("۸-۱۰", 0),
        Pair("۱۰-۱۲", 0),
        Pair("۱۲-۱۴", 0),
        Pair("۱۴-۱۶", 0),
        Pair("۱۶-۱۸", 0),
        Pair("۱۸-۲۰", 0),
        Pair("۲۰-۲۲", 0),
        Pair("۲۲-۲۴", 0)
    )
)

class MessageAnalyticsEngine(initialMessages: List<MessageEntity> = emptyList()) {

    private val _summary = MutableStateFlow(calculateSummary(initialMessages))
    val summary: StateFlow<LocalAnalyticsSummary> = _summary.asStateFlow()

    fun updateMessages(messages: List<MessageEntity>): LocalAnalyticsSummary {
        val updated = calculateSummary(messages)
        _summary.value = updated
        return updated
    }

    companion object {
        fun calculateSummary(messages: List<MessageEntity>): LocalAnalyticsSummary {
            if (messages.isEmpty()) {
                return LocalAnalyticsSummary()
            }

            val totalCount = messages.size
            val incomingCount = messages.count { it.type == 1 }
            val outgoingCount = messages.count { it.type == 2 }
            val spamCount = messages.count { it.category == MessageCategory.SPAM }

            // Top sender based on incoming SMS
            val incomingList = messages.filter { it.type == 1 && it.address.isNotBlank() }
            val topSender = incomingList.groupBy { it.address }
                .maxByOrNull { it.value.size }?.key ?: "-"

            // Hourly distribution and Peak Hour
            val calendar = Calendar.getInstance()
            val hourCounts = IntArray(24)
            for (msg in messages) {
                if (msg.timestamp > 0) {
                    calendar.timeInMillis = msg.timestamp
                    val hour = calendar.get(Calendar.HOUR_OF_DAY)
                    if (hour in 0..23) {
                        hourCounts[hour]++
                    }
                }
            }

            val maxHour = hourCounts.indices.maxByOrNull { hourCounts[it] } ?: 0
            val peakHour = if (totalCount > 0 && hourCounts[maxHour] > 0) {
                String.format(Locale.US, "%02d:00 - %02d:00", maxHour, (maxHour + 1) % 24)
            } else {
                "-"
            }

            val hourlyDistribution = listOf(
                Pair("۰-۴", hourCounts[0] + hourCounts[1] + hourCounts[2] + hourCounts[3]),
                Pair("۴-۸", hourCounts[4] + hourCounts[5] + hourCounts[6] + hourCounts[7]),
                Pair("۸-۱۰", hourCounts[8] + hourCounts[9]),
                Pair("۱۰-۱۲", hourCounts[10] + hourCounts[11]),
                Pair("۱۲-۱۴", hourCounts[12] + hourCounts[13]),
                Pair("۱۴-۱۶", hourCounts[14] + hourCounts[15]),
                Pair("۱۶-۱۸", hourCounts[16] + hourCounts[17]),
                Pair("۱۸-۲۰", hourCounts[18] + hourCounts[19]),
                Pair("۲۰-۲۲", hourCounts[20] + hourCounts[21]),
                Pair("۲۲-۲۴", hourCounts[22] + hourCounts[23])
            )

            // Average response time in minutes
            var totalResponseDeltaMs = 0L
            var responsePairCount = 0
            val threadGroups = messages.filter { it.timestamp > 0 }.groupBy { it.threadId }
            for ((_, threadMsgs) in threadGroups) {
                val sorted = threadMsgs.sortedBy { it.timestamp }
                for (i in 0 until sorted.size - 1) {
                    val current = sorted[i]
                    val next = sorted[i + 1]
                    if (current.type == 1 && next.type == 2) {
                        val delta = next.timestamp - current.timestamp
                        if (delta in 1..(24 * 60 * 60 * 1000L)) {
                            totalResponseDeltaMs += delta
                            responsePairCount++
                        }
                    }
                }
            }

            val averageResponseMinutes = if (responsePairCount > 0) {
                ((totalResponseDeltaMs / responsePairCount) / (1000 * 60)).toInt()
            } else {
                0
            }

            return LocalAnalyticsSummary(
                totalMessagesCount = totalCount,
                incomingMessagesCount = incomingCount,
                outgoingMessagesCount = outgoingCount,
                totalSpamBlocked = spamCount,
                averageResponseTimeMinutes = averageResponseMinutes,
                topSenderName = topSender,
                peakHourOfDay = peakHour,
                hourlyDistribution = hourlyDistribution
            )
        }
    }
}
