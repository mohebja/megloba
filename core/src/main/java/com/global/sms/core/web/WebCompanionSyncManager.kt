package com.global.sms.core.web

import android.content.Context
import com.global.sms.security.crypto.CryptoManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import java.security.SecureRandom
import java.util.Base64
import java.util.UUID

sealed class WebSessionState {
    object Disconnected : WebSessionState()
    data class PairingQrReady(val qrPayload: String, val sessionKey: String, val challengeNonce: String) : WebSessionState()
    data class Connected(val deviceName: String, val clientIp: String) : WebSessionState()
}

/**
 * Enterprise Web & Desktop Companion Pairing System for Global SMS.
 * Manages QR code pairing, AES-256 session key exchange, proof-of-possession challenge verification,
 * and encrypted WebSocket/P2P message frame processing for web browser client synchronization.
 */
class WebCompanionSyncManager(private val context: Context) {

    private val _webSessionState = MutableStateFlow<WebSessionState>(WebSessionState.Disconnected)
    val webSessionState: StateFlow<WebSessionState> = _webSessionState.asStateFlow()

    private var activeSessionKey: String? = null
    private var activeSessionId: String? = null
    private var activeChallengeNonce: String? = null

    /**
     * Initiate a Web Pairing session and construct QR Code payload with challenge nonce.
     */
    fun startWebPairingSession(): String {
        val sessionId = UUID.randomUUID().toString()
        val randomKeyBytes = ByteArray(32)
        SecureRandom().nextBytes(randomKeyBytes)
        val sessionKey = Base64.getEncoder().encodeToString(randomKeyBytes)

        val challengeNonceBytes = ByteArray(16)
        SecureRandom().nextBytes(challengeNonceBytes)
        val challengeNonce = Base64.getEncoder().encodeToString(challengeNonceBytes)

        activeSessionId = sessionId
        activeSessionKey = sessionKey
        activeChallengeNonce = challengeNonce

        val qrPayload = "GLOBALSMS_WEB_V1:${sessionId}:${sessionKey}:${challengeNonce}:https://web.globalsms.app"

        _webSessionState.value = WebSessionState.PairingQrReady(
            qrPayload = qrPayload,
            sessionKey = sessionKey,
            challengeNonce = challengeNonce
        )

        return qrPayload
    }

    /**
     * Verify proof-of-possession and mark companion as authenticated and connected.
     * The connecting client MUST prove possession of the shared session key by returning
     * the challenge nonce encrypted with that key. Unauthenticated connections are strictly rejected.
     */
    fun confirmCompanionConnected(
        deviceName: String,
        clientIp: String,
        encryptedChallengeProof: String
    ): Boolean {
        val sessionKey = activeSessionKey
        val challengeNonce = activeChallengeNonce
        val sessionId = activeSessionId

        if (sessionKey == null || challengeNonce == null || sessionId == null) {
            disconnectWebCompanion()
            return false
        }

        // Verify cryptographic proof of possession of the session key
        try {
            val decryptedNonce = CryptoManager.decryptWithPassword(encryptedChallengeProof, sessionKey)
            if (decryptedNonce != challengeNonce) {
                disconnectWebCompanion()
                return false
            }
        } catch (_: Exception) {
            disconnectWebCompanion()
            return false
        }

        _webSessionState.value = WebSessionState.Connected(
            deviceName = deviceName,
            clientIp = clientIp
        )
        return true
    }

    /**
     * Encrypt and construct an outbound sync packet frame for the Web Companion.
     */
    fun createEncryptedSyncFrame(
        frameType: String,
        payloadJson: String
    ): String {
        val key = activeSessionKey ?: return ""
        val frameObj = JSONObject().apply {
            put("sessionId", activeSessionId)
            put("type", frameType)
            put("timestamp", System.currentTimeMillis())
            put("payload", payloadJson)
        }

        return CryptoManager.encryptWithPassword(frameObj.toString(), key)
    }

    /**
     * Decrypt an inbound sync packet frame received from the Web Companion.
     */
    fun decryptIncomingSyncFrame(encryptedFrame: String): String? {
        val key = activeSessionKey ?: return null
        return try {
            val decryptedJsonStr = CryptoManager.decryptWithPassword(encryptedFrame, key)
            val json = JSONObject(decryptedJsonStr)
            if (json.optString("sessionId") != activeSessionId) {
                return null
            }
            if (json.has("payload")) json.getString("payload") else null
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Disconnect active web session and securely zero/clear session keys.
     */
    fun disconnectWebCompanion() {
        activeSessionKey = null
        activeSessionId = null
        activeChallengeNonce = null
        _webSessionState.value = WebSessionState.Disconnected
    }
}
