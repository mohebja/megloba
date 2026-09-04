package com.global.sms.ui.smart.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.global.sms.data.entity.MessageCategory
import com.global.sms.ui.smart.components.AiSummaryCard
import com.global.sms.ui.smart.components.SmartCategoryChipRow
import com.global.sms.ui.viewmodels.GlobalSmsViewModel

import androidx.compose.material.icons.filled.Key

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartConversationsScreen(
    viewModel: GlobalSmsViewModel,
    onOpenThread: (Long) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenBankDashboard: () -> Unit,
    onOpenOtpCenter: () -> Unit,
    onOpenSettings: () -> Unit,
    onComposeNew: () -> Unit
) {
    val conversations by viewModel.conversations.collectAsStateWithLifecycle()
    val isDefaultSmsApp by viewModel.isDefaultSmsApp.collectAsStateWithLifecycle()
    val isImportingSms by viewModel.isImportingSms.collectAsStateWithLifecycle()
    val smsImportProgress by viewModel.smsImportProgress.collectAsStateWithLifecycle()
    val smsImportStatusText by viewModel.smsImportStatusText.collectAsStateWithLifecycle()
    var selectedCategory by remember { mutableStateOf<MessageCategory?>(null) }
    var showAiSummary by remember { mutableStateOf(true) }

    val filteredConversations = remember(conversations, selectedCategory) {
        if (selectedCategory == null) conversations
        else conversations.filter { it.category == selectedCategory }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            modifier = Modifier.testTag("smart_conversations_screen"),
            topBar = {
                TopAppBar(
                    modifier = Modifier.testTag("smart_top_app_bar"),
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("پیام‌رسان هوشمند (Smart AI)")
                        }
                    },
                    actions = {
                        IconButton(onClick = onOpenOtpCenter, modifier = Modifier.testTag("smart_otp_button")) {
                            Icon(Icons.Default.Key, contentDescription = "مرکز OTP")
                        }
                        IconButton(onClick = onOpenBankDashboard, modifier = Modifier.testTag("smart_bank_button")) {
                            Icon(Icons.Default.AccountBalance, contentDescription = "داشبورد بانکی")
                        }
                        IconButton(onClick = onOpenSearch, modifier = Modifier.testTag("smart_search_button")) {
                            Icon(Icons.Default.Search, contentDescription = "جستجوی هوشمند")
                        }
                        IconButton(onClick = onOpenSettings, modifier = Modifier.testTag("smart_settings_button")) {
                            Icon(Icons.Default.Settings, contentDescription = "تنظیمات")
                        }
                    }
                )
            },

            floatingActionButton = {
                FloatingActionButton(
                    onClick = onComposeNew,
                    containerColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.testTag("smart_compose_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "پیام جدید")
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Category Tabs
                SmartCategoryChipRow(
                    selectedCategory = selectedCategory,
                    onSelectCategory = { selectedCategory = it }
                )

                com.global.sms.ui.components.SmsImportProgressBanner(
                    isImporting = isImportingSms,
                    progress = smsImportProgress,
                    statusText = smsImportStatusText
                )

                val summaryViewModel: com.global.sms.ui.viewmodels.ConversationSummaryViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
                val dynamicSummary by summaryViewModel.overallSummary.collectAsStateWithLifecycle()

                if (showAiSummary && selectedCategory == null && filteredConversations.isNotEmpty()) {
                    AiSummaryCard(
                        summaryText = dynamicSummary,
                        onDismiss = { showAiSummary = false }
                    )
                }

                if (filteredConversations.isEmpty()) {
                    com.global.sms.ui.components.EmptySmsImportPrompt(
                        onImportClick = { viewModel.startHistoricalSmsImport(force = true) },
                        onRequestDefaultSms = { viewModel.showDefaultSmsDialog.value = true },
                        isDefaultSms = isDefaultSmsApp
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("smart_conversations_list")
                    ) {
                        items(
                            items = filteredConversations,
                            key = { it.threadId }
                        ) { item ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                                    .testTag("smart_conversation_item_${item.threadId}")
                                    .clickable { onOpenThread(item.threadId) },
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = when (item.category) {
                                            MessageCategory.OTP -> MaterialTheme.colorScheme.tertiaryContainer
                                            MessageCategory.BANK -> MaterialTheme.colorScheme.primaryContainer
                                            MessageCategory.SPAM -> MaterialTheme.colorScheme.errorContainer
                                            else -> MaterialTheme.colorScheme.surfaceContainerHigh
                                        },
                                        modifier = Modifier.size(44.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = (item.contactName ?: item.address).take(1),
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = item.contactName ?: item.address,
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                            AssistChip(
                                                onClick = {},
                                                label = { Text(item.category.name, fontSize = 10.sp) },
                                                modifier = Modifier.height(24.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(2.dp))

                                        Text(
                                            text = item.lastMessage,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
