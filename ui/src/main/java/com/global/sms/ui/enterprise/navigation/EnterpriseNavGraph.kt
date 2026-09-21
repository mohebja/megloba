package com.global.sms.ui.enterprise.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.global.sms.ui.screens.*
import com.global.sms.ui.viewmodels.EnterpriseViewModel
import com.global.sms.ui.viewmodels.GlobalSmsViewModel

sealed class EnterpriseScreen(val route: String) {
    object Dashboard : EnterpriseScreen("enterprise_dashboard")
    object CrmCustomers : EnterpriseScreen("crm_customers")
    object BusinessTemplates : EnterpriseScreen("business_templates")
    object BulkSmsSafety : EnterpriseScreen("bulk_sms_safety")
    object WorkflowAutomation : EnterpriseScreen("workflow_automation")
    object EnterpriseAnalytics : EnterpriseScreen("enterprise_analytics")
    object SecurityAuditLogs : EnterpriseScreen("security_audit_logs")
    object AdaptiveWorkspace : EnterpriseScreen("enterprise_adaptive_workspace")
    object Customer360 : EnterpriseScreen("customer_360/{customerId}") {
        fun createRoute(customerId: Long): String = "customer_360/$customerId"
    }
}

@Composable
fun EnterpriseNavGraph(
    navController: NavHostController,
    globalViewModel: GlobalSmsViewModel,
    enterpriseViewModel: EnterpriseViewModel = viewModel(),
    onOpenSettings: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = EnterpriseScreen.Dashboard.route
    ) {
        composable(EnterpriseScreen.Dashboard.route) {
            EnterpriseDashboardScreen(
                viewModel = enterpriseViewModel,
                onBack = onOpenSettings,
                onNavigateCrm = { navController.navigate(EnterpriseScreen.CrmCustomers.route) },
                onNavigateTemplates = { navController.navigate(EnterpriseScreen.BusinessTemplates.route) },
                onNavigateBulkSms = { navController.navigate(EnterpriseScreen.BulkSmsSafety.route) },
                onNavigateAutomation = { navController.navigate(EnterpriseScreen.WorkflowAutomation.route) },
                onNavigateAnalytics = { navController.navigate(EnterpriseScreen.EnterpriseAnalytics.route) },
                onNavigateSecurityAudit = { navController.navigate(EnterpriseScreen.SecurityAuditLogs.route) },
                onNavigateAdaptiveWorkspace = {
                    navController.navigate(EnterpriseScreen.AdaptiveWorkspace.route)
                }
            )
        }

        composable(EnterpriseScreen.AdaptiveWorkspace.route) {
            EnterpriseAdaptiveWorkspace(
                viewModel = enterpriseViewModel,
                onNavigateBack = { navController.popBackStack() },
                onOpenCustomer360 = { customerId ->
                    navController.navigate(EnterpriseScreen.Customer360.createRoute(customerId))
                }
            )
        }

        composable(EnterpriseScreen.CrmCustomers.route) {
            CrmCustomerManagementScreen(
                viewModel = enterpriseViewModel,
                onBack = { navController.popBackStack() },
                onSendMessageToCustomer = { phone ->
                    enterpriseViewModel.sendMessage(0L, phone, "سلام، در خدمت شما هستیم.")
                    navController.navigate(EnterpriseScreen.AdaptiveWorkspace.route)
                },
                onOpenCustomer360 = { customerId ->
                    navController.navigate(EnterpriseScreen.Customer360.createRoute(customerId))
                }
            )
        }

        composable(
            route = EnterpriseScreen.Customer360.route,
            arguments = listOf(navArgument("customerId") { type = NavType.LongType })
        ) { backStackEntry ->
            val customerId = backStackEntry.arguments?.getLong("customerId") ?: 0L
            Customer360Screen(
                customerId = customerId,
                viewModel = enterpriseViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(EnterpriseScreen.BusinessTemplates.route) {
            BusinessTemplateScreen(
                viewModel = enterpriseViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(EnterpriseScreen.BulkSmsSafety.route) {
            BulkSmsSafetyScreen(
                viewModel = enterpriseViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(EnterpriseScreen.WorkflowAutomation.route) {
            WorkflowAutomationScreen(
                viewModel = enterpriseViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(EnterpriseScreen.EnterpriseAnalytics.route) {
            EnterpriseAnalyticsScreen(
                viewModel = enterpriseViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(EnterpriseScreen.SecurityAuditLogs.route) {
            SecurityAuditLogScreen(
                viewModel = enterpriseViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
