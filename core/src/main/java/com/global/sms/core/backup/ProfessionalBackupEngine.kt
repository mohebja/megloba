package com.global.sms.core.backup

import java.io.File

data class BackupInspectionPreview(
    val isValid: Boolean,
    val version: Int = 1,
    val timestamp: Long = 0L,
    val messageCount: Int = 0,
    val contactCount: Int = 0,
    val integrityHash: String = "",
    val errorMessage: String? = null
)

class ProfessionalBackupEngine(
    private val backupProvider: BackupProvider = BackupProvider()
) {
    fun exportEncryptedBackup(
        destinationFile: File,
        jsonContent: String,
        passphrase: String
    ): BackupHeader {
        return backupProvider.createEncryptedBackup(destinationFile, jsonContent, passphrase)
    }

    fun inspectBackup(backupFile: File, passphrase: String): BackupInspectionPreview {
        return try {
            val decryptedJson = backupProvider.restoreEncryptedBackup(backupFile, passphrase)
            val msgCount = Regex("\"body\"").findAll(decryptedJson).count()
            val contactCount = Regex("\"phoneNumber\"|\"phone\"").findAll(decryptedJson).count()
            BackupInspectionPreview(
                isValid = true,
                version = 1,
                timestamp = System.currentTimeMillis(),
                messageCount = msgCount,
                contactCount = contactCount,
                errorMessage = null
            )
        } catch (e: Exception) {
            BackupInspectionPreview(
                isValid = false,
                errorMessage = e.message ?: "Failed to decrypt backup"
            )
        }
    }

    fun restoreBackup(backupFile: File, passphrase: String): String {
        return backupProvider.restoreEncryptedBackup(backupFile, passphrase)
    }
}
