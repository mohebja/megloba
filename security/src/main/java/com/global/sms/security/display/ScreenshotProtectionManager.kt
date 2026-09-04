package com.global.sms.security.display

import android.app.Activity
import android.view.WindowManager

/**
 * Screenshot & Screen Capture Protection Manager.
 * Prevents screen recording, screenshots, and task-switcher recent previews when viewing protected content.
 */
object ScreenshotProtectionManager {

    fun setProtection(activity: Activity, enabled: Boolean) {
        activity.runOnUiThread {
            if (enabled) {
                activity.window.setFlags(
                    WindowManager.LayoutParams.FLAG_SECURE,
                    WindowManager.LayoutParams.FLAG_SECURE
                )
            } else {
                activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            }
        }
    }
}
