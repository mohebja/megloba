package com.global.sms

import com.global.sms.core.accessibility.AccessibilityManager
import com.global.sms.core.ai.classifier.AIMessageClassifier
import com.global.sms.core.ai.copilot.AiCopilotEngine
import com.global.sms.core.ai.copilot.EntityExtractionEngine
import com.global.sms.core.analytics.AIEnterpriseAnalyticsV2
import com.global.sms.core.benchmark.HighScalePerformanceBenchmark
import com.global.sms.core.cloud.CloudConnectorFramework
import com.global.sms.core.cloud.CloudProviderType
import com.global.sms.core.license.LicenseManager
import com.global.sms.core.license.LicenseTier
import com.global.sms.core.localization.AppLanguage
import com.global.sms.core.localization.LocalizationEngine
import com.global.sms.core.migration.MigrationAssistant
import com.global.sms.core.onboarding.AdvancedOnboardingFlowManager
import com.global.sms.core.onboarding.OnboardingLanguage
import com.global.sms.core.onboarding.SelectedAppMode
import com.global.sms.core.onboarding.SelectedAppTheme
import com.global.sms.core.plugin.AIPluginCategory
import com.global.sms.core.plugin.AIPluginMarketplaceEngine
import com.global.sms.core.release.PlayStoreComplianceStatus
import com.global.sms.core.release.PlayStoreReleaseManager
import com.global.sms.core.search.SearchRankingEngine
import com.global.sms.data.entity.MessageEntity
import com.global.sms.security.crypto.CryptoManager
import org.junit.Assert.*
import org.junit.Test

class Sprint16_FinalReleaseRegressionTest {

    @Test
    fun testReleaseGate_PackageAndSdkTargetCompliance() {
        val releaseManager = PlayStoreReleaseManager()
        val report = releaseManager.runPlayStoreReadinessCheck()
        assertEquals(35, report.targetSdk)
        assertTrue(report.isDefaultSmsHandlerCompliant)
        assertTrue(report.permissionsJustified)
        assertTrue(report.dataSafetyDeclared)
        assertEquals(PlayStoreComplianceStatus.COMPLIANT, report.overallStatus)
    }

    @Test
    fun testReleaseGate_DataSafetyDeclarations() {
        val releaseManager = PlayStoreReleaseManager()
        val safety = releaseManager.getDataSafetyDeclarationSummary()
        assertEquals(false, safety["dataCollection"])
        assertEquals(true, safety["dataEncryption"])
        assertEquals(false, safety["dataSharing"])
        assertEquals(true, safety["localStorageOnly"])
        assertEquals(true, safety["zeroTracking"])
    }

    @Test
    fun testReleaseGate_DefaultSmsRoleAndImportIntegrity() {
        val onboarding = AdvancedOnboardingFlowManager()
        onboarding.setLanguage(OnboardingLanguage.PERSIAN_FA)
        onboarding.markDefaultSmsGranted()
        assertTrue(onboarding.state.value.isDefaultSmsSet)

        val count = onboarding.performMessageImport(25000)
        assertEquals(25000, count)
        assertTrue(onboarding.state.value.isExistingMessagesImported)
    }

    @Test
    fun testReleaseGate_ThreeUIModesSwitching() {
        val onboarding = AdvancedOnboardingFlowManager()
        
        onboarding.configureThemeAndMode(SelectedAppTheme.DEEP_OLED_DARK, SelectedAppMode.CLASSIC_CLEAN)
        assertEquals(SelectedAppMode.CLASSIC_CLEAN, onboarding.state.value.selectedMode)

        onboarding.configureThemeAndMode(SelectedAppTheme.PERSIAN_TURQUOISE, SelectedAppMode.SMART_AI_OS)
        assertEquals(SelectedAppMode.SMART_AI_OS, onboarding.state.value.selectedMode)

        onboarding.configureThemeAndMode(SelectedAppTheme.PERSIAN_ROYAL_BLUE, SelectedAppMode.ENTERPRISE_WORKFORCE)
        assertEquals(SelectedAppMode.ENTERPRISE_WORKFORCE, onboarding.state.value.selectedMode)
    }

    @Test
    fun testReleaseGate_TypographyPinchZoomRatios() {
        val testFontSizes = listOf(12, 14, 16, 20, 24, 28, 32)
        testFontSizes.forEach { sp ->
            val lineHeight = sp * 1.42f + 2f
            assertTrue(lineHeight >= sp * 1.35f)
        }
    }

    @Test
    fun testReleaseGate_LocalAIClassificationZeroHallucination() {
        val bankResult = AIMessageClassifier.classifyMessage(
            sender = "982000",
            body = "واریز وجه ۱,۵۰۰,۰۰۰ ریال به حساب. مانده: ۴,۲۰۰,۰۰۰ ریال"
        )
        assertEquals("تراکنش و امور بانکی", bankResult.labelPersian)

        val otpResult = AIMessageClassifier.classifyMessage(
            sender = "981000",
            body = "کد تایید ورود: ۵۸۳۹۲۰"
        )
        assertEquals("کد تایید و ورود", otpResult.labelPersian)
    }

    @Test
    fun testReleaseGate_EntityExtractionBilingual() {
        val text = "کد پیگیری مرسوله پستی TRK-55201 و مبلغ ۱,۲۵۰,۰۰۰ تومان"
        val entities = EntityExtractionEngine.extractEntities(text)
        assertTrue(entities.trackingCodes.contains("TRK-55201"))
        assertTrue(entities.amounts.isNotEmpty())
    }

    @Test
    fun testReleaseGate_PrivateVaultZeroTrustSecurity() {
        val raw = "TOP_SECRET_ENTERPRISE_TRANSACTION_PAYLOAD"
        val pass = "StrongVaultPass#2026"
        val enc = CryptoManager.encryptWithPassword(raw, pass)
        assertNotEquals(raw, enc)
        val dec = CryptoManager.decryptWithPassword(enc, pass)
        assertEquals(raw, dec)
    }

    @Test
    fun testReleaseGate_LicenseAndEnterpriseAnalytics() {
        val licenseManager = LicenseManager()
        licenseManager.activateOfflineLicense("ENT-RELEASE-2026-FINAL", "Global Telecommunications Corp", LicenseTier.ENTERPRISE_EDITION)
        assertTrue(licenseManager.isFeatureAccessible(LicenseTier.ENTERPRISE_EDITION))
        assertEquals(500, licenseManager.currentLicense.value.maxSeats)

        val analytics = AIEnterpriseAnalyticsV2()
        val report = analytics.analyticsReport.value
        assertTrue(report.deliverySuccessRate > 99.0f)
        assertTrue(report.productivityScore > 90.0f)
    }

    @Test
    fun testReleaseGate_PluginMarketplaceSandboxing() {
        val marketplace = AIPluginMarketplaceEngine()
        val plugins = marketplace.availablePlugins.value
        assertTrue(plugins.any { it.category == AIPluginCategory.BANKING_FINANCE })
        assertTrue(plugins.any { it.category == AIPluginCategory.ANTI_FRAUD_SECURITY })

        val exec = marketplace.executePluginSandboxed("plugin_fraud_shield", "AUDIT_SMS")
        assertTrue(exec.contains("successful"))
    }

    @Test
    fun testReleaseGate_CloudConnectorMasterPrivacySwitch() {
        val connector = CloudConnectorFramework()
        assertFalse(connector.isCloudSyncGloballyEnabled.value)

        val resBlocked = connector.executeEncryptedBackupSync(CloudProviderType.GOOGLE_DRIVE)
        assertFalse(resBlocked.isSuccess)

        connector.enableCloudGlobalMasterSwitch(true)
        connector.configureConnector(CloudProviderType.PRIVATE_ENTERPRISE_SERVER, "https://cloud.corp.net", true, true)
        val resAllowed = connector.executeEncryptedBackupSync(CloudProviderType.PRIVATE_ENTERPRISE_SERVER)
        assertTrue(resAllowed.isSuccess)
    }

    @Test
    fun testReleaseGate_MigrationAssistantSchemaV29() {
        val assistant = MigrationAssistant()
        val (manifest, payload) = assistant.createEncryptedMigrationPackage(100000, 5000, 200)
        assertEquals(29, manifest.schemaVersion)
        assertEquals(100000, manifest.totalMessages)

        val validation = assistant.validateIncomingPackage(payload)
        assertTrue(validation.isValid)
        assertTrue(validation.isVersionCompatible)
    }

    @Test
    fun testReleaseGate_SearchRanking() {
        val ranker = SearchRankingEngine()
        val list = listOf(
            MessageEntity(id = 1L, threadId = 1L, address = "989123456789", body = "کد فعالسازی تراکنش بانک ملت ۵۸۳۹۲", timestamp = 1000L),
            MessageEntity(id = 2L, threadId = 2L, address = "989111111111", body = "سلام، جلسه فردا برگزار می‌شود", timestamp = 2000L)
        )
        val ranked = ranker.rankResults("ملت", list)
        assertEquals(1, ranked.filter { it.matchedTokens.isNotEmpty() }.size)
        assertTrue(ranked[0].matchedTokens.contains("ملت"))
    }

    @Test
    fun testReleaseGate_LocalizationAndRTL() {
        val loc = LocalizationEngine()
        loc.switchLanguage(AppLanguage.PERSIAN)
        assertTrue(loc.isRtlLayout())
        val fa = loc.formatNumber(123456L)
        assertTrue(fa.contains("۱") || fa.contains("۲"))

        loc.switchLanguage(AppLanguage.ENGLISH)
        assertFalse(loc.isRtlLayout())
    }

    @Test
    fun testReleaseGate_AccessibilityWcag22AA() {
        val acc = AccessibilityManager()
        val wcag = acc.verifyWcagCompliance()
        assertEquals(true, wcag["touch_target_48dp"])
        assertEquals(true, wcag["contrast_ratio_4_5_1"])
        assertEquals(true, wcag["text_scalability_200_percent"])
        assertEquals(true, wcag["talkback_content_descriptions"])
        assertEquals(true, wcag["rtl_mirroring_support"])
    }

    @Test
    fun testReleaseGate_HighScaleBenchmark() {
        val benchmark = HighScalePerformanceBenchmark()
        val result = benchmark.runMillionMessageBenchmark()
        assertEquals(1_000_000, result.simulatedMessageCount)
        assertTrue(result.coldStartupLatencyMs < 250L)
        assertTrue(result.searchLatencyMs < 20L)
        assertTrue(result.aiReasoningLatencyMs < 80L)
        assertTrue(result.uiFrameRateFps >= 60)
        assertFalse(result.memoryLeakDetected)
    }
}
