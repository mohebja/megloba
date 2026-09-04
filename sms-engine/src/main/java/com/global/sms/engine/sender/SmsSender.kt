package com.global.sms.engine.sender

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.telephony.SmsManager
import android.util.Log
import com.global.sms.engine.receiver.DeliveryReportReceiver
import com.global.sms.engine.sim.DualSimManager

data class SmsSendResult(
    val isSuccess: Boolean,
    val messageId: Long,
    val partsCount: Int = 1,
    val errorCode: Int = 0,
    val errorMessage: String? = null
)

object SmsSender {

    private const val TAG = "SmsSender"

    /**
     * Sends an SMS message (supports single & multipart, Unicode, Persian, Emoji, Dual SIM).
     */
    fun sendSms(
        context: Context,
        messageId: Long,
        address: String,
        body: String,
        subId: Int = -1,
        simSlot: Int = 0,
        requestDeliveryReport: Boolean = true,
        requestReadReport: Boolean = false
    ): SmsSendResult {
        if (address.isBlank() || body.isBlank()) {
            return SmsSendResult(
                isSuccess = false,
                messageId = messageId,
                errorCode = -1,
                errorMessage = "Address or body is empty"
            )
        }

        return try {
            val effectiveSubId = if (subId >= 0) {
                subId
            } else {
                DualSimManager.getActiveSimCards(context).firstOrNull { it.slotIndex == simSlot }?.subscriptionId ?: -1
            }

            val smsManager: SmsManager = DualSimManager.getSmsManagerForSubId(context, effectiveSubId)
            
            // Divide message for long SMS / Unicode / Persian / Emoji
            val parts = smsManager.divideMessage(body)
            val partsCount = parts.size

            val sentIntents = ArrayList<PendingIntent>()
            val deliveryIntents = ArrayList<PendingIntent>()

            val flag = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            val msgHash = (messageId xor (messageId ushr 32)).toInt()

            for (i in 0 until partsCount) {
                val sentIntentExtra = Intent(DeliveryReportReceiver.ACTION_SMS_SENT).apply {
                    setPackage(context.packageName)
                    putExtra(DeliveryReportReceiver.EXTRA_MESSAGE_ID, messageId)
                    putExtra(DeliveryReportReceiver.EXTRA_PART_INDEX, i)
                    putExtra(DeliveryReportReceiver.EXTRA_TOTAL_PARTS, partsCount)
                    putExtra(DeliveryReportReceiver.EXTRA_ADDRESS, address)
                    putExtra(DeliveryReportReceiver.EXTRA_SUB_ID, effectiveSubId)
                }
                val sentReqCode = ((msgHash * 31) + i) and 0x7FFFFFFF
                val sentPendingIntent = PendingIntent.getBroadcast(
                    context,
                    sentReqCode,
                    sentIntentExtra,
                    flag
                )
                sentIntents.add(sentPendingIntent)

                if (requestDeliveryReport) {
                    val deliveryIntentExtra = Intent(DeliveryReportReceiver.ACTION_SMS_DELIVERED).apply {
                        setPackage(context.packageName)
                        putExtra(DeliveryReportReceiver.EXTRA_MESSAGE_ID, messageId)
                        putExtra(DeliveryReportReceiver.EXTRA_PART_INDEX, i)
                        putExtra(DeliveryReportReceiver.EXTRA_TOTAL_PARTS, partsCount)
                        putExtra(DeliveryReportReceiver.EXTRA_ADDRESS, address)
                        putExtra(DeliveryReportReceiver.EXTRA_SUB_ID, effectiveSubId)
                    }
                    val deliveryReqCode = ((msgHash * 31) + i + 10000) and 0x7FFFFFFF
                    val deliveryPendingIntent = PendingIntent.getBroadcast(
                        context,
                        deliveryReqCode,
                        deliveryIntentExtra,
                        flag
                    )
                    deliveryIntents.add(deliveryPendingIntent)
                }
            }

            if (partsCount > 1) {
                smsManager.sendMultipartTextMessage(
                    address,
                    null,
                    parts,
                    sentIntents,
                    if (requestDeliveryReport) deliveryIntents else null
                )
            } else {
                smsManager.sendTextMessage(
                    address,
                    null,
                    body,
                    sentIntents.firstOrNull(),
                    if (requestDeliveryReport) deliveryIntents.firstOrNull() else null
                )
            }

            Log.d(TAG, "Sent SMS to ${address.take(4)}*** with $partsCount parts (msgId: $messageId)")
            SmsSendResult(
                isSuccess = true,
                messageId = messageId,
                partsCount = partsCount
            )
        } catch (e: SecurityException) {
            Log.e(TAG, "SEND_SMS permission missing", e)
            SmsSendResult(
                isSuccess = false,
                messageId = messageId,
                errorCode = SmsManager.RESULT_ERROR_GENERIC_FAILURE,
                errorMessage = "SEND_SMS permission missing: ${e.localizedMessage}"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send SMS to ${address.take(4)}***", e)
            SmsSendResult(
                isSuccess = false,
                messageId = messageId,
                errorCode = SmsManager.RESULT_ERROR_GENERIC_FAILURE,
                errorMessage = e.localizedMessage
            )
        }
    }

    /**
     * Sends an MMS message with media attachment.
     */
    fun sendMms(
        context: Context,
        messageId: Long,
        address: String,
        contentUri: Uri,
        subId: Int = -1
    ): SmsSendResult {
        return try {
            val smsManager = DualSimManager.getSmsManagerForSubId(context, subId)
            val sentIntent = Intent(DeliveryReportReceiver.ACTION_MMS_SENT).apply {
                setPackage(context.packageName)
                putExtra(DeliveryReportReceiver.EXTRA_MESSAGE_ID, messageId)
                putExtra(DeliveryReportReceiver.EXTRA_ADDRESS, address)
            }
            val flag = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            val msgHash = (messageId xor (messageId ushr 32)).toInt()
            val reqCode = ((msgHash * 31) + 99) and 0x7FFFFFFF
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                reqCode,
                sentIntent,
                flag
            )

            smsManager.sendMultimediaMessage(
                context,
                contentUri,
                null,
                null,
                pendingIntent
            )

            SmsSendResult(isSuccess = true, messageId = messageId, partsCount = 1)
        } catch (e: Exception) {
            Log.e(TAG, "MMS sending exception", e)
            SmsSendResult(
                isSuccess = false,
                messageId = messageId,
                errorCode = SmsManager.RESULT_ERROR_GENERIC_FAILURE,
                errorMessage = e.localizedMessage
            )
        }
    }
}
