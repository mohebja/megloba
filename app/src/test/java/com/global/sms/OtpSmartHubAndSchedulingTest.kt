package com.global.sms

import com.global.sms.core.ai.otp.OtpExtractor
import com.global.sms.core.ai.otp.OtpManager
import com.global.sms.core.security.PhishingDetector
import com.global.sms.core.security.ThreatLevel
import com.global.sms.core.smart.SmartAction
import com.global.sms.core.smart.SmartActionExtractor
import com.global.sms.data.dao.OtpDao
import com.global.sms.data.entity.OtpEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.TimeUnit

class OtpSmartHubAndSchedulingTest {

    private class FakeOtpDao : OtpDao {
        val otps = mutableListOf<OtpEntity>()

        override fun getAllOtpsFlow(): Flow<List<OtpEntity>> = flowOf(otps)

        override fun getActiveOtpsFlow(): Flow<List<OtpEntity>> = flowOf(otps.filter { !it.isUsed })

        override suspend fun getOtpForMessage(messageId: Long): OtpEntity? =
            otps.find { it.messageId == messageId }

        override suspend fun insertOtp(otp: OtpEntity): Long {
            otps.removeAll { it.id == otp.id }
            otps.add(otp)
            return otp.id
        }

        override suspend fun markAsUsed(id: Long) {
            val idx = otps.indexOfFirst { it.id == id }
            if (idx != -1) {
                val old = otps[idx]
                otps[idx] = old.copy(isUsed = true)
            }
        }

        override suspend fun deleteOtp(id: Long) {
            otps.removeAll { it.id == id }
        }

        override suspend fun deleteOtpsOlderThan(threshold: Long) {
            otps.removeAll { it.receivedTimestamp < threshold }
        }

        override suspend fun clearAllOtps() {
            otps.clear()
        }
    }

    @Test
    fun testOtpExtractionAndServiceIdentification_BankMelli() = runBlocking {
        val fakeDao = FakeOtpDao()
        val manager = OtpManager(fakeDao)

        val smsBody = "بانک ملی ایران: رمز یکبار مصرف شما جهت ورود به سامانه بام: 749215 معتبر تا 2 دقیقه"
        val sender = "BankMelli"

        val result = manager.processAndStoreOtp(
            messageId = 1001L,
            sender = sender,
            body = smsBody,
            timestamp = 1000000L
        )

        assertNotNull(result)
        assertEquals("749215", result?.code)
        assertEquals("بانک ملی", result?.serviceName)
        assertTrue(result?.isSafe == true)
        assertEquals(1000000L + TimeUnit.MINUTES.toMillis(5), result?.expiresTimestamp)

        assertEquals(1, fakeDao.otps.size)
        assertEquals("NORMAL", fakeDao.otps[0].securityLevel)
    }

    @Test
    fun testOtpExtractionAndServiceIdentification_Digikala() = runBlocking {
        val fakeDao = FakeOtpDao()
        val manager = OtpManager(fakeDao)

        val smsBody = "کد تایید ورود به دیجی‌کالا: 58290. لطفا این کد را در اختیار دیگران قرار ندهید."
        val sender = "Digikala"

        val result = manager.processAndStoreOtp(
            messageId = 1002L,
            sender = sender,
            body = smsBody,
            timestamp = 2000000L
        )

        assertNotNull(result)
        assertEquals("58290", result?.code)
        assertEquals("دیجی‌کالا", result?.serviceName)
        assertTrue(result?.isSafe == true)
    }

    @Test
    fun testOtpWithPhishingLink_TriggersHighSecurityLevel() = runBlocking {
        val fakeDao = FakeOtpDao()
        val manager = OtpManager(fakeDao)

        val suspiciousSms = "رمز پویای شما: 981245 جهت تایید کارت به لینک زیر بروید: https://shaparak-fake.xyz/login"
        val sender = "+989123456789"

        val result = manager.processAndStoreOtp(
            messageId = 1003L,
            sender = sender,
            body = suspiciousSms,
            timestamp = 3000000L
        )

        assertNotNull(result)
        assertEquals("981245", result?.code)
        assertFalse(result?.isSafe ?: true)
        assertNotNull(result?.securityWarning)
        assertEquals("HIGH", fakeDao.otps[0].securityLevel)
    }

    @Test
    fun testPhishingDetector_AnalyzesThreatsAccurately() {
        val safeMessage = "سلام علی جان، جلسه کاری امروز ساعت ۱۶ برگزار خواهد شد."
        val safeResult = PhishingDetector.scanMessage("+989121111111", safeMessage)
        assertFalse(safeResult.isSpamOrPhishing)
        assertEquals(ThreatLevel.LOW, safeResult.threatLevel)

        val phishingMessage = "تبریک! شما برنده وام بدون ضامن شدید! برای ثبت نام فوری کلیک کنید: https://bit.ly/bank-vam-fori"
        val threatResult = PhishingDetector.scanMessage("10008899", phishingMessage)
        assertTrue(threatResult.isSpamOrPhishing)
        assertEquals(ThreatLevel.CRITICAL, threatResult.threatLevel)
        assertTrue(threatResult.detectedUrls.isNotEmpty())
    }

    @Test
    fun testSmartActionExtractor_DetectsOtpAndActions() {
        val sms = "کد تایید شما: 631948 برای پرداخت اینترنتی"
        val actions = SmartActionExtractor.extractActions(sms, "Bank")
        val otpAction = actions.filterIsInstance<SmartAction.CopyOtp>().firstOrNull()

        assertNotNull(otpAction)
        assertEquals("631948", otpAction?.code)
    }

    @Test
    fun testOtpMarkAsUsed() = runBlocking {
        val fakeDao = FakeOtpDao()
        val manager = OtpManager(fakeDao)

        manager.processAndStoreOtp(2001L, "BluBank", "کد ورود بلو: 341829", 5000L)
        assertFalse(fakeDao.otps[0].isUsed)

        manager.markOtpAsUsed(fakeDao.otps[0].id)
        assertTrue(fakeDao.otps[0].isUsed)
    }

    @Test
    fun testAutoResponderModesAndMessages() {
        val drivingMode = com.global.sms.core.autoresponder.AutoResponderMode.DRIVING
        assertTrue(drivingMode.defaultMessageFa.contains("رانندگی"))

        val meetingMode = com.global.sms.core.autoresponder.AutoResponderMode.MEETING
        assertTrue(meetingMode.defaultMessageFa.contains("جلسه"))

        val outOfOfficeMode = com.global.sms.core.autoresponder.AutoResponderMode.OUT_OF_OFFICE
        assertTrue(outOfOfficeMode.defaultMessageFa.contains("ساعات کاری") || outOfOfficeMode.defaultMessageFa.contains("ساعات غیراداری"))
    }
}
