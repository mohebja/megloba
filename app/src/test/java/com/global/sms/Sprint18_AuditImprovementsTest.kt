package com.global.sms

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.global.sms.core.financial.DigitalReceiptGenerator
import com.global.sms.core.parser.BankTransactionParser
import com.global.sms.core.util.PersianUtils
import com.global.sms.data.entity.SecurityAuditLogEntity
import com.global.sms.engine.dispatcher.EnterpriseAutomationDispatcher
import com.global.sms.security.panic.EmergencyPanicSecurityManager
import com.global.sms.security.panic.PanicExecutionResult
import com.global.sms.security.panic.PanicState
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Sprint18_AuditImprovementsTest {

    // -------------------------------------------------------------
    // 1. Bidi Isolation Tests
    // -------------------------------------------------------------
    @Test
    fun testBidiIsolation_CardAndTrackingNumbers() {
        val sampleCard = "۶۰۳۷-۹۹۱۸-****-۱۲۳۴"
        val isolated = PersianUtils.formatBidiLtr(sampleCard)

        assertTrue(isolated.startsWith("\u200E"))
        assertTrue(isolated.endsWith("\u200E"))
        assertTrue(isolated.contains(sampleCard))

        // Receipt Generator Bidi output check
        val sms = "بانک سامان\nواریز: ۵۰۰,۰۰۰ تومان\nبه: ۶۲۱۹-۸۶۱۰-۳۳۴۴-۵۵۶۶\nمانده: ۲,۰۰۰,۰۰۰ تومان\nپیگیری: ۷۷۸۸۹۹۰۰"
        val receipt = DigitalReceiptGenerator.generateReceiptFromSms(sender = "SAMAN", body = sms)

        assertNotNull(receipt)
        receipt?.let {
            assertNotNull(it.maskedCardNumber)
            assertTrue(it.maskedCardNumber!!.startsWith("\u200E"))
            assertTrue(it.maskedCardNumber!!.endsWith("\u200E"))
            assertTrue(it.trackingNumber.startsWith("\u200E"))
            assertTrue(it.trackingNumber.endsWith("\u200E"))
            assertTrue(it.formattedDatePersian.startsWith("\u200E"))
        }
    }

    // -------------------------------------------------------------
    // 2. Emergency Panic Security & Window Protection
    // -------------------------------------------------------------
    @Test
    fun testEmergencyPanic_WindowProtectionAndHardening() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val panicManager = EmergencyPanicSecurityManager(context)

        panicManager.resetPanicState()
        assertFalse(panicManager.isWindowProtectionActive())

        // Set duress pin
        panicManager.setDuressPin("8888")
        assertTrue(panicManager.isDuressPinSet())

        // Trigger duress mode
        val res = panicManager.verifyPinWithPanicCheck("8888")
        assertEquals(PanicExecutionResult.DuressDecoyActivated, res)
        assertEquals(PanicState.DURESS_DECOY_ACTIVE, EmergencyPanicSecurityManager.panicState.value)
        assertTrue(panicManager.isWindowProtectionActive())

        // Reset
        panicManager.resetPanicState()
        assertEquals(PanicState.NORMAL, EmergencyPanicSecurityManager.panicState.value)
        assertFalse(panicManager.isWindowProtectionActive())
    }

    // -------------------------------------------------------------
    // 3. Non-Blocking Audit Queue
    // -------------------------------------------------------------
    @Test
    fun testNonBlockingAuditQueue_EnqueueDoesNotBlock() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val auditLog = SecurityAuditLogEntity(
            eventType = "BURST_TEST",
            description = "تست صف غیرمسدودکننده پیامک",
            operatorName = "UnitTester",
            ipOrDeviceId = "TEST_DEVICE"
        )

        // Enqueueing multiple logs concurrently must not throw or block
        for (i in 1..20) {
            EnterpriseAutomationDispatcher.enqueueAuditLog(
                context = context,
                logEntry = auditLog.copy(description = "لاگ شماره $i")
            )
        }
    }

    // -------------------------------------------------------------
    // 4. LRU Memory Cache & Clear
    // -------------------------------------------------------------
    @Test
    fun testBankTransactionParser_LruCacheAndClear() {
        val testSms = "بانک پاسارگاد\nواریز: ۱,۰۰۰,۰۰۰ ریال\nمانده: ۳,۰۰۰,۰۰۰ ریال\nپیگیری: ۱۲۳۴۵"
        BankTransactionParser.analyzeMessage(sender = "PASARGAD", body = testSms)

        assertTrue(BankTransactionParser.getCacheSize() > 0)

        // Clear cache under memory pressure
        BankTransactionParser.clearCache()
        assertEquals(0, BankTransactionParser.getCacheSize())
    }
}
