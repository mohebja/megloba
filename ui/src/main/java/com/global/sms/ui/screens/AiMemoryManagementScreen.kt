package com.global.sms.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.global.sms.data.entity.AiMemoryEntity
import com.global.sms.ui.viewmodels.GlobalSmsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiMemoryManagementScreen(
    viewModel: GlobalSmsViewModel? = null,
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val memoriesFromVm = viewModel?.aiMemoriesFlow?.collectAsStateWithLifecycle()?.value ?: emptyList()
    var localMemories by remember { mutableStateOf<List<AiMemoryEntity>>(emptyList()) }
    val memoryList = if (viewModel != null) memoriesFromVm else localMemories

    var showAddDialog by remember { mutableStateOf(false) }
    var newAddress by remember { mutableStateOf("") }
    var newCategory by remember { mutableStateOf("PREFERENCE") }
    var newKey by remember { mutableStateOf("") }
    var newValue by remember { mutableStateOf("") }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .testTag("ai_memory_management_screen"),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "مدیریت حریم خصوصی و حافظه AI",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.testTag("ai_memory_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "بازگشت"
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                if (viewModel != null) {
                                    viewModel.clearAllAiMemories()
                                } else {
                                    localMemories = emptyList()
                                }
                                Toast.makeText(context, "تمام حافظه محلی هوش مصنوعی پاک‌سازی شد", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.testTag("clear_all_ai_memory_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "پاک‌سازی کل حافظه",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.testTag("add_memory_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "افزودن فاکت جدید")
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                // Info banner card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Memory,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "حافظه و دانش محلی هوش مصنوعی",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "تمام اطلاعات استخراج شده ۱۰۰٪ آفلاین نگهداری می‌شوند و داده‌های حساس به صورت رمزنگاری شده ذخیره می‌گردند.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "فاکت‌ها و واقعیت‌های استخراج‌شده (${memoryList.size})",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (memoryList.isEmpty()) {
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
                                        imageVector = Icons.Default.Memory,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "هیچ دانشی در حافظه ثبت نشده است",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "با دریافت و تحلیل پیام‌های متنی، فاکت‌های استخراج‌شده (مانند دسته‌بندی تراکنش‌ها و اولویت مخاطبین) در اینجا به صورت امن و آفلاین ذخیره می‌گردند.",
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
                                Text("افزودن فاکت یا ترجیح جدید")
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(memoryList, key = { it.id }) { item ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("memory_item_${item.id}"),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = MaterialTheme.colorScheme.secondaryContainer
                                            ) {
                                                Text(
                                                    text = item.category,
                                                    fontSize = 10.sp,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = item.address,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "${item.memoryKey}: ${item.memoryValue}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            if (viewModel != null) {
                                                viewModel.deleteAiMemory(item.id)
                                            } else {
                                                localMemories = localMemories.filter { it.id != item.id }
                                            }
                                            Toast.makeText(context, "حافظه حذف شد", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.testTag("delete_memory_${item.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "حذف حافظه",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("افزودن فاکت یا یادگیری هوش مصنوعی") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newAddress,
                            onValueChange = { newAddress = it },
                            label = { Text("شماره مخاطب / آدرس") },
                            modifier = Modifier.fillMaxWidth().testTag("input_memory_address"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = newKey,
                            onValueChange = { newKey = it },
                            label = { Text("کلید فاکت (مثلاً سمت شغلی، ترجیح)") },
                            modifier = Modifier.fillMaxWidth().testTag("input_memory_key"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = newValue,
                            onValueChange = { newValue = it },
                            label = { Text("مقدار فاکت") },
                            modifier = Modifier.fillMaxWidth().testTag("input_memory_value")
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newKey.isNotBlank() && newValue.isNotBlank()) {
                                val mem = AiMemoryEntity(
                                    address = newAddress.ifBlank { "عمومی" },
                                    category = newCategory,
                                    memoryKey = newKey.trim(),
                                    memoryValue = newValue.trim(),
                                    confidence = 0.95f
                                )
                                if (viewModel != null) {
                                    viewModel.insertAiMemory(mem)
                                } else {
                                    localMemories = listOf(mem.copy(id = System.currentTimeMillis())) + localMemories
                                }
                                showAddDialog = false
                                newAddress = ""
                                newKey = ""
                                newValue = ""
                            }
                        },
                        modifier = Modifier.testTag("save_memory_button")
                    ) {
                        Text("ذخیره")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("انصراف")
                    }
                }
            )
        }
    }
}
