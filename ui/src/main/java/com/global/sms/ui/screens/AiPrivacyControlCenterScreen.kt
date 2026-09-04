package com.global.sms.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.unit.sp
import com.global.sms.core.ai.privacy.AIPrivacyController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiPrivacyControlCenterScreen(
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val controller = remember { AIPrivacyController() }
    val settings by controller.settings.collectAsState()

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .testTag("ai_privacy_control_center_screen"),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "مرکز کنترل حریم خصوصی هوش مصنوعی",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.testTag("ai_privacy_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "بازگشت"
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
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "کنترل و انقضای داده‌های دانش AI",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "مدیریت زمان ماندگاری فاکت‌ها، حذف رمزهای پویا و بازنشانی کامل هوش مصنوعی",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "تنظیمات ماندگاری و انقضا",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "مدت زمان ماندگاری حافظه: ${settings.memoryRetentionDays} روز",
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Slider(
                                value = settings.memoryRetentionDays.toFloat(),
                                onValueChange = { controller.setRetentionPeriod(it.toInt()) },
                                valueRange = 7f..90f,
                                steps = 10
                            )
                        }
                    }
                }

                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "حذف خودکار کدهای OTP", fontWeight = FontWeight.Bold)
                                    Text(text = "عدم ذخیره پیامک‌های حاوی کد تایید در حافظه AI", style = MaterialTheme.typography.bodySmall)
                                }
                                Switch(
                                    checked = settings.autoRemoveOtpFacts,
                                    onCheckedChange = { controller.setAutoRemoveOtpFacts(it) },
                                    modifier = Modifier.testTag("switch_auto_remove_otp")
                                )
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "عدم ذخیره اطلاعات مالی حساس", fontWeight = FontWeight.Bold)
                                    Text(text = "حذف شماره کارت‌ها و مانده حساب از پردازش هوش مصنوعی", style = MaterialTheme.typography.bodySmall)
                                }
                                Switch(
                                    checked = settings.autoRemoveSensitiveFinancialData,
                                    onCheckedChange = { controller.setAutoRemoveSensitiveFinancialData(it) },
                                    modifier = Modifier.testTag("switch_auto_remove_financial")
                                )
                            }
                        }
                    }
                }

                item {
                    Button(
                        onClick = {
                            controller.resetAiLearningState()
                            Toast.makeText(context, "تمام پارامترهای یادگیری AI پاک‌سازی شد", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("reset_ai_learning_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(imageVector = Icons.Default.RestartAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("بازنشانی کامل دانش و حافظه هوش مصنوعی")
                    }
                }
            }
        }
    }
}
