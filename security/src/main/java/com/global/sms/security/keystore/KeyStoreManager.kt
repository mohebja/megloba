package com.global.sms.security.keystore

import android.content.Context
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
    const val AUTO_BACKUP_KEY_ALIAS = "AutoBackupMasterKey_AES256"
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
    fun getOrCreateKey(alias: String = KEY_ALIAS): SecretKey {
        val ks = loadKeyStore()
        if (!ks.containsAlias(alias)) {
            try {
                val keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    ANDROID_KEYSTORE
                )
                val builder = KeyGenParameterSpec.Builder(
                    alias,
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
                        "Failed to generate hardware-backed AES key via AndroidKeyStore in production environment for alias: $alias",
                        e
                    )
                }
                val keyGen = KeyGenerator.getInstance("AES")
                keyGen.init(256)
                val secretKey = keyGen.generateKey()
                val entry = KeyStore.SecretKeyEntry(secretKey)
                val protParam = KeyStore.PasswordProtection(KEYSTORE_PASS)
                ks.setEntry(alias, entry, protParam)

                val file = getFallbackKeystoreFile()
                FileOutputStream(file).use { fos ->
                    ks.store(fos, KEYSTORE_PASS)
                }
            }
        }

        return try {
            val entry = ks.getEntry(alias, null) as? KeyStore.SecretKeyEntry
            entry?.secretKey ?: throw IllegalStateException("KeyStore missing key entry for alias: $alias")
        } catch (e: Exception) {
            if (!isTestEnvironment()) {
                throw SecurityException("Failed to access key from AndroidKeyStore for alias: $alias", e)
            }
            val protParam = KeyStore.PasswordProtection(KEYSTORE_PASS)
            val entry = ks.getEntry(alias, protParam) as? KeyStore.SecretKeyEntry
            entry?.secretKey ?: throw IllegalStateException("Fallback KeyStore missing key entry for alias: $alias", e)
        }
    }

    @Synchronized
    fun getOrCreateMasterKey(): SecretKey = getOrCreateKey(KEY_ALIAS)

    /**
     * Encrypts plain text using a specified SecretKey with AES-256-GCM.
     */
    fun encryptWithKey(plainText: String, secretKey: SecretKey): String {
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)

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
     * Decrypts Base64 string using a specified SecretKey with AES-256-GCM.
     */
    fun decryptWithKey(encryptedBase64: String, secretKey: SecretKey): String {
        return try {
            val combined = Base64.decode(encryptedBase64, Base64.NO_WRAP)
            if (combined.size <= IV_SIZE) {
                throw IllegalArgumentException("Invalid encrypted payload size: ${combined.size}")
            }

            val iv = ByteArray(IV_SIZE)
            val cipherText = ByteArray(combined.size - IV_SIZE)
            System.arraycopy(combined, 0, iv, 0, IV_SIZE)
            System.arraycopy(combined, IV_SIZE, cipherText, 0, cipherText.size)

            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

            val decryptedBytes = cipher.doFinal(cipherText)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            throw SecurityException("KeyStore hardware decryption failed", e)
        }
    }

    /**
     * Encrypts plain text using Hardware-Backed AES-256-GCM (Master Key).
     */
    fun encrypt(plainText: String): String = encryptWithKey(plainText, getOrCreateMasterKey())

    /**
     * Decrypts Base64 string using Hardware-Backed AES-256-GCM (Master Key).
     */
    fun decrypt(encryptedBase64: String): String = decryptWithKey(encryptedBase64, getOrCreateMasterKey())

    /**
     * Retrieves or generates a dedicated 256-bit hardware-encrypted master key for automated backups.
     * The raw 32 bytes are protected at rest via KeyStore hardware encryption (AutoBackupMasterKey_AES256).
     */
    @Synchronized
    fun getOrCreateAutoBackupMasterKeyBytes(context: Context): ByteArray {
        val keyFile = File(context.filesDir, "security/autobackup_key.enc")
        val autoBackupKey = getOrCreateKey(AUTO_BACKUP_KEY_ALIAS)
        if (keyFile.exists() && keyFile.length() > 0) {
            val encryptedBase64 = keyFile.readText(Charsets.UTF_8).trim()
            val decryptedHex = decryptWithKey(encryptedBase64, autoBackupKey)
            return hexToBytes(decryptedHex)
        }

        val parentDir = keyFile.parentFile
        if (parentDir != null && !parentDir.exists()) parentDir.mkdirs()

        val rawBytes = ByteArray(32).also { java.security.SecureRandom().nextBytes(it) }
        val hex = rawBytes.joinToString("") { "%02x".format(it) }
        val encryptedBase64 = encryptWithKey(hex, autoBackupKey)
        keyFile.writeText(encryptedBase64, Charsets.UTF_8)
        return rawBytes
    }

    private fun hexToBytes(hex: String): ByteArray {
        val result = ByteArray(hex.length / 2)
        for (i in result.indices) {
            val index = i * 2
            result[i] = hex.substring(index, index + 2).toInt(16).toByte()
        }
        return result
    }
}
