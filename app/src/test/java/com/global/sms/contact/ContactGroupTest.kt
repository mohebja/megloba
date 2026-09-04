package com.global.sms.contact

import com.global.sms.data.entity.ContactGroupEntity
import com.global.sms.data.entity.ContactGroupMemberEntity
import org.junit.Assert.*
import org.junit.Test

class ContactGroupTest {

    @Test
    fun testContactGroupEntityCreation() {
        val group = ContactGroupEntity(
            id = 1,
            name = "همکاران",
            description = "گروهکاری شرکت",
            color = 0xFF1A73E8,
            members = "09121111111, 09122222222"
        )

        assertEquals("همکاران", group.name)
        assertEquals(1L, group.id)
        val memberList = group.members.split(",").map { it.trim() }
        assertEquals(2, memberList.size)
        assertTrue(memberList.contains("09121111111"))
    }

    @Test
    fun testContactGroupMemberEntity() {
        val member = ContactGroupMemberEntity(groupId = 10, contactId = 100)
        assertEquals(10L, member.groupId)
        assertEquals(100L, member.contactId)
    }
}
