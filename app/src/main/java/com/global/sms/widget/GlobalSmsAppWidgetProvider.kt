package com.global.sms.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.RemoteViews
import android.widget.Toast
import com.global.sms.MainActivity
import com.global.sms.R
import com.global.sms.core.util.PersianUtils
import com.global.sms.data.db.GlobalSmsDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class GlobalSmsAppWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_COPY_WIDGET_OTP) {
            val code = intent.getStringExtra(EXTRA_OTP_CODE) ?: ""
            if (code.isNotBlank()) {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText("OTP Code", code)
                clipboard?.setPrimaryClip(clip)
                Toast.makeText(context, "کد تایید کپی شد ($code) - پاک‌سازی امن پس از ۴۵ ثانیه", Toast.LENGTH_SHORT).show()

                // Security: Auto-clear clipboard after 45 seconds
                Handler(Looper.getMainLooper()).postDelayed({
                    try {
                        val currentClip = clipboard?.primaryClip
                        if (currentClip != null && currentClip.itemCount > 0) {
                            if (currentClip.getItemAt(0)?.text?.toString() == code) {
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                                    clipboard?.clearPrimaryClip()
                                } else {
                                    clipboard?.setPrimaryClip(android.content.ClipData.newPlainText("", ""))
                                }
                            }
                        }
                    } catch (e: Exception) {
                        // ignore
                    }
                }, 45000L)
            }
        }
    }

    companion object {
        const val ACTION_COPY_WIDGET_OTP = "com.global.sms.widget.ACTION_COPY_WIDGET_OTP"
        const val EXTRA_OTP_CODE = "extra_otp_code"

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, GlobalSmsAppWidgetProvider::class.java)
            val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            for (id in allWidgetIds) {
                updateAppWidget(context, appWidgetManager, id)
            }
        }

        private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_global_sms)

            // 1. Root Click -> Open Main Activity
            val mainIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val mainPendingIntent = PendingIntent.getActivity(
                context,
                0,
                mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, mainPendingIntent)

            // 2. Compose Button Click -> Open New Message
            val composeIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("action_compose", true)
            }
            val composePendingIntent = PendingIntent.getActivity(
                context,
                1,
                composeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_compose, composePendingIntent)

            // 3. Load DB data asynchronously
            CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
                try {
                    val db = GlobalSmsDatabase.getInstance(context)

                    // Unread conversations count
                    val convs = db.conversationDao().getAllConversationsSync()
                    val totalUnread = convs.sumOf { it.unreadCount }

                    val unreadText = if (totalUnread > 0) {
                        "${PersianUtils.toPersianDigits(totalUnread.toString())} پیام ناخوانده"
                    } else {
                        "همه پیام‌ها خوانده شده"
                    }
                    views.setTextViewText(R.id.widget_unread_badge, unreadText)

                    // Active OTP Code
                    val activeOtps = db.otpDao().getActiveOtpsFlow().firstOrNull() ?: emptyList()
                    val latestActiveOtp = activeOtps.firstOrNull { it.expiresTimestamp > System.currentTimeMillis() }

                    if (latestActiveOtp != null) {
                        val serviceName = latestActiveOtp.serviceName.ifBlank { "کد تایید پیامکی" }
                        val codeDisplay = PersianUtils.toPersianDigits(latestActiveOtp.code)

                        views.setTextViewText(R.id.widget_otp_service, "کد تایید فعال ($serviceName):")
                        views.setTextViewText(R.id.widget_otp_code, codeDisplay)
                        views.setViewVisibility(R.id.widget_btn_copy_otp, View.VISIBLE)

                        val copyIntent = Intent(context, GlobalSmsAppWidgetProvider::class.java).apply {
                            action = ACTION_COPY_WIDGET_OTP
                            putExtra(EXTRA_OTP_CODE, latestActiveOtp.code)
                        }
                        val copyPendingIntent = PendingIntent.getBroadcast(
                            context,
                            2,
                            copyIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        views.setOnClickPendingIntent(R.id.widget_btn_copy_otp, copyPendingIntent)
                    } else {
                        views.setTextViewText(R.id.widget_otp_service, "آخرین کد تایید بانکی (OTP):")
                        views.setTextViewText(R.id.widget_otp_code, "کد فعالی موجود نیست")
                        views.setViewVisibility(R.id.widget_btn_copy_otp, View.GONE)
                    }

                    appWidgetManager.updateAppWidget(appWidgetId, views)
                } catch (e: Exception) {
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            }
        }
    }
}
