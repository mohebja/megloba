package com.global.sms.ui.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.global.sms.ui.components.GlobalSmsTopBar
import com.global.sms.ui.components.OfflineNoticeBanner
import com.global.sms.ui.theme.StatusError
import com.global.sms.ui.theme.StatusErrorContainer
import com.global.sms.ui.theme.StatusErrorDarkContainer
import com.global.sms.ui.theme.StatusInfo
import com.global.sms.ui.theme.StatusSuccess
import com.global.sms.ui.theme.StatusSuccessContainer
import com.global.sms.ui.theme.StatusSuccessDarkContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIAgentSecurityDashboard(
    suggestedActionsCount: Int = 0,
    userConfirmedActionsCount: Int = 0,
    blockedThreatsCount: Int = 0,
    onNavigateBack: () -> Unit = {}
) {
    val isKillSwitchActive by CommunicationAgent.isKillSwitchActive.collectAsState()
    val isDark = isSystemInDarkTheme()

    val killSwitchCardBg = if (isKillSwitchActive) {
        if (isDark) StatusErrorDarkContainer else StatusErrorContainer
    } else {
        if (isDark) StatusSuccessDarkContainer else StatusSuccessContainer
    }

    val killSwitchTextColor = if (isKillSwitchActive) StatusError else StatusSuccess

    Scaffold(
        modifier = Modifier.testTag("ai_agent_security_dashboard"),
        topBar = {
            GlobalSmsTopBar(
                title = "مرکز کنترل امنیت عامل هوش مصنوعی",
                subtitle = "سیاست‌های ایمنی و حریم خصوصی ۱۰۰٪ آفلاین",
                onNavigationClick = onNavigateBack
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
                    colors = CardDefaults.cardColors(containerColor = killSwitchCardBg),
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
                                color = killSwitchTextColor
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isKillSwitchActive) "عامل هوش مصنوعی کاملاً متوقف شده است." else "عامل هوش مصنوعی در حال پایش و پیشنهاد خودکار است.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isKillSwitchActive,
                            onCheckedChange = { CommunicationAgent.setKillSwitch(it) },
                            modifier = Modifier.testTag("ai_kill_switch")
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

            item {
                OfflineNoticeBanner(message = "بدون نیاز به اینترنت و بدون ارسال کوچک‌ترین داده به خارج از دستگاه.")
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
                        color = StatusInfo,
                        modifier = Modifier.weight(1f)
                    )
                    SecurityStatBox(
                        title = "تأیید کاربر",
                        value = userConfirmedActionsCount.toString(),
                        color = StatusSuccess,
                        modifier = Modifier.weight(1f)
                    )
                    SecurityStatBox(
                        title = "مسدود شده",
                        value = blockedThreatsCount.toString(),
                        color = StatusError,
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
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
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
                            Text(item.desc, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(
                            imageVector = if (item.isGranted) Icons.Default.CheckCircle else Icons.Default.Cancel,
                            contentDescription = null,
                            tint = if (item.isGranted) StatusSuccess else StatusError
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
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f)),
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
