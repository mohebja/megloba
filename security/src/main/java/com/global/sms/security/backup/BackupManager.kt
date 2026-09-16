package com.global.sms.security.backup

import android.content.Context
import com.global.sms.data.entity.MessageEntity
import java.io.File

object BackupManager {
    fun exportBackup(context: Context, messages: List<MessageEntity>, password: String): File {
        val model = EnterpriseBackupModel(
            version = 1,
            timestamp = System.currentTimeMillis(),
            messages = messages.map {
                BackupMessageItem(
                    id = it.id,
                    threadId = it.threadId,
                    address = it.address,
                    body = it.body,
                    date = it.timestamp,
                    type = it.type,
                    read = if (it.isRead) 1 else 0,
                    status = it.deliveryStatus
                )
            }
        )
        return EncryptedBackupManager.createEncryptedBackup(context, model, password)
    }

    fun importBackup(backupFile: File, password: String): List<MessageEntity>? {
        return try {
            val model = EncryptedBackupManager.restoreEncryptedBackup(backupFile, password)
            model.messages.map {
                MessageEntity(
                    id = it.id,
                    threadId = it.threadId,
                    address = it.address,
                    body = it.body,
                    timestamp = it.date,
                    type = it.type,
                    isRead = it.read == 1,
                    deliveryStatus = it.status
                )
            }
        } catch (e: Exception) {
            null
        }
    }
}
