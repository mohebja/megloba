package com.global.sms.core.battery

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.PowerManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AiProcessingLevel {
    ALWAYS_ACTIVE,
    CHARGING_ONLY,
    BALANCED,
    LOW_POWER_DISABLED
}

data class BatteryStatusInfo(
    val batteryLevelPercentage: Int = 100,
    val isCharging: Boolean = false,
    val isPowerSaveMode: Boolean = false,
    val isIgnoringBatteryOptimizations: Boolean = true,
    val processingLevel: AiProcessingLevel = AiProcessingLevel.BALANCED
)

class BatteryOptimizationManager(private val context: Context) {

    private val _status = MutableStateFlow(BatteryStatusInfo())
    val status: StateFlow<BatteryStatusInfo> = _status.asStateFlow()

    init {
        updateBatteryState()
    }

    fun updateBatteryState(): BatteryStatusInfo {
        val batteryStatusIntent: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { filter ->
            context.registerReceiver(null, filter)
        }

        val level = batteryStatusIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatusIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val pct = if (level >= 0 && scale > 0) (level * 100 / scale.toFloat()).toInt() else 100

        val status = batteryStatusIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val isPowerSave = powerManager?.isPowerSaveMode ?: false
        val isIgnoring = if (powerManager != null) {
            powerManager.isIgnoringBatteryOptimizations(context.packageName)
        } else {
            true
        }

        val info = BatteryStatusInfo(
            batteryLevelPercentage = pct,
            isCharging = isCharging,
            isPowerSaveMode = isPowerSave,
            isIgnoringBatteryOptimizations = isIgnoring,
            processingLevel = when {
                isPowerSave || pct < 15 -> AiProcessingLevel.LOW_POWER_DISABLED
                isCharging -> AiProcessingLevel.ALWAYS_ACTIVE
                else -> AiProcessingLevel.BALANCED
            }
        )

        _status.value = info
        return info
    }

    fun setProcessingLevel(level: AiProcessingLevel) {
        _status.value = _status.value.copy(processingLevel = level)
    }

    fun shouldRunHeavyAiJob(): Boolean {
        val current = _status.value
        return when (current.processingLevel) {
            AiProcessingLevel.ALWAYS_ACTIVE -> true
            AiProcessingLevel.CHARGING_ONLY -> current.isCharging
            AiProcessingLevel.BALANCED -> current.batteryLevelPercentage > 20 && !current.isPowerSaveMode
            AiProcessingLevel.LOW_POWER_DISABLED -> false
        }
    }
}
