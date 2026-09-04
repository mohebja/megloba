package com.global.sms.security.crypto

import com.global.sms.security.keystore.KeyStoreManager
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Advanced AES-256 Cryptographic Engine.
 * Supports:
 * - Hardware-backed Android KeyStore AES-256 GCM
 * - Password-Based Key Derivation (PBKDF2WithHmacSHA256, 210,000 iterations, 256-bit key) with random salt & 12-byte GCM IVs.
 */
object CryptoManager {

    private const val PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val CIPHER_TRANSFORMATION = "AES/GCM/NoPadding"
    private const val KEY_SIZE = 256
    private const val ITERATION_COUNT = 210_000
    private const val SALT_SIZE = 16
    private const val IV_SIZE = 12
    private const val GCM_TAG_LENGTH = 128

    private fun base64Encode(bytes: ByteArray): String {
        return try {
            Base64.getEncoder().encodeToString(bytes)
        } catch (e: Throwable) {
            android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
        }
    }

    private fun base64Decode(str: String): ByteArray {
        return try {
            Base64.getDecoder().decode(str)
        } catch (e: Throwable) {
            android.util.Base64.decode(str, android.util.Base64.NO_WRAP)
        }
    }

    /**
     * Hardware-Backed AES-256 GCM encryption using Android KeyStore.
     */
    fun encryptHardware(plainText: String): String {
        return KeyStoreManager.encrypt(plainText)
    }

    /**
     * Hardware-Backed AES-256 GCM decryption using Android KeyStore.
     */
    fun decryptHardware(encryptedBase64: String): String {
        return KeyStoreManager.decrypt(encryptedBase64)
    }

    /**
     * Password-based AES-256 GCM Encryption (PBKDF2 100,000 iterations + 16-byte random salt + 12-byte GCM IV).
     * Output format: Base64(Salt [16B] + IV [12B] + CipherText)
     */
    fun encryptWithPassword(plainText: String, password: String): String {
        return try {
            val random = SecureRandom()
            val salt = ByteArray(SALT_SIZE).apply { random.nextBytes(this) }
            val iv = ByteArray(IV_SIZE).apply { random.nextBytes(this) }

            val secretKey = deriveKeyFromPassword(password, salt)
            val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
            val parameterSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec)

            val cipherText = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

            // Combine Salt + IV + CipherText
            val result = ByteArray(SALT_SIZE + IV_SIZE + cipherText.size)
            System.arraycopy(salt, 0, result, 0, SALT_SIZE)
            System.arraycopy(iv, 0, result, SALT_SIZE, IV_SIZE)
            System.arraycopy(cipherText, 0, result, SALT_SIZE + IV_SIZE, cipherText.size)

            base64Encode(result)
        } catch (e: Exception) {
            throw SecurityException("Password-based encryption failed", e)
        }
    }

    /**
     * Password-based AES-256 GCM Decryption.
     */
    fun decryptWithPassword(encryptedBase64: String, password: String): String {
        return try {
            val combined = base64Decode(encryptedBase64)
            if (combined.size <= SALT_SIZE + IV_SIZE) {
                throw IllegalArgumentException("Invalid encrypted payload length: ${combined.size}")
            }

            val salt = ByteArray(SALT_SIZE)
            val iv = ByteArray(IV_SIZE)
            val cipherText = ByteArray(combined.size - SALT_SIZE - IV_SIZE)

            System.arraycopy(combined, 0, salt, 0, SALT_SIZE)
            System.arraycopy(combined, SALT_SIZE, iv, 0, IV_SIZE)
            System.arraycopy(combined, SALT_SIZE + IV_SIZE, cipherText, 0, cipherText.size)

            val secretKey = deriveKeyFromPassword(password, salt)
            val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
            val parameterSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec)

            val decryptedBytes = cipher.doFinal(cipherText)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            throw SecurityException("Password-based decryption failed", e)
        }
    }

    /**
     * Backward compatible lightweight encrypt/decrypt for legacy values.
     */
    fun encrypt(plainText: String, password: String): String = encryptWithPassword(plainText, password)
    fun decrypt(encryptedText: String, password: String): String = decryptWithPassword(encryptedText, password)

    private fun deriveKeyFromPassword(password: String, salt: ByteArray): SecretKey {
        val factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_SIZE)
        val tmp = factory.generateSecret(spec)
        return SecretKeySpec(tmp.encoded, "AES")
    }
}
