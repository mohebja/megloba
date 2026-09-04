package com.global.sms.security.lock

import android.content.Context
import androidx.core.content.edit
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class AppLockManager(context: Context) {

    private val prefs = context.getSharedPreferences("global_sms_lock_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256"
        private const val ITERATION_COUNT = 210_000
        private const val KEY_SIZE = 256
        private const val SALT_SIZE = 16
        private const val PREF_PIN_HASH = "app_pin_hash"
        private const val PREF_PIN_SALT = "app_pin_salt"
    }

    var isLockEnabled: Boolean
        get() = prefs.getBoolean("is_lock_enabled", false)
        set(value) = prefs.edit { putBoolean("is_lock_enabled", value) }

    var isBiometricEnabled: Boolean
        get() = prefs.getBoolean("is_biometric_enabled", false)
        set(value) = prefs.edit { putBoolean("is_biometric_enabled", value) }

    var isPrivateNotificationMode: Boolean
        get() = prefs.getBoolean("private_notification_mode", true)
        set(value) = prefs.edit { putBoolean("private_notification_mode", value) }

    fun setPin(pin: String) {
        val random = SecureRandom()
        val salt = ByteArray(SALT_SIZE).apply { random.nextBytes(this) }
        val hash = hashPin(pin, salt)

        val saltB64 = base64Encode(salt)
        val hashB64 = base64Encode(hash)

        prefs.edit {
            putString(PREF_PIN_SALT, saltB64)
            putString(PREF_PIN_HASH, hashB64)
            remove("app_pin") // purge legacy insecure key if present
        }
    }

    fun verifyPin(inputPin: String): Boolean {
        val saltB64 = prefs.getString(PREF_PIN_SALT, null) ?: return false
        val savedHashB64 = prefs.getString(PREF_PIN_HASH, null) ?: return false

        return try {
            val salt = base64Decode(saltB64)
            val expectedHash = base64Decode(savedHashB64)
            val computedHash = hashPin(inputPin, salt)
            MessageDigest.isEqual(expectedHash, computedHash)
        } catch (_: Exception) {
            false
        }
    }

    fun hasPinSet(): Boolean {
        return prefs.contains(PREF_PIN_HASH) && !prefs.getString(PREF_PIN_HASH, null).isNullOrEmpty()
    }

    fun clearPin() {
        prefs.edit {
            remove(PREF_PIN_SALT)
            remove(PREF_PIN_HASH)
            remove("app_pin")
        }
    }

    private fun hashPin(pin: String, salt: ByteArray): ByteArray {
        val factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)
        val spec = PBEKeySpec(pin.toCharArray(), salt, ITERATION_COUNT, KEY_SIZE)
        return factory.generateSecret(spec).encoded
    }

    private fun base64Encode(bytes: ByteArray): String {
        return try {
            Base64.getEncoder().encodeToString(bytes)
        } catch (_: Throwable) {
            android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
        }
    }

    private fun base64Decode(str: String): ByteArray {
        return try {
            Base64.getDecoder().decode(str)
        } catch (_: Throwable) {
            android.util.Base64.decode(str, android.util.Base64.NO_WRAP)
        }
    }
}
