package com.global.sms.engine.responder

import java.util.Calendar

object BusinessHoursAutoResponder {

    data class BusinessHoursConfig(
        val isEnabled: Boolean = true,
        val startHour: Int = 8,
        val endHour: Int = 17,
        val autoReplyText: String = "با سلام، پیام شما خارج از ساعات کاری دریافت شد. در اولین ساعات کاری روز بعد پاسخگوی شما خواهیم بود."
    )

    fun isOutsideBusinessHours(config: BusinessHoursConfig): Boolean {
        if (!config.isEnabled) return false
        val cal = Calendar.getInstance()
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        if (dayOfWeek == Calendar.FRIDAY) return true

        val currentHour = cal.get(Calendar.HOUR_OF_DAY)
        return currentHour < config.startHour || currentHour >= config.endHour
    }
}
