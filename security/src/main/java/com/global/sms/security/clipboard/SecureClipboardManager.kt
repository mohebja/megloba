package com.global.sms.security.clipboard

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PersistableBundle
import android.util.Log

/**
 * Secure Clipboard Manager.
 * - Masks copied text on Android 13+ keyboards/previews using EXTRA_IS_SENSITIVE.
 * - Schedules automatic clipboard wiping after configurable auto-clear timeout (default 30s).
 */
object SecureClipboardManager {

    private const val DEFAULT_AUTO_CLEAR_DELAY_MS = 30_000L // 30 seconds
    private val handler = Handler(Looper.getMainLooper())
    private var clearRunnable: Runnable? = null

    fun copyToClipboard(
        context: Context,
        label: String,
        text: String,
        isSensitive: Boolean = true,
        autoClearDelayMs: Long = DEFAULT_AUTO_CLEAR_DELAY_MS
    ) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
        val clipData = ClipData.newPlainText(label, text)

        if (isSensitive && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            clipData.description.extras = PersistableBundle().apply {
                putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true)
            }
        }

        clipboard.setPrimaryClip(clipData)

        // Cancel previous clear job if any
        clearRunnable?.let { handler.removeCallbacks(it) }

        if (isSensitive) {
            val runnable = Runnable {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        clipboard.clearPrimaryClip()
                    } else {
                        clipboard.setPrimaryClip(ClipData.newPlainText("", ""))
                    }
                } catch (e: Exception) {
                    Log.w("SecureClipboardManager", "Failed to auto-clear clipboard", e)
                }
            }
            clearRunnable = runnable
            handler.postDelayed(runnable, autoClearDelayMs)
        }
    }
}
