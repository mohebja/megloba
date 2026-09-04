package com.global.sms.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.global.sms.core.automation.AutomationActionType
import com.global.sms.core.automation.AutomationIntentParser
import com.global.sms.core.automation.AutomationRule
import com.global.sms.core.automation.AutomationTriggerType

data class WorkflowApprovalStep(
    val stepId: String,
    val stepName: String,
    val requiresManagerApproval: Boolean = true,
    val conditionRule: String,
    val isApproved: Boolean = true
)

data class WorkflowAuditLog(
    val auditId: String,
    val workflowName: String,
    val actionTaken: String,
    val status: String,
    val timestamp: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkflowDesignerScreen(
    onNavigateBack: () -> Unit = {}
) {
    var rules by remember { mutableStateOf<List<AutomationRule>>(emptyList()) }
    var auditLogs by remember { mutableStateOf<List<WorkflowAuditLog>>(emptyList()) }

    var naturalLanguagePrompt by remember { mutableStateOf("") }
    var isGeneratingAi by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }

    var newRuleName by remember { mutableStateOf("") }
    var selectedTrigger by remember { mutableStateOf(AutomationTriggerType.BODY_CONTAINS) }
    var triggerVal by remember { mutableStateOf("") }
    var selectedAction by remember { mutableStateOf(AutomationActionType.AUTO_REPLY) }
    var actionVal by remember { mutableStateOf("") }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .testTag("workflow_designer_screen"),
            topBar = {
                TopAppBar(
                    title = { Text("طراح جریان‌های کاری خودکار (Workflow Designer)", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("workflow_designer_back")) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
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
                    modifier = Modifier.testTag("add_workflow_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "افزودن جریان کاری")
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                // AI Prompt Assistant Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .testTag("ai_workflow_prompt_card"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "مولد هوشمند جریان کار (AI Workflow Generator)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "سناریوی کاری خود را به زبان ساده بنویسید تا محرک‌ها (Trigger) و اقدامات (Action) خودکار استخراج شوند:",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = naturalLanguagePrompt,
                            onValueChange = { naturalLanguagePrompt = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("ai_workflow_input"),
                            placeholder = { Text("مثال: اگر پیامک حاوی 'تخفیف' بود مسدود کن، یا پیام‌های بانک را در مالی ثبت کن") },
                            maxLines = 2
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                if (naturalLanguagePrompt.isNotBlank()) {
                                    isGeneratingAi = true
                                    val generatedRule = AutomationIntentParser.parsePromptToRule(naturalLanguagePrompt)
                                    val log = WorkflowAuditLog(
                                        auditId = "audit_${System.currentTimeMillis()}",
                                        workflowName = generatedRule.name,
                                        actionTaken = "استخراج هوشمند قانون بر اساس کلیدواژه‌ها و قصد کاربر",
                                        status = "فعال شد"
                                    )
                                    rules = listOf(generatedRule) + rules
                                    auditLogs = listOf(log) + auditLogs
                                    naturalLanguagePrompt = ""
                                    isGeneratingAi = false
                                }
                            },
                            modifier = Modifier
                                .align(Alignment.End)
                                .testTag("generate_workflow_button"),
                            enabled = !isGeneratingAi && naturalLanguagePrompt.isNotBlank()
                        ) {
                            Text(if (isGeneratingAi) "در حال پردازش..." else "تولید قانون از متن")
                        }
                    }
                }

                // Rules List Section
                Text("جریان‌های کاری فعال (${rules.size})", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))

                if (rules.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.AccountTree,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "هیچ جریان کاری فعالی تعریف نشده است.",
                                color = MaterialTheme.colorScheme.outline,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        items(rules, key = { it.id }) { rule ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("rule_card_${rule.id}"),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (rule.isEnabled) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.AccountTree,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                rule.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                        }
                                        Switch(
                                            checked = rule.isEnabled,
                                            onCheckedChange = { checked ->
                                                rules = rules.map {
                                                    if (it.id == rule.id) it.copy(isEnabled = checked) else it
                                                }
                                            },
                                            modifier = Modifier.testTag("switch_${rule.id}")
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Visual Logic Flow Node
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                            .padding(8.dp)
                                    ) {
                                        Badge(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
                                            Text("اگر (IF)", modifier = Modifier.padding(2.dp), fontSize = 10.sp)
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            "${rule.triggerType.name}: \"${rule.triggerValue}\"",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.weight(1f))
                                        Badge(containerColor = MaterialTheme.colorScheme.tertiaryContainer) {
                                            Text("آنگاه (THEN)", modifier = Modifier.padding(2.dp), fontSize = 10.sp)
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            rule.actionType.name,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Audit History Section
                Text("تاریخچه اجرای جریان‌ها (Audit History)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(6.dp))

                if (auditLogs.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = "هنوز رویداد اتوماسیونی اجرا نشده است.",
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .height(100.dp)
                            .fillMaxWidth()
                    ) {
                        items(auditLogs, key = { it.auditId }) { log ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(log.workflowName, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        Text(log.actionTaken, fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                    }
                                    Badge(containerColor = Color(0xFF2E7D32)) {
                                        Text(log.status, color = Color.White, fontSize = 9.sp, modifier = Modifier.padding(2.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Manual Rule Creation Dialog
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("افزودن جریان کاری جدید") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newRuleName,
                            onValueChange = { newRuleName = it },
                            label = { Text("نام جریان") },
                            modifier = Modifier.fillMaxWidth().testTag("workflow_name_input")
                        )
                        OutlinedTextField(
                            value = triggerVal,
                            onValueChange = { triggerVal = it },
                            label = { Text("مقدار شرط (کلمه کلیدی یا شماره)") },
                            modifier = Modifier.fillMaxWidth().testTag("workflow_trigger_val_input")
                        )
                        OutlinedTextField(
                            value = actionVal,
                            onValueChange = { actionVal = it },
                            label = { Text("متن پاسخ یا اقدام") },
                            modifier = Modifier.fillMaxWidth().testTag("workflow_action_val_input")
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newRuleName.isNotBlank()) {
                                val rule = AutomationRule(
                                    id = "rule_${System.currentTimeMillis()}",
                                    name = newRuleName,
                                    triggerType = selectedTrigger,
                                    triggerValue = triggerVal,
                                    actionType = selectedAction,
                                    actionValue = actionVal.ifBlank { null },
                                    isEnabled = true
                                )
                                rules = listOf(rule) + rules
                                showAddDialog = false
                                newRuleName = ""
                                triggerVal = ""
                                actionVal = ""
                            }
                        },
                        modifier = Modifier.testTag("confirm_add_workflow_btn")
                    ) {
                        Text("ذخیره جریان")
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
