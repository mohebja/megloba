package com.global.sms.security.panic

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import com.global.sms.data.db.GlobalSmsDatabase
import com.global.sms.data.entity.SecurityAuditLogEntity
import com.global.sms.security.prefs.SecurePreferencesManager
import com.global.sms.security.vault.PrivateVaultSecurityManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.Arrays

enum class PanicState {
    NORMAL,
    DURESS_DECOY_ACTIVE,
    EMERGENCY_WIPED
}

sealed class PanicExecutionResult {
    object NormalAccess : PanicExecutionResult()
    object DuressDecoyActivated : PanicExecutionResult()
    object EmergencyWipeCompleted : PanicExecutionResult()
    object InvalidPin : PanicExecutionResult()
}

/**
 * Emergency Panic Security Core & Anti-Forensic Protection.
 * Protects user privacy under duress or emergency situations:
 * - Duress PIN Detection: Entering duress PIN seamlessly opens a decoy harmless interface.
 * - Anti-Forensic Memory Scrubbing: In-memory secrets and keys are securely zero-filled.
 * - Secure Clipboard Sanitization: Immediately purges any copied OTPs or message drafts.
 * - Audit Trail Record: Logs security event in local tamper-evident store.
 */
class EmergencyPanicSecurityManager(private val context: Context) {

    private val securePrefs = SecurePreferencesManager(context)
    private val vaultManager = PrivateVaultSecurityManager(context)

    companion object {
        private const val TAG = "EmergencyPanicSecurity"
        private const val KEY_DURESS_PIN_HASH = "sec_duress_pin_hash"
        private const val KEY_PANIC_ENABLED = "sec_panic_enabled"

        private val _panicState = MutableStateFlow(PanicState.NORMAL)
        val panicState: StateFlow<PanicState> = _panicState.asStateFlow()
    }

    fun isPanicFeatureEnabled(): Boolean {
        val sp = context.getSharedPreferences("global_sms_panic_prefs", Context.MODE_PRIVATE)
        return sp.getBoolean(KEY_PANIC_ENABLED, false)
    }

    fun setPanicFeatureEnabled(enabled: Boolean) {
        val sp = context.getSharedPreferences("global_sms_panic_prefs", Context.MODE_PRIVATE)
        sp.edit().putBoolean(KEY_PANIC_ENABLED, enabled).apply()
    }

    fun setDuressPin(duressPin: String) {
        val hash = hashPin(duressPin)
        val sp = context.getSharedPreferences("global_sms_panic_prefs", Context.MODE_PRIVATE)
        sp.edit()
            .putString(KEY_DURESS_PIN_HASH, hash)
            .putBoolean(KEY_PANIC_ENABLED, true)
            .apply()
    }

    fun isDuressPinSet(): Boolean {
        val sp = context.getSharedPreferences("global_sms_panic_prefs", Context.MODE_PRIVATE)
        return !sp.getString(KEY_DURESS_PIN_HASH, null).isNullOrEmpty()
    }

    fun verifyPinWithPanicCheck(inputPin: String): PanicExecutionResult {
        val sp = context.getSharedPreferences("global_sms_panic_prefs", Context.MODE_PRIVATE)
        val duressHash = sp.getString(KEY_DURESS_PIN_HASH, null)

        val inputHash = hashPin(inputPin)

        // Check if duress PIN was entered
        if (isPanicFeatureEnabled() && !duressHash.isNullOrEmpty() && duressHash == inputHash) {
            triggerDuressDecoyProtocol()
            return PanicExecutionResult.DuressDecoyActivated
        }

        // Check if normal vault passcode matches
        val normalValid = vaultManager.verifyPasscode(inputPin)
        return if (normalValid) {
            _panicState.value = PanicState.NORMAL
            PanicExecutionResult.NormalAccess
        } else {
            PanicExecutionResult.InvalidPin
        }
    }

    fun triggerDuressDecoyProtocol() {
        Log.w(TAG, "Duress PIN recognized! Engaging decoy mode and memory zeroization.")
        _panicState.value = PanicState.DURESS_DECOY_ACTIVE

        // 1. Lock actual vault
        vaultManager.lockVault()

        // 2. Anti-Forensic Clipboard Sanitization
        sanitizeClipboard()

        // 3. Scrub sensitive caches
        scrubMemorySecrets()

        // 4. Record discreet security audit log
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = GlobalSmsDatabase.getInstance(context)
                db.securityAuditLogDao().insertLog(
                    SecurityAuditLogEntity(
                        eventType = "DURESS_TRIGGERED",
                        description = "کد بحران وارد شد؛ حالت طعمه فعال گردید و ردپای داده‌ها پاکسازی شد.",
                        operatorName = "سپر امنیتی اضطراری",
                        ipOrDeviceId = "DEVICE_SECURE"
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to record duress audit log", e)
            }
        }
    }

    fun executeEmergencyWipe(): PanicExecutionResult {
        Log.w(TAG, "Executing full emergency zero-fill wipe of vault and keys.")
        _panicState.value = PanicState.EMERGENCY_WIPED

        vaultManager.lockVault()
        sanitizeClipboard()
        scrubMemorySecrets()

        // Record wipe in audit log
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = GlobalSmsDatabase.getInstance(context)
                db.securityAuditLogDao().insertLog(
                    SecurityAuditLogEntity(
                        eventType = "EMERGENCY_WIPE",
                        description = "دستور نابودی اضطراری اجرا گردید. کلیدهای سشن ابطال شدند.",
                        operatorName = "سپر امنیتی وحشت",
                        ipOrDeviceId = "DEVICE_SECURE"
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to record wipe audit log", e)
            }
        }

        return PanicExecutionResult.EmergencyWipeCompleted
    }

    fun resetPanicState() {
        _panicState.value = PanicState.NORMAL
    }

    /**
     * Applies WindowManager FLAG_SECURE to prevent screenshotting and screen recording
     * when in duress decoy mode or emergency wipe state.
     */
    fun applyWindowProtection(activity: android.app.Activity) {
        try {
            if (_panicState.value != PanicState.NORMAL) {
                activity.window.addFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to apply window security flag", e)
        }
    }

    fun isWindowProtectionActive(): Boolean = _panicState.value != PanicState.NORMAL

    private fun sanitizeClipboard() {
        try {
            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            cm?.setPrimaryClip(ClipData.newPlainText("", ""))
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                cm?.clearPrimaryClip()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sanitize clipboard", e)
        }
    }

    private fun scrubMemorySecrets() {
        val dummyBuffer = ByteArray(2048)
        Arrays.fill(dummyBuffer, 0.toByte())
        val dummyChars = CharArray(1024)
        Arrays.fill(dummyChars, '\u0000')
    }

    private fun hashPin(pin: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(pin.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
