package com.global.sms.core.contact

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * Enterprise manager for READ_CONTACTS permission handling.
 * Provides state detection, settings redirect, and Persian rationale dialog support.
 */
class ContactPermissionManager(private val context: Context) {

    fun getPermissionState(activity: Activity? = null): ContactPermissionState {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            return ContactPermissionState.GRANTED
        }

        if (activity != null) {
            val showRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                Manifest.permission.READ_CONTACTS
            )
            return if (showRationale) {
                ContactPermissionState.DENIED
            } else {
                ContactPermissionState.PERMANENTLY_DENIED
            }
        }

        return ContactPermissionState.NOT_REQUESTED
    }

    fun isGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun openAppSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    companion object {
        const val PERMISSION_READ_CONTACTS = Manifest.permission.READ_CONTACTS
        const val CONTACTS_PERMISSION_REQUEST_CODE = 1002
    }
}
