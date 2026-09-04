package com.global.sms.engine.retry

import android.content.Context
import android.telephony.SmsManager
import android.util.Log
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.global.sms.data.db.GlobalSmsDatabase
import com.global.sms.data.entity.MessageStatus
import com.global.sms.data.entity.MessageType
import java.util.concurrent.TimeUnit

object SmsRetryManager {

    private const val TAG = "SmsRetryManager"
    private const val INITIAL_RETRY_DELAY_SECONDS = 10L
    private const val MAX_RETRIES = 3

    suspend fun handleSendFailure(context: Context, messageId: Long, errorCode: Int) {
        val db = GlobalSmsDatabase.getInstance(context)
        val messageDao = db.messageDao()
        val message = messageDao.getMessageById(messageId) ?: return

        val currentRetries = message.retryCount
        val maxRetries = message.maxRetries.coerceAtLeast(MAX_RETRIES)

        val isRetriable = isRetriableError(errorCode)

        if (isRetriable && currentRetries < maxRetries) {
            val newRetryCount = currentRetries + 1
            messageDao.updateRetryCount(messageId, newRetryCount)
            messageDao.updateDeliveryStatus(messageId, MessageStatus.RETRYING.code)

            val backoffSeconds = INITIAL_RETRY_DELAY_SECONDS * (1 shl (newRetryCount - 1))
            Log.d(TAG, "Scheduling retry #$newRetryCount for message $messageId in $backoffSeconds seconds")

            scheduleRetryWorker(context, messageId, backoffSeconds)
        } else {
            Log.w(TAG, "Max retries reached or non-retriable error ($errorCode) for message $messageId")
            messageDao.updateMessageDeliveryAndType(
                messageId = messageId,
                status = MessageStatus.FAILED.code,
                type = MessageType.FAILED.code
            )
        }
    }

    private fun scheduleRetryWorker(context: Context, messageId: Long, delaySeconds: Long) {
        try {
            val data = Data.Builder()
                .putLong(SmsRetryWorker.KEY_MESSAGE_ID, messageId)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<SmsRetryWorker>()
                .setInputData(data)
                .setInitialDelay(delaySeconds, TimeUnit.SECONDS)
                .addTag("sms_retry_$messageId")
                .build()

            WorkManager.getInstance(context).enqueue(workRequest)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule retry worker via WorkManager", e)
        }
    }

    private fun isRetriableError(errorCode: Int): Boolean {
        return when (errorCode) {
            SmsManager.RESULT_ERROR_GENERIC_FAILURE,
            SmsManager.RESULT_ERROR_NO_SERVICE,
            SmsManager.RESULT_ERROR_NULL_PDU,
            SmsManager.RESULT_ERROR_RADIO_OFF,
            SmsManager.RESULT_ERROR_LIMIT_EXCEEDED -> true
            else -> true // Default to retriable unless known permanent error
        }
    }
}
