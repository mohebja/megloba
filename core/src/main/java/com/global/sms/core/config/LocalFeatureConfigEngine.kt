package com.global.sms.core.config

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class FeatureFlags(
    val enableAiCopilot: Boolean = true,
    val enableLocalAiBrain: Boolean = true,
    val enablePrivateVault: Boolean = true,
    val enableEnterpriseCrm: Boolean = true,
    val enableCampaignManager: Boolean = true,
    val enableBankScanner: Boolean = true,
    val enableFraudProtection: Boolean = true,
    val enableAutoReplyWorkflows: Boolean = true,
    val enableExperimentalV3Summarizer: Boolean = true
)

/**
 * Local Feature Configuration Engine for Global SMS.
 * Manages feature flags, experiment toggles, and enterprise modes locally without cloud dependencies.
 */
class LocalFeatureConfigEngine private constructor(private val context: Context) {

    private val _flags = MutableStateFlow(FeatureFlags())
    val flags: StateFlow<FeatureFlags> = _flags.asStateFlow()

    companion object {
        @Volatile
        private var instance: LocalFeatureConfigEngine? = null

        fun getInstance(context: Context): LocalFeatureConfigEngine {
            return instance ?: synchronized(this) {
                instance ?: LocalFeatureConfigEngine(context.applicationContext).also { instance = it }
            }
        }
    }

    fun isFeatureEnabled(featureKey: String): Boolean {
        val current = _flags.value
        return when (featureKey) {
            "ai_copilot" -> current.enableAiCopilot
            "local_ai_brain" -> current.enableLocalAiBrain
            "private_vault" -> current.enablePrivateVault
            "enterprise_crm" -> current.enableEnterpriseCrm
            "campaign_manager" -> current.enableCampaignManager
            "bank_scanner" -> current.enableBankScanner
            "fraud_protection" -> current.enableFraudProtection
            "auto_reply" -> current.enableAutoReplyWorkflows
            "experimental_v3_summarizer" -> current.enableExperimentalV3Summarizer
            else -> true
        }
    }

    fun updateFlags(newFlags: FeatureFlags) {
        _flags.value = newFlags
    }
}
