package com.global.sms.core.desktop

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

data class CompanionDevice(
    val deviceId: String,
    val deviceName: String,
    val platform: String, // "WINDOWS", "MACOS", "WEB", "LINUX"
    val pairedAt: Long = System.currentTimeMillis(),
    val lastActive: Long = System.currentTimeMillis(),
    val isSessionActive: Boolean = true
)

data class SyncPacket(
    val packetId: String = UUID.randomUUID().toString(),
    val senderDeviceId: String,
    val targetDeviceId: String,
    val actionType: String, // "SMS_RECEIVED", "SMS_SEND_REQUEST", "AGENT_ACTION_APPROVE", "CONTACT_SYNC"
    val encryptedPayload: String,
    val timestamp: Long = System.currentTimeMillis()
)

class DesktopSyncProtocol {

    private val _pairedDevices = MutableStateFlow<List<CompanionDevice>>(emptyList())
    val pairedDevices: StateFlow<List<CompanionDevice>> = _pairedDevices.asStateFlow()

    private val _pendingSyncPackets = MutableStateFlow<List<SyncPacket>>(emptyList())
    val pendingSyncPackets: StateFlow<List<SyncPacket>> = _pendingSyncPackets.asStateFlow()

    fun generatePairingToken(): String {
        val rawToken = "G-SMS-PAIR-${UUID.randomUUID().toString().take(8).uppercase()}-${System.currentTimeMillis()}"
        return rawToken
    }

    fun validateAndPairDevice(token: String, deviceName: String, platform: String): CompanionDevice? {
        if (token.isBlank()) return null

        val newDevice = CompanionDevice(
            deviceId = "DEV-${UUID.randomUUID().toString().take(6)}",
            deviceName = deviceName,
            platform = platform
        )

        _pairedDevices.value = _pairedDevices.value + newDevice
        return newDevice
    }

    fun sendSyncPacket(targetDeviceId: String, actionType: String, payload: String): SyncPacket {
        val packet = SyncPacket(
            senderDeviceId = "HOST_ANDROID",
            targetDeviceId = targetDeviceId,
            actionType = actionType,
            encryptedPayload = "AES256_GCM[$payload]"
        )
        _pendingSyncPackets.value = _pendingSyncPackets.value + packet
        return packet
    }

    fun handleHeartbeat(deviceId: String): Boolean {
        val devices = _pairedDevices.value
        val device = devices.find { it.deviceId == deviceId } ?: return false

        _pairedDevices.value = devices.map {
            if (it.deviceId == deviceId) it.copy(lastActive = System.currentTimeMillis(), isSessionActive = true) else it
        }
        return true
    }

    fun revokeDeviceSession(deviceId: String): Boolean {
        val current = _pairedDevices.value
        if (current.none { it.deviceId == deviceId }) return false

        _pairedDevices.value = current.filterNot { it.deviceId == deviceId }
        return true
    }
}
