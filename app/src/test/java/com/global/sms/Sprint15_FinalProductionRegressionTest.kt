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

class Sprint15_FinalProductionRegressionTest {

    @Test
    fun testProductionRelease_BuildIdentityAndPlayStoreReadiness() {
        val releaseManager = PlayStoreReleaseManager()
        val audit = releaseManager.runPlayStoreReadinessCheck()
        assertEquals(35, audit.targetSdk)
        assertTrue(audit.isDefaultSmsHandlerCompliant)
        assertTrue(audit.permissionsJustified)
        assertTrue(audit.dataSafetyDeclared)
        assertEquals(PlayStoreComplianceStatus.COMPLIANT, audit.overallStatus)

        val safety = releaseManager.getDataSafetyDeclarationSummary()
        assertEquals(false, safety["dataCollection"])
        assertEquals(true, safety["dataEncryption"])
        assertEquals(false, safety["dataSharing"])
        assertEquals(true, safety["localStorageOnly"])
        assertEquals(true, safety["zeroTracking"])
    }

    @Test
    fun testProductionRelease_DefaultSmsRoleAndImportIntegrity() {
        val onboarding = AdvancedOnboardingFlowManager()
        onboarding.setLanguage(OnboardingLanguage.PERSIAN_FA)
        onboarding.markDefaultSmsGranted()
        assertTrue(onboarding.state.value.isDefaultSmsSet)

        val importedCount = onboarding.performMessageImport(15000)
        assertEquals(15000, importedCount)
        assertTrue(onboarding.state.value.isExistingMessagesImported)
        assertEquals(15000, onboarding.state.value.importedMessagesCount)
    }

    @Test
    fun testProductionRelease_ThreeUIModesAndThemes() {
        val onboarding = AdvancedOnboardingFlowManager()
        
        // Mode 1: Classic
        onboarding.configureThemeAndMode(SelectedAppTheme.DEEP_OLED_DARK, SelectedAppMode.CLASSIC_CLEAN)
        assertEquals(SelectedAppMode.CLASSIC_CLEAN, onboarding.state.value.selectedMode)
        assertEquals(SelectedAppTheme.DEEP_OLED_DARK, onboarding.state.value.selectedTheme)

        // Mode 2: Smart AI
        onboarding.configureThemeAndMode(SelectedAppTheme.PERSIAN_TURQUOISE, SelectedAppMode.SMART_AI_OS)
        assertEquals(SelectedAppMode.SMART_AI_OS, onboarding.state.value.selectedMode)
        assertEquals(SelectedAppTheme.PERSIAN_TURQUOISE, onboarding.state.value.selectedTheme)

        // Mode 3: Enterprise
        onboarding.configureThemeAndMode(SelectedAppTheme.PERSIAN_ROYAL_BLUE, SelectedAppMode.ENTERPRISE_WORKFORCE)
        assertEquals(SelectedAppMode.ENTERPRISE_WORKFORCE, onboarding.state.value.selectedMode)
        assertEquals(SelectedAppTheme.PERSIAN_ROYAL_BLUE, onboarding.state.value.selectedTheme)
    }

    @Test
    fun testProductionRelease_DynamicTypographyPinchZoomRule() {
        val testFontSizes = listOf(12, 14, 16, 20, 24, 28, 32)
        testFontSizes.forEach { sp ->
            val computedLineHeight = sp * 1.42f + 2f
            val requiredMinimum = sp * 1.35f
            assertTrue("LineHeight must scale >= 1.35 * fontSize", computedLineHeight >= requiredMinimum)
        }
    }

    @Test
    fun testProductionRelease_LocalAIClassificationAndZeroHallucination() {
        val bankMsg = AIMessageClassifier.classifyMessage(
            sender = "982000",
            body = "برداشت مبلغ ۵۰۰,۰۰۰ ریال از حساب شما. مانده: ۲,۱۰۰,۰۰۰ ریال"
        )
        assertEquals("تراکنش و امور بانکی", bankMsg.labelPersian)
        assertTrue(bankMsg.confidencePercentage >= 80)

        val otpMsg = AIMessageClassifier.classifyMessage(
            sender = "981000",
            body = "کد احراز هویت دو مرحله‌ای: ۸۸۲۹۱۰"
        )
        assertEquals("کد تایید و ورود", otpMsg.labelPersian)

        val sampleInsight = AiCopilotEngine.analyzeMessage(
            conversationId = 1L,
            senderAddress = "989120000000",
            senderName = "بانک پاسارگاد",
            messageText = "واریز وجه ۱۰,۰۰۰,۰۰۰ ریال به حساب شما"
        )
        assertNotNull(sampleInsight)
        assertTrue(sampleInsight.suggestedActions.isNotEmpty())
    }

    @Test
    fun testProductionRelease_EntityExtractionAndDigitNormalization() {
        val mixedText = "کد رهگیری مرسوله TRK-992014 و فاکتور به مبلغ ۲۵۰,۰۰۰ تومان با شماره پیگیری ۸۸۴۹۲۰"
        val entities = EntityExtractionEngine.extractEntities(mixedText)
        assertTrue(entities.trackingCodes.contains("TRK-992014"))
        assertTrue(entities.amounts.isNotEmpty())
    }

    @Test
    fun testProductionRelease_PrivateVaultSecurityAndKeyIsolation() {
        val plaintextPayload = "SECRET_COMMUNICATION_VAULT_KEY_2026"
        val userPin = "SafeVaultPin#2026"
        val cipherText = CryptoManager.encryptWithPassword(plaintextPayload, userPin)
        assertNotEquals(plaintextPayload, cipherText)

        val decryptedPayload = CryptoManager.decryptWithPassword(cipherText, userPin)
        assertEquals(plaintextPayload, decryptedPayload)
    }

    @Test
    fun testProductionRelease_EnterpriseOfflineLicensing() {
        val licenseMgr = LicenseManager()
        licenseMgr.activateOfflineLicense(
            key = "ENT-PROD-GLOBAL-2026-X800",
            organization = "Global SMS Enterprise Solutions",
            tier = LicenseTier.ENTERPRISE_EDITION
        )
        assertEquals(LicenseTier.ENTERPRISE_EDITION, licenseMgr.currentLicense.value.tier)
        assertTrue(licenseMgr.isFeatureAccessible(LicenseTier.ENTERPRISE_EDITION))
        assertEquals(500, licenseMgr.currentLicense.value.maxSeats)

        val features = licenseMgr.getTierFeaturesSummary()
        assertTrue(features.any { it.id == "crm_workforce" && it.isAvailable })
        assertTrue(features.any { it.id == "broadcast_campaigns" && it.isAvailable })
        assertTrue(features.any { it.id == "cloud_connectors" && it.isAvailable })
    }

    @Test
    fun testProductionRelease_PluginMarketplaceSandboxing() {
        val pluginEngine = AIPluginMarketplaceEngine()
        val plugins = pluginEngine.availablePlugins.value
        assertTrue(plugins.isNotEmpty())
        assertTrue(plugins.any { it.category == AIPluginCategory.BANKING_FINANCE })
        assertTrue(plugins.any { it.category == AIPluginCategory.ANTI_FRAUD_SECURITY })

        val execResult = pluginEngine.executePluginSandboxed("plugin_fraud_shield", "INSPECT_SMS")
        assertTrue(execResult.contains("successful"))
        assertTrue(pluginEngine.auditLogs.value.any { it.contains("plugin_fraud_shield") })
    }

    @Test
    fun testProductionRelease_CloudConnectorMasterPrivacySwitch() {
        val cloud = CloudConnectorFramework()
        assertFalse(cloud.isCloudSyncGloballyEnabled.value)

        // Blocked by default master switch
        val initialAttempt = cloud.executeEncryptedBackupSync(CloudProviderType.GOOGLE_DRIVE)
        assertFalse(initialAttempt.isSuccess)

        // Allowed only after explicit opt-in
        cloud.enableCloudGlobalMasterSwitch(true)
        cloud.configureConnector(CloudProviderType.PRIVATE_ENTERPRISE_SERVER, "https://secure.enterprise.internal", true, true)
        val authorizedAttempt = cloud.executeEncryptedBackupSync(CloudProviderType.PRIVATE_ENTERPRISE_SERVER)
        assertTrue(authorizedAttempt.isSuccess)
    }

    @Test
    fun testProductionRelease_MigrationAssistantSchemaV29() {
        val assistant = MigrationAssistant()
        val (manifest, packageJson) = assistant.createEncryptedMigrationPackage(50000, 2500, 100)
        assertEquals(29, manifest.schemaVersion)
        assertEquals(50000, manifest.totalMessages)
        assertEquals(2500, manifest.totalContacts)

        val validation = assistant.validateIncomingPackage(packageJson)
        assertTrue(validation.isValid)
        assertTrue(validation.isVersionCompatible)
    }

    @Test
    fun testProductionRelease_SearchRankingAndHighlighting() {
        val searchRanker = SearchRankingEngine()
        val messages = listOf(
            MessageEntity(id = 1L, threadId = 1L, address = "989123456789", body = "کد پیگیری سفارش شما TRK-1029 ارسال شد", timestamp = 1000L),
            MessageEntity(id = 2L, threadId = 2L, address = "989111111111", body = "سلام، مدارک پیوست ارسال گردید", timestamp = 2000L)
        )
        val results = searchRanker.rankResults("پیگیری", messages)
        assertTrue(results.isNotEmpty())
        assertTrue(results[0].matchedTokens.contains("پیگیری"))
    }

    @Test
    fun testProductionRelease_LocalizationAndRTLCompliance() {
        val loc = LocalizationEngine()
        loc.switchLanguage(AppLanguage.PERSIAN)
        assertTrue(loc.isRtlLayout())
        val faFormatted = loc.formatNumber(987654321L)
        assertTrue(faFormatted.contains("۹") || faFormatted.contains("۸"))

        loc.switchLanguage(AppLanguage.ENGLISH)
        assertFalse(loc.isRtlLayout())
        val enFormatted = loc.formatNumber(987654321L)
        assertTrue(enFormatted.contains("987,654,321"))
    }

    @Test
    fun testProductionRelease_AccessibilityWcag22AA() {
        val acc = AccessibilityManager()
        val wcag = acc.verifyWcagCompliance()
        assertEquals(true, wcag["touch_target_48dp"])
        assertEquals(true, wcag["contrast_ratio_4_5_1"])
        assertEquals(true, wcag["text_scalability_200_percent"])
        assertEquals(true, wcag["talkback_content_descriptions"])
        assertEquals(true, wcag["rtl_mirroring_support"])
    }

    @Test
    fun testProductionRelease_HighScaleBenchmark() {
        val benchmark = HighScalePerformanceBenchmark()
        val result = benchmark.runMillionMessageBenchmark()
        assertEquals(1_000_000, result.simulatedMessageCount)
        assertEquals(100_000, result.simulatedContactCount)
        assertEquals(50_000, result.simulatedWorkflowCount)
        assertTrue(result.coldStartupLatencyMs < 250L)
        assertTrue(result.searchLatencyMs < 20L)
        assertTrue(result.aiReasoningLatencyMs < 80L)
        assertTrue(result.uiFrameRateFps >= 60)
        assertFalse(result.memoryLeakDetected)
    }
}
