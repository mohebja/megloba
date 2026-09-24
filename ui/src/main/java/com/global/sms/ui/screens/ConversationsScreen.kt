package com.global.sms.ui.screens

import com.global.sms.ui.viewmodels.GlobalSmsViewModel
import com.global.sms.ui.components.ConversationCard
import com.global.sms.ui.components.GlobalEmptyState

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.global.sms.core.util.PersianUtils
import com.global.sms.data.entity.ConversationEntity
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import androidx.paging.LoadState
import com.global.sms.data.entity.MessageCategory
import com.global.sms.ui.theme.BankCategoryColor
import com.global.sms.ui.theme.ImportantCategoryColor
import com.global.sms.ui.theme.PrivateCategoryColor
import com.global.sms.ui.theme.SpamCategoryColor
import com.global.sms.ui.theme.WorkCategoryColor

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import com.global.sms.ui.components.ContactAvatar
import com.global.sms.ui.components.rememberContactInfo
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.GroupAdd

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationsScreen(
    viewModel: GlobalSmsViewModel,
    onOpenThread: (Long) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenVault: () -> Unit,
    onOpenScheduled: () -> Unit,
    onOpenSpam: () -> Unit,
    onOpenStats: () -> Unit,
    onOpenBank: () -> Unit = {},
    onOpenPerformance: () -> Unit = {},
    onOpenSettings: () -> Unit,
    onOpenGroupManagement: () -> Unit = {},
    onOpenMultiCompose: () -> Unit = {},
    onComposeNew: () -> Unit,
    onRequestDefaultSms: () -> Unit = {}
) {
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val usePersianDigits by viewModel.usePersianDigits.collectAsStateWithLifecycle()
    val usePersianCalendar by viewModel.usePersianCalendar.collectAsStateWithLifecycle()
    val isDefaultSmsApp by viewModel.isDefaultSmsApp.collectAsStateWithLifecycle()
    val isImportingSms by viewModel.isImportingSms.collectAsStateWithLifecycle()
    val smsImportProgress by viewModel.smsImportProgress.collectAsStateWithLifecycle()
    val smsImportStatusText by viewModel.smsImportStatusText.collectAsStateWithLifecycle()
    val settings by viewModel.settingsState.collectAsStateWithLifecycle()

    var isSearchActive by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "پیام‌رسان جهانی",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                actions = {
                    IconButton(
                        onClick = onOpenSearch,
                        modifier = Modifier.testTag("search_button")
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "جستجوی پیشرفته")
                    }

                    IconButton(
                        onClick = onOpenVault,
                        modifier = Modifier.testTag("vault_button")
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = "گاوصندوق پیام‌ها", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(
                        onClick = onOpenBank,
                        modifier = Modifier.testTag("bank_center_button")
                    ) {
                        Icon(Icons.Default.AccountBalance, contentDescription = "مرکز بانکداری هوشمند", tint = BankCategoryColor)
                    }
                    IconButton(
                        onClick = onOpenScheduled,
                        modifier = Modifier.testTag("scheduled_button")
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = "پیام‌های زمان‌بندی شده")
                    }
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.testTag("conversations_more_menu_button")
                    ) {
                        Icon(Icons.Default.MoreVert, contentDescription = "منو")
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("ارسال پیامک چند مخاطبی / گروهی") },
                            leadingIcon = { Icon(Icons.Default.GroupAdd, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            onClick = { menuExpanded = false; onOpenMultiCompose() }
                        )
                        DropdownMenuItem(
                            text = { Text("مدیریت گروه‌های مخاطبین") },
                            leadingIcon = { Icon(Icons.Default.Group, contentDescription = null) },
                            onClick = { menuExpanded = false; onOpenGroupManagement() }
                        )
                        DropdownMenuItem(
                            text = { Text("مرکز بانکداری هوشمند") },
                            leadingIcon = { Icon(Icons.Default.AccountBalance, contentDescription = null, tint = BankCategoryColor) },
                            onClick = { menuExpanded = false; onOpenBank() }
                        )
                        DropdownMenuItem(
                            text = { Text("پوشه اسپم و آگهی‌ها") },
                            leadingIcon = { Icon(Icons.Default.Security, contentDescription = null) },
                            onClick = { menuExpanded = false; onOpenSpam() }
                        )
                        DropdownMenuItem(
                            text = { Text("آمار پیامک‌ها") },
                            leadingIcon = { Icon(Icons.Default.BarChart, contentDescription = null) },
                            onClick = { menuExpanded = false; onOpenStats() }
                        )
                        DropdownMenuItem(
                            text = { Text("گزارش سرعت و بهینه‌سازی") },
                            leadingIcon = { Icon(Icons.Default.Speed, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            onClick = { menuExpanded = false; onOpenPerformance() }
                        )
                        DropdownMenuItem(
                            text = { Text("تنظیمات برنامه") },
                            leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) },
                            onClick = { menuExpanded = false; onOpenSettings() }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onComposeNew,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("compose_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "ارسال پیامک جدید")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (!isDefaultSmsApp) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "برنامه پیش‌فرض پیامک نیستید",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "برای ارسال و دریافت مستقیم پیامک‌ها، برنامه را پیش‌فرض کنید.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = onRequestDefaultSms,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("تنظیم", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Import progress indicator banner
            com.global.sms.ui.components.SmsImportProgressBanner(
                isImporting = isImportingSms,
                progress = smsImportProgress,
                statusText = smsImportStatusText
            )

            // Category Filter Chips Row
            CategoryChipsRow(
                selectedCategory = selectedCategory,
                onSelectCategory = { viewModel.setCategoryFilter(it) }
            )

            val pagedConversations = viewModel.conversationsPagingFlow.collectAsLazyPagingItems()

            if (pagedConversations.loadState.refresh is LoadState.NotLoading && pagedConversations.itemCount == 0) {
                com.global.sms.ui.components.EmptySmsImportPrompt(
                    onImportClick = { viewModel.startHistoricalSmsImport(force = true) },
                    onRequestDefaultSms = onRequestDefaultSms,
                    isDefaultSms = isDefaultSmsApp
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        count = pagedConversations.itemCount,
                        key = { index -> pagedConversations.peek(index)?.threadId ?: index },
                        contentType = { "conversation_card" }
                    ) { index ->
                        val conversation = pagedConversations[index]
                        if (conversation != null) {
                            ConversationCard(
                                conversation = conversation,
                                style = settings.conversationStyle,
                                usePersianDigits = usePersianDigits,
                                usePersianCalendar = usePersianCalendar,
                                onClick = { onOpenThread(conversation.threadId) },
                                onPinToggle = { viewModel.togglePinConversation(conversation.threadId, conversation.isPinned) },
                                onHideToVault = { viewModel.hideConversation(conversation.threadId, true) },
                                onDelete = { viewModel.deleteConversation(conversation.threadId) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryChipsRow(
    selectedCategory: MessageCategory?,
    onSelectCategory: (MessageCategory?) -> Unit
) {
    val categories = listOf(
        null to "همه",
        MessageCategory.OTP to "کد تایید (OTP)",
        MessageCategory.BANK to "بانک",
        MessageCategory.TRANSACTIONS to "تراکنش مالی",
        MessageCategory.SHOPPING to "خرید آنلاین",
        MessageCategory.DELIVERY to "مرسوله",
        MessageCategory.GOVERNMENT to "سامانه دولتی",
        MessageCategory.BUSINESS to "اداری/تجاری",
        MessageCategory.PERSONAL to "شخصی",
        MessageCategory.SPAM to "اسپم",
        MessageCategory.ADVERTISEMENT to "تبلیغات"
    )

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { (cat, label) ->
            FilterChip(
                selected = selectedCategory == cat,
                onClick = { onSelectCategory(cat) },
                label = { Text(label, style = MaterialTheme.typography.labelMedium) }
            )
        }
    }
}

@Composable
fun EmptyConversationsView() {
    GlobalEmptyState(
        title = "هیچ گفتگو یا پیامکی یافت نشد",
        message = "پیامک‌های دریافتی یا ارسالی شما در این بخش نمایش داده می‌شوند.",
        icon = Icons.Default.FilterList
    )
}

