package com.global.sms.security.backup

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.global.sms.data.db.GlobalSmsDatabase
import com.global.sms.security.keystore.KeyStoreManager
import com.global.sms.security.prefs.SecurePreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AutoBackupWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val securePrefs = SecurePreferencesManager(applicationContext)
            if (!securePrefs.isAutoBackupEnabled) {
                Log.d(TAG, "Auto backup is disabled in settings, skipping periodic work")
                return@withContext Result.success(workDataOf("status" to "skipped_disabled"))
            }

            val db = GlobalSmsDatabase.getInstance(applicationContext)
            val messages = db.messageDao().getAllMessagesSync()
            if (messages.isEmpty()) {
                Log.d(TAG, "No messages found to back up")
                securePrefs.lastAutoBackupTimestamp = System.currentTimeMillis()
                return@withContext Result.success(workDataOf("status" to "empty_messages"))
            }

            val backupItems = messages.map {
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

            val model = EnterpriseBackupModel(
                version = 1,
                timestamp = System.currentTimeMillis(),
                messages = backupItems
            )

            val masterKeyBytes = KeyStoreManager.getOrCreateAutoBackupMasterKeyBytes(applicationContext)
            val backupFile = EncryptedBackupManager.createEncryptedBackupWithMasterKey(
                context = applicationContext,
                model = model,
                masterKey = masterKeyBytes
            )

            val now = System.currentTimeMillis()
            securePrefs.lastAutoBackupTimestamp = now
            Log.i(TAG, "Auto-backup successfully completed: ${backupFile.name} (${messages.size} messages, ${backupFile.length()} bytes)")

            Result.success(
                workDataOf(
                    "messageCount" to messages.size,
                    "fileSizeBytes" to backupFile.length(),
                    "timestamp" to now
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error executing periodic auto backup", e)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "AutoBackupWorker"
    }
}
