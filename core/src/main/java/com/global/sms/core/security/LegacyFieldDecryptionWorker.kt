package com.global.sms.core.security

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/** Runs [LegacyFieldDecryptionMigration] in the background; it retries until every legacy row is converted. */
class LegacyFieldDecryptionWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val outcome = LegacyFieldDecryptionMigration.run(applicationContext)
            if (outcome.complete) Result.success() else Result.retry()
        } catch (e: Exception) {
            Log.e(TAG, "Legacy field decryption failed; will retry", e)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "LegacyFieldDecryptWork"
        const val UNIQUE_WORK_NAME = "legacy_field_decryption"

        fun enqueueIfNeeded(context: Context) {
            val appContext = context.applicationContext
            if (LegacyFieldDecryptionMigration.isDone(appContext)) return
            val request = OneTimeWorkRequestBuilder<LegacyFieldDecryptionWorker>()
                .setBackoffCriteria(BackoffPolicy.LINEAR, 30, TimeUnit.SECONDS)
                .build()
            WorkManager.getInstance(appContext)
                .enqueueUniqueWork(UNIQUE_WORK_NAME, ExistingWorkPolicy.KEEP, request)
        }
    }
}
