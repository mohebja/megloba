package com.global.sms.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.global.sms.ui.viewmodels.EnterpriseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BulkSmsSafetyScreen(
    viewModel: EnterpriseViewModel,
    onBack: () -> Unit
) {
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val templates by viewModel.templates.collectAsStateWithLifecycle()

    var campaignTitle by remember { mutableStateOf("کمپین پیامکی اطلاع‌رسانی") }
    var rawPhoneInput by remember { mutableStateOf("") }
    var selectedTemplateText by remember { mutableStateOf("سلام {name} عزیز، خدمات جدید مجموعه ما آماده استفاده است.") }
    var isCrmTargeted by remember { mutableStateOf(true) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    val crmPhoneList = remember(customers) { customers.map { it.phoneNumber } }
    val manualPhoneList = remember(rawPhoneInput) {
        rawPhoneInput.split(",", "\n", ";").map { it.trim() }.filter { it.length >= 10 }
    }
    val finalRecipientsCount = if (isCrmTargeted) (crmPhoneList.size + manualPhoneList.size).coerceAtLeast(1) else manualPhoneList.size.coerceAtLeast(1)
    val smsSegments = (selectedTemplateText.length / 70) + 1
    val estimatedTotalSegments = finalRecipientsCount * smsSegments
    val estimatedCostToman = estimatedTotalSegments * 120 // 120 Toman per segment standard rate
    
    // Real pacing calculation: ~1.2s per message + 4s batch pause per 10 messages
    val estimatedTotalDurationSeconds = remember(finalRecipientsCount) {
        val messageDelays = finalRecipientsCount * 1.2
        val batchPauses = (finalRecipientsCount / 10) * 4.0
        (messageDelays + batchPauses).toInt().coerceAtLeast(1)
    }
    val formattedDuration = remember(estimatedTotalDurationSeconds) {
        val minutes = estimatedTotalDurationSeconds / 60
        val seconds = estimatedTotalDurationSeconds % 60
        if (minutes > 0) "$minutes دقیقه و $seconds ثانیه" else "$seconds ثانیه"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ارسال انبوه و ایمن پیامک (Bulk SMS Safety)") },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("تنظیمات کمپین ارسال", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = campaignTitle,
                        onValueChange = { campaignTitle = it },
                        label = { Text("عنوان کمپین") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isCrmTargeted, onCheckedChange = { isCrmTargeted = it })
                        Text("ارسال به تمامی مشتریان ثبت شده در CRM (${customers.size} مخاطب)")
                    }

                    OutlinedTextField(
                        value = rawPhoneInput,
                        onValueChange = { rawPhoneInput = it },
                        label = { Text("افزودن شماره‌های دستی یا CSV (جداشده با کاما یا خط جدید)") },
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = selectedTemplateText,
                        onValueChange = { selectedTemplateText = it },
                        label = { Text("متن پیامک (پشتیبانی از {name})") },
                        maxLines = 5,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Cost & Safety Estimation Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("تخمین هزینه و ایمنی ارسال", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("تعداد دریافت‌کنندگان:")
                        Text("$finalRecipientsCount نفر", fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("تعداد بخش‌های پیامک (Segments):")
                        Text("$estimatedTotalSegments بخش", fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("تخمین هزینه تقریبی:")
                        Text("$estimatedCostToman تومان", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("مدت زمان تخمینی ارسال با کنترل سرعت:")
                        Text(formattedDuration, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }

            Button(
                onClick = { showConfirmDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("btn_start_bulk_sms")
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("تایید نهایی و شروع ارسال کمپین")
            }
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("تایید ارسال انبوه") },
            text = {
                Text("آیا از ارسال این پیامک به $finalRecipientsCount مخاطب با تخمین هزینه $estimatedCostToman تومان اطمینان کامل دارید؟")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.createBulkJob(campaignTitle, finalRecipientsCount, selectedTemplateText)
                        showConfirmDialog = false
                        onBack()
                    }
                ) {
                    Text("ارسال فوری")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("انصراف")
                }
            }
        )
    }
}
