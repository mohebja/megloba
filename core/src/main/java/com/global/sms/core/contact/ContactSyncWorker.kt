package com.global.sms.core.contact

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf

/**
 * Background WorkManager worker for performing contact synchronization.
 */
class ContactSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        if (!ContactPermissionHelper.hasReadContactsPermission(applicationContext)) {
            return Result.failure(workDataOf("error" to "Read Contacts permission not granted"))
        }

        return try {
            val startTime = System.currentTimeMillis()
            val stats = ContactSyncManager.getInstance(applicationContext).performSyncInternal(force = true)
            val duration = System.currentTimeMillis() - startTime

            Result.success(
                workDataOf(
                    "totalContacts" to stats.totalContactsCount,
                    "addedCount" to stats.lastDiff.addedCount,
                    "removedCount" to stats.lastDiff.removedCount,
                    "updatedCount" to stats.lastDiff.updatedCount,
                    "durationMs" to duration,
                    "timestamp" to stats.lastSyncTimeMillis
                )
            )
        } catch (e: Exception) {
            Log.e("ContactSyncWorker", "Error executing contact sync in background worker", e)
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME_PERIODIC = "global_sms_contact_sync_periodic"
        const val WORK_NAME_ONE_TIME = "global_sms_contact_sync_one_time"
    }
}
