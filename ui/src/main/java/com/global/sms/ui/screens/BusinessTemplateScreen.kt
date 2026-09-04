package com.global.sms.ui.screens

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.global.sms.data.entity.BusinessTemplateEntity
import com.global.sms.ui.viewmodels.EnterpriseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessTemplateScreen(
    viewModel: EnterpriseViewModel,
    onBack: () -> Unit
) {
    val templates by viewModel.templates.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingTemplate by remember { mutableStateOf<BusinessTemplateEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("مدیریت قالب‌های پیامک تجاری") },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingTemplate = null
                    showAddDialog = true
                },
                modifier = Modifier.testTag("fab_add_template")
            ) {
                Icon(Icons.Default.Add, contentDescription = "افزودن قالب")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Info Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "می‌توانید از متغیرهای {name}، {date}، {order_number} و {amount} در متن قالب استفاده کنید تا به‌صورت خودکار جایگزین شوند.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            if (templates.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("هیچ قالبی تعریف نشده است. جهت افزودن روی دکمه + کلیک کنید.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(templates, key = { it.id }) { template ->
                        TemplateCardItem(
                            template = template,
                            onEdit = {
                                editingTemplate = template
                                showAddDialog = true
                            },
                            onDelete = { viewModel.deleteTemplate(template) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        TemplateEditDialog(
            template = editingTemplate,
            onDismiss = { showAddDialog = false },
            onSave = { updated ->
                viewModel.saveTemplate(updated)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun TemplateCardItem(
    template: BusinessTemplateEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = template.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.weight(1f))
                AssistChip(onClick = {}, label = { Text(template.category) })
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "ویرایش")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = template.body,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}

@Composable
fun TemplateEditDialog(
    template: BusinessTemplateEntity?,
    onDismiss: () -> Unit,
    onSave: (BusinessTemplateEntity) -> Unit
) {
    var title by remember { mutableStateOf(template?.title ?: "") }
    var category by remember { mutableStateOf(template?.category ?: "عمومی") }
    var body by remember { mutableStateOf(template?.body ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (template == null) "ایجاد قالب پیامک جدید" else "ویرایش قالب") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("عنوان قالب") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("دسته‌بندی (فروش، پشتیبانی، عمومی)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    label = { Text("متن قالب") },
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    val addVar = { varName: String -> body += " {$varName}" }
                    SuggestionChip(onClick = { addVar("name") }, label = { Text("{name}", fontSize = 11.sp) })
                    SuggestionChip(onClick = { addVar("order_number") }, label = { Text("{order_number}", fontSize = 11.sp) })
                    SuggestionChip(onClick = { addVar("amount") }, label = { Text("{amount}", fontSize = 11.sp) })
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank() && body.isNotBlank()) {
                        onSave(
                            (template ?: BusinessTemplateEntity(title = title, body = body)).copy(
                                title = title,
                                category = category,
                                body = body
                            )
                        )
                    }
                }
            ) {
                Text("ذخیره")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("انصراف")
            }
        }
    )
}
