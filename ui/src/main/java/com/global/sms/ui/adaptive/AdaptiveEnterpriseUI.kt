package com.global.sms.ui.adaptive

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.global.sms.ui.screens.*

enum class EnterpriseScreen {
    WORKSPACE,
    WORKFLOW_DESIGNER,
    BI_DASHBOARD,
    SECURITY_CENTER
}

@Composable
fun AdaptiveEnterpriseUI(
    initialScreen: EnterpriseScreen = EnterpriseScreen.WORKSPACE,
    onNavigateBack: () -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val isWideScreen = configuration.screenWidthDp >= 800

    var currentScreen by remember { mutableStateOf(initialScreen) }

    if (isWideScreen) {
        // Desktop / Tablet Row layout with NavigationRail
        Row(modifier = Modifier.fillMaxSize().testTag("adaptive_enterprise_ui_wide")) {
            NavigationRail {
                FloatingActionButton(
                    onClick = { currentScreen = EnterpriseScreen.WORKSPACE },
                    modifier = Modifier.padding(vertical = 12.dp)
                ) {
                    Icon(Icons.Default.Business, contentDescription = "Workspace")
                }
                NavigationRailItem(
                    selected = currentScreen == EnterpriseScreen.WORKSPACE,
                    onClick = { currentScreen = EnterpriseScreen.WORKSPACE },
                    icon = { Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null) },
                    label = { Text("Workspace") }
                )
                NavigationRailItem(
                    selected = currentScreen == EnterpriseScreen.WORKFLOW_DESIGNER,
                    onClick = { currentScreen = EnterpriseScreen.WORKFLOW_DESIGNER },
                    icon = { Icon(Icons.Default.AccountTree, contentDescription = null) },
                    label = { Text("Workflows") }
                )
                NavigationRailItem(
                    selected = currentScreen == EnterpriseScreen.BI_DASHBOARD,
                    onClick = { currentScreen = EnterpriseScreen.BI_DASHBOARD },
                    icon = { Icon(Icons.Default.Analytics, contentDescription = null) },
                    label = { Text("BI Analytics") }
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                when (currentScreen) {
                    EnterpriseScreen.WORKSPACE -> EnterpriseChatWorkspaceScreen(onNavigateBack = onNavigateBack)
                    EnterpriseScreen.WORKFLOW_DESIGNER -> WorkflowDesignerScreen(onNavigateBack = onNavigateBack)
                    EnterpriseScreen.BI_DASHBOARD -> BusinessIntelligenceDashboard(onNavigateBack = onNavigateBack)
                    EnterpriseScreen.SECURITY_CENTER -> AIAgentSecurityDashboard(onNavigateBack = onNavigateBack)
                }
            }
        }
    } else {
        // Phone layout with BottomNavigationBar
        Scaffold(
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentScreen == EnterpriseScreen.WORKSPACE,
                        onClick = { currentScreen = EnterpriseScreen.WORKSPACE },
                        icon = { Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null) },
                        label = { Text("Chat") }
                    )
                    NavigationBarItem(
                        selected = currentScreen == EnterpriseScreen.WORKFLOW_DESIGNER,
                        onClick = { currentScreen = EnterpriseScreen.WORKFLOW_DESIGNER },
                        icon = { Icon(Icons.Default.AccountTree, contentDescription = null) },
                        label = { Text("Workflows") }
                    )
                    NavigationBarItem(
                        selected = currentScreen == EnterpriseScreen.BI_DASHBOARD,
                        onClick = { currentScreen = EnterpriseScreen.BI_DASHBOARD },
                        icon = { Icon(Icons.Default.Analytics, contentDescription = null) },
                        label = { Text("BI") }
                    )
                }
            },
            modifier = Modifier.testTag("adaptive_enterprise_ui_phone")
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                when (currentScreen) {
                    EnterpriseScreen.WORKSPACE -> EnterpriseChatWorkspaceScreen(onNavigateBack = onNavigateBack)
                    EnterpriseScreen.WORKFLOW_DESIGNER -> WorkflowDesignerScreen(onNavigateBack = onNavigateBack)
                    EnterpriseScreen.BI_DASHBOARD -> BusinessIntelligenceDashboard(onNavigateBack = onNavigateBack)
                    EnterpriseScreen.SECURITY_CENTER -> AIAgentSecurityDashboard(onNavigateBack = onNavigateBack)
                }
            }
        }
    }
}
