package com.global.sms.core.search

import com.global.sms.data.entity.MessageFtsEntity
import org.junit.Assert.*
import org.junit.Test

class FTSTest {

    @Test
    fun testFtsEntityMapping() {
        val fts = MessageFtsEntity(
            rowid = 101L,
            body = "تست جستجوی سریع متن در دیتابیس FTS5",
            address = "MELLAT"
        )

        assertEquals(101L, fts.rowid)
        assertTrue(fts.body.contains("FTS5"))
        assertEquals("MELLAT", fts.address)
    }
}
