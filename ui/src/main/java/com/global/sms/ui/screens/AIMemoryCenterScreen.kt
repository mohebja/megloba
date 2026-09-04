package com.global.sms.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.global.sms.core.ai.memory.AIMemoryRecord
import com.global.sms.core.ai.memory.AdvancedMemoryEngine
import com.global.sms.core.ai.memory.MemoryType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIMemoryCenterScreen(
    onNavigateBack: () -> Unit = {}
) {
    val memoryEngine = remember { AdvancedMemoryEngine() }
    val memories by memoryEngine.memories.collectAsState()

    var showEditDialog by remember { mutableStateOf<AIMemoryRecord?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var newKey by remember { mutableStateOf("") }
    var newContent by remember { mutableStateOf("") }
    var editedContent by remember { mutableStateOf("") }
    var showResetConfirm by remember { mutableStateOf(false) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .testTag("ai_memory_center_screen"),
            topBar = {
                TopAppBar(
                    title = { Text("مرکز حافظه هوش مصنوعی (AI Memory)", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("memory_center_back_button")) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { showResetConfirm = true },
                            modifier = Modifier.testTag("reset_all_memories_button")
                        ) {
                            Icon(Icons.Default.DeleteForever, contentDescription = "Reset AI", tint = MaterialTheme.colorScheme.error)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.testTag("add_ai_memory_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "افزودن حافظه")
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                // Header Info Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("memory_info_card")
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Psychology,
                            contentDescription = null,
                            modifier = Modifier.size(36.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "حافظه بلندمدت و کوتاه‌مدت AI (کاملاً محلی)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                "تمامی حافظه‌ها و یادگیری‌های هوش مصنوعی صرفاً در حافظه امن دستگاه شما نگهداری می‌شوند.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("لیست دانش و ترجیحات یادگیری شده (${memories.size})", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    TextButton(onClick = { memoryEngine.purgeExpiredMemories() }) {
                        Text("پاکسازی حافظه منقضی")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (memories.isEmpty()) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(24.dp)
                                .fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(64.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Psychology,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "حافظه هوش مصنوعی هنوز خالی است",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "موتور هوش مصنوعی به صورت خودکار با تحلیل پیامک‌های دریافتی، الگوهای پرتکرار (مانند ترجیحات، دسته‌بندی‌های مالی و مخاطبین کلیدی) را به صورت ۱۰۰٪ آفلاین در حافظه امن ثبت می‌کند.",
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall,
                                lineHeight = 20.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            FilledTonalButton(
                                onClick = { showAddDialog = true }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("افزودن دستی حافظه یا ترجیح")
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(memories, key = { it.memoryId }) { item ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("memory_item_${item.memoryId}"),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Badge(
                                            containerColor = when (item.type) {
                                                MemoryType.LONG_TERM -> Color(0xFF1565C0)
                                                MemoryType.USER_PREFERENCE -> Color(0xFF2E7D32)
                                                MemoryType.CONTACT_RELATIONSHIP -> Color(0xFF6A1B9A)
                                                MemoryType.SHORT_TERM -> Color(0xFFE65100)
                                            }
                                        ) {
                                            Text(
                                                item.type.name,
                                                color = Color.White,
                                                fontSize = 10.sp,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }

                                        Row {
                                            IconButton(
                                                onClick = {
                                                    showEditDialog = item
                                                    editedContent = item.memoryContent
                                                },
                                                modifier = Modifier.size(28.dp).testTag("edit_memory_${item.memoryId}")
                                            ) {
                                                Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(18.dp))
                                            }
                                            IconButton(
                                                onClick = { memoryEngine.deleteMemory(item.memoryId) },
                                                modifier = Modifier.size(28.dp).testTag("delete_memory_${item.memoryId}")
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(item.subjectKey, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(item.memoryContent, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("امتیاز اهمیت: ${(item.importanceScore * 100).toInt()}%", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Memory Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("افزودن یادگیری جدید به حافظه AI") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newKey,
                        onValueChange = { newKey = it },
                        label = { Text("عنوان / کلید (موضوع)") },
                        modifier = Modifier.fillMaxWidth().testTag("add_memory_key_input")
                    )
                    OutlinedTextField(
                        value = newContent,
                        onValueChange = { newContent = it },
                        label = { Text("محتوای حافظه") },
                        modifier = Modifier.fillMaxWidth().testTag("add_memory_content_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newKey.isNotBlank() && newContent.isNotBlank()) {
                            memoryEngine.storeMemory(
                                type = MemoryType.USER_PREFERENCE,
                                subjectKey = newKey.trim(),
                                content = newContent.trim()
                            )
                            showAddDialog = false
                            newKey = ""
                            newContent = ""
                        }
                    },
                    modifier = Modifier.testTag("confirm_add_memory_btn")
                ) {
                    Text("افزودن")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("انصراف")
                }
            }
        )
    }

    // Edit Memory Dialog
    showEditDialog?.let { targetMemory ->
        AlertDialog(
            onDismissRequest = { showEditDialog = null },
            title = { Text("ویرایش یادگیری هوش مصنوعی") },
            text = {
                OutlinedTextField(
                    value = editedContent,
                    onValueChange = { editedContent = it },
                    label = { Text("متن حافظه") },
                    modifier = Modifier.fillMaxWidth().testTag("edit_memory_input")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        memoryEngine.updateMemory(targetMemory.memoryId, editedContent)
                        showEditDialog = null
                    },
                    modifier = Modifier.testTag("save_edit_memory_btn")
                ) {
                    Text("ذخیره تغییرات")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = null }) {
                    Text("انصراف")
                }
            }
        )
    }

    // Reset Confirm Dialog
    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("بازنشانی کامل حافظه AI") },
            text = { Text("آیا اطمینان دارید؟ تمامی یادگیری‌ها، ترجیحات و حافظه AI پاکسازی خواهند شد.") },
            confirmButton = {
                Button(
                    onClick = {
                        memoryEngine.resetAllAiMemories()
                        showResetConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_reset_all_memories")
                ) {
                    Text("بله، بازنشانی کن", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) {
                    Text("انصراف")
                }
            }
        )
    }
}
