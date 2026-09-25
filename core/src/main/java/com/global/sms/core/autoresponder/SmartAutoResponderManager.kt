package com.global.sms.core.autoresponder

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AutoResponderMode(val titleFa: String, val defaultMessageFa: String) {
    DRIVING("حالت رانندگی", "در حال رانندگی هستم، به محض توقف با شما تماس خواهم گرفت."),
    MEETING("جلسه کاری", "در حال حاضر در جلسه کاری حضور دارم. پیام شما دریافت شد و به‌زودی پاسخ می‌دهم."),
    OUT_OF_OFFICE("ساعات غیراداری", "پیام شما خارج از ساعات کاری دریافت گردید. در اولین فرصت کاری پاسخگوی شما خواهیم بود."),
    CUSTOM("پاسخگوی سفارشی", "پیام شما دریافت شد. با تشکر.")
}

data class AutoResponderConfig(
    val isEnabled: Boolean = false,
    val mode: AutoResponderMode = AutoResponderMode.DRIVING,
    val customMessage: String = "",
    val startHour: Int = 18,
    val endHour: Int = 8,
    val applyOnlyToContacts: Boolean = false,
    val totalAutoRepliesSent: Int = 0
)

object SmartAutoResponderManager {

    private const val PREFS_NAME = "global_sms_auto_responder"
    private const val KEY_ENABLED = "ar_enabled"
    private const val KEY_MODE = "ar_mode"
    private const val KEY_CUSTOM_MSG = "ar_custom_msg"
    private const val KEY_START_HR = "ar_start_hr"
    private const val KEY_END_HR = "ar_end_hr"
    private const val KEY_ONLY_CONTACTS = "ar_only_contacts"
    private const val KEY_SENT_COUNT = "ar_sent_count"

    private val _config = MutableStateFlow(AutoResponderConfig())
    val config: StateFlow<AutoResponderConfig> = _config.asStateFlow()

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isEnabled = prefs.getBoolean(KEY_ENABLED, false)
        val modeStr = prefs.getString(KEY_MODE, AutoResponderMode.DRIVING.name) ?: AutoResponderMode.DRIVING.name
        val mode = try { AutoResponderMode.valueOf(modeStr) } catch (e: Exception) { AutoResponderMode.DRIVING }
        val customMsg = prefs.getString(KEY_CUSTOM_MSG, "") ?: ""
        val startHr = prefs.getInt(KEY_START_HR, 18)
        val endHr = prefs.getInt(KEY_END_HR, 8)
        val onlyContacts = prefs.getBoolean(KEY_ONLY_CONTACTS, false)
        val count = prefs.getInt(KEY_SENT_COUNT, 0)

        _config.value = AutoResponderConfig(
            isEnabled = isEnabled,
            mode = mode,
            customMessage = customMsg,
            startHour = startHr,
            endHour = endHr,
            applyOnlyToContacts = onlyContacts,
            totalAutoRepliesSent = count
        )
    }

    fun updateConfig(context: Context, newConfig: AutoResponderConfig) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean(KEY_ENABLED, newConfig.isEnabled)
            .putString(KEY_MODE, newConfig.mode.name)
            .putString(KEY_CUSTOM_MSG, newConfig.customMessage)
            .putInt(KEY_START_HR, newConfig.startHour)
            .putInt(KEY_END_HR, newConfig.endHour)
            .putBoolean(KEY_ONLY_CONTACTS, newConfig.applyOnlyToContacts)
            .putInt(KEY_SENT_COUNT, newConfig.totalAutoRepliesSent)
            .apply()

        _config.value = newConfig
    }

    fun getActiveReplyMessage(): String {
        val current = _config.value
        return if (current.customMessage.isNotBlank()) {
            current.customMessage
        } else {
            current.mode.defaultMessageFa
        }
    }

    fun incrementSentCount(context: Context) {
        val current = _config.value
        val updated = current.copy(totalAutoRepliesSent = current.totalAutoRepliesSent + 1)
        updateConfig(context, updated)
    }
}
