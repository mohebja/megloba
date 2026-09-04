package com.global.sms.engine.service

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import android.util.Log
import com.global.sms.engine.dispatcher.MessageDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MmsReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "MmsReceiver"
        private const val MMS_RECEIVED_ACTION = "android.provider.Telephony.MMS_RECEIVED"
        private const val WAP_PUSH_DELIVER_ACTION = "android.provider.Telephony.WAP_PUSH_DELIVER"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == MMS_RECEIVED_ACTION || action == WAP_PUSH_DELIVER_ACTION) {
            Log.d(TAG, "MmsReceiver triggered with action: $action")
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
                try {
                    val address = intent.getStringExtra("address") ?: "MMS Sender"
                    val body = intent.getStringExtra("subject") ?: "New MMS Message"
                    MessageDispatcher.onIncomingMms(
                        context = context,
                        address = address,
                        body = body,
                        attachmentUri = null,
                        mimeType = intent.type ?: "application/vnd.wap.mms-message"
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error handling MMS receive", e)
                } finally {
                    pendingResult?.finish()
                }
            }
        }
    }
}

/**
 * Required by Google Play for default SMS app compliance (Respond via Message).
 */
class HeadlessSmsSendService : Service() {

    companion object {
        private const val ACTION_RESPOND_VIA_MESSAGE = "android.intent.action.RESPOND_VIA_MESSAGE"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == ACTION_RESPOND_VIA_MESSAGE) {
            val uri: Uri? = intent.data
            val recipient = uri?.schemeSpecificPart ?: ""
            val text = intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""
            if (recipient.isNotBlank() && text.isNotBlank()) {
                CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
                    MessageDispatcher.dispatchSendMessage(
                        context = applicationContext,
                        address = recipient,
                        body = text
                    )
                }
            }
        }
        stopSelf()
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
