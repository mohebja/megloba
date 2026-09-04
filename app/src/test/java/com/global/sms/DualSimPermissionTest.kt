package com.global.sms

import android.Manifest
import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.global.sms.core.sim.DualSimManager
import com.global.sms.core.sim.DualSimResult
import com.global.sms.core.sim.SimPermissionManager
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DualSimPermissionTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun testSimPermissionManager_StateAndMessage() {
        val permissionState = SimPermissionManager.getPermissionState(context)
        assertNotNull("PermissionState should not be null", permissionState)
        assertEquals(Manifest.permission.READ_PHONE_STATE, permissionState.permissionName)
        assertNotNull("Status message should be populated", permissionState.statusMessage)
        assertTrue("Status message should mention READ_PHONE_STATE", permissionState.statusMessage.contains("READ_PHONE_STATE"))
    }

    @Test
    fun testDualSimManager_WhenPermissionDenied_ReturnsEmptyListWithoutCrash() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val shadowApp = shadowOf(app)
        shadowApp.denyPermissions(Manifest.permission.READ_PHONE_STATE)

        assertFalse("Permission should be denied", SimPermissionManager.hasReadPhoneStatePermission(context))

        val activeSims = DualSimManager.getActiveSimCards(context)
        assertNotNull("Returned list must not be null", activeSims)
        assertTrue("When permission is denied, SIM list must be empty", activeSims.isEmpty())
    }

    @Test
    fun testDualSimManager_GetActiveSimCardsDetailed_HandlesDeniedPermission() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val shadowApp = shadowOf(app)
        shadowApp.denyPermissions(Manifest.permission.READ_PHONE_STATE)

        val result = DualSimManager.getActiveSimCardsDetailed(context)
        assertTrue("Result should be PermissionDenied", result is DualSimResult.PermissionDenied)
        val deniedResult = result as DualSimResult.PermissionDenied
        assertTrue("Denied message should describe missing permission", deniedResult.message.contains("READ_PHONE_STATE"))
    }

    @Test
    fun testDualSimManager_UserStatusMessage_ReturnsHumanReadableMessage() {
        val statusMessage = DualSimManager.getSimStatusUserMessage(context)
        assertNotNull("Status message must not be null", statusMessage)
        assertFalse("Status message must not be blank", statusMessage.isBlank())
    }

    @Test
    fun testDualSimManager_GetSmsManagerForSubId_ReturnsNonNullSmsManager() {
        val smsManagerValid = DualSimManager.getSmsManagerForSubId(context, 1)
        assertNotNull("SmsManager for subId 1 should not be null", smsManagerValid)

        val smsManagerDefault = DualSimManager.getSmsManagerForSubId(context, -1)
        assertNotNull("SmsManager for default subId (-1) should not be null", smsManagerDefault)
    }

    @Test
    fun testDualSimManager_WhenPermissionGranted_ExecutesSafely() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val shadowApp = shadowOf(app)
        shadowApp.grantPermissions(Manifest.permission.READ_PHONE_STATE)

        assertTrue("Permission should be granted", SimPermissionManager.hasReadPhoneStatePermission(context))

        val result = DualSimManager.getActiveSimCardsDetailed(context)
        assertNotNull("Detailed result should not be null", result)
        assertTrue(
            "Result should be Success or Unavailable",
            result is DualSimResult.Success || result is DualSimResult.Unavailable
        )
    }
}

