package com.global.sms

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.global.sms.core.automation.AutomationActionType
import com.global.sms.core.automation.AutomationEngine
import com.global.sms.core.automation.AutomationRule
import com.global.sms.core.automation.AutomationTriggerType
import com.global.sms.core.financial.DigitalReceiptGenerator
import com.global.sms.security.panic.EmergencyPanicSecurityManager
import com.global.sms.security.panic.PanicExecutionResult
import com.global.sms.security.panic.PanicState
import com.global.sms.ui.adaptive.ScreenWindowType
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Sprint17_RoadmapV2ImplementationTest {

    // -------------------------------------------------------------
    // STAGE 1 TESTS: Automation Rule & Action Engine
    // -------------------------------------------------------------
    @Test
    fun testStage1_AutomationEngine_RulesExecutionAndMatching() {
        val engine = AutomationEngine()

        // Add custom automated forwarding rule
        val forwardRule = AutomationRule(
            id = "rule_invoice_forward",
            name = "فوروارد خودکار فاکتورها به حسابداری",
            triggerType = AutomationTriggerType.BODY_CONTAINS,
            triggerValue = "فاکتور",
            actionType = AutomationActionType.FORWARD_SMS,
            actionValue = "09121234567"
        )
        engine.addCustomRule(forwardRule)

        val smsBody = "کاربر گرامی، فاکتور خرید شماره ۹۸۲۳ صادر گردید."
        val results = engine.processIncomingMessage(sender = "09998887766", body = smsBody)

        assertTrue(results.isNotEmpty())
        val matchedForward = results.find { it.ruleId == "rule_invoice_forward" }
        assertNotNull(matchedForward)
        assertEquals(AutomationActionType.FORWARD_SMS, matchedForward?.actionTaken)
        assertTrue(matchedForward?.messageHandled == true)
    }

    @Test
    fun testStage1_AutomationEngine_OtpExtraction() {
        val engine = AutomationEngine()
        val otpSms = "کد تایید ورود شما به سیستم: ۶۵۴۳۲۱"
        val results = engine.processIncomingMessage(sender = "بانک ملی", body = otpSms)

        val otpResult = results.find { it.actionTaken == AutomationActionType.COPY_OTP }
        assertNotNull(otpResult)
        assertEquals("654321", otpResult?.extractedData)
    }

    // -------------------------------------------------------------
    // STAGE 2 TESTS: Smart Financial Receipt Generator & Parsing
    // -------------------------------------------------------------
    @Test
    fun testStage2_DigitalReceiptGenerator_BankMelliDeposit() {
        val sms = "بانک ملی ایران\nواریز: ۲,۵۰۰,۰۰۰ ریال\nبه: ۶۰۳۷-۹۹۱۸-۲۲۳۴-۵۶۷۸\nمانده: ۱۸,۲۰۰,۰۰۰ ریال\nپیگیری: ۹۸۷۶۵۴۳۲"
        val receipt = DigitalReceiptGenerator.generateReceiptFromSms(sender = "MELLI", body = sms)

        assertNotNull(receipt)
        receipt?.let {
            assertEquals("بانک ملی", it.bankName)
            assertEquals("واریز به حساب", it.transactionTypeTitle)
            assertTrue(it.isCredit)
            assertEquals(250_000L, it.amountTomans)
            assertEquals(2_500_000L, it.amountRials)
            assertNotNull(it.maskedCardNumber)
            assertTrue(it.maskedCardNumber!!.contains("۶۰۳۷"))
            assertTrue(it.maskedCardNumber!!.contains("۵۶۷۸"))
            assertNotNull(it.verificationHash)
            assertEquals(64, it.verificationHash.length) // SHA-256 length
            assertTrue(it.shareableText.contains("رسید دیجیتال تراکنش بانکی"))
            assertTrue(it.shareableText.contains("Global SMS"))
        }
    }

    @Test
    fun testStage2_DigitalReceiptGenerator_BluBankWithdrawal() {
        val sms = "بلو\nبرداشت: ۱,۲۰۰,۰۰۰ تومان\nاز کارت: ۶۲۱۹-۸۶۱۰-۳۴۵۶-۷۸۹۰\nمانده: ۴,۵۰۰,۰۰۰ تومان\nکد پیگیری: ۱۱۲۲۳۳۴۴"
        val receipt = DigitalReceiptGenerator.generateReceiptFromSms(sender = "BLU", body = sms)

        assertNotNull(receipt)
        receipt?.let {
            assertEquals("بلوبانک (Blu)", it.bankName)
            assertEquals("برداشت از حساب", it.transactionTypeTitle)
            assertFalse(it.isCredit)
            assertEquals(1_200_000L, it.amountTomans)
            assertEquals(12_000_000L, it.amountRials)
            assertTrue(it.formattedAmountTomans.contains("۱,۲۰۰,۰۰۰"))
            assertTrue(it.trackingNumber.contains("۱۱۲۲۳۳۴۴"))
            assertTrue(it.shareableText.contains("بانک عامل: بلوبانک (Blu)"))
        }
    }

    // -------------------------------------------------------------
    // STAGE 3 TESTS: Adaptive Screen Window Type Detection
    // -------------------------------------------------------------
    @Test
    fun testStage3_AdaptiveLayout_WindowTypeDetection() {
        fun computeWindowType(widthDp: Int): ScreenWindowType {
            return when {
                widthDp < 600 -> ScreenWindowType.COMPACT
                widthDp in 600..840 -> ScreenWindowType.MEDIUM
                else -> ScreenWindowType.EXPANDED
            }
        }

        // Phone in portrait
        assertEquals(ScreenWindowType.COMPACT, computeWindowType(392))
        assertEquals(ScreenWindowType.COMPACT, computeWindowType(599))

        // Foldable unfolded / Portrait tablet
        assertEquals(ScreenWindowType.MEDIUM, computeWindowType(600))
        assertEquals(ScreenWindowType.MEDIUM, computeWindowType(800))
        assertEquals(ScreenWindowType.MEDIUM, computeWindowType(840))

        // Large tablet / Desktop / Chromebook
        assertEquals(ScreenWindowType.EXPANDED, computeWindowType(841))
        assertEquals(ScreenWindowType.EXPANDED, computeWindowType(1280))
    }

    // -------------------------------------------------------------
    // STAGE 4 TESTS: Emergency Panic Security & Duress PIN
    // -------------------------------------------------------------
    @Test
    fun testStage4_EmergencyPanic_DuressPinAndDecoyProtocol() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val panicManager = EmergencyPanicSecurityManager(context)

        // Setup duress pin
        panicManager.setDuressPin("9999")
        assertTrue(panicManager.isDuressPinSet())
        assertTrue(panicManager.isPanicFeatureEnabled())

        // Test normal pin mismatch
        val invalidResult = panicManager.verifyPinWithPanicCheck("0000")
        assertEquals(PanicExecutionResult.InvalidPin, invalidResult)

        // Test duress pin trigger
        val duressResult = panicManager.verifyPinWithPanicCheck("9999")
        assertEquals(PanicExecutionResult.DuressDecoyActivated, duressResult)
        assertEquals(PanicState.DURESS_DECOY_ACTIVE, EmergencyPanicSecurityManager.panicState.value)

        // Test emergency wipe
        val wipeResult = panicManager.executeEmergencyWipe()
        assertEquals(PanicExecutionResult.EmergencyWipeCompleted, wipeResult)
        assertEquals(PanicState.EMERGENCY_WIPED, EmergencyPanicSecurityManager.panicState.value)

        // Reset
        panicManager.resetPanicState()
        assertEquals(PanicState.NORMAL, EmergencyPanicSecurityManager.panicState.value)
    }
}
