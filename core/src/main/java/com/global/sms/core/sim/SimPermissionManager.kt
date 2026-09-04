package com.global.sms.core.sim

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

object SimPermissionManager {

    const val REQUIRED_PERMISSION = Manifest.permission.READ_PHONE_STATE

    fun hasReadPhoneStatePermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            REQUIRED_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun getPermissionStatusMessage(context: Context): String {
        return if (hasReadPhoneStatePermission(context)) {
            "READ_PHONE_STATE permission granted."
        } else {
            "READ_PHONE_STATE permission is required to detect Dual SIM configurations and subscription details."
        }
    }

    fun getPermissionState(context: Context): SimPermissionState {
        val granted = hasReadPhoneStatePermission(context)
        return SimPermissionState(
            isGranted = granted,
            permissionName = REQUIRED_PERMISSION,
            statusMessage = getPermissionStatusMessage(context)
        )
    }
}

data class SimPermissionState(
    val isGranted: Boolean,
    val permissionName: String,
    val statusMessage: String
)
