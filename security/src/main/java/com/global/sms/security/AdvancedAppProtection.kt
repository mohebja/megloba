package com.global.sms.security

import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Build
import java.io.File

data class ThreatAssessmentReport(
    val isRooted: Boolean,
    val isDebuggable: Boolean,
    val isEmulator: Boolean,
    val isScreenshotBlocked: Boolean,
    val securityScore: Int, // 0 to 100
    val privacyScore: Int,  // 0 to 100
    val detectedThreats: List<String>
)

class AdvancedAppProtection(private val context: Context) {

    fun assessDeviceSecurity(): ThreatAssessmentReport {
        val threats = mutableListOf<String>()
        var secScore = 100
        var privScore = 100

        val rooted = checkRoot()
        if (rooted) {
            secScore -= 35
            privScore -= 20
            threats.add("دستگاه روت شده شناسایی شد (خطر دسترسی مستقیم بدافزارها).")
        }

        val debuggable = isDebuggable()
        if (debuggable) {
            secScore -= 15
            threats.add("برنامه در حالت Debugging اجرا می‌شود.")
        }

        val emulator = checkEmulator()
        if (emulator) {
            secScore -= 20
            threats.add("محیط شبیه‌ساز (Emulator) شناسایی شد.")
        }

        return ThreatAssessmentReport(
            isRooted = rooted,
            isDebuggable = debuggable,
            isEmulator = emulator,
            isScreenshotBlocked = true,
            securityScore = secScore.coerceIn(0, 100),
            privacyScore = privScore.coerceIn(0, 100),
            detectedThreats = threats
        )
    }

    private fun checkRoot(): Boolean {
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su"
        )
        return paths.any { File(it).exists() }
    }

    private fun isDebuggable(): Boolean {
        return (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }

    private fun checkEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk" == Build.PRODUCT)
    }
}
