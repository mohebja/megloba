package com.global.sms.engine.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.global.sms.engine.importer.SmsImporter

class SmsImportWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val count = SmsImporter.importSystemSms(applicationContext) { _, _, _ -> }
            Result.success()
        } catch (e: Exception) {
            Log.e("SmsImportWorker", "SmsImportWorker failed to import SMS", e)
            Result.retry()
        }
    }
}
