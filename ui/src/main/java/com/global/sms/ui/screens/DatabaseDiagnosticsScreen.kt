package com.global.sms.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.global.sms.data.entity.SmsImportLogEntity
import com.global.sms.ui.viewmodels.GlobalSmsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatabaseDiagnosticsScreen(
    viewModel: GlobalSmsViewModel,
    onBack: () -> Unit
) {
    val totalSmsCount by viewModel.totalImportedSmsCount.collectAsStateWithLifecycle()
    val totalConversations by viewModel.totalConversationsCount.collectAsStateWithLifecycle()
    val importLogs by viewModel.importLogs.collectAsStateWithLifecycle()
    val isImporting by viewModel.isImportingSms.collectAsStateWithLifecycle()
    val importProgress by viewModel.smsImportProgress.collectAsStateWithLifecycle()
    val importStatusText by viewModel.smsImportStatusText.collectAsStateWithLifecycle()

    val latestLog = importLogs.firstOrNull()
    val totalDuplicatesSkipped = importLogs.sumOf { it.skippedDuplicatesCount }
    val totalFailedImports = importLogs.sumOf { it.failedCount }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("عیب‌یابی دیتابیس و همگام‌سازی", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Database Statistics Overview
            item {
                Text(
                    text = "آمار و وضعیت کلی پایگاه داده",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DiagnosticStatCard(
                        title = "کل پیامک‌ها",
                        value = totalSmsCount.toString(),
                        icon = Icons.AutoMirrored.Filled.Message,
                        color = Color(0xFF1976D2),
                        modifier = Modifier.weight(1f)
                    )
                    DiagnosticStatCard(
                        title = "تعداد گفتگوها",
                        value = totalConversations.toString(),
                        icon = Icons.Default.QuestionAnswer,
                        color = Color(0xFF388E3C),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DiagnosticStatCard(
                        title = "تکراری‌های ردشده",
                        value = totalDuplicatesSkipped.toString(),
                        icon = Icons.Default.Sync,
                        color = Color(0xFFF57C00),
                        modifier = Modifier.weight(1f)
                    )
                    DiagnosticStatCard(
                        title = "خطاهای واردات",
                        value = totalFailedImports.toString(),
                        icon = Icons.Default.Warning,
                        color = Color(0xFFD32F2F),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Section 2: Manual Re-import Action Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "بازخوانی و همگام‌سازی دستی پیامک‌ها (Re-import SMS)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "اسکن مجدد تمام پیامک‌های سیستم، یکپارچه‌سازی گفتگوها و حذف تکراری‌ها",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        AnimatedVisibility(visible = isImporting) {
                            Column(modifier = Modifier.padding(bottom = 12.dp)) {
                                LinearProgressIndicator(
                                    progress = { importProgress },
                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = importStatusText,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        Button(
                            onClick = { viewModel.startHistoricalSmsImport(force = true) },
                            enabled = !isImporting,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reimport_sms_button")
                        ) {
                            Icon(Icons.Default.Sync, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isImporting) "در حال همگام‌سازی..." else "شروع بازخوانی مجدد پیامک‌ها")
                        }
                    }
                }
            }

            // Section 3: Import History Logs Title
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "تاریخچه همگام‌سازی‌های اخیر (Import History)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (importLogs.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier.padding(24.dp).fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "هنوز سابقه همگام‌سازی ثبت نشده است. با زدن دکمه بالا اولین بازخوانی را انجام دهید.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(importLogs, key = { it.id }) { log ->
                    ImportLogItemCard(log = log)
                }
            }
        }
    }
}

@Composable
fun DiagnosticStatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ImportLogItemCard(log: SmsImportLogEntity) {
    val dateFormat = SimpleDateFormat("yyyy/MM/dd - HH:mm:ss", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(log.timestamp))

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formattedDate,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                val (statusColor, statusLabel, statusIcon) = when (log.status) {
                    "SUCCESS" -> Triple(Color(0xFF2E7D32), "موفق", Icons.Default.CheckCircle)
                    "PARTIAL" -> Triple(Color(0xFFEF6C00), "بخشی", Icons.Default.Warning)
                    else -> Triple(Color(0xFFC62828), "ناموفق", Icons.Default.Error)
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(statusIcon, contentDescription = null, tint = statusColor, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = statusLabel, fontSize = 11.sp, color = statusColor, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "کل پیامک‌های پیدا شده: ${log.totalSystemSms}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "جدید اضافه شده: ${log.newlyImportedCount}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "تکراری ردشده: ${log.skippedDuplicatesCount}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "زمان اجرا: ${log.durationMs} میلی‌ثانیه", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
