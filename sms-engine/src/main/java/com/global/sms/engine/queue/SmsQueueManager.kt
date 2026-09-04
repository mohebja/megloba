package com.global.sms.engine.queue

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.global.sms.core.security.FieldEncryptionManager
import com.global.sms.data.db.GlobalSmsDatabase
import com.global.sms.data.entity.ConversationEntity
import com.global.sms.data.entity.MessageCategory
import com.global.sms.data.entity.MessageEntity
import com.global.sms.data.entity.MessageStatus
import com.global.sms.data.entity.MessageType
import com.global.sms.data.entity.ScheduledMessageEntity
import com.global.sms.engine.retry.SmsRetryManager
import com.global.sms.engine.sender.SmsSender
import com.global.sms.engine.worker.SmsSchedulerWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

object SmsQueueManager {

    private const val TAG = "SmsQueueManager"
    private val queueMutex = Mutex()
    var bulkSendDelayMs: Long = 1200L // Configurable pacing delay between bulk SMS dispatches
    var bulkBatchSize: Int = 10 // Size of sub-batch before a longer cooling pause
    var bulkBatchCoolingPauseMs: Long = 4000L // Cooling pause after every batch of messages

    suspend fun enqueueMessage(
        context: Context,
        address: String,
        body: String,
        simSlot: Int = 0,
        subId: Int = -1,
        attachmentUri: String? = null,
        mimeType: String? = null,
        threadId: Long? = null
    ): Long = withContext(Dispatchers.IO) {
        val db = GlobalSmsDatabase.getInstance(context)
        val messageDao = db.messageDao()
        val conversationDao = db.conversationDao()

        val activeThreadId = threadId ?: (address.hashCode().toLong() and 0x7FFFFFFF)

        val existingConv = conversationDao.getConversationByThreadId(activeThreadId)
        val newConv = ConversationEntity(
            threadId = activeThreadId,
            address = address,
            contactName = existingConv?.contactName,
            lastMessage = body,
            lastTimestamp = System.currentTimeMillis(),
            unreadCount = existingConv?.unreadCount ?: 0,
            category = existingConv?.category ?: MessageCategory.PERSONAL
        )
        val encryptedConv = FieldEncryptionManager.encryptConversation(newConv)
        conversationDao.insertOrUpdateConversation(encryptedConv)

        val message = MessageEntity(
            threadId = activeThreadId,
            address = address,
            body = body,
            timestamp = System.currentTimeMillis(),
            type = MessageType.OUTBOX.code,
            simSlot = simSlot,
            deliveryStatus = MessageStatus.PENDING.code,
            retryCount = 0,
            attachmentUri = attachmentUri,
            mimeType = mimeType,
            subId = subId,
            isMms = !attachmentUri.isNullOrEmpty()
        )

        val encryptedMessage = FieldEncryptionManager.encryptMessage(message)
        val messageId = messageDao.insertMessage(encryptedMessage)
        processPendingQueue(context)
        return@withContext messageId
    }

    suspend fun enqueueScheduledMessage(
        context: Context,
        address: String,
        body: String,
        scheduledTimestamp: Long,
        simSlot: Int = 0,
        subId: Int = -1
    ): Long = withContext(Dispatchers.IO) {
        val db = GlobalSmsDatabase.getInstance(context)
        val scheduledDao = db.scheduledMessageDao()

        val scheduledEntity = ScheduledMessageEntity(
            address = address,
            body = body,
            scheduledTimestamp = scheduledTimestamp,
            simSlot = simSlot,
            status = "PENDING"
        )
        val encryptedScheduled = FieldEncryptionManager.encryptScheduledMessage(scheduledEntity)
        val id = scheduledDao.insertScheduledMessage(encryptedScheduled)

        val delayMs = (scheduledTimestamp - System.currentTimeMillis()).coerceAtLeast(0)
        val inputData = Data.Builder()
            .putLong("SCHEDULED_MESSAGE_ID", id)
            .putString("ADDRESS", address)
            .putString("BODY", body)
            .putInt("SIM_SLOT", simSlot)
            .putInt("SUB_ID", subId)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<SmsSchedulerWorker>()
            .setInputData(inputData)
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .addTag("scheduled_sms_$id")
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
        Log.d(TAG, "Scheduled message $id for address ${FieldEncryptionManager.redactedForLog(address)} in ${delayMs / 1000}s")
        return@withContext id
    }

    suspend fun processPendingQueue(context: Context) = withContext(Dispatchers.IO) {
        val db = GlobalSmsDatabase.getInstance(context)
        val messageDao = db.messageDao()
        var processedCount = 0

        while (true) {
            // Atomically claim exactly one pending message with a brief lock acquisition
            val messageToProcess = queueMutex.withLock {
                val nextMessage = messageDao.getNextPendingMessage()
                if (nextMessage != null) {
                    // Atomically mark as in-flight / queued to prevent duplicate pickup by concurrent triggers
                    messageDao.updateMessageDeliveryAndType(
                        messageId = nextMessage.id,
                        status = MessageStatus.NONE.code,
                        type = MessageType.QUEUED.code
                    )
                }
                nextMessage
            }

            // If no more pending messages, exit processing
            if (messageToProcess == null) {
                break
            }

            // Lock is released! Message sending, decryption, and logging happen without holding the mutex
            val decryptedMessage = FieldEncryptionManager.decryptMessage(messageToProcess)
            val result = SmsSender.sendSms(
                context = context,
                messageId = decryptedMessage.id,
                address = decryptedMessage.address,
                body = decryptedMessage.body,
                subId = decryptedMessage.subId,
                simSlot = decryptedMessage.simSlot
            )

            if (!result.isSuccess) {
                Log.w(TAG, "Synchronous send failure for message ${messageToProcess.id}: ${result.errorMessage}")
                SmsRetryManager.handleSendFailure(context, messageToProcess.id, result.errorCode)
            }

            processedCount++

            // Apply throttling outside of the mutex lock if more pending messages exist
            val hasMorePending = messageDao.getNextPendingMessage() != null
            if (hasMorePending) {
                val isBatchBoundary = processedCount % bulkBatchSize == 0
                val sleepTime = if (isBatchBoundary) bulkBatchCoolingPauseMs else bulkSendDelayMs
                if (sleepTime > 0) {
                    kotlinx.coroutines.delay(sleepTime)
                }
            }
        }
    }

    suspend fun retryFailedMessage(context: Context, messageId: Long) = withContext(Dispatchers.IO) {
        val db = GlobalSmsDatabase.getInstance(context)
        val messageDao = db.messageDao()
        messageDao.updateRetryCount(messageId, 0)
        messageDao.updateMessageDeliveryAndType(
            messageId = messageId,
            status = MessageStatus.PENDING.code,
            type = MessageType.OUTBOX.code
        )
        processPendingQueue(context)
    }

    suspend fun cancelScheduledMessage(context: Context, scheduledId: Long) = withContext(Dispatchers.IO) {
        val db = GlobalSmsDatabase.getInstance(context)
        db.scheduledMessageDao().updateStatus(scheduledId, "CANCELLED")
        WorkManager.getInstance(context).cancelAllWorkByTag("scheduled_sms_$scheduledId")
    }

    suspend fun clearFailedQueue(context: Context) = withContext(Dispatchers.IO) {
        val db = GlobalSmsDatabase.getInstance(context)
        val failed = db.messageDao().getFailedMessagesOnce()
        for (msg in failed) {
            db.messageDao().deleteMessage(msg.id)
        }
    }

    fun getPendingQueue(context: Context): kotlinx.coroutines.flow.Flow<List<MessageEntity>> {
        return GlobalSmsDatabase.getInstance(context).messageDao().getPendingMessages()
    }

    fun getRetryQueue(context: Context): kotlinx.coroutines.flow.Flow<List<MessageEntity>> {
        return GlobalSmsDatabase.getInstance(context).messageDao().getRetryMessages()
    }

    fun getFailedQueue(context: Context): kotlinx.coroutines.flow.Flow<List<MessageEntity>> {
        return GlobalSmsDatabase.getInstance(context).messageDao().getFailedMessages()
    }
}
