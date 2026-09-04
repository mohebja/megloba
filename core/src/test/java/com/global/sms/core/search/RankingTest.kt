package com.global.sms.core.search

import com.global.sms.data.entity.MessageCategory
import com.global.sms.data.entity.MessageEntity
import org.junit.Assert.*
import org.junit.Test

class RankingTest {

    private val rankingEngine = SearchRankingEngine()

    @Test
    fun testRelevancyRanking() {
        val messages = listOf(
            MessageEntity(id = 1, threadId = 1, address = "Bank", body = "پرداخت صورتحساب متفرقه"),
            MessageEntity(id = 2, threadId = 2, address = "Bank", body = "پرداخت قبوض بانک ملت"),
            MessageEntity(id = 3, threadId = 3, address = "Bank", body = "پرداخت")
        )

        val query = "پرداخت"
        val ranked = rankingEngine.rankResults(messages, query)

        assertEquals(3, ranked.size)
        // Exact match "پرداخت" should receive highest score
        assertEquals(3L, ranked.first().message.id)
    }
}
