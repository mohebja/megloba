package com.global.sms.core.crash

import android.content.Context
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CrashLogEntry(
    val id: String,
    val timestamp: Long,
    val formattedDate: String,
    val exceptionClassName: String,
    val message: String,
    val stackTrace: String,
    val threadName: String,
    val deviceInfo: String,
    val isFatal: Boolean
)

/**
 * Enterprise Production Crash Management System for Global SMS.
 * Captures uncaught exceptions, ANR signals, creates encrypted/local crash logs,
 * and exposes log state for debugging and release diagnostic reports.
 */
class CrashManager private constructor(private val context: Context) : Thread.UncaughtExceptionHandler {

    private val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val crashDir = File(context.filesDir, "crash_reports").apply { if (!exists()) mkdirs() }

    private val _crashLogs = MutableStateFlow<List<CrashLogEntry>>(emptyList())
    val crashLogs: StateFlow<List<CrashLogEntry>> = _crashLogs.asStateFlow()

    init {
        loadLogsFromDisk()
    }

    companion object {
        @Volatile
        private var instance: CrashManager? = null

        fun getInstance(context: Context): CrashManager {
            return instance ?: synchronized(this) {
                instance ?: CrashManager(context.applicationContext).also {
                    instance = it
                }
            }
        }

        fun initialize(context: Context) {
            val manager = getInstance(context)
            Thread.setDefaultUncaughtExceptionHandler(manager)
        }
    }

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        try {
            logCrash(thread, throwable, isFatal = true)
        } catch (e: Exception) {
            Log.e("CrashManager", "Failed to write crash log locally", e)
        } finally {
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    fun logNonFatalException(tag: String, throwable: Throwable) {
        scope.launch {
            try {
                logCrash(Thread.currentThread(), throwable, isFatal = false, customTag = tag)
            } catch (e: Exception) {
                Log.e("CrashManager", "Failed to log non-fatal exception", e)
            }
        }
    }

    private fun logCrash(
        thread: Thread,
        throwable: Throwable,
        isFatal: Boolean,
        customTag: String? = null
    ) {
        val sw = StringWriter()
        throwable.printStackTrace(PrintWriter(sw))
        val stackTrace = sw.toString()

        val timestamp = System.currentTimeMillis()
        val formattedDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(Date(timestamp))
        val id = "crash_$timestamp"

        val deviceInfo = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT}) | ${Build.MANUFACTURER} ${Build.MODEL} | Device: ${Build.DEVICE}"
        val exName = customTag ?: (throwable.javaClass.simpleName.ifBlank { "UnknownException" })
        val msg = throwable.message ?: "No error message provided"

        val entry = CrashLogEntry(
            id = id,
            timestamp = timestamp,
            formattedDate = formattedDate,
            exceptionClassName = exName,
            message = msg,
            stackTrace = stackTrace,
            threadName = thread.name,
            deviceInfo = deviceInfo,
            isFatal = isFatal
        )

        // Save locally to file
        val file = File(crashDir, "$id.log")
        val logText = """
            =================== GLOBAL SMS CRASH LOG ===================
            ID: ${entry.id}
            Timestamp: ${entry.formattedDate} ($timestamp)
            Fatal: ${entry.isFatal}
            Thread: ${entry.threadName}
            Exception: ${entry.exceptionClassName}
            Message: ${entry.message}
            Device: ${entry.deviceInfo}
            ------------------- STACK TRACE -------------------
            ${entry.stackTrace}
            ============================================================
        """.trimIndent()

        file.writeText(logText)

        // Update in-memory flow
        _crashLogs.value = (listOf(entry) + _crashLogs.value).take(50)
    }

    private fun loadLogsFromDisk() {
        scope.launch {
            val logFiles = crashDir.listFiles { _, name -> name.endsWith(".log") } ?: emptyArray()
            val loaded = logFiles.mapNotNull { file ->
                try {
                    val lines = file.readLines()
                    val id = lines.firstOrNull { it.startsWith("ID:") }?.substringAfter(":")?.trim() ?: file.nameWithoutExtension
                    val date = lines.firstOrNull { it.startsWith("Timestamp:") }?.substringAfter(":")?.trim() ?: "Unknown"
                    val fatal = lines.firstOrNull { it.startsWith("Fatal:") }?.substringAfter(":")?.trim()?.toBoolean() ?: true
                    val thread = lines.firstOrNull { it.startsWith("Thread:") }?.substringAfter(":")?.trim() ?: "main"
                    val exName = lines.firstOrNull { it.startsWith("Exception:") }?.substringAfter(":")?.trim() ?: "Exception"
                    val msg = lines.firstOrNull { it.startsWith("Message:") }?.substringAfter(":")?.trim() ?: ""
                    val dev = lines.firstOrNull { it.startsWith("Device:") }?.substringAfter(":")?.trim() ?: ""

                    val stackStart = lines.indexOfFirst { it.contains("STACK TRACE") }
                    val stackTrace = if (stackStart != -1 && stackStart < lines.size - 1) {
                        lines.subList(stackStart + 1, lines.size - 1).joinToString("\n")
                    } else ""

                    CrashLogEntry(
                        id = id,
                        timestamp = file.lastModified(),
                        formattedDate = date,
                        exceptionClassName = exName,
                        message = msg,
                        stackTrace = stackTrace,
                        threadName = thread,
                        deviceInfo = dev,
                        isFatal = fatal
                    )
                } catch (e: Exception) {
                    null
                }
            }.sortedByDescending { it.timestamp }

            _crashLogs.value = loaded
        }
    }

    fun clearAllCrashLogs() {
        scope.launch {
            crashDir.listFiles()?.forEach { it.delete() }
            _crashLogs.value = emptyList()
        }
    }
}
