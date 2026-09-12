package com.global.sms.security.backup

import android.content.Context
import java.io.File

object EncryptedBackupManager {

    private val backupProvider = BackupProvider()

    fun createEncryptedBackup(
        context: Context,
        model: EnterpriseBackupModel,
        password: String,
        targetFile: File? = null
    ): File {
        val backupDir = File(context.filesDir, "backups")
        if (!backupDir.exists()) backupDir.mkdirs()

        val destFile = targetFile ?: File(
            backupDir,
            "backup_${System.currentTimeMillis()}.gsms"
        )

        val jsonPayload = model.toJson()
        backupProvider.createEncryptedBackup(destFile, jsonPayload, password)
        return destFile
    }

    fun restoreEncryptedBackup(
        backupFile: File,
        password: String
    ): EnterpriseBackupModel {
        val jsonPayload = backupProvider.restoreEncryptedBackup(backupFile, password)
        return EnterpriseBackupModel.fromJson(jsonPayload)
    }

    fun inspectBackup(
        backupFile: File,
        password: String
    ): EnterpriseBackupModel? {
        return try {
            restoreEncryptedBackup(backupFile, password)
        } catch (e: Exception) {
            null
        }
    }
}
