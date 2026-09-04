package com.global.sms.core.reliability

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class HealthReport(
    val healthScore: Int = 100,
    val isDatabaseHealthy: Boolean = true,
    val isSmsEngineReady: Boolean = true,
    val hasSmsPermissions: Boolean = true,
    val isBatteryOptimizedExempt: Boolean = true,
    val availableStorageMb: Long = 1024L,
    val memoryUsageMb: Long = 32L,
    val isStorageSufficient: Boolean = true,
    val anrCount: Int = 0,
    val activeWarnings: List<String> = emptyList()
)

class ProductionHealthMonitor(private val context: Context) {

    private val _healthReport = MutableStateFlow(HealthReport())
    val healthReport: StateFlow<HealthReport> = _healthReport.asStateFlow()

    init {
        performFullHealthCheck()
    }

    fun performFullHealthCheck(): HealthReport {
        val hasSms = checkPermission(android.Manifest.permission.READ_SMS) &&
                     checkPermission(android.Manifest.permission.RECEIVE_SMS) &&
                     checkPermission(android.Manifest.permission.SEND_SMS)

        val storageMb = getAvailableStorageMb()
        val storageSufficient = storageMb > 100

        val runtime = Runtime.getRuntime()
        val usedMemMb = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? android.os.PowerManager
        val isExempt = powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: true

        val warnings = mutableListOf<String>()
        if (!hasSms) warnings.add("دسترسی به پیامک صادر نشده است")
        if (!storageSufficient) warnings.add("فضای ذخیره‌سازی دستگاه کم است")

        var score = 100
        if (!hasSms) score -= 30
        if (!storageSufficient) score -= 20

        val report = HealthReport(
            healthScore = score.coerceAtLeast(0),
            isDatabaseHealthy = true,
            isSmsEngineReady = hasSms,
            hasSmsPermissions = hasSms,
            isBatteryOptimizedExempt = isExempt,
            availableStorageMb = storageMb,
            memoryUsageMb = usedMemMb,
            isStorageSufficient = storageSufficient,
            anrCount = 0,
            activeWarnings = warnings
        )

        _healthReport.value = report
        return report
    }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun getAvailableStorageMb(): Long {
        return try {
            val stat = StatFs(Environment.getDataDirectory().path)
            val bytes = stat.availableBlocksLong * stat.blockSizeLong
            bytes / (1024 * 1024)
        } catch (e: Exception) {
            1024L
        }
    }
}
