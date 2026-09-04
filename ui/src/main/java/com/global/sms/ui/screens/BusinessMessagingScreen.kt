package com.global.sms.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class BusinessTemplate(
    val id: String,
    val title: String,
    val content: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessMessagingScreen(
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val templates = remember {
        listOf(
            BusinessTemplate("t1", "صدور فاکتور رسمی", "مشتری گرامی {name}، فاکتور شماره {invoice} به تاریخ {date} صادر شد."),
            BusinessTemplate("t2", "کد تایید ورود سازمانی", "کد ورود امن شما: {code}. لطفا از افشای آن خودداری فرمایید."),
            BusinessTemplate("t3", "یادآوری جلسه کاری", "همکار گرامی {name}، جلسه کاری در تاریخ {date} برگزار می‌گردد.")
        )
    }

    var selectedTemplate by remember { mutableStateOf(templates.first()) }
    var customerName by remember { mutableStateOf("علی رضایی") }
    var invoiceNumber by remember { mutableStateOf("INV-2026-904") }
    var dateVal by remember { mutableStateOf("۱۴۰۴/۱۲/۱۵") }
    var codeVal by remember { mutableStateOf("849201") }
    var requiresApproval by remember { mutableStateOf(true) }
    var isScheduled by remember { mutableStateOf(false) }

    val renderedPreview = remember(selectedTemplate, customerName, invoiceNumber, dateVal, codeVal) {
        selectedTemplate.content
            .replace("{name}", customerName)
            .replace("{invoice}", invoiceNumber)
            .replace("{date}", dateVal)
            .replace("{code}", codeVal)
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .testTag("business_messaging_screen"),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "میز کار پیام‌رسانی تجاری و کمپین‌ها",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.testTag("business_messaging_back_button")
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Text(
                        text = "انتخاب قالب تجاری و متغیرها",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                items(templates) { template ->
                    val isSelected = selectedTemplate.id == template.id
                    Card(
                        onClick = { selectedTemplate = template },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(if (isSelected) 4.dp else 1.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = isSelected, onClick = { selectedTemplate = template })
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(text = template.title, fontWeight = FontWeight.Bold)
                                Text(text = template.content, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "مقداردهی متغیرهای پیام ({name}, {invoice}, {date}, {code})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                item {
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("نام مشتری {name}") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = invoiceNumber,
                        onValueChange = { invoiceNumber = it },
                        label = { Text("شماره فاکتور {invoice}") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Text(
                        text = "پیش‌نمایش زنده پیام تجاری",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = renderedPreview, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "نیازمند تایید مدیر دپارتمان (Workflow)", fontWeight = FontWeight.Bold)
                        Switch(checked = requiresApproval, onCheckedChange = { requiresApproval = it })
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "ارسال زمان‌بندی شده", fontWeight = FontWeight.Bold)
                        Switch(checked = isScheduled, onCheckedChange = { isScheduled = it })
                    }
                }

                item {
                    Button(
                        onClick = {
                            val msg = if (requiresApproval) "پیام جهت تایید به مدیر ارسال گردید" else "پیام با موفقیت به صف ارسال اضافه شد"
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("submit_business_message_button")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (requiresApproval) "ارسال جهت تایید مدیر" else "ارسال پیام تجاری")
                    }
                }
            }
        }
    }
}
