package com.global.sms.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.global.sms.core.repository.AiHomeDashboardData
import com.global.sms.ui.viewmodels.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiHomeDashboardScreen(
    dashboardViewModel: DashboardViewModel,
    onNavigateBack: () -> Unit = {},
    onOpenAiChat: () -> Unit = {},
    onOpenThread: (Long) -> Unit = {},
    onOpenFinancial: () -> Unit = {},
    onOpenTasks: () -> Unit = {},
    onOpenSecurity: () -> Unit = {}
) {
    val state by dashboardViewModel.aiDashboardState.collectAsStateWithLifecycle()

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .testTag("ai_home_dashboard_screen"),
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "داشبورد هوشمند (AI Dashboard)",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.testTag("ai_dashboard_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "بازگشت"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = onOpenAiChat,
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = "دستیار AI"
                        )
                    },
                    text = { Text("گفتگو با هوش مصنوعی") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("open_ai_chat_fab")
                )
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 80.dp, top = 8.dp)
            ) {
                // Today's Communication Summary
                item {
                    TodaySummaryHeaderCard(data = state)
                }

                // AI Suggestions Row/Card
                item {
                    AiSuggestionsCard(suggestions = state.aiSuggestions)
                }

                // 1. Important Messages Card
                item {
                    DashboardSectionCard(
                        title = "پیام‌های مهم و سنجاق شده",
                        badge = "${state.importantMessages.size} پیام",
                        icon = Icons.Default.Star,
                        accentColor = Color(0xFF1E88E5),
                        testTag = "important_messages_card"
                    ) {
                        if (state.importantMessages.isEmpty()) {
                            Text(
                                text = "پیام مهم یا سنجاق‌شده‌ای وجود ندارد.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                state.importantMessages.forEach { msg ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                            .clickable { onOpenThread(msg.threadId) }
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.MarkAsUnread,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = msg.address,
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Text(
                                                text = msg.body,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. Pending Replies Card
                item {
                    DashboardSectionCard(
                        title = "پیام‌های نیازمند پاسخ",
                        badge = "${state.pendingRepliesCount} مورد",
                        icon = Icons.AutoMirrored.Filled.Reply,
                        accentColor = Color(0xFFFB8C00),
                        testTag = "pending_replies_card"
                    ) {
                        if (state.pendingReplyMessages.isEmpty()) {
                            Text(
                                text = "هیچ پیام پاسخ‌داده‌نشده‌ای ندارید.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                state.pendingReplyMessages.forEach { msg ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                            .clickable { onOpenThread(msg.threadId) }
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = msg.address,
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Text(
                                                text = msg.body,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Button(
                                            onClick = { onOpenThread(msg.threadId) },
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("پاسخ", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. Financial Alerts Card
                item {
                    DashboardSectionCard(
                        title = "هشدارها و پرداخت‌های مالی",
                        badge = "${state.upcomingPaymentsCount} هشدار",
                        icon = Icons.Default.AccountBalance,
                        accentColor = Color(0xFF43A047),
                        testTag = "financial_alerts_card",
                        onClickHeader = onOpenFinancial
                    ) {
                        if (state.financialAlerts.isEmpty()) {
                            Text(
                                text = "تراکنش یا پرداخت آتی ثبت نشده است.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                state.financialAlerts.forEach { txn ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = txn.bankName.ifEmpty { "تراکنش بانکی" },
                                            fontWeight = FontWeight.Medium,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = "${txn.amount} ریال",
                                            fontWeight = FontWeight.Bold,
                                            color = if (txn.transactionType == "EXPENSE") Color(0xFFE53935) else Color(0xFF43A047)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. Tasks Card
                item {
                    DashboardSectionCard(
                        title = "وظایف و یادآورهای استخراج شده",
                        badge = "${state.tasks.size} کار",
                        icon = Icons.Default.Task,
                        accentColor = Color(0xFF8E24AA),
                        testTag = "tasks_card",
                        onClickHeader = onOpenTasks
                    ) {
                        if (state.tasks.isEmpty()) {
                            Text(
                                text = "هیچ وظیفه فعال یا استخراج‌شده‌ای وجود ندارد.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                state.tasks.forEach { task ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircleOutline,
                                            contentDescription = null,
                                            tint = Color(0xFF8E24AA),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = task.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 5. Security Status Card
                item {
                    DashboardSectionCard(
                        title = "وضعیت امنیت و اسپم",
                        badge = "${state.blockedSpamCount} قوانین فعال",
                        icon = Icons.Default.Shield,
                        accentColor = Color(0xFFE53935),
                        testTag = "security_status_card",
                        onClickHeader = onOpenSecurity
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "پیامک‌های مشکوک مسدود شده:",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "${state.securityLogsCount} مورد شناسه اسپم",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE53935)
                                )
                            }
                            OutlinedButton(onClick = onOpenSecurity) {
                                Text("جزئیات امنیت")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TodaySummaryHeaderCard(data: AiHomeDashboardData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("today_summary_header_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Today,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "خلاصه ارتباطات امروز",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Text(
                text = "امروز ${data.bankMessagesTodayCount} پیام بانکی دریافت کردید.",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(
                    onClick = {},
                    label = { Text("${data.pendingRepliesCount} نیاز به پاسخ") },
                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.Reply, contentDescription = null) }
                )
                AssistChip(
                    onClick = {},
                    label = { Text("${data.upcomingPaymentsCount} پرداخت شناسایی شد") },
                    leadingIcon = { Icon(Icons.Default.CreditCard, contentDescription = null) }
                )
            }
        }
    }
}

@Composable
private fun AiSuggestionsCard(suggestions: List<String>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ai_suggestions_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = Color(0xFFFB8C00)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "پیشنهادات هوش مصنوعی",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall
                )
            }

            suggestions.forEach { suggestion ->
                Text(
                    text = "• $suggestion",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
private fun DashboardSectionCard(
    title: String,
    badge: String,
    icon: ImageVector,
    accentColor: Color,
    testTag: String,
    onClickHeader: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (onClickHeader != null) Modifier.clickable { onClickHeader() } else Modifier),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = accentColor.copy(alpha = 0.15f),
                        contentColor = accentColor,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = badge,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            content()
        }
    }
}
