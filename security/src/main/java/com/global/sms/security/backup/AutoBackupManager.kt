package com.global.sms.security.backup

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.global.sms.security.prefs.SecurePreferencesManager
import java.util.concurrent.TimeUnit

object AutoBackupManager {
    const val AUTO_BACKUP_WORK_NAME = "com.global.sms.security.backup.PERIODIC_AUTO_BACKUP"
    private const val TAG = "AutoBackupManager"

    fun init(context: Context) {
        val securePrefs = SecurePreferencesManager(context)
        if (securePrefs.isAutoBackupEnabled) {
            schedulePeriodicBackup(context, securePrefs.autoBackupIntervalHours)
        } else {
            cancelPeriodicBackup(context)
        }
    }

    fun isAutoBackupEnabled(context: Context): Boolean {
        return SecurePreferencesManager(context).isAutoBackupEnabled
    }

    fun getAutoBackupIntervalHours(context: Context): Long {
        return SecurePreferencesManager(context).autoBackupIntervalHours
    }

    fun getLastBackupTimestamp(context: Context): Long {
        return SecurePreferencesManager(context).lastAutoBackupTimestamp
    }

    fun setAutoBackupEnabled(context: Context, enabled: Boolean, intervalHours: Long = 24L) {
        val securePrefs = SecurePreferencesManager(context)
        securePrefs.isAutoBackupEnabled = enabled
        securePrefs.autoBackupIntervalHours = intervalHours
        if (enabled) {
            schedulePeriodicBackup(context, intervalHours)
            Log.i(TAG, "Auto-backup enabled with interval $intervalHours hours")
        } else {
            cancelPeriodicBackup(context)
            Log.i(TAG, "Auto-backup disabled and periodic work cancelled")
        }
    }

    fun schedulePeriodicBackup(context: Context, intervalHours: Long = 24L) {
        try {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .setRequiresStorageNotLow(true)
                .build()

            val effectiveHours = intervalHours.coerceAtLeast(12L)
            val workRequest = PeriodicWorkRequestBuilder<AutoBackupWorker>(
                effectiveHours, TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .addTag(AUTO_BACKUP_WORK_NAME)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                AUTO_BACKUP_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
            )
        } catch (e: Exception) {
            Log.e(TAG, "Could not enqueue periodic auto backup work", e)
        }
    }

    fun cancelPeriodicBackup(context: Context) {
        try {
            WorkManager.getInstance(context).cancelUniqueWork(AUTO_BACKUP_WORK_NAME)
        } catch (e: Exception) {
            Log.e(TAG, "Could not cancel auto backup work", e)
        }
    }
}
