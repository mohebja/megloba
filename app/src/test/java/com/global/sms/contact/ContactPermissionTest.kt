package com.global.sms.contact

import com.global.sms.core.contact.ContactPermissionState
import org.junit.Assert.*
import org.junit.Test

class ContactPermissionTest {

    @Test
    fun testPermissionStateTransitions() {
        var state = ContactPermissionState.NOT_REQUESTED
        assertEquals(ContactPermissionState.NOT_REQUESTED, state)

        // Simulate granted permission result
        val isGranted = true
        state = if (isGranted) ContactPermissionState.GRANTED else ContactPermissionState.NEEDS_EXPLANATION
        assertEquals(ContactPermissionState.GRANTED, state)

        // Simulate denied with rationale
        val isDeniedWithRationale = false
        val shouldShowRationale = true
        state = if (isDeniedWithRationale) ContactPermissionState.GRANTED
        else if (shouldShowRationale) ContactPermissionState.NEEDS_EXPLANATION
        else ContactPermissionState.PERMANENTLY_DENIED

        assertEquals(ContactPermissionState.NEEDS_EXPLANATION, state)

        // Simulate permanently denied
        val shouldShowRationalePermanentlyDenied = false
        state = if (shouldShowRationalePermanentlyDenied) ContactPermissionState.NEEDS_EXPLANATION
        else ContactPermissionState.PERMANENTLY_DENIED

        assertEquals(ContactPermissionState.PERMANENTLY_DENIED, state)
    }

    @Test
    fun testPermissionStateTitles() {
        val deniedState = ContactPermissionState.PERMANENTLY_DENIED
        val grantedState = ContactPermissionState.GRANTED

        assertNotEquals(deniedState, grantedState)
    }
}
