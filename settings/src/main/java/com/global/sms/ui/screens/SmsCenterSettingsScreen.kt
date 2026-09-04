package com.global.sms.ui.screens

import com.global.sms.ui.viewmodels.SettingsViewModel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.global.sms.core.sim.DualSimManager
import com.global.sms.core.sim.SimCardInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsCenterSettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val settings by viewModel.settingsState.collectAsStateWithLifecycle()

    var sim1Smsc by remember(settings) { mutableStateOf(settings.sim1SmscAddress) }
    var sim2Smsc by remember(settings) { mutableStateOf(settings.sim2SmscAddress) }
    var autoDetect by remember(settings) { mutableStateOf(settings.autoDetectSmsc) }

    var sim1Error by remember { mutableStateOf<String?>(null) }
    var sim2Error by remember { mutableStateOf<String?>(null) }
    var showSuccessSnackbar by remember { mutableStateOf(false) }

    val activeSims: List<SimCardInfo> = remember { DualSimManager.getActiveSimCards(context) }

    fun validateSmsc(number: String): Boolean {
        if (number.isBlank()) return false
        val clean = number.trim()
        return clean.startsWith("+") && clean.drop(1).all { it.isDigit() } && clean.length in 10..16
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "تنظیمات مرکز خدمات پیامک (SMSC)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Info Banner complying with Android and Google Play Policies
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "راهنمای شماره مرکز پیامک (SMS Center)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "شماره SMSC توسط اپراتور سیم‌کارت (همراه اول، ایرانسل، رایتل) برای ارسال پیامک استفاده می‌شود. برای اکثر کاربران شناسایی خودکار پیشنهادی است.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                // Active SIM Info Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "سیم‌کارت‌های فعال در دستگاه",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        if (activeSims.isEmpty()) {
                            Text(
                                "هیچ سیم‌کارت فعالی شناسایی نشد یا دسترسی سیم‌کارت غیرفعال است (از مقادیر پیش‌فرض استفاده می‌شود).",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            activeSims.forEach { sim ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.SimCard,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "سیم‌کارت ${sim.slotIndex + 1}: ${sim.carrierName} (${sim.displayName})",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }

                // Auto-Detect Toggle
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "شناسایی خودکار مرکز پیامک",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                "دریافت خودکار تنظیمات از شبکه اپراتور",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = autoDetect,
                            onCheckedChange = { autoDetect = it }
                        )
                    }
                }

                // Auto-Detect Button
                OutlinedButton(
                    onClick = {
                        val detected = viewModel.autoDetectSmscForSims(context)
                        sim1Smsc = detected.first
                        sim2Smsc = detected.second
                        sim1Error = null
                        sim2Error = null
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Icon(Icons.Default.Autorenew, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("شناسایی مجدد خودکار مرکز پیامک")
                }

                // SIM 1 SMSC Configuration
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "شماره مرکز پیامک سیم‌کارت اول (SIM 1)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        OutlinedTextField(
                            value = sim1Smsc,
                            onValueChange = {
                                sim1Smsc = it
                                sim1Error = null
                            },
                            label = { Text("مرکز پیامک SIM 1 (مثال: 9891100500+)") },
                            isError = sim1Error != null,
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("sim1_smsc_input")
                        )
                        sim1Error?.let {
                            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        }
                        Text(
                            "پیش‌فرض همراه اول: +9891100500 | ایرانسل: +989350000000",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                // SIM 2 SMSC Configuration
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "شماره مرکز پیامک سیم‌کارت دوم (SIM 2)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        OutlinedTextField(
                            value = sim2Smsc,
                            onValueChange = {
                                sim2Smsc = it
                                sim2Error = null
                            },
                            label = { Text("مرکز پیامک SIM 2") },
                            isError = sim2Error != null,
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("sim2_smsc_input")
                        )
                        sim2Error?.let {
                            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                // Save Button with Validation
                Button(
                    onClick = {
                        var valid = true
                        if (!validateSmsc(sim1Smsc)) {
                            sim1Error = "شماره مرکز پیامک نا معتبر است (باید با + شروع شده و شامل ۱۰ تا ۱۵ رقم باشد)."
                            valid = false
                        }
                        if (!validateSmsc(sim2Smsc)) {
                            sim2Error = "شماره مرکز پیامک نا معتبر است."
                            valid = false
                        }

                        if (valid) {
                            viewModel.updateSmscSettings(
                                sim1Smsc = sim1Smsc.trim(),
                                sim2Smsc = sim2Smsc.trim(),
                                autoDetect = autoDetect
                            )
                            showSuccessSnackbar = true
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_smsc_button")
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("اعتبارپنجی و ذخیره تنظیمات")
                }

                if (showSuccessSnackbar) {
                    Snackbar(
                        dismissAction = { showSuccessSnackbar = false }
                    ) {
                        Text("تنظیمات مرکز پیامک با موفقیت ذخیره شد.")
                    }
                }
            }
        }
    }
}
