package com.global.sms

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.global.sms.ui.screens.AiAssistantSettingsScreen
import com.global.sms.ui.screens.BankDashboardScreen
import com.global.sms.ui.screens.BulkSmsSafetyScreen
import com.global.sms.ui.screens.BusinessTemplateScreen
import com.global.sms.ui.screens.CategoryManagementScreen
import com.global.sms.ui.screens.ClassificationRuleEditorScreen
import com.global.sms.ui.screens.ColorCustomizationScreen
import com.global.sms.ui.screens.ContactPickerScreen
import com.global.sms.ui.screens.ConversationsScreen
import com.global.sms.ui.screens.CrmCustomerManagementScreen
import com.global.sms.ui.screens.Customer360Screen
import com.global.sms.ui.screens.EnterpriseAnalyticsScreen
import com.global.sms.ui.screens.EnterpriseBackupScreen
import com.global.sms.ui.screens.EnterpriseDashboardScreen
import com.global.sms.ui.screens.FontSettingsScreen
import com.global.sms.ui.screens.GroupManagementScreen
import com.global.sms.ui.screens.SecurityAuditLogScreen
import com.global.sms.ui.screens.WorkflowAutomationScreen
import com.global.sms.ui.viewmodels.ContactViewModel
import com.global.sms.ui.viewmodels.EnterpriseViewModel
import com.global.sms.ui.screens.MessageStatsScreen
import com.global.sms.ui.screens.MessageThreadScreen
import com.global.sms.ui.screens.MultiContactComposeScreen
import com.global.sms.ui.screens.PerformanceReportScreen
import com.global.sms.ui.screens.PrivateVaultScreen
import com.global.sms.ui.screens.ScheduledMessagesScreen
import com.global.sms.ui.screens.SearchScreen
import com.global.sms.ui.screens.SettingsScreen

import com.global.sms.ui.screens.SmsCenterSettingsScreen
import com.global.sms.ui.screens.SpamFolderScreen
import com.global.sms.ui.screens.SplashScreen
import com.global.sms.ui.theme.GlobalSmsTheme
import com.global.sms.ui.viewmodels.GlobalSmsViewModel
import com.global.sms.ui.viewmodels.SettingsViewModel

class MainActivity : FragmentActivity() {

    private val viewModel: GlobalSmsViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        viewModel.checkDefaultSmsApp()
        val readGranted = results[Manifest.permission.READ_SMS] == true ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
        if (readGranted) {
            viewModel.startHistoricalSmsImport()
        }
    }

    private val defaultSmsRoleLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.checkDefaultSmsApp()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED) {
            viewModel.startHistoricalSmsImport()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val isReadSmsGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED

        requestSmsPermissions()
        viewModel.checkDefaultSmsApp()

        if (isReadSmsGranted) {
            viewModel.startHistoricalSmsImport()
        }

        setContent {
            val settings by settingsViewModel.settingsState.collectAsStateWithLifecycle()
            val darkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
            val amoledMode by viewModel.isAmoledMode.collectAsStateWithLifecycle()
            val isRtl by viewModel.isRtlPersian.collectAsStateWithLifecycle()

            GlobalSmsTheme(
                darkTheme = settings.isDarkTheme || darkTheme,
                isAmoled = settings.isAmoledMode || amoledMode,
                isRtl = settings.isRtlPersian || isRtl
            ) {
                GlobalSmsAppNavHost(
                    viewModel = viewModel,
                    settingsViewModel = settingsViewModel,
                    onRequestDefaultSms = { requestDefaultSmsRole() }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkDefaultSmsApp()
    }

    fun requestDefaultSmsRole() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(android.app.role.RoleManager::class.java)
            if (roleManager != null && roleManager.isRoleAvailable(android.app.role.RoleManager.ROLE_SMS)) {
                val intent = roleManager.createRequestRoleIntent(android.app.role.RoleManager.ROLE_SMS)
                defaultSmsRoleLauncher.launch(intent)
            }
        } else {
            val intent = android.content.Intent(android.provider.Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
            intent.putExtra(android.provider.Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, packageName)
            defaultSmsRoleLauncher.launch(intent)
        }
    }

    private fun requestSmsPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.READ_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_PHONE_STATE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val missing = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missing.isNotEmpty()) {
            permissionLauncher.launch(missing.toTypedArray())
        }
    }
}

@Composable
fun GlobalSmsAppNavHost(
    viewModel: GlobalSmsViewModel,
    settingsViewModel: SettingsViewModel,
    onRequestDefaultSms: () -> Unit = {}
) {
    val navController = rememberNavController()
    val showDefaultSmsDialog by viewModel.showDefaultSmsDialog.collectAsStateWithLifecycle()

    com.global.sms.ui.components.DefaultSmsRoleDialog(
        showDialog = showDefaultSmsDialog,
        onConfirm = { onRequestDefaultSms() },
        onDismiss = { viewModel.showDefaultSmsDialog.value = false }
    )

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            val isDbReady by viewModel.isDbInitialized.collectAsStateWithLifecycle()
            SplashScreen(
                isReady = isDbReady,
                onSplashFinished = {
                    navController.navigate("conversations") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        composable("conversations") {
            val settings by settingsViewModel.settingsState.collectAsStateWithLifecycle()
            when (settings.conversationStyle.uppercase()) {
                "CLASSIC" -> {
                    com.global.sms.ui.classic.screens.ClassicConversationsScreen(
                        viewModel = viewModel,
                        onOpenThread = { threadId ->
                            viewModel.selectThread(threadId)
                            navController.navigate("thread/$threadId")
                        },
                        onOpenSearch = { navController.navigate("search") },
                        onOpenSettings = { navController.navigate("settings") },
                        onComposeNew = {
                            navController.navigate("multi_compose?numbers=")
                        }
                    )
                }
                "ENTERPRISE" -> {
                    val enterpriseViewModel: EnterpriseViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
                    EnterpriseDashboardScreen(
                        viewModel = enterpriseViewModel,
                        onBack = { navController.navigate("settings") },
                        onNavigateCrm = { navController.navigate("crm_customers") },
                        onNavigateTemplates = { navController.navigate("business_templates") },
                        onNavigateBulkSms = { navController.navigate("bulk_sms_safety") },
                        onNavigateAutomation = { navController.navigate("workflow_automation") },
                        onNavigateAnalytics = { navController.navigate("enterprise_analytics") },
                        onNavigateSecurityAudit = { navController.navigate("security_audit_logs") }
                    )
                }
                else -> {
                    com.global.sms.ui.adaptive.AdaptiveConversationLayout(
                        viewModel = viewModel,
                        onOpenThread = { threadId ->
                            viewModel.selectThread(threadId)
                            navController.navigate("thread/$threadId")
                        },
                        onOpenSearch = { navController.navigate("search") },
                        onOpenBankDashboard = { navController.navigate("bank") },
                        onOpenOtpCenter = { navController.navigate("otp_center") },
                        onOpenSettings = { navController.navigate("settings") },
                        onComposeNew = {
                            navController.navigate("multi_compose?numbers=")
                        }
                    )
                }
            }
        }

        composable("search") {
            SearchScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onOpenThread = { threadId ->
                    viewModel.selectThread(threadId)
                    navController.navigate("thread/$threadId")
                }
            )
        }


        composable(
            route = "thread/{threadId}",
            arguments = listOf(navArgument("threadId") { type = NavType.LongType })
        ) { backStackEntry ->
            val threadId = backStackEntry.arguments?.getLong("threadId") ?: 0L
            val settings by settingsViewModel.settingsState.collectAsStateWithLifecycle()
            if (settings.conversationStyle.uppercase() == "CLASSIC") {
                com.global.sms.ui.classic.screens.ClassicMessageThreadScreen(
                    viewModel = viewModel,
                    threadId = threadId,
                    onBack = { navController.popBackStack() }
                )
            } else {
                MessageThreadScreen(
                    viewModel = viewModel,
                    threadId = threadId,
                    onBack = { navController.popBackStack() }
                )
            }
        }

        composable("vault") {
            PrivateVaultScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("scheduled") {
            ScheduledMessagesScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("spam") {
            SpamFolderScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("stats") {
            MessageStatsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("bank") {
            BankDashboardScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("otp_center") {
            com.global.sms.ui.screens.OtpScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("performance") {
            PerformanceReportScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("settings") {
            val context = androidx.compose.ui.platform.LocalContext.current
            val securePrefs = androidx.compose.runtime.remember {
                com.global.sms.security.prefs.SecurePreferencesManager(context)
            }
            val appSettings by settingsViewModel.settingsState.collectAsStateWithLifecycle()
            val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
            val isAmoledMode by viewModel.isAmoledMode.collectAsStateWithLifecycle()
            val usePersianDigits by viewModel.usePersianDigits.collectAsStateWithLifecycle()
            val usePersianCalendar by viewModel.usePersianCalendar.collectAsStateWithLifecycle()

            var isScreenshotProtectionEnabled by androidx.compose.runtime.remember {
                androidx.compose.runtime.mutableStateOf(securePrefs.isScreenshotProtectionEnabled)
            }
            var isSecureClipboardEnabled by androidx.compose.runtime.remember {
                androidx.compose.runtime.mutableStateOf(securePrefs.isSecureClipboardEnabled)
            }
            var isLinkSecurityEnabled by androidx.compose.runtime.remember {
                androidx.compose.runtime.mutableStateOf(securePrefs.isLinkSecurityEnabled)
            }
            var isUssdProtectionEnabled by androidx.compose.runtime.remember {
                androidx.compose.runtime.mutableStateOf(securePrefs.isUssdProtectionEnabled)
            }

            val activity = androidx.activity.compose.LocalActivity.current

            SettingsScreen(
                conversationStyle = appSettings.conversationStyle,
                onConversationStyleChange = { settingsViewModel.updateConversationStyle(it) },
                messageFontScale = appSettings.messageFontScale,
                onMessageFontScaleChange = { settingsViewModel.updateMessageFontScale(it) },
                isDarkTheme = isDarkTheme,
                onDarkThemeChange = { viewModel.isDarkTheme.value = it },
                isAmoledMode = isAmoledMode,
                onAmoledModeChange = { viewModel.isAmoledMode.value = it },
                usePersianDigits = usePersianDigits,
                onPersianDigitsChange = { viewModel.usePersianDigits.value = it },
                usePersianCalendar = usePersianCalendar,
                onPersianCalendarChange = { viewModel.usePersianCalendar.value = it },
                isPrivateNotificationMode = viewModel.appLockManager.isPrivateNotificationMode,
                onPrivateNotificationModeChange = { viewModel.appLockManager.isPrivateNotificationMode = it },
                isBiometricEnabled = viewModel.appLockManager.isBiometricEnabled,
                onBiometricEnabledChange = { viewModel.appLockManager.isBiometricEnabled = it },
                isScreenshotProtectionEnabled = isScreenshotProtectionEnabled,
                onScreenshotProtectionChange = { enabled ->
                    isScreenshotProtectionEnabled = enabled
                    securePrefs.isScreenshotProtectionEnabled = enabled
                    if (activity != null) {
                        com.global.sms.security.display.ScreenshotProtectionManager.setProtection(activity, enabled)
                    }
                },
                isSecureClipboardEnabled = isSecureClipboardEnabled,
                onSecureClipboardChange = { enabled ->
                    isSecureClipboardEnabled = enabled
                    securePrefs.isSecureClipboardEnabled = enabled
                },
                isLinkSecurityEnabled = isLinkSecurityEnabled,
                onLinkSecurityChange = { enabled ->
                    isLinkSecurityEnabled = enabled
                    securePrefs.isLinkSecurityEnabled = enabled
                },
                isUssdProtectionEnabled = isUssdProtectionEnabled,
                onUssdProtectionChange = { enabled ->
                    isUssdProtectionEnabled = enabled
                    securePrefs.isUssdProtectionEnabled = enabled
                },
                onExportBackup = { pwd, callback -> viewModel.exportBackup(pwd, callback) },
                onBack = { navController.popBackStack() },
                onNavigateToCategories = { navController.navigate("categories") },
                onNavigateToClassificationRules = { navController.navigate("classification_rules") },
                onNavigateToFontSettings = { navController.navigate("font_settings") },
                onNavigateToColorCustomization = { navController.navigate("color_customization") },
                onNavigateToGroupManagement = { navController.navigate("groups") },
                onNavigateToAiSettings = { navController.navigate("ai_settings") },
                onNavigateToEnterpriseDashboard = { navController.navigate("enterprise_dashboard") },
                onNavigateToSmsCenterSettings = { navController.navigate("smsc_settings") },
                onNavigateToEnterpriseBackup = { navController.navigate("enterprise_backup") },
                onNavigateToDiagnostics = { navController.navigate("db_diagnostics") },
                onNavigateToReliability = { navController.navigate("reliability") },
                onReimportSms = { viewModel.startHistoricalSmsImport(force = true) }
            )
        }

        composable("ai_settings") {
            AiAssistantSettingsScreen(
                viewModel = settingsViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("enterprise_dashboard") {
            val enterpriseViewModel: EnterpriseViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
            EnterpriseDashboardScreen(
                viewModel = enterpriseViewModel,
                onBack = { navController.popBackStack() },
                onNavigateCrm = { navController.navigate("crm_customers") },
                onNavigateTemplates = { navController.navigate("business_templates") },
                onNavigateBulkSms = { navController.navigate("bulk_sms_safety") },
                onNavigateAutomation = { navController.navigate("workflow_automation") },
                onNavigateAnalytics = { navController.navigate("enterprise_analytics") },
                onNavigateSecurityAudit = { navController.navigate("security_audit_logs") }
            )
        }

        composable("crm_customers") {
            val enterpriseViewModel: EnterpriseViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
            CrmCustomerManagementScreen(
                viewModel = enterpriseViewModel,
                onBack = { navController.popBackStack() },
                onSendMessageToCustomer = { phone ->
                    val newThreadId = System.currentTimeMillis()
                    viewModel.selectThread(newThreadId)
                    navController.navigate("thread/$newThreadId")
                },
                onOpenCustomer360 = { customerId ->
                    navController.navigate("customer_360?customerId=$customerId")
                }
            )
        }

        composable("business_templates") {
            val enterpriseViewModel: EnterpriseViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
            BusinessTemplateScreen(
                viewModel = enterpriseViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("bulk_sms_safety") {
            val enterpriseViewModel: EnterpriseViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
            BulkSmsSafetyScreen(
                viewModel = enterpriseViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("workflow_automation") {
            val enterpriseViewModel: EnterpriseViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
            WorkflowAutomationScreen(
                viewModel = enterpriseViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("enterprise_analytics") {
            val enterpriseViewModel: EnterpriseViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
            EnterpriseAnalyticsScreen(
                viewModel = enterpriseViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("security_audit_logs") {
            val enterpriseViewModel: EnterpriseViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
            SecurityAuditLogScreen(
                viewModel = enterpriseViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "customer_360?customerId={customerId}",
            arguments = listOf(navArgument("customerId") {
                type = NavType.LongType
                defaultValue = 0L
            })
        ) { backStackEntry ->
            val enterpriseViewModel: EnterpriseViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
            val customerId = backStackEntry.arguments?.getLong("customerId") ?: 0L
            Customer360Screen(
                customerId = customerId,
                viewModel = enterpriseViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }


        composable("groups") {
            GroupManagementScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onOpenMultiCompose = { numbers ->
                    val query = numbers.joinToString(",")
                    navController.navigate("multi_compose?numbers=$query")
                }
            )
        }

        composable(
            route = "multi_compose?numbers={numbers}",
            arguments = listOf(navArgument("numbers") {
                type = NavType.StringType
                defaultValue = ""
            })
        ) { backStackEntry ->
            val numbersArg = backStackEntry.arguments?.getString("numbers") ?: ""
            val initialList = if (numbersArg.isNotBlank()) numbersArg.split(",") else emptyList()
            MultiContactComposeScreen(
                viewModel = viewModel,
                initialNumbers = initialList,
                onOpenContactPicker = { navController.navigate("contact_picker") },
                onBack = { navController.popBackStack() },
                onSent = { navController.popBackStack() }
            )
        }

        composable("contact_picker") {
            val contactViewModel: ContactViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
            ContactPickerScreen(
                contactViewModel = contactViewModel,
                globalViewModel = viewModel,
                onBack = { navController.popBackStack() },
                onContactsSelected = { selected ->
                    navController.popBackStack()
                }
            )
        }

        composable("categories") {
            CategoryManagementScreen(
                viewModel = settingsViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("classification_rules") {
            ClassificationRuleEditorScreen(
                viewModel = settingsViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("font_settings") {
            FontSettingsScreen(
                viewModel = settingsViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("color_customization") {
            ColorCustomizationScreen(
                viewModel = settingsViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("smsc_settings") {
            SmsCenterSettingsScreen(
                viewModel = settingsViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("theme_customizer") {
            com.global.sms.ui.screens.ThemeCustomizerScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("reliability") {
            com.global.sms.ui.screens.ReliabilityDashboardScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("enterprise_backup") {
            EnterpriseBackupScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable("db_diagnostics") {
            com.global.sms.ui.screens.DatabaseDiagnosticsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
