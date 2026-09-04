package com.global.sms.database

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.ListenableWorker.Result
import com.global.sms.data.db.DatabaseMaintenanceManager
import com.global.sms.data.db.GlobalSmsDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DatabaseMaintenanceWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val db = GlobalSmsDatabase.getInstance(applicationContext)
            DatabaseMaintenanceManager.runMaintenance(db)
            Result.success()
        } catch (e: Exception) {
            Log.e("DatabaseMaintWorker", "Database maintenance worker failed", e)
            Result.retry()
        }
    }
}
