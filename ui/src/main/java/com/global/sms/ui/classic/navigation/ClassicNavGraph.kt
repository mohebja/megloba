package com.global.sms.ui.classic.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.global.sms.ui.classic.screens.ClassicConversationsScreen
import com.global.sms.ui.classic.screens.ClassicMessageThreadScreen
import com.global.sms.ui.viewmodels.GlobalSmsViewModel

sealed class ClassicScreen(val route: String) {
    object Conversations : ClassicScreen("classic_conversations")
    object Thread : ClassicScreen("classic_thread/{threadId}") {
        fun createRoute(threadId: Long) = "classic_thread/$threadId"
    }
}

@Composable
fun ClassicNavGraph(
    navController: NavHostController,
    viewModel: GlobalSmsViewModel,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = ClassicScreen.Conversations.route
    ) {
        composable(ClassicScreen.Conversations.route) {
            ClassicConversationsScreen(
                viewModel = viewModel,
                onOpenThread = { threadId ->
                    viewModel.selectThread(threadId)
                    navController.navigate(ClassicScreen.Thread.createRoute(threadId))
                },
                onOpenSearch = onOpenSearch,
                onOpenSettings = onOpenSettings,
                onComposeNew = {
                    val newThreadId = System.currentTimeMillis()
                    viewModel.selectThread(newThreadId)
                    navController.navigate(ClassicScreen.Thread.createRoute(newThreadId))
                }
            )
        }

        composable(
            route = ClassicScreen.Thread.route,
            arguments = listOf(navArgument("threadId") { type = NavType.LongType })
        ) { backStackEntry ->
            val threadId = backStackEntry.arguments?.getLong("threadId") ?: 0L
            ClassicMessageThreadScreen(
                viewModel = viewModel,
                threadId = threadId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
