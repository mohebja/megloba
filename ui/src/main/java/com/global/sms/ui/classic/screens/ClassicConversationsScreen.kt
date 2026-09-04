package com.global.sms.ui.classic.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.global.sms.data.entity.ConversationEntity
import com.global.sms.ui.classic.components.ClassicThreadCard
import com.global.sms.ui.classic.components.ClassicTopBar
import com.global.sms.ui.components.ConversationMenuBottomSheet
import com.global.sms.ui.components.ConversationSwipeRow
import com.global.sms.ui.viewmodels.GlobalSmsViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ClassicConversationsScreen(
    viewModel: GlobalSmsViewModel,
    onOpenThread: (Long) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    onComposeNew: () -> Unit
) {
    val conversations by viewModel.conversations.collectAsStateWithLifecycle()
    val isDefaultSmsApp by viewModel.isDefaultSmsApp.collectAsStateWithLifecycle()
    val isImportingSms by viewModel.isImportingSms.collectAsStateWithLifecycle()
    val smsImportProgress by viewModel.smsImportProgress.collectAsStateWithLifecycle()
    val smsImportStatusText by viewModel.smsImportStatusText.collectAsStateWithLifecycle()
    var selectedConversationForMenu by remember { mutableStateOf<ConversationEntity?>(null) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            modifier = Modifier.testTag("classic_conversations_screen"),
            topBar = {
                ClassicTopBar(
                    title = "پیامک‌های من (کلاسیک)",
                    onSearchClick = onOpenSearch,
                    onMenuClick = onOpenSettings
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onComposeNew,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("classic_compose_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "پیامک جدید"
                    )
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                com.global.sms.ui.components.SmsImportProgressBanner(
                    isImporting = isImportingSms,
                    progress = smsImportProgress,
                    statusText = smsImportStatusText
                )

                if (conversations.isEmpty()) {
                    com.global.sms.ui.components.EmptySmsImportPrompt(
                        onImportClick = { viewModel.startHistoricalSmsImport(force = true) },
                        onRequestDefaultSms = { viewModel.showDefaultSmsDialog.value = true },
                        isDefaultSms = isDefaultSmsApp
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("classic_conversations_list")
                    ) {
                        items(
                            items = conversations,
                            key = { it.threadId }
                        ) { conversation ->
                            ConversationSwipeRow(
                                conversation = conversation,
                                onClick = { onOpenThread(conversation.threadId) },
                                onLongClick = { selectedConversationForMenu = conversation },
                                onSwipeRightToMarkReadUnread = {
                                    viewModel.toggleReadUnread(conversation)
                                },
                                onSwipeLeftToArchive = {
                                    viewModel.archiveConversation(conversation.threadId)
                                }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .testTag("classic_conversation_item_${conversation.threadId}")
                                        .combinedClickable(
                                            onClick = { onOpenThread(conversation.threadId) },
                                            onLongClick = { selectedConversationForMenu = conversation }
                                        )
                                ) {
                                    ClassicThreadCard(
                                        conversation = conversation,
                                        onClick = { onOpenThread(conversation.threadId) }
                                    )
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }

        selectedConversationForMenu?.let { conversation ->
            ConversationMenuBottomSheet(
                conversation = conversation,
                viewModel = viewModel,
                onDismiss = { selectedConversationForMenu = null }
            )
        }
    }
}
