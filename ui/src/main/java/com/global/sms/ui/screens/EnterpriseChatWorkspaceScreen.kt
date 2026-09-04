package com.global.sms.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.global.sms.core.ai.agent.AgentActionPlan
import com.global.sms.core.ai.agent.AgentRoleType
import com.global.sms.core.ai.agent.EnterpriseAIAgent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnterpriseChatWorkspaceScreen(
    onNavigateBack: () -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp

    val layoutMode = when {
        screenWidthDp >= 950 -> "DESKTOP_THREE_PANE"
        screenWidthDp >= 600 -> "TABLET_TWO_PANE"
        else -> "PHONE_SINGLE_PANE"
    }

    val aiAgent = remember { EnterpriseAIAgent() }
    val pendingPlans by aiAgent.pendingActionPlans.collectAsState()

    var activeConversationId by remember { mutableStateOf("c1") }
    var selectedTone by remember { mutableStateOf("FORMAL / رسمی") }

    LaunchedEffect(Unit) {
        if (pendingPlans.isEmpty()) {
            aiAgent.processMessage(
                sender = "09123456789",
                content = "سلام، در مورد قیمت سرویس پیامک انبوه سوال داشتم. قیمت‌ها چطوره؟",
                role = AgentRoleType.CUSTOMER_SUPPORT
            )
            aiAgent.processMessage(
                sender = "09987654321",
                content = "از سرویس پشتیبانی بسیار ناراضی هستم و میخوام شکایت ثبت کنم!",
                role = AgentRoleType.COMPLAINT_HANDLER
            )
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            modifier = Modifier.testTag("enterprise_chat_workspace_screen"),
            topBar = {
                TopAppBar(
                    modifier = Modifier.testTag("enterprise_top_app_bar"),
                    title = { Text("Enterprise AI Chat Workspace ($layoutMode)", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("enterprise_back_button")) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                when (layoutMode) {
                    "DESKTOP_THREE_PANE" -> {
                        Row(modifier = Modifier.fillMaxSize()) {
                            // Pane 1: Navigation Drawer
                            Box(
                                modifier = Modifier
                                    .width(240.dp)
                                    .fillMaxHeight()
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .padding(12.dp)
                            ) {
                                WorkspaceNavPane(activeId = activeConversationId, onSelect = { activeConversationId = it })
                            }
                            VerticalDivider()
                            // Pane 2: Conversation View
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .padding(16.dp)
                            ) {
                                WorkspaceChatStreamPane(activeId = activeConversationId)
                            }
                            VerticalDivider()
                            // Pane 3: Copilot Approval Panel
                            Box(
                                modifier = Modifier
                                    .width(340.dp)
                                    .fillMaxHeight()
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                    .padding(12.dp)
                            ) {
                                AiCopilotActionPanel(
                                    pendingPlans = pendingPlans,
                                    selectedTone = selectedTone,
                                    onToneSelect = { selectedTone = it },
                                    onApprove = { id -> aiAgent.confirmActionPlan(id) },
                                    onReject = { id -> aiAgent.rejectActionPlan(id, "User cancelled") }
                                )
                            }
                        }
                    }
                    "TABLET_TWO_PANE" -> {
                        Row(modifier = Modifier.fillMaxSize()) {
                            Box(
                                modifier = Modifier
                                    .width(300.dp)
                                    .fillMaxHeight()
                                    .padding(12.dp)
                            ) {
                                WorkspaceNavPane(activeId = activeConversationId, onSelect = { activeConversationId = it })
                            }
                            VerticalDivider()
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .padding(16.dp)
                            ) {
                                Column {
                                    WorkspaceChatStreamPane(activeId = activeConversationId)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    AiCopilotActionPanel(
                                        pendingPlans = pendingPlans,
                                        selectedTone = selectedTone,
                                        onToneSelect = { selectedTone = it },
                                        onApprove = { id -> aiAgent.confirmActionPlan(id) },
                                        onReject = { id -> aiAgent.rejectActionPlan(id, "User cancelled") }
                                    )
                                }
                            }
                        }
                    }
                    else -> {
                        // Phone Single Pane
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            WorkspaceChatStreamPane(activeId = activeConversationId)
                            Spacer(modifier = Modifier.height(12.dp))
                            AiCopilotActionPanel(
                                pendingPlans = pendingPlans,
                                selectedTone = selectedTone,
                                onToneSelect = { selectedTone = it },
                                onApprove = { id -> aiAgent.confirmActionPlan(id) },
                                onReject = { id -> aiAgent.rejectActionPlan(id, "User cancelled") }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkspaceNavPane(activeId: String, onSelect: (String) -> Unit) {
    Column {
        Text("Conversations / گفتگوها", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(12.dp))
        NavigationDrawerItem(
            label = { Text("شرکت پارس تکنولوژی (09123456789)") },
            selected = activeId == "c1",
            onClick = { onSelect("c1") },
            icon = { Icon(Icons.Default.Person, contentDescription = null) }
        )
        NavigationDrawerItem(
            label = { Text("مشتری شاکی (09987654321)") },
            selected = activeId == "c2",
            onClick = { onSelect("c2") },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red) }
        )
    }
}

@Composable
private fun WorkspaceChatStreamPane(activeId: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("workspace_chat_pane"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("مخاطب سازمانی: $activeId", fontWeight = FontWeight.Bold)
                    Text("پاسخگوی خودکار AI V3 فعال است (100% Offline)", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // AI Smart Summary Header
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("ai_smart_summary_card")
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("خلاصه هوشمند و تصمیمات گفتگو (AI Summary)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        if (activeId == "c1")
                            "موضوع: استعلام قیمت پیامک انبوه. تصمیمات اتخاذ شده: ارائه کاتالوگ ۵ ستاره و تعرفه سازمانی."
                        else
                            "موضوع: شکایت عدم دریافت پیامک تایید. تصمیمات اتخاذ شده: ارجاع به مدیر فنی و ارسال هدیه اعتبار SMS.",
                        fontSize = 11.sp
                    )
                }
            }

            // Conversation Messages Timeline
            Text("تایم‌لاین پیام‌ها و تاریخچه ارتباطی", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    if (activeId == "c1")
                        "سلام، در مورد قیمت سرویس پیامک انبوه سوال داشتم. قیمت‌ها چطوره؟"
                    else
                        "از سرویس پشتیبانی بسیار ناراضی هستم و میخوام شکایت ثبت کنم!"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Related Contacts & Documents
            Text("مخاطبین و اسناد مرتبط (Related Documents)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(6.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                AssistChip(
                    onClick = { },
                    label = { Text("کاتالوگ_تعرفه_۱۴۰۵.pdf", fontSize = 11.sp) },
                    leadingIcon = { Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    modifier = Modifier.testTag("doc_chip_1")
                )
                AssistChip(
                    onClick = { },
                    label = { Text("پیش‌نویس_قرارداد.docx", fontSize = 11.sp) },
                    leadingIcon = { Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    modifier = Modifier.testTag("doc_chip_2")
                )
            }
        }
    }
}

@Composable
private fun AiCopilotActionPanel(
    pendingPlans: List<AgentActionPlan>,
    selectedTone: String,
    onToneSelect: (String) -> Unit,
    onApprove: (String) -> Unit,
    onReject: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ai_copilot_panel"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("AI Agent Copilot & Approval", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("لحن پاسخگویی (Tone): $selectedTone", fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                FilterChip(selected = selectedTone.contains("FORMAL"), onClick = { onToneSelect("FORMAL / رسمی") }, label = { Text("رسمی") })
                FilterChip(selected = selectedTone.contains("EMPATHETIC"), onClick = { onToneSelect("EMPATHETIC / ابراز همدردی") }, label = { Text("همدردی") })
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("اقدامات پیشنهادی نیاز به تایید (${pendingPlans.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp)

            if (pendingPlans.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .testTag("empty_pending_plans_card"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "تمام اقدامات پیشنهادی بررسی شدند. هیچ اقدام معلقی وجود ندارد.",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    items(pendingPlans, key = { it.actionId }) { plan ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Badge(containerColor = MaterialTheme.colorScheme.tertiaryContainer) {
                                    Text(plan.agentType.name, modifier = Modifier.padding(2.dp), fontSize = 10.sp)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(plan.proposedResponse, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("دلیل AI: ${plan.reasoningSummary}", fontSize = 10.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextButton(
                                        onClick = { onReject(plan.actionId) },
                                        modifier = Modifier.testTag("reject_${plan.actionId}")
                                    ) {
                                        Text("رد", color = Color.Red)
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Button(
                                        onClick = { onApprove(plan.actionId) },
                                        modifier = Modifier.testTag("approve_${plan.actionId}")
                                    ) {
                                        Text("تایید و ارسال")
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
