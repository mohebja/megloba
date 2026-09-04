package com.global.sms.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnterpriseAdaptiveWorkspace(
    onNavigateBack: () -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp

    // Determine layout mode based on window size / fold state
    val layoutMode = when {
        screenWidthDp >= 900 -> "THREE_PANE_FOLDABLE"
        screenWidthDp >= 600 -> "TWO_PANE_TABLET"
        else -> "SINGLE_PANE_PHONE"
    }

    var selectedThreadId by remember { mutableStateOf("t1") }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .testTag("enterprise_adaptive_workspace"),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "میز کار انطباقی (Adaptive Workspace - $layoutMode)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.testTag("adaptive_workspace_back_button")
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                when (layoutMode) {
                    "THREE_PANE_FOLDABLE" -> {
                        // Foldable / Wide Tablet: 3 panes (Nav + List + Detail/CRM)
                        Row(modifier = Modifier.fillMaxSize()) {
                            // Pane 1: Organization Nav (200.dp)
                            Box(
                                modifier = Modifier
                                    .width(220.dp)
                                    .fillMaxHeight()
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(12.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text(text = "دپارتمان‌ها", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    NavigationDrawerItem(
                                        label = { Text("فروش و بازاریابی") },
                                        selected = true,
                                        onClick = {},
                                        icon = { Icon(Icons.Default.BusinessCenter, contentDescription = null) }
                                    )
                                    NavigationDrawerItem(
                                        label = { Text("پشتیبانی مشتریان") },
                                        selected = false,
                                        onClick = {},
                                        icon = { Icon(Icons.Default.SupportAgent, contentDescription = null) }
                                    )
                                }
                            }
                            VerticalDivider()
                            // Pane 2: Message Threads (300.dp)
                            Box(
                                modifier = Modifier
                                    .width(320.dp)
                                    .fillMaxHeight()
                                    .padding(12.dp)
                            ) {
                                MessageThreadsListPane(selectedThreadId = selectedThreadId, onSelectThread = { selectedThreadId = it })
                            }
                            VerticalDivider()
                            // Pane 3: Conversation & Customer 360 CRM (Weight 1)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .padding(16.dp)
                            ) {
                                ConversationAndCrmPane(selectedThreadId = selectedThreadId)
                            }
                        }
                    }
                    "TWO_PANE_TABLET" -> {
                        // Tablet: 2 panes (List + Detail)
                        Row(modifier = Modifier.fillMaxSize()) {
                            Box(
                                modifier = Modifier
                                    .width(320.dp)
                                    .fillMaxHeight()
                                    .padding(12.dp)
                            ) {
                                MessageThreadsListPane(selectedThreadId = selectedThreadId, onSelectThread = { selectedThreadId = it })
                            }
                            VerticalDivider()
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .padding(16.dp)
                            ) {
                                ConversationAndCrmPane(selectedThreadId = selectedThreadId)
                            }
                        }
                    }
                    else -> {
                        // Phone: Single pane view
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            MessageThreadsListPane(selectedThreadId = selectedThreadId, onSelectThread = { selectedThreadId = it })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageThreadsListPane(
    selectedThreadId: String,
    onSelectThread: (String) -> Unit
) {
    val threads = listOf(
        Pair("t1", "شرکت همراه سازان پیشرو"),
        Pair("t2", "پشتیبانی فنی بانک ملی"),
        Pair("t3", "بازاریابی دیجیتال ایران")
    )
    Column {
        Text(text = "گفتگوهای فعال سازمان", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(threads) { (id, title) ->
                val isSelected = selectedThreadId == id
                Card(
                    onClick = { onSelectThread(id) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.Message, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = title, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ConversationAndCrmPane(selectedThreadId: String) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "جزئیات گفتگو و هوش مشتری 360 CRM", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "شناسه گفتگو: $selectedThreadId", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "آخرین پیام: فاکتور خرید تایید شد و پیامک تشکر صادر گردید.", fontSize = 14.sp)
            }
        }
    }
}
