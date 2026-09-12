package com.global.sms.security.backup

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

data class BackupHeader(
    val version: Int = 1,
    val timestamp: Long = System.currentTimeMillis(),
    val integrityHashSha256: String,
    val saltHex: String,
    val ivHex: String
)

class BackupProvider(private val masterKey: ByteArray? = null) {

    companion object {
        private const val MAGIC_HEADER = "GSMS"
        private const val CURRENT_VERSION = 1
        private const val GCM_TAG_LENGTH = 128
        private const val IV_LENGTH = 12
        private const val SALT_LENGTH = 16
        private const val PBKDF2_ITERATIONS = 210000
        private const val KEY_LENGTH = 256
    }

    private val secureRandom = SecureRandom()

    fun deriveKeyFromPassword(password: CharArray, salt: ByteArray): SecretKey {
        val spec = PBEKeySpec(password, salt, PBKDF2_ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val secretBytes = factory.generateSecret(spec).encoded
        return SecretKeySpec(secretBytes, "AES")
    }

    fun createEncryptedBackup(backupFile: File, jsonPayload: String, password: String? = null): BackupHeader {
        val plaintextBytes = jsonPayload.toByteArray(Charsets.UTF_8)
        val sha256Digest = MessageDigest.getInstance("SHA-256").digest(plaintextBytes)
        val integrityHash = sha256Digest.joinToString("") { "%02x".format(it) }

        val salt = ByteArray(SALT_LENGTH).also { secureRandom.nextBytes(it) }
        val iv = ByteArray(IV_LENGTH).also { secureRandom.nextBytes(it) }

        val secretKey: SecretKey = if (password != null) {
            deriveKeyFromPassword(password.toCharArray(), salt)
        } else if (masterKey != null) {
            SecretKeySpec(masterKey, "AES")
        } else {
            throw IllegalArgumentException("Either password or masterKey must be provided for encryption")
        }

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec)
        cipher.updateAAD(MAGIC_HEADER.toByteArray(Charsets.UTF_8))
        val ciphertext = cipher.doFinal(plaintextBytes)

        FileOutputStream(backupFile).use { fos ->
            fos.write(MAGIC_HEADER.toByteArray(Charsets.UTF_8))
            val headerBuffer = ByteBuffer.allocate(4 + 8 + 4 + SALT_LENGTH + 4 + IV_LENGTH + 4 + sha256Digest.size)
            headerBuffer.putInt(CURRENT_VERSION)
            headerBuffer.putLong(System.currentTimeMillis())
            headerBuffer.putInt(SALT_LENGTH)
            headerBuffer.put(salt)
            headerBuffer.putInt(IV_LENGTH)
            headerBuffer.put(iv)
            headerBuffer.putInt(sha256Digest.size)
            headerBuffer.put(sha256Digest)
            val headerBytes = headerBuffer.array()

            fos.write(ByteBuffer.allocate(4).putInt(headerBytes.size).array())
            fos.write(headerBytes)
            fos.write(ciphertext)
        }

        return BackupHeader(
            version = CURRENT_VERSION,
            timestamp = System.currentTimeMillis(),
            integrityHashSha256 = integrityHash,
            saltHex = salt.joinToString("") { "%02x".format(it) },
            ivHex = iv.joinToString("") { "%02x".format(it) }
        )
    }

    fun restoreEncryptedBackup(
        backupFile: File,
        password: String? = null,
        expectedHash: String? = null
    ): String {
        if (!backupFile.exists() || backupFile.length() < 8) {
            throw IllegalArgumentException("Invalid backup file: file does not exist or is too small")
        }

        FileInputStream(backupFile).use { fis ->
            val magic = ByteArray(4)
            fis.read(magic)
            val magicStr = String(magic, Charsets.UTF_8)
            if (magicStr != MAGIC_HEADER) {
                throw IllegalArgumentException("Invalid backup container format: expected $MAGIC_HEADER magic header")
            }

            val headerSizeBuffer = ByteArray(4)
            fis.read(headerSizeBuffer)
            val headerSize = ByteBuffer.wrap(headerSizeBuffer).int

            val headerBytes = ByteArray(headerSize)
            fis.read(headerBytes)
            val headerBuffer = ByteBuffer.wrap(headerBytes)

            val version = headerBuffer.int
            val timestamp = headerBuffer.long
            val saltLen = headerBuffer.int
            val salt = ByteArray(saltLen).also { headerBuffer.get(it) }
            val ivLen = headerBuffer.int
            val iv = ByteArray(ivLen).also { headerBuffer.get(it) }
            val hashLen = headerBuffer.int
            val sha256Expected = ByteArray(hashLen).also { headerBuffer.get(it) }
            val calculatedExpectedHash = sha256Expected.joinToString("") { "%02x".format(it) }

            val ciphertext = fis.readBytes()

            val secretKey: SecretKey = if (password != null) {
                deriveKeyFromPassword(password.toCharArray(), salt)
            } else if (masterKey != null) {
                SecretKeySpec(masterKey, "AES")
            } else {
                throw IllegalArgumentException("No password or masterKey provided for decryption")
            }

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
            cipher.updateAAD(MAGIC_HEADER.toByteArray(Charsets.UTF_8))
            val decryptedBytes = cipher.doFinal(ciphertext)

            val actualDigest = MessageDigest.getInstance("SHA-256").digest(decryptedBytes)
            val actualHash = actualDigest.joinToString("") { "%02x".format(it) }

            if (actualHash != calculatedExpectedHash) {
                throw IllegalStateException("Backup integrity check failed! Computed: $actualHash, Header: $calculatedExpectedHash")
            }
            if (expectedHash != null && actualHash != expectedHash) {
                throw IllegalStateException("Integrity hash does not match expected hash!")
            }

            return String(decryptedBytes, Charsets.UTF_8)
        }
    }
}
