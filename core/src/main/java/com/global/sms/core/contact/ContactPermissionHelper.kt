package com.global.sms.core.contact

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * Utility helper for managing contacts permissions optimized for Android 14 (API 34) and 15 (API 35+).
 */
object ContactPermissionHelper {

    val REQUIRED_PERMISSIONS: Array<String>
        get() = arrayOf(
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS
        )

    fun hasReadContactsPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun hasWriteContactsPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun hasAllContactsPermissions(context: Context): Boolean {
        return hasReadContactsPermission(context)
    }

    fun getPermissionState(activity: Activity): ContactPermissionState {
        val hasRead = hasReadContactsPermission(activity)
        if (hasRead) return ContactPermissionState.GRANTED

        val showRationale = ActivityCompat.shouldShowRequestPermissionRationale(
            activity,
            Manifest.permission.READ_CONTACTS
        )

        return if (showRationale) {
            ContactPermissionState.NEEDS_EXPLANATION
        } else {
            ContactPermissionState.PERMANENTLY_DENIED
        }
    }

    /**
     * Checks if running on Android 14 (UPSIDE_DOWN_CAKE - API 34) or above.
     */
    fun isAndroid14OrAbove(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
    }

    /**
     * Checks if running on Android 15 (VANILLA_ICE_CREAM - API 35) or above.
     */
    fun isAndroid15OrAbove(): Boolean {
        return Build.VERSION.SDK_INT >= 35
    }
}
