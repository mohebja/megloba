package com.global.sms.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.global.sms.core.ai.agent.CommunicationAgent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIAgentSecurityDashboard(
    suggestedActionsCount: Int = 0,
    userConfirmedActionsCount: Int = 0,
    blockedThreatsCount: Int = 0,
    onNavigateBack: () -> Unit = {}
) {
    val isKillSwitchActive by CommunicationAgent.isKillSwitchActive.collectAsState()

    Scaffold(
        modifier = Modifier.testTag("ai_agent_security_dashboard"),
        topBar = {
            TopAppBar(
                title = { Text("مرکز کنترل امنیت عامل هوش مصنوعی", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("ai_agent_security_back_button")
                    ) {
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
            // Kill Switch Card
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isKillSwitchActive) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "کلید توقف اضطراری (Kill Switch)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = if (isKillSwitchActive) Color(0xFFC62828) else Color(0xFF2E7D32)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isKillSwitchActive) "عامل هوش مصنوعی کاملاً متوقف شده است." else "عامل هوش مصنوعی در حال پایش و پیشنهاد خودکار است.",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                        Switch(
                            checked = isKillSwitchActive,
                            onCheckedChange = { CommunicationAgent.setKillSwitch(it) }
                        )
                    }
                }
            }

            // Privacy Score Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("شاخص حریم خصوصی: ۱۰۰٪ درون‌دستگاهی", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "تمام تحلیل‌ها، یادگیری سبک ارتباطی و قوانین گردش‌کار به صورت ۱۰۰٪ آفلاین و ذخیره رمزنگاری شده محلی انجام می‌شود.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // AI Statistics Summary
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SecurityStatBox(
                        title = "اقدامات پیشنهادی",
                        value = suggestedActionsCount.toString(),
                        color = Color(0xFF1976D2),
                        modifier = Modifier.weight(1f)
                    )
                    SecurityStatBox(
                        title = "تأیید کاربر",
                        value = userConfirmedActionsCount.toString(),
                        color = Color(0xFF388E3C),
                        modifier = Modifier.weight(1f)
                    )
                    SecurityStatBox(
                        title = "مسدود شده",
                        value = blockedThreatsCount.toString(),
                        color = Color(0xFFD32F2F),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // AI Permissions List
            item {
                Text("مجوزهای دسترسی عامل هوشمند", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            items(
                listOf(
                    PermissionItem("پایش پیام‌های ورودی", "برای پیشنهاد پاسخ و یادآوری", true),
                    PermissionItem("دسترسی به تقویم", "فقط با تأیید صریح کاربر برای رویدادها", true),
                    PermissionItem("ارسال پیام پاسخ", "نیازمند فشردن دکمه تأیید نهایی توسط کاربر", true),
                    PermissionItem("اتصال به اینترنت", "کاملاً غیرفعال (Zero Network Access)", false)
                )
            ) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(item.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(item.desc, fontSize = 12.sp, color = Color.Gray)
                        }
                        Icon(
                            imageVector = if (item.isGranted) Icons.Default.CheckCircle else Icons.Default.Cancel,
                            contentDescription = null,
                            tint = if (item.isGranted) Color(0xFF388E3C) else Color(0xFFD32F2F)
                        )
                    }
                }
            }
        }
    }
}

private data class PermissionItem(val name: String, val desc: String, val isGranted: Boolean)

@Composable
private fun SecurityStatBox(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = color)
            Spacer(modifier = Modifier.height(4.dp))
            Text(title, fontSize = 11.sp, color = color)
        }
    }
}
