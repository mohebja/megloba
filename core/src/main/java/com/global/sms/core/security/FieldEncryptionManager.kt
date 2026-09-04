package com.global.sms.core.security

import com.global.sms.data.entity.ConversationEntity
import com.global.sms.data.entity.MessageEntity
import com.global.sms.data.entity.QuickReplyEntity
import com.global.sms.data.entity.ScheduledMessageEntity
import com.global.sms.security.crypto.CryptoManager

/**
 * Enterprise Field-Level Encryption Manager.
 *
 * Provides transparent AES-256-GCM hardware-backed field encryption for sensitive entity fields
 * (Message body, contact names, and conversation snippets) backed by the durable Android KeyStore
 * master key.
 */
object FieldEncryptionManager {

    private const val PREFIX = "enc:v1:"

    /**
     * Encrypts a plaintext string with Hardware-Backed AES-256-GCM via Android KeyStore.
     * Returns "enc:v1:<Base64(12-byte IV + Ciphertext + 16-byte Tag)>".
     *
     * Known Edge Case Note: If a plaintext string naturally begins with "enc:v1:",
     * it will be treated as already encrypted.
     */
    fun encrypt(plainText: String): String {
        if (plainText.isEmpty()) return plainText
        if (isEncrypted(plainText)) return plainText // Idempotent

        val ciphertextBase64 = CryptoManager.encryptHardware(plainText)
        return "$PREFIX$ciphertextBase64"
    }

    /**
     * Decrypts a hardware-encrypted string using Android KeyStore master key.
     * If the string does not have the "enc:v1:" prefix, it is treated as legacy plaintext and returned as-is.
     * If decryption fails due to key mismatch or corrupted ciphertext, it throws a SecurityException (fail-closed).
     */
    fun decrypt(encryptedOrPlain: String): String {
        if (!isEncrypted(encryptedOrPlain)) {
            return encryptedOrPlain
        }

        val base64Payload = encryptedOrPlain.removePrefix(PREFIX)
        return try {
            CryptoManager.decryptHardware(base64Payload)
        } catch (e: Exception) {
            throw SecurityException("Field decryption failed: corrupted ciphertext or invalid KeyStore master key", e)
        }
    }

    fun isEncrypted(text: String?): Boolean {
        return text != null && text.startsWith(PREFIX)
    }

    fun encryptNullable(plainText: String?): String? {
        if (plainText == null) return null
        return encrypt(plainText)
    }

    fun decryptNullable(encryptedOrPlain: String?): String? {
        if (encryptedOrPlain == null) return null
        return decrypt(encryptedOrPlain)
    }

    /**
     * Redacts phone numbers or identifiers for privacy-preserving, zero-leak application logs.
     */
    fun redactedForLog(address: String?): String {
        if (address.isNullOrBlank()) return "***"
        val trimmed = address.trim()
        return if (trimmed.length <= 4) {
            "***"
        } else {
            "${trimmed.take(4)}***"
        }
    }

    // --- Entity Level Wrappers ---

    fun encryptMessage(message: MessageEntity): MessageEntity {
        return message.copy(
            body = encrypt(message.body),
            isEncrypted = true
        )
    }

    fun decryptMessage(message: MessageEntity): MessageEntity {
        return message.copy(
            body = decrypt(message.body),
            isEncrypted = false
        )
    }

    fun encryptConversation(conversation: ConversationEntity): ConversationEntity {
        return conversation.copy(
            contactName = encryptNullable(conversation.contactName),
            lastMessage = encrypt(conversation.lastMessage)
        )
    }

    fun decryptConversation(conversation: ConversationEntity): ConversationEntity {
        return conversation.copy(
            contactName = decryptNullable(conversation.contactName),
            lastMessage = decrypt(conversation.lastMessage)
        )
    }

    fun encryptScheduledMessage(scheduled: ScheduledMessageEntity): ScheduledMessageEntity {
        return scheduled.copy(body = encrypt(scheduled.body))
    }

    fun decryptScheduledMessage(scheduled: ScheduledMessageEntity): ScheduledMessageEntity {
        return scheduled.copy(body = decrypt(scheduled.body))
    }

    fun encryptQuickReply(reply: QuickReplyEntity): QuickReplyEntity {
        return reply.copy(content = encrypt(reply.content))
    }

    fun decryptQuickReply(reply: QuickReplyEntity): QuickReplyEntity {
        return reply.copy(content = decrypt(reply.content))
    }
}
