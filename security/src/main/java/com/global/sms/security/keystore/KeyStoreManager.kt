package com.global.sms.security.keystore

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Android KeyStore Manager providing Hardware-Backed AES-256 GCM Encryption & Decryption.
 * In production Android devices, keys are generated and stored in Android's secure hardware key storage (TEE / StrongBox).
 * In JVM test environments, fallback is durable and backed by a persisted keystore file.
 */
object KeyStoreManager {

    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "GlobalSmsMasterKey_AES256"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_TAG_LENGTH = 128
    private const val IV_SIZE = 12
    private val KEYSTORE_PASS = "global_sms_keystore_pass".toCharArray()

    private fun getFallbackKeystoreFile(): File {
        val dir = File(System.getProperty("java.io.tmpdir") ?: ".", "global_sms_security")
        if (!dir.exists()) dir.mkdirs()
        return File(dir, "keystore_master.bks")
    }

    /**
     * Strictly verifies whether execution is happening within a JVM/Robolectric test environment.
     * In any production/release Android OS runtime, hardware-backed AndroidKeyStore is mandatory and
     * file-backed fallback is strictly blocked.
     */
    private fun isTestEnvironment(): Boolean {
        val fingerprint = android.os.Build.FINGERPRINT ?: ""
        if (fingerprint.contains("robolectric", ignoreCase = true)) return true
        val runtimeName = System.getProperty("java.runtime.name") ?: ""
        if (!runtimeName.contains("Android", ignoreCase = true)) return true
        return try {
            Class.forName("org.robolectric.Robolectric")
            true
        } catch (_: Throwable) {
            false
        }
    }

    private fun loadKeyStore(): KeyStore {
        return try {
            KeyStore.getInstance(ANDROID_KEYSTORE).apply {
                load(null)
            }
        } catch (e: Exception) {
            if (!isTestEnvironment()) {
                throw SecurityException(
                    "AndroidKeyStore is strictly required in production Android runtime. Fallback is prohibited outside test suites.",
                    e
                )
            }
            val ks = KeyStore.getInstance("PKCS12")
            val file = getFallbackKeystoreFile()
            if (file.exists() && file.length() > 0) {
                FileInputStream(file).use { fis ->
                    ks.load(fis, KEYSTORE_PASS)
                }
            } else {
                ks.load(null, KEYSTORE_PASS)
            }
            ks
        }
    }

    @Synchronized
    fun getOrCreateMasterKey(): SecretKey {
        val ks = loadKeyStore()
        if (!ks.containsAlias(KEY_ALIAS)) {
            try {
                val keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    ANDROID_KEYSTORE
                )
                val builder = KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)

                keyGenerator.init(builder.build())
                keyGenerator.generateKey()
            } catch (e: Exception) {
                if (!isTestEnvironment()) {
                    throw SecurityException(
                        "Failed to generate hardware-backed AES key via AndroidKeyStore in production environment.",
                        e
                    )
                }
                val keyGen = KeyGenerator.getInstance("AES")
                keyGen.init(256)
                val secretKey = keyGen.generateKey()
                val entry = KeyStore.SecretKeyEntry(secretKey)
                val protParam = KeyStore.PasswordProtection(KEYSTORE_PASS)
                ks.setEntry(KEY_ALIAS, entry, protParam)

                val file = getFallbackKeystoreFile()
                FileOutputStream(file).use { fos ->
                    ks.store(fos, KEYSTORE_PASS)
                }
            }
        }

        return try {
            val entry = ks.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
            entry?.secretKey ?: throw IllegalStateException("KeyStore missing master key entry")
        } catch (e: Exception) {
            if (!isTestEnvironment()) {
                throw SecurityException("Failed to access master key from AndroidKeyStore in production environment.", e)
            }
            val protParam = KeyStore.PasswordProtection(KEYSTORE_PASS)
            val entry = ks.getEntry(KEY_ALIAS, protParam) as? KeyStore.SecretKeyEntry
            entry?.secretKey ?: throw IllegalStateException("Fallback KeyStore missing master key entry", e)
        }
    }

    /**
     * Encrypts plain text using Hardware-Backed AES-256-GCM.
     * Returns Base64 encoded string containing [12 bytes IV + CipherText].
     */
    fun encrypt(plainText: String): String {
        return try {
            val masterKey = getOrCreateMasterKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, masterKey)

            val iv = cipher.iv // 12 bytes
            val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

            val combined = ByteArray(iv.size + encryptedBytes.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(encryptedBytes, 0, combined, iv.size, encryptedBytes.size)

            Base64.encodeToString(combined, Base64.NO_WRAP)
        } catch (e: Exception) {
            throw SecurityException("KeyStore hardware encryption failed", e)
        }
    }

    /**
     * Decrypts Base64 string containing [12 bytes IV + CipherText] using Hardware-Backed AES-256-GCM.
     */
    fun decrypt(encryptedBase64: String): String {
        return try {
            val combined = Base64.decode(encryptedBase64, Base64.NO_WRAP)
            if (combined.size <= IV_SIZE) {
                throw IllegalArgumentException("Invalid encrypted payload size: ${combined.size}")
            }

            val iv = ByteArray(IV_SIZE)
            val cipherText = ByteArray(combined.size - IV_SIZE)
            System.arraycopy(combined, 0, iv, 0, IV_SIZE)
            System.arraycopy(combined, IV_SIZE, cipherText, 0, cipherText.size)

            val masterKey = getOrCreateMasterKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, masterKey, spec)

            val decryptedBytes = cipher.doFinal(cipherText)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            throw SecurityException("KeyStore hardware decryption failed", e)
        }
    }
}
