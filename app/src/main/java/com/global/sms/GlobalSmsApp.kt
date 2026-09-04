package com.global.sms

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.global.sms.crash.GlobalCrashHandler
import com.global.sms.core.crash.CrashManager
import com.global.sms.data.db.GlobalSmsDatabase
import com.global.sms.database.DatabaseMaintenanceWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.TimeUnit

class GlobalSmsApp : Application(), ImageLoaderFactory, Configuration.Provider {

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()

        GlobalCrashHandler.init(this)
        CrashManager.initialize(this)

        // 1. Asynchronous Database Warmup & Indexes Pre-cache (Startup Optimization)
        applicationScope.launch(Dispatchers.IO) {
            try {
                val db = GlobalSmsDatabase.getInstance(this@GlobalSmsApp)
                // Execute a lightweight ping query to open database connection & warm up memory mapping
                db.openHelper.writableDatabase.version
            } catch (e: Exception) {
                Log.w("GlobalSmsApp", "Async database warmup failed (non-fatal)", e)
            }
        }

        // 2. Battery & Resource-Optimized WorkManager Background Maintenance Job
        scheduleBackgroundMaintenance()
    }

    private fun scheduleBackgroundMaintenance() {
        try {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .setRequiresStorageNotLow(true)
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build()

            val maintenanceRequest = PeriodicWorkRequestBuilder<DatabaseMaintenanceWorker>(
                24, TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "GlobalSmsDbMaintenanceWork",
                ExistingPeriodicWorkPolicy.KEEP,
                maintenanceRequest
            )
        } catch (e: Throwable) {
            Log.e("GlobalSmsApp", "Failed to schedule background database maintenance", e)
        }
    }

    // 3. Image Optimization: Custom Coil ImageLoader with Memory & Disk Caching
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25) // Max 25% of app available memory
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(File(cacheDir, "image_cache"))
                    .maxSizeBytes(50 * 1024 * 1024) // 50 MB disk cache limit
                    .build()
            }
            .allowHardware(true)
            .crossfade(true)
            .respectCacheHeaders(false)
            .build()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        // Modern Android Q+ memory management: trim caches when system requests memory reclaim
        if (level >= TRIM_MEMORY_BACKGROUND || level >= TRIM_MEMORY_MODERATE) {
            coil.Coil.imageLoader(this).memoryCache?.clear()
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        coil.Coil.imageLoader(this).memoryCache?.clear()
    }
}
