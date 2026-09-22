package com.global.sms.data.db.crypto

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.security.GeneralSecurityException
import java.security.KeyStore
import java.security.SecureRandom
import java.security.UnrecoverableKeyException
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Owns the 256-bit key that protects the SQLCipher database.
 *
 * The raw key is never stored in clear text. It is wrapped with AES-256-GCM by a non-exportable
 * Android Keystore key ([WRAP_KEY_ALIAS]) and the wrapped blob is written to `noBackupFilesDir`,
 * which Android never includes in cloud backups or device-to-device transfers. A restored copy of
 * the database is therefore unreadable on purpose: the wrapping key cannot leave the device.
 *
 * The wrapping key deliberately does NOT require user authentication: incoming SMS must be stored
 * while the phone is locked.
 */
internal object DatabaseKeyStore {

    /** Result of trying to read the stored database key. */
    sealed interface Load {
        class Ok(val key: ByteArray) : Load

        /** No key has been stored yet. */
        data object Missing : Load

        /** A key blob exists but can never be unwrapped again (alias gone, tag mismatch, ...). */
        data object Unrecoverable : Load
    }

    private const val TAG = "DatabaseKeyStore"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val WRAP_KEY_ALIAS = "GlobalSmsDbWrapKey_v1"
    private const val KEY_FILE_NAME = "global_sms_db_key_v1.bin"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val KEY_SIZE_BYTES = 32
    private const val IV_SIZE_BYTES = 12
    private const val GCM_TAG_BITS = 128
    private val AAD = "com.global.sms.database.key.v1".toByteArray(Charsets.UTF_8)

    private fun keyFile(context: Context) = File(context.noBackupFilesDir, KEY_FILE_NAME)

    /**
     * Reads and unwraps the stored key. Transient Keystore failures are NOT mapped to
     * [Load.Unrecoverable]; they propagate so the caller never destroys data because of a hiccup.
     */
    @Synchronized
    fun load(context: Context): Load {
        val file = keyFile(context)
        if (!file.isFile) return Load.Missing

        val blob = file.readBytes()
        if (blob.size < IV_SIZE_BYTES + GCM_TAG_BITS / 8 + 1) return Load.Unrecoverable
        val wrapKey = findWrapKey() ?: return Load.Unrecoverable

        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, wrapKey, GCMParameterSpec(GCM_TAG_BITS, blob, 0, IV_SIZE_BYTES))
            cipher.updateAAD(AAD)
            val key = cipher.doFinal(blob, IV_SIZE_BYTES, blob.size - IV_SIZE_BYTES)
            if (key.size == KEY_SIZE_BYTES) Load.Ok(key) else Load.Unrecoverable
        } catch (e: AEADBadTagException) {
            Log.e(TAG, "Stored database key failed authentication", e)
            Load.Unrecoverable
        } catch (e: KeyPermanentlyInvalidatedException) {
            Log.e(TAG, "Keystore wrapping key was permanently invalidated", e)
            Load.Unrecoverable
        }
    }

    /** Generates a fresh random database key, stores it wrapped, and returns the raw key. */
    @Synchronized
    fun createNew(context: Context): ByteArray {
        val key = ByteArray(KEY_SIZE_BYTES).also { SecureRandom().nextBytes(it) }
        atomicWrite(keyFile(context), wrap(key))
        return key
    }

    private fun wrap(key: ByteArray): ByteArray {
        findWrapKey()?.let { existing ->
            try {
                return wrapWith(existing, key)
            } catch (e: GeneralSecurityException) {
                Log.w(TAG, "Existing Keystore wrapping key is unusable; replacing it", e)
                deleteWrapKey()
            }
        }
        return wrapWith(generateWrapKey(), key)
    }

    private fun wrapWith(wrapKey: SecretKey, key: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, wrapKey) // the Keystore generates the IV
        cipher.updateAAD(AAD)
        val cipherText = cipher.doFinal(key)
        return cipher.iv + cipherText
    }

    private fun keyStore(): KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

    private fun findWrapKey(): SecretKey? =
        try {
            keyStore().getKey(WRAP_KEY_ALIAS, null) as? SecretKey
        } catch (e: UnrecoverableKeyException) {
            Log.e(TAG, "Keystore wrapping key cannot be recovered", e)
            null
        }

    private fun deleteWrapKey() {
        try {
            keyStore().deleteEntry(WRAP_KEY_ALIAS)
        } catch (e: Exception) {
            Log.w(TAG, "Could not delete the Keystore wrapping key", e)
        }
    }

    private fun generateWrapKey(): SecretKey {
        val spec = KeyGenParameterSpec.Builder(
            WRAP_KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        generator.init(spec)
        return generator.generateKey()
    }

    private fun atomicWrite(target: File, bytes: ByteArray) {
        target.parentFile?.mkdirs()
        val tmp = File(target.path + ".tmp")
        FileOutputStream(tmp).use { out ->
            out.write(bytes)
            out.fd.sync()
        }
        if (!tmp.renameTo(target)) {
            tmp.delete()
            throw java.io.IOException("Could not store the wrapped database key")
        }
    }
}
