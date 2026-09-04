package com.global.sms.engine.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.global.sms.data.db.GlobalSmsDatabase
import com.global.sms.engine.queue.SmsQueueManager
import com.global.sms.engine.sim.DualSimManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SmsSchedulerWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "SmsSchedulerWorker"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val scheduledId = inputData.getLong("SCHEDULED_MESSAGE_ID", -1L)
        if (scheduledId == -1L) return@withContext Result.failure()

        val db = GlobalSmsDatabase.getInstance(applicationContext)
        val dao = db.scheduledMessageDao()

        return@withContext try {
            val address = inputData.getString("ADDRESS") ?: return@withContext Result.failure()
            val body = inputData.getString("BODY") ?: return@withContext Result.failure()
            val simSlot = inputData.getInt("SIM_SLOT", 0)
            val subId = inputData.getInt("SUB_ID", -1)

            val effectiveSubId = if (subId >= 0) {
                subId
            } else if (applicationContext.checkSelfPermission(android.Manifest.permission.READ_PHONE_STATE) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                DualSimManager.getActiveSimCards(applicationContext).firstOrNull { it.slotIndex == simSlot }?.subscriptionId ?: -1
            } else {
                -1
            }

            Log.d(TAG, "Triggering scheduled message $scheduledId for ${address.take(4)}*** via subId=$effectiveSubId slot=$simSlot")

            // Enqueue message into database and dispatch send
            SmsQueueManager.enqueueMessage(
                context = applicationContext,
                address = address,
                body = body,
                simSlot = simSlot,
                subId = effectiveSubId
            )

            dao.updateStatus(scheduledId, "SENT")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to execute scheduled SMS for ID $scheduledId", e)
            dao.updateStatus(scheduledId, "FAILED")
            Result.retry()
        }
    }
}
