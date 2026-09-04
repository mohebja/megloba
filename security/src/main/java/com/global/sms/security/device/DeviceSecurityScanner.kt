package com.global.sms.security.device

import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Build
import android.os.Debug
import java.io.File

data class SecurityReport(
    val isRooted: Boolean,
    val isDebuggerAttached: Boolean,
    val isDebuggableBuild: Boolean,
    val securityScore: Int, // 0 to 100
    val rootWarnings: List<String>,
    val overallStatusMessage: String
)

/**
 * Root Detection, Debugger Inspector, and System Integrity Scanner.
 */
object DeviceSecurityScanner {

    private val SU_PATHS = arrayOf(
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

    fun scanDevice(context: Context): SecurityReport {
        val rootWarnings = mutableListOf<String>()

        // 1. Check for test-keys in Build tags
        val buildTags = Build.TAGS
        val hasTestKeys = buildTags != null && buildTags.contains("test-keys")
        if (hasTestKeys) {
            rootWarnings.add("فریم‌ورک سیستم دارای کلیدهای تست (test-keys) است.")
        }

        // 2. Check for su binaries
        var hasSuBinary = false
        for (path in SU_PATHS) {
            if (File(path).exists()) {
                hasSuBinary = true
                rootWarnings.add("فایل اجرای روت پیدا شد: $path")
                break
            }
        }

        // 3. Check for Debugger attached
        val isDebuggerAttached = Debug.isDebuggerConnected()

        // 4. Check debuggable app flag
        val isDebuggableBuild = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

        val isRooted = hasTestKeys || hasSuBinary

        var score = 100
        if (isRooted) score -= 40
        if (isDebuggerAttached) score -= 30
        if (isDebuggableBuild) score -= 10

        val statusMsg = when {
            score >= 90 -> "امنیت دستگاه ممتاز و تایید شده است."
            score >= 70 -> "هشدار: برخی متغیرهای امنیتی نیازمند توجه هستند."
            else -> "هشدار امنیتی شدید: دستگاه روت شده یا اشکال‌زدایی فعال است."
        }

        return SecurityReport(
            isRooted = isRooted,
            isDebuggerAttached = isDebuggerAttached,
            isDebuggableBuild = isDebuggableBuild,
            securityScore = score.coerceIn(0, 100),
            rootWarnings = rootWarnings,
            overallStatusMessage = statusMsg
        )
    }
}
