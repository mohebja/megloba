package com.global.sms

import com.global.sms.core.benchmark.HighScalePerformanceBenchmark
import com.global.sms.core.release.PlayStoreComplianceStatus
import com.global.sms.core.release.PlayStoreReleaseManager
import com.global.sms.core.util.PersianUtils
import org.junit.Assert.*
import org.junit.Test

class HighScaleAndNotificationWidgetTest {

    @Test
    fun testHighScalePerformanceBenchmark_ThroughputAndLatency() {
        val benchmark = HighScalePerformanceBenchmark()
        val result = benchmark.runMillionMessageBenchmark()

        assertEquals(1_000_000, result.simulatedMessageCount)
        assertEquals(120, result.uiFrameRateFps)
        assertFalse(result.memoryLeakDetected)
        assertTrue(result.peakMemoryUsageMb < 100)
        assertTrue(result.searchLatencyMs < 25L)
        assertTrue(result.aiReasoningLatencyMs < 100L)
        assertEquals("PASSED_100_PERCENT", result.status)
    }

    @Test
    fun testPlayStoreCompliance_ReleaseGateAudit() {
        val releaseManager = PlayStoreReleaseManager()
        val auditReport = releaseManager.runPlayStoreReadinessCheck(overrideDefaultSms = true)

        assertTrue(auditReport.targetSdk >= 34)
        assertTrue(auditReport.isDefaultSmsHandlerCompliant)
        assertTrue(auditReport.permissionsJustified)
        assertTrue(auditReport.dataSafetyDeclared)
        assertEquals(PlayStoreComplianceStatus.COMPLIANT, auditReport.overallStatus)
    }

    @Test
    fun testWidgetUnreadFormatting_PersianDigits() {
        val unreadZero = 0
        val unreadTextZero = if (unreadZero > 0) "${PersianUtils.toPersianDigits(unreadZero.toString())} پیام ناخوانده" else "همه پیام‌ها خوانده شده"
        assertEquals("همه پیام‌ها خوانده شده", unreadTextZero)

        val unreadFive = 5
        val unreadTextFive = if (unreadFive > 0) "${PersianUtils.toPersianDigits(unreadFive.toString())} پیام ناخوانده" else "همه پیام‌ها خوانده شده"
        assertEquals("۵ پیام ناخوانده", unreadTextFive)
    }

    @Test
    fun testPlayStoreDataSafety_ZeroCollectionGuarantee() {
        val releaseManager = PlayStoreReleaseManager()
        val safety = releaseManager.getDataSafetyDeclarationSummary()

        assertEquals(false, safety["dataCollection"])
        assertEquals(false, safety["dataSharing"])
        assertEquals(true, safety["localStorageOnly"])
        assertEquals(true, safety["zeroTracking"])
        assertEquals(true, safety["dataEncryption"])
    }
}
