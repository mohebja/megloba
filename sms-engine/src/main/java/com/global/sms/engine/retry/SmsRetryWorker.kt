package com.global.sms.engine.retry

import android.content.Context
import android.telephony.SmsManager
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.global.sms.core.security.FieldEncryptionManager
import com.global.sms.data.db.GlobalSmsDatabase
import com.global.sms.data.entity.MessageStatus
import com.global.sms.data.entity.MessageType
import com.global.sms.engine.sender.SmsSender
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SmsRetryWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val TAG = "SmsRetryWorker"
        const val KEY_MESSAGE_ID = "KEY_MESSAGE_ID"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val messageId = inputData.getLong(KEY_MESSAGE_ID, -1L)
        if (messageId == -1L) return@withContext Result.failure()

        Log.d(TAG, "Executing retry worker for messageId=$messageId")

        val db = GlobalSmsDatabase.getInstance(applicationContext)
        val messageDao = db.messageDao()
        val message = messageDao.getMessageById(messageId) ?: return@withContext Result.failure()

        // If message is already sent or delivered, no retry needed
        if (message.deliveryStatus == MessageStatus.SENT.code || message.deliveryStatus == MessageStatus.DELIVERED.code) {
            return@withContext Result.success()
        }

        val decryptedMessage = FieldEncryptionManager.decryptMessage(message)

        val result = SmsSender.sendSms(
            context = applicationContext,
            messageId = decryptedMessage.id,
            address = decryptedMessage.address,
            body = decryptedMessage.body,
            subId = decryptedMessage.subId,
            simSlot = decryptedMessage.simSlot
        )

        return@withContext if (result.isSuccess) {
            Result.success()
        } else {
            if (message.retryCount >= message.maxRetries) {
                messageDao.updateMessageDeliveryAndType(
                    messageId = message.id,
                    status = MessageStatus.FAILED.code,
                    type = MessageType.FAILED.code
                )
            } else {
                SmsRetryManager.handleSendFailure(applicationContext, message.id, result.errorCode)
            }
            Result.failure()
        }
    }
}
