package com.global.sms.core.battery

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.PowerManager

data class BatteryStatusReport(
    val batteryPct: Int,
    val isCharging: Boolean,
    val isPowerSaveMode: Boolean,
    val isLowPowerThrottled: Boolean,
    val recommendedAiProfile: String
)

object BatteryAwareProcessingEngine {

    fun checkStatus(context: Context): BatteryStatusReport {
        val batteryStatusIntent = context.registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )

        val level = batteryStatusIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatusIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct = if (level != -1 && scale != -1) {
            ((level / scale.toFloat()) * 100).toInt()
        } else {
            100
        }

        val status = batteryStatusIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val isPowerSaveMode = powerManager?.isPowerSaveMode == true

        // Throttle when battery is below 15% and not charging, or in system power save mode
        val isThrottled = (batteryPct < 15 && !isCharging) || isPowerSaveMode

        val profile = when {
            isCharging -> "حداکثر عملکرد (Full Power - بدون محدودیت)"
            isThrottled -> "حالت ذخیره انرژی (Low Power - بهینه‌سازی حداکثری)"
            else -> "عملکرد متعادل (Balanced)"
        }

        return BatteryStatusReport(
            batteryPct = batteryPct,
            isCharging = isCharging,
            isPowerSaveMode = isPowerSaveMode,
            isLowPowerThrottled = isThrottled,
            recommendedAiProfile = profile
        )
    }

    fun canRunHeavyAiTask(context: Context): Boolean {
        val report = checkStatus(context)
        return !report.isLowPowerThrottled
    }
}
