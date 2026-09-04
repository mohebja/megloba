package com.global.sms.core.ai.privacy

import java.security.MessageDigest
import java.util.Locale

object AiPrivacyController {

    private const val MASK_CHAR = "*"

    /**
     * Anonymizes sensitive phone numbers and personal names before processing.
     */
    fun anonymizeAddress(address: String): String {
        val trimmed = address.trim()
        return if (trimmed.length > 6) {
            val start = trimmed.take(3)
            val end = trimmed.takeLast(2)
            "$start***$end"
        } else {
            trimmed
        }
    }

    /**
     * Masks card or account numbers found in text.
     */
    fun maskCardNumbers(body: String): String {
        val cardRegex = Regex("\\b\\d{4}[\\s-]?\\d{4}[\\s-]?\\d{4}[\\s-]?\\d{4}\\b")
        return cardRegex.replace(body) { match ->
            val clean = match.value.replace("-", "").replace(" ", "")
            val start = clean.take(4)
            val end = clean.takeLast(4)
            "$start-****-****-$end"
        }
    }

    /**
     * Generates an encrypted/hashed token for local AI metadata tracking.
     */
    fun hashMessageId(messageId: Long): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val bytes = digest.digest(messageId.toString().toByteArray(Charsets.UTF_8))
            bytes.joinToString("") { "%02x".format(it) }.take(16)
        } catch (e: Exception) {
            "token_${messageId.hashCode()}"
        }
    }

    /**
     * Verifies if cloud processing is permitted based on user privacy preferences.
     */
    fun isCloudProcessingAllowed(localProcessingOnly: Boolean): Boolean {
        return !localProcessingOnly
    }
}
