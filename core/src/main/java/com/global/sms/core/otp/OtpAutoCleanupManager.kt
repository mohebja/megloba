package com.global.sms.core.otp

import android.content.Context
import android.util.Log
import com.global.sms.data.db.GlobalSmsDatabase
import com.global.sms.data.entity.SecurityAuditLogEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Manages automatic detection and purging of expired One-Time Password (OTP) messages.
 * Frees database storage and prevents inbox clutter while preserving audit traceability.
 */
object OtpAutoCleanupManager {

    private const val TAG = "OtpAutoCleanup"

    // Default expiration thresholds
    const val THRESHOLD_1_HOUR = 60 * 60 * 1000L
    const val THRESHOLD_24_HOURS = 24 * 60 * 60 * 1000L
    const val THRESHOLD_7_DAYS = 7 * 24 * 60 * 60 * 1000L

    data class CleanupReport(
        val deletedCount: Int,
        val thresholdMillis: Long,
        val freedSpaceEstimateBytes: Long,
        val timestamp: Long = System.currentTimeMillis()
    )

    /**
     * Purges OTP messages older than [olderThanMillis].
     * Returns a [CleanupReport] with the count of removed messages.
     */
    suspend fun purgeExpiredOtpMessages(
        context: Context,
        olderThanMillis: Long = THRESHOLD_24_HOURS
    ): CleanupReport = withContext(Dispatchers.IO) {
        val db = GlobalSmsDatabase.getInstance(context)
        val cutoffTimestamp = System.currentTimeMillis() - olderThanMillis

        return@withContext try {
            // Find OTP messages older than cutoff
            val allMessages = db.messageDao().getMessagesForThreadSync(-1L)
            // Query OTP messages across all threads
            val otpDao = db.otpDao()
            val oldOtps = otpDao.getExpiredOtps(cutoffTimestamp)
            var deletedMessagesCount = 0

            // Delete expired entries from OtpDao
            for (otp in oldOtps) {
                otpDao.deleteOtp(otp.id)
                deletedMessagesCount++
            }

            // Estimate average 512 bytes per record including FTS indexing
            val freedBytes = deletedMessagesCount * 512L

            // Record security audit log
            if (deletedMessagesCount > 0) {
                db.securityAuditLogDao().insertLog(
                    SecurityAuditLogEntity(
                        eventType = "OTP_AUTO_CLEANUP",
                        description = "پاکسازی خودکار $deletedMessagesCount پیامک حاوی رمز یکبارمصرف منقضی‌شده",
                        operatorName = "مدیر پاکسازی حافظه",
                        ipOrDeviceId = "DEVICE_LOCAL"
                    )
                )
            }

            Log.i(TAG, "Successfully purged $deletedMessagesCount expired OTPs (older than ${olderThanMillis / 3600000}h)")
            CleanupReport(
                deletedCount = deletedMessagesCount,
                thresholdMillis = olderThanMillis,
                freedSpaceEstimateBytes = freedBytes
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error executing OTP auto cleanup", e)
            CleanupReport(
                deletedCount = 0,
                thresholdMillis = olderThanMillis,
                freedSpaceEstimateBytes = 0L
            )
        }
    }
}
