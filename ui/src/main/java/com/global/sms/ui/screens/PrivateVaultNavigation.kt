package com.global.sms.ui.screens

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.global.sms.ui.viewmodels.GlobalSmsViewModel

fun NavGraphBuilder.privateVaultGraph(
    navController: NavController,
    viewModel: GlobalSmsViewModel
) {
    composable("private_vault") {
        PrivateVaultScreen(
            viewModel = viewModel,
            onBack = { navController.popBackStack() }
        )
    }
}
