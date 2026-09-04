package com.global.sms.crash

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GlobalCrashHandler private constructor(private val context: Context) : Thread.UncaughtExceptionHandler {

    private val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        try {
            // Sanitize crash stacktrace to remove sensitive SMS content
            val sanitizedLog = buildSanitizedCrashLog(thread, throwable)
            saveCrashLogToFile(sanitizedLog)
        } catch (e: Exception) {
            Log.e("GlobalCrashHandler", "Failed to write crash log", e)
        } finally {
            // Forward to default system handler for standard process exit
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    private fun buildSanitizedCrashLog(thread: Thread, throwable: Throwable): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        val timestamp = dateFormat.format(Date())

        val rawStackTrace = Log.getStackTraceString(throwable)
        // Redact phone numbers and bank card patterns from stack trace for Data Safety
        val sanitizedStackTrace = rawStackTrace
            .replace(Regex("""\+?\d{10,13}"""), "[REDACTED_PHONE]")
            .replace(Regex("""\b\d{4}[- ]?\d{4}[- ]?\d{4}[- ]?\d{4}\b"""), "[REDACTED_CARD]")

        return """
            === GLOBAL SMS CRASH LOG ===
            Timestamp: $timestamp
            Thread: ${thread.name} (ID: ${thread.name.hashCode()})
            Exception: ${throwable.javaClass.simpleName}
            Message: ${throwable.message?.replace(Regex("""\d{4,}"""), "[REDACTED_NUM]")}
            
            Stack Trace:
            $sanitizedStackTrace
            ===========================
        """.trimIndent()
    }

    private fun saveCrashLogToFile(logText: String) {
        val crashDir = File(context.filesDir, "crash_logs")
        if (!crashDir.exists()) {
            crashDir.mkdirs()
        }
        val crashFile = File(crashDir, "last_crash.txt")
        FileWriter(crashFile, false).use { writer ->
            writer.write(logText)
        }
    }

    companion object {
        fun init(context: Context) {
            val handler = GlobalCrashHandler(context.applicationContext)
            Thread.setDefaultUncaughtExceptionHandler(handler)
        }
    }
}
