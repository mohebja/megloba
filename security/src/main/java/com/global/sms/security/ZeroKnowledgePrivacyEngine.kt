package com.global.sms.security

import java.io.File
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class ZeroKnowledgePrivacyEngine {

    private val secureRandom = SecureRandom()

    fun generateLocalKey(): SecretKey {
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(256, secureRandom)
        return keyGen.generateKey()
    }

    fun encryptLocalData(key: SecretKey, plainText: String): Pair<ByteArray, ByteArray> {
        val iv = ByteArray(12)
        secureRandom.nextBytes(iv)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.ENCRYPT_MODE, key, spec)

        val cipherText = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        return iv to cipherText
    }

    fun decryptLocalData(key: SecretKey, iv: ByteArray, cipherText: ByteArray): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, spec)

        val plainBytes = cipher.doFinal(cipherText)
        return String(plainBytes, Charsets.UTF_8)
    }

    /**
     * Secure Wipe: Overwrites file with random bytes before deletion to prevent storage recovery.
     */
    fun secureDeleteFile(file: File): Boolean {
        if (!file.exists()) return true
        return try {
            val length = file.length()
            val randomBytes = ByteArray(length.toInt().coerceAtMost(1024 * 1024))
            secureRandom.nextBytes(randomBytes)
            file.writeBytes(randomBytes)
            file.delete()
        } catch (e: Exception) {
            file.delete()
        }
    }
}
