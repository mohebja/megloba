package com.global.sms.core.message

import java.io.File

data class ExtendedMessageMeta(
    val messageId: String,
    val isPinned: Boolean = false,
    val isStarred: Boolean = false,
    val isBookmarked: Boolean = false,
    val userNote: String? = null,
    val reminderTimestamp: Long? = null,
    val isConvertedToTask: Boolean = false,
    val taskTitle: String? = null
)

data class MessageExportResult(
    val file: File,
    val format: String, // "TXT", "PDF"
    val sizeBytes: Long,
    val exportTimestamp: Long = System.currentTimeMillis()
)

class AdvancedMessageOperationsEngine {

    private val metadataMap = mutableMapOf<String, ExtendedMessageMeta>()

    fun togglePin(messageId: String): Boolean {
        val current = metadataMap[messageId] ?: ExtendedMessageMeta(messageId)
        val updated = current.copy(isPinned = !current.isPinned)
        metadataMap[messageId] = updated
        return updated.isPinned
    }

    fun toggleStar(messageId: String): Boolean {
        val current = metadataMap[messageId] ?: ExtendedMessageMeta(messageId)
        val updated = current.copy(isStarred = !current.isStarred)
        metadataMap[messageId] = updated
        return updated.isStarred
    }

    fun toggleBookmark(messageId: String): Boolean {
        val current = metadataMap[messageId] ?: ExtendedMessageMeta(messageId)
        val updated = current.copy(isBookmarked = !current.isBookmarked)
        metadataMap[messageId] = updated
        return updated.isBookmarked
    }

    fun addUserNote(messageId: String, note: String): ExtendedMessageMeta {
        val current = metadataMap[messageId] ?: ExtendedMessageMeta(messageId)
        val updated = current.copy(userNote = note)
        metadataMap[messageId] = updated
        return updated
    }

    fun setReminder(messageId: String, timeInMillis: Long): ExtendedMessageMeta {
        val current = metadataMap[messageId] ?: ExtendedMessageMeta(messageId)
        val updated = current.copy(reminderTimestamp = timeInMillis)
        metadataMap[messageId] = updated
        return updated
    }

    fun convertToTask(messageId: String, taskTitle: String): ExtendedMessageMeta {
        val current = metadataMap[messageId] ?: ExtendedMessageMeta(messageId)
        val updated = current.copy(isConvertedToTask = true, taskTitle = taskTitle)
        metadataMap[messageId] = updated
        return updated
    }

    fun exportMessageToText(sender: String, body: String, destinationDir: File): MessageExportResult {
        if (!destinationDir.exists()) destinationDir.mkdirs()
        val file = File(destinationDir, "SMS_Export_${System.currentTimeMillis()}.txt")
        file.writeText("فرستنده: $sender\nتاریخ: ${System.currentTimeMillis()}\nمتن پیام:\n$body\n\n--- صادر شده توسط Global SMS ---")
        return MessageExportResult(file, "TXT", file.length())
    }

    fun exportMessageToPdfSimulated(sender: String, body: String, destinationDir: File): MessageExportResult {
        if (!destinationDir.exists()) destinationDir.mkdirs()
        val file = File(destinationDir, "SMS_Export_${System.currentTimeMillis()}.pdf")
        file.writeBytes("PDF_HEADER_GLOBAL_SMS_EXPORTER\nSender: $sender\nBody: $body".toByteArray())
        return MessageExportResult(file, "PDF", file.length())
    }

    fun getMeta(messageId: String): ExtendedMessageMeta {
        return metadataMap[messageId] ?: ExtendedMessageMeta(messageId)
    }
}
