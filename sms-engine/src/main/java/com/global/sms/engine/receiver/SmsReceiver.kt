package com.global.sms.engine.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.global.sms.engine.dispatcher.MessageDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "SmsReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action != Telephony.Sms.Intents.SMS_DELIVER_ACTION && action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            return
        }

        Log.d(TAG, "SmsReceiver onReceive action: $action")

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        val address = messages[0].displayOriginatingAddress ?: messages[0].originatingAddress ?: "Unknown"
        val timestamp = messages[0].timestampMillis

        // Join multipart SMS fragments into complete string (preserves Unicode, Persian, Emoji)
        val fullBodyBuilder = StringBuilder()
        for (sms in messages) {
            fullBodyBuilder.append(sms.displayMessageBody ?: sms.messageBody ?: "")
        }
        val fullBody = fullBodyBuilder.toString()

        val extras = intent.extras
        val subId = extras?.getInt("subscription", -1)
            ?.takeIf { it >= 0 }
            ?: extras?.getInt("android.telephony.extra.SUBSCRIPTION_INDEX", -1)
            ?.takeIf { it >= 0 }
            ?: extras?.getInt("sub_id", -1)
            ?.takeIf { it >= 0 }
            ?: extras?.getInt("subscription_id", -1)
            ?: -1

        val simSlot = extras?.getInt("slot", -1)
            ?.takeIf { it >= 0 }
            ?: extras?.getInt("simId", -1)
            ?.takeIf { it >= 0 }
            ?: extras?.getInt("phone", 0)
            ?.coerceAtLeast(0)
            ?: 0

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            try {
                MessageDispatcher.onIncomingSms(
                    context = context,
                    address = address,
                    body = fullBody,
                    timestamp = timestamp,
                    simSlot = simSlot,
                    subId = subId
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error processing incoming SMS in MessageDispatcher", e)
            } finally {
                pendingResult?.finish()
            }
        }
    }
}
