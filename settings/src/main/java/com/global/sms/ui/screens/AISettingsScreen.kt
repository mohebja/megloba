package com.global.sms.ui.screens

import androidx.compose.runtime.Composable
import com.global.sms.ui.viewmodels.SettingsViewModel

@Composable
fun AISettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    AiAssistantSettingsScreen(
        viewModel = viewModel,
        onBack = onBack
    )
}
