package com.global.sms.core.ai.otp

import com.global.sms.data.dao.OtpDao
import com.global.sms.data.entity.OtpEntity
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit

data class ProcessedOtpResult(
    val code: String,
    val serviceName: String,
    val expiresTimestamp: Long,
    val securityWarning: String?,
    val isSafe: Boolean
)

/**
 * OTP Management Center Manager for Sprint 2.3.
 * Handles automatic extraction, expiration tracking, service identification, and copy safety.
 */
class OtpManager(private val otpDao: OtpDao) {

    fun getActiveOtpsFlow(): Flow<List<OtpEntity>> = otpDao.getActiveOtpsFlow()

    fun getAllOtpsFlow(): Flow<List<OtpEntity>> = otpDao.getAllOtpsFlow()

    suspend fun processAndStoreOtp(
        messageId: Long,
        sender: String,
        body: String,
        timestamp: Long = System.currentTimeMillis()
    ): ProcessedOtpResult? {
        val extracted = OtpExtractor.extractDetails(body) ?: return null
        val serviceName = detectServiceName(sender, body)
        val expirationMillis = timestamp + TimeUnit.MINUTES.toMillis(5)

        val hasPhishingLink = body.contains("http://") || body.contains("https://") || body.contains("t.me/")
        val securityLevel = if (hasPhishingLink) "HIGH" else "NORMAL"
        val warning = if (hasPhishingLink) "هشدار: این پیامک حاوی لینک مشکوک است. کد را در سایت‌های ناشناس وارد نکنید." else null

        val entity = OtpEntity(
            messageId = messageId,
            code = extracted.code,
            serviceName = serviceName,
            address = sender,
            receivedTimestamp = timestamp,
            expiresTimestamp = expirationMillis,
            isUsed = false,
            securityLevel = securityLevel
        )

        otpDao.insertOtp(entity)

        return ProcessedOtpResult(
            code = extracted.code,
            serviceName = serviceName,
            expiresTimestamp = expirationMillis,
            securityWarning = warning,
            isSafe = !hasPhishingLink
        )
    }

    suspend fun markOtpAsUsed(otpId: Long) {
        otpDao.markAsUsed(otpId)
    }

    suspend fun deleteOtp(otpId: Long) {
        otpDao.deleteOtp(otpId)
    }

    suspend fun cleanupExpiredOtps(olderThanMillis: Long) {
        otpDao.deleteOtpsOlderThan(olderThanMillis)
    }

    private fun detectServiceName(sender: String, body: String): String {
        val lowerSender = sender.lowercase()
        val lowerBody = body.lowercase()

        return when {
            lowerSender.contains("melli") || lowerBody.contains("بانک ملی") -> "بانک ملی"
            lowerSender.contains("mellat") || lowerBody.contains("بانک ملت") -> "بانک ملت"
            lowerSender.contains("saderat") || lowerBody.contains("بانک صادرات") -> "بانک صادرات"
            lowerSender.contains("tejarat") || lowerBody.contains("بانک تجارت") -> "بانک تجارت"
            lowerSender.contains("saman") || lowerBody.contains("بانک سامان") -> "بانک سامان"
            lowerSender.contains("blu") || lowerBody.contains("بلو بانک") -> "بلو بانک"
            lowerSender.contains("snapp") || lowerBody.contains("اسنپ") -> "اسنپ"
            lowerSender.contains("tapsi") || lowerBody.contains("تپسی") -> "تپسی"
            lowerSender.contains("digikala") || lowerBody.contains("دیجی کالا") -> "دیجی‌کالا"
            lowerSender.contains("divar") || lowerBody.contains("دیوار") -> "دیوار"
            lowerSender.contains("rubika") || lowerBody.contains("روبیکا") -> "روبیکا"
            lowerSender.contains("bale") || lowerBody.contains("بله") -> "بله"
            lowerSender.contains("eitaa") || lowerBody.contains("ایتا") -> "ایتا"
            lowerSender.contains("telegram") || lowerBody.contains("تلگرام") -> "تلگرام"
            lowerSender.contains("whatsapp") -> "واتساپ"
            lowerSender.contains("google") -> "گوگل"
            else -> if (sender.isNotBlank()) sender else "سرویس عمومی"
        }
    }
}
