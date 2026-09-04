package com.global.sms.ui.smart.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.global.sms.ui.classic.screens.ClassicMessageThreadScreen
import com.global.sms.ui.screens.BankDashboardScreen
import com.global.sms.ui.screens.SearchScreen
import com.global.sms.ui.smart.screens.SmartConversationsScreen
import com.global.sms.ui.viewmodels.GlobalSmsViewModel

sealed class SmartScreen(val route: String) {
    object Conversations : SmartScreen("smart_conversations")
    object Thread : SmartScreen("smart_thread/{threadId}") {
        fun createRoute(threadId: Long) = "smart_thread/$threadId"
    }
    object Search : SmartScreen("smart_search")
    object BankDashboard : SmartScreen("smart_bank")
    object OtpCenter : SmartScreen("smart_otp_center")
}


@Composable
fun SmartNavGraph(
    navController: NavHostController,
    viewModel: GlobalSmsViewModel,
    onOpenSettings: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = SmartScreen.Conversations.route
    ) {
        composable(SmartScreen.Conversations.route) {
            SmartConversationsScreen(
                viewModel = viewModel,
                onOpenThread = { threadId ->
                    viewModel.selectThread(threadId)
                    navController.navigate(SmartScreen.Thread.createRoute(threadId))
                },
                onOpenSearch = { navController.navigate(SmartScreen.Search.route) },
                onOpenBankDashboard = { navController.navigate(SmartScreen.BankDashboard.route) },
                onOpenOtpCenter = { navController.navigate(SmartScreen.OtpCenter.route) },
                onOpenSettings = onOpenSettings,

                onComposeNew = {
                    val newThreadId = System.currentTimeMillis()
                    viewModel.selectThread(newThreadId)
                    navController.navigate(SmartScreen.Thread.createRoute(newThreadId))
                }
            )
        }

        composable(
            route = SmartScreen.Thread.route,
            arguments = listOf(navArgument("threadId") { type = NavType.LongType })
        ) { backStackEntry ->
            val threadId = backStackEntry.arguments?.getLong("threadId") ?: 0L
            ClassicMessageThreadScreen(
                viewModel = viewModel,
                threadId = threadId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(SmartScreen.Search.route) {
            SearchScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onOpenThread = { threadId ->
                    viewModel.selectThread(threadId)
                    navController.navigate(SmartScreen.Thread.createRoute(threadId))
                }
            )
        }

        composable(SmartScreen.BankDashboard.route) {
            BankDashboardScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(SmartScreen.OtpCenter.route) {
            com.global.sms.ui.screens.OtpScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

