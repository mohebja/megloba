package com.global.sms.security.vault

import android.content.Context
import com.global.sms.data.entity.MessageEntity
import com.global.sms.security.crypto.CryptoManager
import com.global.sms.security.prefs.SecurePreferencesManager
import java.security.MessageDigest

/**
 * Private Vault Security Core.
 * Manages AES-256 message encryption inside the private vault, PIN authentication, and session state.
 */
class PrivateVaultSecurityManager(context: Context) {

    private val securePrefs = SecurePreferencesManager(context)
    private var isUnlockedSession = false

    fun isVaultSetup(): Boolean {
        return !securePrefs.vaultPasscodeHash.isNullOrEmpty() || !securePrefs.pinHash.isNullOrEmpty()
    }

    fun setVaultPasscode(passcode: String) {
        val hash = hashPasscode(passcode)
        securePrefs.vaultPasscodeHash = hash
        securePrefs.pinHash = hash
    }

    fun verifyPasscode(inputPasscode: String): Boolean {
        val storedHash = securePrefs.vaultPasscodeHash ?: securePrefs.pinHash
        if (storedHash.isNullOrEmpty()) {
            return false
        }
        val inputHash = hashPasscode(inputPasscode)
        val isValid = storedHash == inputHash
        if (isValid) {
            isUnlockedSession = true
        }
        return isValid
    }

    fun isUnlocked(): Boolean = isUnlockedSession

    fun lockVault() {
        isUnlockedSession = false
    }

    /**
     * Encrypts message content before persisting into Private Vault.
     */
    fun encryptVaultMessage(message: MessageEntity, passcode: String): MessageEntity {
        val encryptedBody = CryptoManager.encryptWithPassword(message.body, passcode)
        return message.copy(
            body = encryptedBody,
            isEncrypted = true,
            isHidden = true
        )
    }

    /**
     * Decrypts message content when viewing inside Private Vault.
     */
    fun decryptVaultMessage(message: MessageEntity, passcode: String): MessageEntity {
        if (!message.isEncrypted) return message
        val decryptedBody = CryptoManager.decryptWithPassword(message.body, passcode)
        return message.copy(body = decryptedBody)
    }

    private fun hashPasscode(passcode: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(passcode.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
