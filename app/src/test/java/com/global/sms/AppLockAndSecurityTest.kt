package com.global.sms

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.global.sms.core.web.WebCompanionSyncManager
import com.global.sms.core.web.WebSessionState
import com.global.sms.security.crypto.CryptoManager
import com.global.sms.security.lock.AppLockManager
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AppLockAndSecurityTest {

    private lateinit var context: Context
    private lateinit var appLockManager: AppLockManager
    private lateinit var webCompanionSyncManager: WebCompanionSyncManager

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        appLockManager = AppLockManager(context)
        appLockManager.clearPin()
        webCompanionSyncManager = WebCompanionSyncManager(context)
    }

    @Test
    fun testAppLockBypassIsRemoved() {
        // Confirm that when no PIN is set, verifyPin("1234") returns false (NO BYPASS!)
        assertFalse("hasPinSet() should be false initially", appLockManager.hasPinSet())
        assertFalse("Magic '1234' bypass MUST return false when no PIN is set", appLockManager.verifyPin("1234"))
        assertFalse("Empty PIN MUST return false", appLockManager.verifyPin(""))
    }

    @Test
    fun testAppLockSetAndVerifyPinWithPbkdf2() {
        // Set real user PIN
        appLockManager.setPin("7890")
        assertTrue("hasPinSet() should be true after setting PIN", appLockManager.hasPinSet())

        // Verify correct PIN
        assertTrue("Correct PIN verification should succeed", appLockManager.verifyPin("7890"))

        // Verify incorrect PINs
        assertFalse("Old '1234' bypass must fail", appLockManager.verifyPin("1234"))
        assertFalse("Off-by-one PIN must fail", appLockManager.verifyPin("7891"))
        assertFalse("Empty PIN must fail", appLockManager.verifyPin(""))

        // Clear PIN and verify state
        appLockManager.clearPin()
        assertFalse("hasPinSet() should be false after clear", appLockManager.hasPinSet())
        assertFalse("Verification should fail after clear", appLockManager.verifyPin("7890"))
    }

    @Test
    fun testWebCompanionPairingAndProofOfPossession() {
        // Start web pairing session
        val qrPayload = webCompanionSyncManager.startWebPairingSession()
        assertTrue("QR payload must start with GLOBALSMS_WEB_V1", qrPayload.startsWith("GLOBALSMS_WEB_V1"))

        val state = webCompanionSyncManager.webSessionState.value
        assertTrue("State must be PairingQrReady", state is WebSessionState.PairingQrReady)
        val qrReadyState = state as WebSessionState.PairingQrReady
        val sessionKey = qrReadyState.sessionKey
        val nonce = qrReadyState.challengeNonce

        // 1. Attempt connection with invalid proof (fake key)
        val fakeProof = CryptoManager.encryptWithPassword(nonce, "FakeKeyWrongSecret123456789012")
        val rejectResult = webCompanionSyncManager.confirmCompanionConnected("Chrome Web", "192.168.1.50", fakeProof)
        assertFalse("Connection with invalid challenge proof must be rejected", rejectResult)
        assertTrue("State must reset to Disconnected on auth failure", webCompanionSyncManager.webSessionState.value is WebSessionState.Disconnected)

        // 2. Start a fresh session and connect with valid proof
        webCompanionSyncManager.startWebPairingSession()
        val validState = webCompanionSyncManager.webSessionState.value as WebSessionState.PairingQrReady
        val validKey = validState.sessionKey
        val validNonce = validState.challengeNonce

        val validProof = CryptoManager.encryptWithPassword(validNonce, validKey)
        val acceptResult = webCompanionSyncManager.confirmCompanionConnected("Chrome Web", "192.168.1.50", validProof)
        assertTrue("Connection with valid challenge proof must be accepted", acceptResult)
        assertTrue("State must be Connected", webCompanionSyncManager.webSessionState.value is WebSessionState.Connected)

        // 3. Test frame encryption and decryption
        val frame = webCompanionSyncManager.createEncryptedSyncFrame("SYNC_MESSAGES", "{\"count\":5}")
        assertTrue("Encrypted frame should not be empty", frame.isNotEmpty())

        val decryptedPayload = webCompanionSyncManager.decryptIncomingSyncFrame(frame)
        assertEquals("{\"count\":5}", decryptedPayload)

        // 4. Test disconnect clears session keys
        webCompanionSyncManager.disconnectWebCompanion()
        assertTrue("State must be Disconnected", webCompanionSyncManager.webSessionState.value is WebSessionState.Disconnected)
        val frameAfterDisconnect = webCompanionSyncManager.createEncryptedSyncFrame("SYNC", "{}")
        assertEquals("Frame creation after disconnect must return empty string", "", frameAfterDisconnect)
    }
}
