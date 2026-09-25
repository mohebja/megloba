package com.global.sms.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.global.sms.core.autoresponder.AutoResponderMode
import com.global.sms.core.autoresponder.SmartAutoResponderManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoResponderSettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentConfig by SmartAutoResponderManager.config.collectAsState()

    var isEnabled by remember(currentConfig) { mutableStateOf(currentConfig.isEnabled) }
    var selectedMode by remember(currentConfig) { mutableStateOf(currentConfig.mode) }
    var customMessage by remember(currentConfig) { mutableStateOf(currentConfig.customMessage) }
    var applyOnlyToContacts by remember(currentConfig) { mutableStateOf(currentConfig.applyOnlyToContacts) }

    LaunchedEffect(Unit) {
        SmartAutoResponderManager.init(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("پاسخگوی خودکار هوشمند (Auto-Responder)") },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("btn_back_auto_responder")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                }
            )
        },
        modifier = modifier.testTag("auto_responder_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Main Switch Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isEnabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text(
                            text = "فعال‌سازی پاسخگوی خودکار",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = if (isEnabled) "پاسخگوی خودکار فعال است و در شرایط معین به پیامک‌ها پاسخ می‌دهد." else "پاسخگوی خودکار غیرفعال است.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { isEnabled = it },
                        modifier = Modifier.testTag("switch_auto_responder")
                    )
                }
            }

            // Mode Selection
            Text(
                text = "انتخاب حالت پیش‌فرض",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AutoResponderMode.values().forEach { mode ->
                    FilterChip(
                        selected = selectedMode == mode,
                        onClick = { selectedMode = mode },
                        label = { Text(mode.titleFa, fontSize = 11.sp) },
                        leadingIcon = {
                            val icon = when (mode) {
                                AutoResponderMode.DRIVING -> Icons.Default.DirectionsCar
                                AutoResponderMode.MEETING -> Icons.Default.MeetingRoom
                                AutoResponderMode.OUT_OF_OFFICE -> Icons.Default.Schedule
                                AutoResponderMode.CUSTOM -> Icons.Default.Settings
                            }
                            Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                    )
                }
            }

            // Message Preview & Edit
            Card(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "متن پیام پاسخ:",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val defaultText = selectedMode.defaultMessageFa
                    Text(
                        text = "متن پیش‌فرض سیستم: $defaultText",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = customMessage,
                        onValueChange = { customMessage = it },
                        label = { Text("متن سفارشی (اختیاری)") },
                        placeholder = { Text(defaultText) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_custom_auto_reply_msg"),
                        maxLines = 4
                    )
                }
            }

            // Rule & Filter Card
            Card(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text("ارسال تنها برای مخاطبین ذخیره‌شده", fontWeight = FontWeight.Bold)
                        Text(
                            "از ارسال پاسخ به شماره‌های ناشناس، تبلیغاتی و بانک‌ها خودداری می‌شود.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    Switch(
                        checked = applyOnlyToContacts,
                        onCheckedChange = { applyOnlyToContacts = it },
                        modifier = Modifier.testTag("switch_only_contacts")
                    )
                }
            }

            // Stats Card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "تعداد پیام‌های پاسخ‌داده‌شده خودکار: ${currentConfig.totalAutoRepliesSent}",
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    )
                }
            }

            // Save Button
            Button(
                onClick = {
                    val updated = currentConfig.copy(
                        isEnabled = isEnabled,
                        mode = selectedMode,
                        customMessage = customMessage,
                        applyOnlyToContacts = applyOnlyToContacts
                    )
                    SmartAutoResponderManager.updateConfig(context, updated)
                    Toast.makeText(context, "تنظیمات پاسخگوی خودکار ذخیره شد", Toast.LENGTH_SHORT).show()
                    onNavigateBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_save_auto_responder")
            ) {
                Text("ذخیره تنظیمات")
            }
        }
    }
}
