package com.global.sms.contact

import com.global.sms.core.contact.ContactPermissionState
import org.junit.Assert.*
import org.junit.Test

class PermissionFlowTest {

    @Test
    fun testPermissionStateEnumValues() {
        val states = ContactPermissionState.values()
        assertTrue(states.contains(ContactPermissionState.NOT_REQUESTED))
        assertTrue(states.contains(ContactPermissionState.GRANTED))
        assertTrue(states.contains(ContactPermissionState.DENIED))
        assertTrue(states.contains(ContactPermissionState.PERMANENTLY_DENIED))
    }
}
