package com.global.sms.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.global.sms.security.AdvancedAppProtection
import com.global.sms.security.audit.BackupEncryptionStatus
import com.global.sms.security.audit.PrivacyAuditEngine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityDashboardScreen(
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val auditReport = remember { PrivacyAuditEngine(context).runAudit() }
    val threatReport = remember { AdvancedAppProtection(context).assessDeviceSecurity() }

    Scaffold(
        modifier = Modifier.testTag("security_dashboard_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "داشبورد مرکز امنیت و حریم خصوصی (Sprint 4)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("security_nav_back")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "بازگشت"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Security & Privacy Score Card
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (threatReport.securityScore >= 80) Color(0xFF1B5E20) else Color(0xFFB71C1C)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("card_security_score")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${threatReport.securityScore}٪",
                            fontWeight = FontWeight.Bold,
                            fontSize = 26.sp,
                            color = Color.White
                        )
                        Text(
                            text = "امتیاز امنیت",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (auditReport.privacyScore >= 80) Color(0xFF0D47A1) else Color(0xFFB71C1C)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("card_privacy_score")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${auditReport.privacyScore}٪",
                            fontWeight = FontWeight.Bold,
                            fontSize = 26.sp,
                            color = Color.White
                        )
                        Text(
                            text = "امتیاز حریم خصوصی",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            Text(
                text = "وضعیت ماژول‌های امنیتی و حریم خصوصی",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            // Status List Cards
            SecurityModuleRow(
                title = "حالت حریم خصوصی Zero-Knowledge",
                statusText = "پردازش ۱۰۰٪ محلی و بدون وابستگی به ابر",
                isOk = true,
                testTag = "sec_status_zero_knowledge"
            )

            SecurityModuleRow(
                title = "محافظت در برابر اسکرین‌شات و ضبط صفحه",
                statusText = if (threatReport.isScreenshotBlocked) "مسدود شده (FLAG_SECURE)" else "آزاد",
                isOk = threatReport.isScreenshotBlocked,
                testTag = "sec_status_screenshot"
            )

            SecurityModuleRow(
                title = "بررسی روت دستگاه (Root Detection)",
                statusText = if (threatReport.isRooted) "روت شده (ریسک بسیار بالا)" else "دستگاه امن و روت‌نشده",
                isOk = !threatReport.isRooted,
                testTag = "sec_status_root"
            )

            SecurityModuleRow(
                title = "شناسایی شبیه‌ساز (Emulator Detection)",
                statusText = if (threatReport.isEmulator) "محیط شبیه‌ساز" else "دستگاه واقعی و تایید شده",
                isOk = !threatReport.isEmulator,
                testTag = "sec_status_emulator"
            )

            SecurityModuleRow(
                title = "رمزنگاری گاوصندوق (AES-256-GCM)",
                statusText = if (auditReport.isVaultEncrypted) "فعال و سخت‌افزاری" else "غیرفعال",
                isOk = auditReport.isVaultEncrypted,
                testTag = "sec_status_vault"
            )

            val backupStatusLabel = when (auditReport.backupStatus) {
                BackupEncryptionStatus.ENCRYPTED -> "رمزنگاری شده (GSMS)"
                BackupEncryptionStatus.UNENCRYPTED -> "رمزنگاری نشده (ناامن)"
                BackupEncryptionStatus.NO_BACKUP_FOUND -> "هنوز فایل پشتیبان ایجاد نشده"
            }
            SecurityModuleRow(
                title = "وضعیت رمزنگاری فایل پشتیبان",
                statusText = backupStatusLabel,
                isOk = auditReport.backupStatus != BackupEncryptionStatus.UNENCRYPTED,
                testTag = "sec_status_backup"
            )

            val allThreats = threatReport.detectedThreats + auditReport.securityWarnings
            if (allThreats.isNotEmpty()) {
                Text(
                    text = "تهدیدها و هشدارهای امنیتی شناسایی شده",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.error
                )

                allThreats.distinct().forEach { threat ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = threat,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SecurityModuleRow(
    title: String,
    statusText: String,
    isOk: Boolean,
    testTag: String
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = if (isOk) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (isOk) Color(0xFF00C853) else Color(0xFFE53935)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(
                        text = statusText,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
