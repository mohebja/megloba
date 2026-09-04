package com.global.sms.core.ai.reminder

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.global.sms.core.ai.copilot.EntityExtractionEngine
import java.util.concurrent.TimeUnit

enum class ReminderType {
    BANK_PAYMENT,
    DELIVERY,
    GENERIC_TASK
}

data class ScheduledReminderInfo(
    val reminderId: String,
    val title: String,
    val description: String,
    val triggerTimeMillis: Long,
    val type: ReminderType
)

class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        val title = inputData.getString("title") ?: "یادآوری جدید"
        val message = inputData.getString("message") ?: ""
        // In actual app execution, SmartNotificationManager triggers notification
        return Result.success()
    }
}

class SmartReminderManager(private val context: Context) {

    private val workManager = WorkManager.getInstance(context)

    fun scheduleAutomaticReminderFromSms(senderAddress: String, messageText: String): ScheduledReminderInfo? {
        val entities = EntityExtractionEngine.extractEntities(messageText)

        if (messageText.contains("پرداخت") || messageText.contains("قبض") || messageText.contains("صورتحساب")) {
            val amount = entities.amounts.firstOrNull() ?: ""
            val title = "یادآوری پرداخت قبض/مبلغ"
            val desc = "پرداخت $amount به $senderAddress".trim()
            val delayMillis = 86400000L // 24 hours
            scheduleWork(title, desc, delayMillis)
            return ScheduledReminderInfo(
                reminderId = "REM_BANK_${System.currentTimeMillis()}",
                title = title,
                description = desc,
                triggerTimeMillis = System.currentTimeMillis() + delayMillis,
                type = ReminderType.BANK_PAYMENT
            )
        } else if (messageText.contains("تحویل") || messageText.contains("مرسوله") || messageText.contains("پست")) {
            val trackCode = entities.trackingCodes.firstOrNull() ?: ""
            val title = "یادآوری تحویل مرسوله"
            val desc = "مرسوله $trackCode از $senderAddress".trim()
            val delayMillis = 43200000L // 12 hours
            scheduleWork(title, desc, delayMillis)
            return ScheduledReminderInfo(
                reminderId = "REM_DELIV_${System.currentTimeMillis()}",
                title = title,
                description = desc,
                triggerTimeMillis = System.currentTimeMillis() + delayMillis,
                type = ReminderType.DELIVERY
            )
        }

        return null
    }

    fun snoozeReminder(reminderId: String, snoozeMinutes: Long = 15) {
        scheduleWork("یادآوری تکرار شده", "یادآوری به تعویق افتاده", snoozeMinutes * 60 * 1000L)
    }

    fun completeReminder(reminderId: String) {
        workManager.cancelAllWorkByTag(reminderId)
    }

    fun dismissReminder(reminderId: String) {
        workManager.cancelAllWorkByTag(reminderId)
    }

    private fun scheduleWork(title: String, message: String, delayMillis: Long) {
        val inputData = Data.Builder()
            .putString("title", title)
            .putString("message", message)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .build()

        workManager.enqueue(workRequest)
    }
}
