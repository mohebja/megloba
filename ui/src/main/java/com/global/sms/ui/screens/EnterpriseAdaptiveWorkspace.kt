package com.global.sms.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.global.sms.core.util.PersianUtils
import com.global.sms.data.entity.BusinessTemplateEntity
import com.global.sms.data.entity.ConversationEntity
import com.global.sms.data.entity.CrmCustomerEntity
import com.global.sms.data.entity.MessageEntity
import com.global.sms.ui.viewmodels.EnterpriseViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnterpriseAdaptiveWorkspace(
    viewModel: EnterpriseViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    initialThreadId: Long? = null,
    onNavigateBack: () -> Unit = {},
    onOpenCustomer360: (Long) -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp

    // Determine layout mode adaptively
    val layoutMode = when {
        screenWidthDp >= 900 -> "THREE_PANE_FOLDABLE"
        screenWidthDp >= 600 -> "TWO_PANE_TABLET"
        else -> "SINGLE_PANE_PHONE"
    }

    val conversations by viewModel.conversations.collectAsStateWithLifecycle()
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val departments by viewModel.departments.collectAsStateWithLifecycle()
    val templates by viewModel.templates.collectAsStateWithLifecycle()

    var selectedThreadId by remember(conversations) {
        mutableStateOf<Long?>(initialThreadId ?: conversations.firstOrNull()?.threadId)
    }
    var selectedTagFilter by remember { mutableStateOf("همه") }
    var searchQuery by remember { mutableStateOf("") }
    var showNewMessageDialog by remember { mutableStateOf(false) }

    val filteredConversations = remember(conversations, customers, searchQuery, selectedTagFilter) {
        conversations.filter { conv ->
            val matchedCustomer = customers.find { it.phoneNumber == conv.address }
            val matchesQuery = searchQuery.isBlank() ||
                    (conv.contactName?.contains(searchQuery, ignoreCase = true) == true) ||
                    conv.address.contains(searchQuery) ||
                    conv.lastMessage.contains(searchQuery, ignoreCase = true) ||
                    (matchedCustomer?.company?.contains(searchQuery, ignoreCase = true) == true)

            val matchesTag = when (selectedTagFilter) {
                "همه" -> true
                "VIP" -> matchedCustomer?.customerStatus.equals("VIP", ignoreCase = true) || matchedCustomer?.tags?.contains("VIP") == true
                "مشتری" -> matchedCustomer != null
                "سرنخ (Lead)" -> matchedCustomer?.customerStatus.equals("LEAD", ignoreCase = true)
                else -> true
            }

            matchesQuery && matchesTag
        }
    }

    val activeConversation = remember(conversations, selectedThreadId) {
        conversations.find { it.threadId == selectedThreadId }
    }

    val matchedCustomer = remember(customers, activeConversation) {
        if (activeConversation != null) {
            customers.find { it.phoneNumber == activeConversation.address }
        } else null
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .testTag("enterprise_adaptive_workspace"),
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "میز کار ارتباطات و هوش مشتریان",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "حالت نمایش: ${when(layoutMode) { "THREE_PANE_FOLDABLE" -> "نمای سه‌ستونه تاشو"; "TWO_PANE_TABLET" -> "نمای دوپنلی تبلت"; else -> "نمای تلفن همراه" }}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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
                    },
                    actions = {
                        IconButton(
                            onClick = { showNewMessageDialog = true },
                            modifier = Modifier.testTag("btn_workspace_new_message")
                        ) {
                            Icon(Icons.Default.AddComment, contentDescription = "ارسال پیام جدید")
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
                        Row(modifier = Modifier.fillMaxSize()) {
                            // Pane 1: Organization & Filters Navigation (220.dp)
                            Box(
                                modifier = Modifier
                                    .width(230.dp)
                                    .fillMaxHeight()
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .padding(12.dp)
                            ) {
                                OrganizationNavPane(
                                    departments = departments.map { it.name },
                                    selectedTag = selectedTagFilter,
                                    onSelectTag = { selectedTagFilter = it },
                                    totalConversations = conversations.size,
                                    totalCustomers = customers.size
                                )
                            }

                            VerticalDivider()

                            // Pane 2: Conversations List (320.dp)
                            Box(
                                modifier = Modifier
                                    .width(320.dp)
                                    .fillMaxHeight()
                                    .padding(8.dp)
                            ) {
                                ConversationListPane(
                                    conversations = filteredConversations,
                                    customers = customers,
                                    selectedThreadId = selectedThreadId,
                                    searchQuery = searchQuery,
                                    onSearchQueryChange = { searchQuery = it },
                                    onSelectThread = { selectedThreadId = it },
                                    onNewMessage = { showNewMessageDialog = true }
                                )
                            }

                            VerticalDivider()

                            // Pane 3: Chat Thread & CRM 360 (Weight 1)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .padding(8.dp)
                            ) {
                                if (activeConversation != null) {
                                    ChatAndCrmDetailPane(
                                        conversation = activeConversation,
                                        customer = matchedCustomer,
                                        viewModel = viewModel,
                                        templates = templates,
                                        onOpenCustomer360 = onOpenCustomer360,
                                        onBackToList = null
                                    )
                                } else {
                                    EmptyThreadSelectionPlaceholder(
                                        onNewMessage = { showNewMessageDialog = true }
                                    )
                                }
                            }
                        }
                    }

                    "TWO_PANE_TABLET" -> {
                        Row(modifier = Modifier.fillMaxSize()) {
                            // Pane 1: Conversations List (340.dp)
                            Box(
                                modifier = Modifier
                                    .width(340.dp)
                                    .fillMaxHeight()
                                    .padding(8.dp)
                            ) {
                                ConversationListPane(
                                    conversations = filteredConversations,
                                    customers = customers,
                                    selectedThreadId = selectedThreadId,
                                    searchQuery = searchQuery,
                                    onSearchQueryChange = { searchQuery = it },
                                    onSelectThread = { selectedThreadId = it },
                                    onNewMessage = { showNewMessageDialog = true }
                                )
                            }

                            VerticalDivider()

                            // Pane 2: Chat Thread & CRM 360 (Weight 1)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .padding(8.dp)
                            ) {
                                if (activeConversation != null) {
                                    ChatAndCrmDetailPane(
                                        conversation = activeConversation,
                                        customer = matchedCustomer,
                                        viewModel = viewModel,
                                        templates = templates,
                                        onOpenCustomer360 = onOpenCustomer360,
                                        onBackToList = null
                                    )
                                } else {
                                    EmptyThreadSelectionPlaceholder(
                                        onNewMessage = { showNewMessageDialog = true }
                                    )
                                }
                            }
                        }
                    }

                    else -> {
                        // Phone: Single pane view toggles between List and Chat
                        if (selectedThreadId != null && activeConversation != null) {
                            ChatAndCrmDetailPane(
                                conversation = activeConversation,
                                customer = matchedCustomer,
                                viewModel = viewModel,
                                templates = templates,
                                onOpenCustomer360 = onOpenCustomer360,
                                onBackToList = { selectedThreadId = null }
                            )
                        } else {
                            ConversationListPane(
                                conversations = filteredConversations,
                                customers = customers,
                                selectedThreadId = selectedThreadId,
                                searchQuery = searchQuery,
                                onSearchQueryChange = { searchQuery = it },
                                onSelectThread = { selectedThreadId = it },
                                onNewMessage = { showNewMessageDialog = true }
                            )
                        }
                    }
                }
            }
        }

        if (showNewMessageDialog) {
            NewEnterpriseMessageDialog(
                customers = customers,
                templates = templates,
                onDismiss = { showNewMessageDialog = false },
                onSend = { phone, text ->
                    viewModel.sendMessage(0L, phone, text)
                    showNewMessageDialog = false
                }
            )
        }
    }
}

@Composable
private fun OrganizationNavPane(
    departments: List<String>,
    selectedTag: String,
    onSelectTag: (String) -> Unit,
    totalConversations: Int,
    totalCustomers: Int
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "دسته‌بندی و فیلترها",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.primary
        )

        val filterItems = listOf(
            Triple("همه", "همه گفتگوها ($totalConversations)", Icons.AutoMirrored.Filled.Message),
            Triple("VIP", "مشتریان ویژه VIP", Icons.Default.Star),
            Triple("مشتری", "مشتریان ثبت‌شده ($totalCustomers)", Icons.Default.People),
            Triple("سرنخ (Lead)", "سرنخ‌های فروش", Icons.Default.TrendingUp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            filterItems.forEach { (tag, label, icon) ->
                NavigationDrawerItem(
                    label = { Text(label, fontSize = 12.sp) },
                    selected = selectedTag == tag,
                    onClick = { onSelectTag(tag) },
                    icon = { Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        HorizontalDivider()

        Text(
            text = "دپارتمان‌های سازمانی",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(departments) { deptName ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Icon(
                        Icons.Default.Folder,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(deptName, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun ConversationListPane(
    conversations: List<ConversationEntity>,
    customers: List<CrmCustomerEntity>,
    selectedThreadId: Long?,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSelectThread: (Long) -> Unit,
    onNewMessage: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("جستجو در گفتگوها...", fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = if (searchQuery.isNotEmpty()) {
                {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = null)
                    }
                }
            } else null,
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        if (conversations.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Message,
                        contentDescription = null,
                        modifier = Modifier.size(44.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "گفتگویی یافت نشد.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                    Button(onClick = onNewMessage) {
                        Text("ارسال پیام سازمانی جدید", fontSize = 12.sp)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(conversations, key = { it.threadId }) { conv ->
                    val isSelected = selectedThreadId == conv.threadId
                    val customer = customers.find { it.phoneNumber == conv.address }
                    val displayName = customer?.name ?: conv.contactName.orEmpty().ifBlank { conv.address }

                    Card(
                        onClick = { onSelectThread(conv.threadId) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(if (isSelected) 3.dp else 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (customer?.customerStatus.equals("VIP", ignoreCase = true))
                                            Color(0xFFFFB300)
                                        else
                                            MaterialTheme.colorScheme.primary
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = displayName.take(1),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = displayName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = PersianUtils.formatTimestamp(conv.lastTimestamp),
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                customer?.company?.let { comp ->
                                    Text(
                                        text = comp,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        maxLines = 1
                                    )
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = conv.lastMessage.ifBlank { "بدون پیام" },
                                    fontSize = 12.sp,
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

@Composable
private fun ChatAndCrmDetailPane(
    conversation: ConversationEntity,
    customer: CrmCustomerEntity?,
    viewModel: EnterpriseViewModel,
    templates: List<BusinessTemplateEntity>,
    onOpenCustomer360: (Long) -> Unit,
    onBackToList: (() -> Unit)? = null
) {
    val messages by viewModel.getMessagesForThread(conversation.threadId)
        .collectAsStateWithLifecycle(emptyList())

    var replyText by remember { mutableStateOf("") }
    var showTemplateSelector by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // CRM 360 Header Bar
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (onBackToList != null) {
                    IconButton(onClick = onBackToList) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت به لیست")
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = customer?.name ?: conversation.contactName.orEmpty().ifBlank { conversation.address },
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        if (customer?.customerStatus != null) {
                            Spacer(modifier = Modifier.width(6.dp))
                            AssistChip(
                                onClick = {},
                                label = { Text(customer.customerStatus, fontSize = 10.sp) }
                            )
                        }
                    }
                    Text(
                        text = "${customer?.company ?: "شماره"}: ${conversation.address}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                if (customer != null) {
                    Button(
                        onClick = { onOpenCustomer360(customer.id) },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("نمای ۳۶۰ CRM", fontSize = 11.sp)
                    }
                } else {
                    OutlinedButton(
                        onClick = {
                            viewModel.saveCustomer(
                                CrmCustomerEntity(
                                    name = conversation.contactName.orEmpty().ifBlank { "مشتری جدید" },
                                    phoneNumber = conversation.address,
                                    customerStatus = "ACTIVE"
                                )
                            )
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("ثبت در CRM", fontSize = 11.sp)
                    }
                }
            }
        }

        // Messages List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            reverseLayout = true,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                val isSent = msg.type == 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isSent) Arrangement.End else Arrangement.Start
                ) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSent)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.widthIn(max = 320.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = msg.body,
                                color = if (isSent) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = PersianUtils.formatTimestamp(msg.timestamp),
                                color = (if (isSent) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant).copy(alpha = 0.7f),
                                fontSize = 9.sp,
                                modifier = Modifier.align(Alignment.End)
                            )
                        }
                    }
                }
            }
        }

        // Bottom Message Composer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = { showTemplateSelector = true },
                modifier = Modifier.testTag("btn_pick_template")
            ) {
                Icon(Icons.Default.PostAdd, contentDescription = "درج قالب تجاری", tint = MaterialTheme.colorScheme.primary)
            }

            OutlinedTextField(
                value = replyText,
                onValueChange = { replyText = it },
                placeholder = { Text("پاسخ به ${customer?.name ?: conversation.address}...", fontSize = 12.sp) },
                maxLines = 3,
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = {
                    if (replyText.isNotBlank()) {
                        viewModel.sendMessage(conversation.threadId, conversation.address, replyText.trim())
                        replyText = ""
                    }
                },
                enabled = replyText.isNotBlank(),
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (replyText.isNotBlank()) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f))
                    .testTag("btn_send_reply")
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = "ارسال",
                    tint = Color.White
                )
            }
        }
    }

    if (showTemplateSelector) {
        AlertDialog(
            onDismissRequest = { showTemplateSelector = false },
            title = { Text("انتخاب قالب پیامک تجاری") },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(templates) { template ->
                        Card(
                            onClick = {
                                val recipientName = customer?.name ?: "همراه گرامی"
                                val filled = template.body
                                    .replace("{name}", recipientName)
                                    .replace("{date}", SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date()))
                                replyText = filled
                                showTemplateSelector = false
                            },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(template.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(template.body, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showTemplateSelector = false }) {
                    Text("انصراف")
                }
            }
        )
    }
}

@Composable
private fun EmptyThreadSelectionPlaceholder(onNewMessage: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.Message,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
            Text(
                text = "جهت مشاهده گفتگو و هوش ۳۶۰ مشتری، یک مخاطب را از لیست انتخاب فرمایید.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(onClick = onNewMessage) {
                Text("شروع گفتگوی جدید")
            }
        }
    }
}

@Composable
private fun NewEnterpriseMessageDialog(
    customers: List<CrmCustomerEntity>,
    templates: List<BusinessTemplateEntity>,
    onDismiss: () -> Unit,
    onSend: (String, String) -> Unit
) {
    var phoneNumber by remember { mutableStateOf("") }
    var messageBody by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("ارسال پیام سازمانی جدید") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("شماره گیرنده") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (customers.isNotEmpty()) {
                    Text("انتخاب سریع از مشتریان CRM:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(customers) { c ->
                            AssistChip(
                                onClick = { phoneNumber = c.phoneNumber },
                                label = { Text(c.name, fontSize = 10.sp) }
                            )
                        }
                    }
                }

                if (templates.isNotEmpty()) {
                    Text("درج قالب آماده:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(templates) { t ->
                            AssistChip(
                                onClick = { messageBody = t.body },
                                label = { Text(t.title, fontSize = 10.sp) }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = messageBody,
                    onValueChange = { messageBody = it },
                    label = { Text("متن پیامک") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (phoneNumber.isNotBlank() && messageBody.isNotBlank()) {
                        onSend(phoneNumber.trim(), messageBody.trim())
                    }
                },
                enabled = phoneNumber.isNotBlank() && messageBody.isNotBlank()
            ) {
                Text("ارسال")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("انصراف")
            }
        }
    )
}
