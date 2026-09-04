package com.global.sms.engine.receiver

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import android.util.Log
import com.global.sms.data.db.GlobalSmsDatabase
import com.global.sms.data.entity.MessageStatus
import com.global.sms.data.entity.MessageType
import com.global.sms.engine.retry.SmsRetryManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

class DeliveryReportReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "DeliveryReportReceiver"

        const val ACTION_SMS_SENT = "com.global.sms.engine.ACTION_SMS_SENT"
        const val ACTION_SMS_DELIVERED = "com.global.sms.engine.ACTION_SMS_DELIVERED"
        const val ACTION_MMS_SENT = "com.global.sms.engine.ACTION_MMS_SENT"

        const val EXTRA_MESSAGE_ID = "extra_message_id"
        const val EXTRA_PART_INDEX = "extra_part_index"
        const val EXTRA_TOTAL_PARTS = "extra_total_parts"
        const val EXTRA_ADDRESS = "extra_address"
        const val EXTRA_SUB_ID = "extra_sub_id"

        // Thread-safe tracking of received parts across asynchronous OS callbacks
        private val pendingSentParts = ConcurrentHashMap<Long, MutableSet<Int>>()
        private val pendingDeliveredParts = ConcurrentHashMap<Long, MutableSet<Int>>()
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val messageId = intent.getLongExtra(EXTRA_MESSAGE_ID, -1L)
        if (messageId == -1L) return

        val resultCode = resultCode
        val partIndex = intent.getIntExtra(EXTRA_PART_INDEX, 0)
        val totalParts = intent.getIntExtra(EXTRA_TOTAL_PARTS, 1)

        Log.d(TAG, "onReceive action=$action, msgId=$messageId, resultCode=$resultCode, part=$partIndex/$totalParts")

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            try {
                val db = GlobalSmsDatabase.getInstance(context)
                val messageDao = db.messageDao()

                when (action) {
                    ACTION_SMS_SENT -> {
                        val currentMessage = messageDao.getMessageById(messageId)
                        val isAlreadyFailedOrRetrying = currentMessage != null && (
                            currentMessage.deliveryStatus == MessageStatus.FAILED.code ||
                            currentMessage.deliveryStatus == MessageStatus.RETRYING.code
                        )

                        if (resultCode == Activity.RESULT_OK) {
                            if (!isAlreadyFailedOrRetrying) {
                                if (totalParts <= 1) {
                                    pendingSentParts.remove(messageId)
                                    messageDao.updateMessageDeliveryAndType(
                                        messageId = messageId,
                                        status = MessageStatus.SENT.code,
                                        type = MessageType.SENT.code
                                    )
                                    Log.d(TAG, "Message $messageId sent successfully (single part)")
                                } else {
                                    val partsSet = pendingSentParts.computeIfAbsent(messageId) {
                                        Collections.newSetFromMap(ConcurrentHashMap())
                                    }
                                    partsSet.add(partIndex)

                                    if (partsSet.size >= totalParts) {
                                        pendingSentParts.remove(messageId)
                                        messageDao.updateMessageDeliveryAndType(
                                            messageId = messageId,
                                            status = MessageStatus.SENT.code,
                                            type = MessageType.SENT.code
                                        )
                                        Log.d(TAG, "Message $messageId ($totalParts/$totalParts parts) sent successfully")
                                    } else {
                                        Log.d(TAG, "Message $messageId part $partIndex succeeded (${partsSet.size}/$totalParts parts completed)")
                                    }
                                }
                            } else {
                                pendingSentParts.remove(messageId)
                                Log.d(TAG, "Message $messageId part $partIndex succeeded but message was already marked failed/retrying")
                            }
                        } else {
                            // Explicit failure path: clean up tracking immediately and schedule retry
                            pendingSentParts.remove(messageId)
                            Log.w(TAG, "Message $messageId send failed on part $partIndex/$totalParts with resultCode=$resultCode")
                            SmsRetryManager.handleSendFailure(context, messageId, resultCode)
                        }
                    }

                    ACTION_SMS_DELIVERED -> {
                        val currentMessage = messageDao.getMessageById(messageId)
                        if (resultCode == Activity.RESULT_OK) {
                            if (totalParts <= 1) {
                                pendingDeliveredParts.remove(messageId)
                                messageDao.updateDeliveryStatus(
                                    messageId = messageId,
                                    status = MessageStatus.DELIVERED.code
                                )
                                Log.d(TAG, "Message $messageId delivered successfully")
                            } else {
                                val partsSet = pendingDeliveredParts.computeIfAbsent(messageId) {
                                    Collections.newSetFromMap(ConcurrentHashMap())
                                }
                                partsSet.add(partIndex)

                                if (partsSet.size >= totalParts) {
                                    pendingDeliveredParts.remove(messageId)
                                    messageDao.updateDeliveryStatus(
                                        messageId = messageId,
                                        status = MessageStatus.DELIVERED.code
                                    )
                                    Log.d(TAG, "Message $messageId ($totalParts/$totalParts parts) delivered successfully")
                                }
                            }
                        } else {
                            pendingDeliveredParts.remove(messageId)
                            Log.w(TAG, "Message $messageId delivery failed on part $partIndex with resultCode=$resultCode")
                            if (currentMessage?.deliveryStatus != MessageStatus.DELIVERED.code) {
                                messageDao.updateDeliveryStatus(
                                    messageId = messageId,
                                    status = MessageStatus.FAILED.code
                                )
                            }
                        }
                    }

                    ACTION_MMS_SENT -> {
                        if (resultCode == Activity.RESULT_OK) {
                            messageDao.updateMessageDeliveryAndType(
                                messageId = messageId,
                                status = MessageStatus.SENT.code,
                                type = MessageType.SENT.code
                            )
                        } else {
                            SmsRetryManager.handleSendFailure(context, messageId, resultCode)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling delivery/sent report", e)
            } finally {
                pendingResult?.finish()
            }
        }
    }
}
