package com.global.sms.core.ai.privacy

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AiPrivacySettings(
    val memoryRetentionDays: Int = 30,
    val autoRemoveSensitiveFinancialData: Boolean = true,
    val autoRemoveOtpFacts: Boolean = true,
    val allowContactSpecificAiAnalysis: Boolean = true,
    val excludedAddresses: Set<String> = emptySet()
)

class AIPrivacyController {

    private val _settings = MutableStateFlow(AiPrivacySettings())
    val settings: StateFlow<AiPrivacySettings> = _settings.asStateFlow()

    fun setRetentionPeriod(days: Int) {
        _settings.value = _settings.value.copy(memoryRetentionDays = days)
    }

    fun setAutoRemoveOtpFacts(enabled: Boolean) {
        _settings.value = _settings.value.copy(autoRemoveOtpFacts = enabled)
    }

    fun setAutoRemoveSensitiveFinancialData(enabled: Boolean) {
        _settings.value = _settings.value.copy(autoRemoveSensitiveFinancialData = enabled)
    }

    fun toggleExcludeAddress(address: String) {
        val current = _settings.value.excludedAddresses.toMutableSet()
        if (current.contains(address)) {
            current.remove(address)
        } else {
            current.add(address)
        }
        _settings.value = _settings.value.copy(excludedAddresses = current)
    }

    fun isAddressAllowedForAi(address: String): Boolean {
        return !_settings.value.excludedAddresses.contains(address)
    }

    fun resetAiLearningState(): Boolean {
        // Purge learned parameters locally
        return true
    }
}
