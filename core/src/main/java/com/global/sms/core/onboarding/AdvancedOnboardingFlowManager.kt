package com.global.sms.core.onboarding

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class OnboardingLanguage {
    PERSIAN_FA,
    ENGLISH_US,
    ARABIC,
    FRENCH,
    SPANISH,
    GERMAN
}

enum class SelectedAppMode {
    CLASSIC_CLEAN,
    SMART_AI_OS,
    ENTERPRISE_WORKFORCE
}

enum class SelectedAppTheme {
    PERSIAN_TURQUOISE,
    PERSIAN_ROYAL_BLUE,
    DEEP_OLED_DARK,
    DYNAMIC_MONET
}

data class OnboardingState(
    val language: OnboardingLanguage = OnboardingLanguage.PERSIAN_FA,
    val isDefaultSmsSet: Boolean = false,
    val isExistingMessagesImported: Boolean = false,
    val importedMessagesCount: Int = 0,
    val selectedTheme: SelectedAppTheme = SelectedAppTheme.DEEP_OLED_DARK,
    val selectedMode: SelectedAppMode = SelectedAppMode.SMART_AI_OS,
    val isAiReasoningActive: Boolean = true,
    val isOtpExtractionActive: Boolean = true,
    val isZeroTrustVaultActive: Boolean = false,
    val isCompleted: Boolean = false
)

class AdvancedOnboardingFlowManager {
    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    fun setLanguage(language: OnboardingLanguage) {
        _state.value = _state.value.copy(language = language)
    }

    fun markDefaultSmsGranted() {
        _state.value = _state.value.copy(isDefaultSmsSet = true)
    }

    fun performMessageImport(count: Int): Int {
        _state.value = _state.value.copy(
            isExistingMessagesImported = true,
            importedMessagesCount = count
        )
        return count
    }

    fun configureThemeAndMode(theme: SelectedAppTheme, mode: SelectedAppMode) {
        _state.value = _state.value.copy(
            selectedTheme = theme,
            selectedMode = mode
        )
    }

    fun configureAiPreferences(aiReasoning: Boolean, otpExtract: Boolean, zeroTrust: Boolean) {
        _state.value = _state.value.copy(
            isAiReasoningActive = aiReasoning,
            isOtpExtractionActive = otpExtract,
            isZeroTrustVaultActive = zeroTrust
        )
    }

    fun completeOnboarding(): Boolean {
        _state.value = _state.value.copy(isCompleted = true)
        return true
    }
}
