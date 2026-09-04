package com.global.sms.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.global.sms.data.entity.AiSettingsEntity
import com.global.sms.ui.viewmodels.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantSettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val aiSettings by viewModel.aiSettingsState.collectAsStateWithLifecycle()
    var showClearDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تنظیمات دستیار هوشمند (AI Assistant)") },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Info Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "دستیار هوش مصنوعی حریم‌خصوصی‌محور",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "تمام تحلیل‌ها و پردازش‌ها به‌صورت ۱۰۰٪ محلی در دستگاه شما انجام می‌شود.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Toggles Group
            Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

                    AiSettingToggleRow(
                        title = "طبقه‌بندی هوشمند پیامک‌ها",
                        subtitle = "تشخیص خودکار تراکنش‌ها، کدهای ورود، مرسولات، تبلیغات و خدمات دولتی",
                        icon = Icons.Default.Category,
                        checked = aiSettings.aiClassificationEnabled,
                        onCheckedChange = { viewModel.updateAiSettings(aiSettings.copy(aiClassificationEnabled = it)) }
                    )

                    HorizontalDivider()

                    AiSettingToggleRow(
                        title = "پاسخ‌های هوشمند سریع (Smart Reply)",
                        subtitle = "پیشنهاد پاسخ‌های مرتبط فارسی و انگلیسی همراه با ایموجی در محیط گفتگو",
                        icon = Icons.Default.Quickreply,
                        checked = aiSettings.smartReplyEnabled,
                        onCheckedChange = { viewModel.updateAiSettings(aiSettings.copy(smartReplyEnabled = it)) }
                    )

                    HorizontalDivider()

                    AiSettingToggleRow(
                        title = "کشف هوشمند فیشینگ و لینک‌های مشکوک",
                        subtitle = "تحلیل امنیتی آدرس‌های وب، پیامک‌های کلاهبرداری و جعل بانک",
                        icon = Icons.Default.Security,
                        checked = aiSettings.fraudDetectionEnabled,
                        onCheckedChange = { viewModel.updateAiSettings(aiSettings.copy(fraudDetectionEnabled = it)) }
                    )

                    HorizontalDivider()

                    AiSettingToggleRow(
                        title = "خلاصه‌سازی هوشمند گفتگوها",
                        subtitle = "تولید خلاصه کوتاه از نکات کلیدی و تراکنش‌های گفتگوهای طولانی",
                        icon = Icons.Default.Summarize,
                        checked = aiSettings.summariesEnabled,
                        onCheckedChange = { viewModel.updateAiSettings(aiSettings.copy(summariesEnabled = it)) }
                    )

                    HorizontalDivider()

                    AiSettingToggleRow(
                        title = "دستیار صوتی و خوانش پیامک‌ها",
                        subtitle = "قرائت صوتی پیامک‌های دریافتی به زبان فارسی و انگلیسی",
                        icon = Icons.Default.RecordVoiceOver,
                        checked = aiSettings.voiceAssistantEnabled,
                        onCheckedChange = { viewModel.updateAiSettings(aiSettings.copy(voiceAssistantEnabled = it)) }
                    )

                    HorizontalDivider()

                    AiSettingToggleRow(
                        title = "پردازش کاملاً محلی (Local Privacy-First)",
                        subtitle = "عدم ارسال متن پیامک‌ها به سرورهای ابری خارجی",
                        icon = Icons.Default.PrivacyTip,
                        checked = aiSettings.localProcessingOnly,
                        onCheckedChange = { viewModel.updateAiSettings(aiSettings.copy(localProcessingOnly = it)) }
                    )
                }
            }

            // OTP Auto-Delete Option
            Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "پاک‌سازی خودکار کدهای یکبارمصرف (OTP)",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "مدت زمان نگهداری کدهای ورود پس از استفاده",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = aiSettings.autoDeleteOtpDays == 0,
                            onClick = { viewModel.updateAiSettings(aiSettings.copy(autoDeleteOtpDays = 0)) },
                            label = { Text("غیرفعال") }
                        )
                        FilterChip(
                            selected = aiSettings.autoDeleteOtpDays == 1,
                            onClick = { viewModel.updateAiSettings(aiSettings.copy(autoDeleteOtpDays = 1)) },
                            label = { Text("بعد از ۲۴ ساعت") }
                        )
                        FilterChip(
                            selected = aiSettings.autoDeleteOtpDays == 7,
                            onClick = { viewModel.updateAiSettings(aiSettings.copy(autoDeleteOtpDays = 7)) },
                            label = { Text("بعد از ۷ روز") }
                        )
                    }
                }
            }

            // Action: Clear AI Data
            OutlinedButton(
                onClick = { showClearDialog = true },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_clear_ai_data")
            ) {
                Icon(Icons.Default.DeleteForever, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("پاک‌سازی تاریخچه و داده‌های هوش مصنوعی")
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("پاک‌سازی داده‌های هوش مصنوعی") },
            text = { Text("آیا از حذف تمام متادیتاهای تحلیل شده، ترجیحات یادگیری و خلاصه گفتگوها اطمینان دارید؟ پیامک‌های اصلی شما حذف نخواهند شد.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAiData()
                        showClearDialog = false
                    }
                ) {
                    Text("حذف داده‌ها", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("انصراف")
                }
            }
        )
    }
}

@Composable
private fun AiSettingToggleRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
