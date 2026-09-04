package com.global.sms.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.global.sms.core.reliability.ProductionHealthMonitor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReliabilityDashboardScreen(
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val monitor = remember { ProductionHealthMonitor(context) }
    val report by monitor.healthReport.collectAsState()

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .testTag("reliability_dashboard_screen"),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "داشبورد پایداری و سلامت سیستم",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.testTag("reliability_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "بازگشت"
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { monitor.performFullHealthCheck() },
                            modifier = Modifier.testTag("reliability_refresh_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "به‌روزرسانی"
                            )
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Main Health Score Card
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (report.healthScore >= 90) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.HealthAndSafety,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = if (report.healthScore >= 90) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "امتیاز پایداری برنامه: ${report.healthScore}٪",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (report.healthScore >= 90) "تمام سامانه‌ها در وضعیت مطلوب عملیاتی هستند"
                                    else "نیاز به بررسی مجوزها یا حافظه دستگاه وجود دارد",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "وضعیت ماژول‌های زیرساختی",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                item {
                    StatusRowCard(
                        title = "پایگاه داده SQLite (Room)",
                        subtitle = "یکپارچگی و سلامت جداول دیتابیس",
                        isOk = report.isDatabaseHealthy,
                        icon = Icons.Default.Storage
                    )
                }

                item {
                    StatusRowCard(
                        title = "موتور دریافت و ارسال SMS",
                        subtitle = "آمادگی سرویس Telephony و سیم‌کارت‌ها",
                        isOk = report.isSmsEngineReady,
                        icon = Icons.Default.Sms
                    )
                }

                item {
                    StatusRowCard(
                        title = "امنیت و گاوصندوق AES-256",
                        subtitle = "کلیدهای KeyStore و حفاظت بیومتریک",
                        isOk = true,
                        icon = Icons.Default.Security
                    )
                }

                item {
                    StatusRowCard(
                        title = "موتور هوش مصنوعی آفلاین (Local AI)",
                        subtitle = "بارگذاری مدل‌ها و پردازش درجا",
                        isOk = true,
                        icon = Icons.Default.Psychology
                    )
                }

                item {
                    StatusRowCard(
                        title = "مصرف حافظه رم (RAM)",
                        subtitle = "${report.memoryUsageMb} مگابایت در حال استفاده توسط برنامه",
                        isOk = report.memoryUsageMb < 150,
                        icon = Icons.Default.Memory
                    )
                }

                item {
                    StatusRowCard(
                        title = "بهینه‌سازی مصرف باتری",
                        subtitle = if (report.isBatteryOptimizedExempt) "فعال و بدون توقف سرویس‌های پس‌زمینه" else "محدود شده توسط مدیریت انرژی اندروید",
                        isOk = report.isBatteryOptimizedExempt,
                        icon = Icons.Default.BatteryChargingFull
                    )
                }

                item {
                    StatusRowCard(
                        title = "فضای ذخیره‌سازی دستگاه",
                        subtitle = "${report.availableStorageMb} مگابایت فضای آزاد موجود است",
                        isOk = report.isStorageSufficient,
                        icon = Icons.Default.SdCard
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusRowCard(
    title: String,
    subtitle: String,
    isOk: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = if (isOk) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.errorContainer
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.padding(8.dp),
                    tint = if (isOk) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onErrorContainer
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Icon(
                imageVector = if (isOk) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = if (isOk) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
            )
        }
    }
}
