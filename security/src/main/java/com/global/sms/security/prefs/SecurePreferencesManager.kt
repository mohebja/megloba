package com.global.sms.security.prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

import java.io.File

/**
 * KeyStore Backed Encrypted Preferences Manager.
 * Stores sensitive application state, PIN hashes, vault master tokens, and security flags securely.
 */
class SecurePreferencesManager(private val context: Context) {

    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val prefs: SharedPreferences by lazy {
        try {
            createEncryptedPrefs(context)
        } catch (e: Exception) {
            // Attempt clean recovery once by deleting potentially corrupted prefs file and recreating
            try {
                val file = File(context.filesDir?.parentFile, "shared_prefs/$PREFS_FILENAME.xml")
                if (file.exists()) file.delete()
                createEncryptedPrefs(context)
            } catch (recoveryEx: Exception) {
                throw SecurityException("SecurePreferencesManager initialization failed: cannot establish hardware-backed encrypted storage", recoveryEx)
            }
        }
    }

    private fun createEncryptedPrefs(ctx: Context): SharedPreferences {
        return EncryptedSharedPreferences.create(
            ctx,
            PREFS_FILENAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    // Lock & Authentication
    var isAppLockEnabled: Boolean
        get() = prefs.getBoolean(KEY_APP_LOCK_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_APP_LOCK_ENABLED, value).apply()

    var isBiometricEnabled: Boolean
        get() = prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, value).apply()

    var isScreenshotProtectionEnabled: Boolean
        get() = prefs.getBoolean(KEY_SCREENSHOT_PROTECTION, true)
        set(value) = prefs.edit().putBoolean(KEY_SCREENSHOT_PROTECTION, value).apply()

    var isSecureClipboardEnabled: Boolean
        get() = prefs.getBoolean(KEY_SECURE_CLIPBOARD, true)
        set(value) = prefs.edit().putBoolean(KEY_SECURE_CLIPBOARD, value).apply()

    var isLinkSecurityEnabled: Boolean
        get() = prefs.getBoolean(KEY_LINK_SECURITY, true)
        set(value) = prefs.edit().putBoolean(KEY_LINK_SECURITY, value).apply()

    var isUssdProtectionEnabled: Boolean
        get() = prefs.getBoolean(KEY_USSD_PROTECTION, true)
        set(value) = prefs.edit().putBoolean(KEY_USSD_PROTECTION, value).apply()

    var pinHash: String?
        get() = prefs.getString(KEY_PIN_HASH, null)
        set(value) = prefs.edit().putString(KEY_PIN_HASH, value).apply()

    var vaultPasscodeHash: String?
        get() = prefs.getString(KEY_VAULT_PASSCODE, null)
        set(value) = prefs.edit().putString(KEY_VAULT_PASSCODE, value).apply()

    fun clearAll() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_FILENAME = "global_sms_secure_prefs"
        private const val PREFS_FILENAME_FALLBACK = "global_sms_secure_prefs_fallback"
        private const val KEY_APP_LOCK_ENABLED = "app_lock_enabled"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_SCREENSHOT_PROTECTION = "screenshot_protection"
        private const val KEY_SECURE_CLIPBOARD = "secure_clipboard"
        private const val KEY_LINK_SECURITY = "link_security"
        private const val KEY_USSD_PROTECTION = "ussd_protection"
        private const val KEY_PIN_HASH = "pin_hash"
        private const val KEY_VAULT_PASSCODE = "vault_passcode"
    }
}
