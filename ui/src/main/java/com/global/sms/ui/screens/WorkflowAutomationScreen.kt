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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.global.sms.data.entity.AutomationRuleEntity
import com.global.sms.ui.viewmodels.EnterpriseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkflowAutomationScreen(
    viewModel: EnterpriseViewModel,
    onBack: () -> Unit
) {
    val rules by viewModel.automationRules.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .testTag("workflow_automation_screen"),
            topBar = {
                TopAppBar(
                    title = { Text("موتور اتوماسیون فرآیندها (Workflow Automation)") },
                    navigationIcon = {
                        IconButton(onClick = onBack, modifier = Modifier.testTag("btn_back")) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.testTag("fab_add_rule")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "افزودن قانون")
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
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PrecisionManufacturing, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "تعریف قوانین شرطی: اگر پیامک حاوی کلمه مشخصی بود، اقدام خودکار (پیشنهاد قالب، تغییر برچسب یا پاسخ) اجرا شود.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                if (rules.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("هیچ قانون اتوماسیونی ثبت نشده است.")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(rules, key = { it.id }) { rule ->
                            AutomationRuleCardItem(
                                rule = rule,
                                onToggle = { enabled ->
                                    viewModel.saveAutomationRule(rule.copy(isEnabled = enabled))
                                },
                                onDelete = { viewModel.deleteAutomationRule(rule) }
                            )
                        }
                    }
                }
            }
        }

        if (showAddDialog) {
            AutomationRuleEditDialog(
                onDismiss = { showAddDialog = false },
                onSave = { rule ->
                    viewModel.saveAutomationRule(rule)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun AutomationRuleCardItem(
    rule: AutomationRuleEntity,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = rule.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("کلیدواژه محرک: «${rule.triggerKeyword}»", style = MaterialTheme.typography.bodyMedium)
                Text("اقدام: ${rule.actionType} (${rule.actionValue})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = rule.isEnabled, onCheckedChange = onToggle)
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun AutomationRuleEditDialog(
    onDismiss: () -> Unit,
    onSave: (AutomationRuleEntity) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var keyword by remember { mutableStateOf("") }
    var actionType by remember { mutableStateOf("SUGGEST_TEMPLATE") }
    var actionValue by remember { mutableStateOf("قالب پیشنهادی فروش") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تعریف قانون اتوماسیون جدید") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("عنوان قانون") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = keyword,
                    onValueChange = { keyword = it },
                    label = { Text("کلمه کلیدی محرک (مثلاً: قیمت، لغو، آدرس)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = actionValue,
                    onValueChange = { actionValue = it },
                    label = { Text("مقدار اقدام (نام قالب یا برچسب)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank() && keyword.isNotBlank()) {
                        onSave(
                            AutomationRuleEntity(
                                name = name,
                                triggerKeyword = keyword,
                                actionType = actionType,
                                actionValue = actionValue
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
