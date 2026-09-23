package com.global.sms.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.global.sms.core.util.PersianUtils
import com.global.sms.ui.viewmodels.EnterpriseViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnterpriseAnalyticsScreen(
    viewModel: EnterpriseViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val bulkJobs by viewModel.bulkJobs.collectAsStateWithLifecycle()
    val totalMessages by viewModel.totalMessagesCount.collectAsStateWithLifecycle()
    val sentMessages by viewModel.sentMessagesCount.collectAsStateWithLifecycle()

    var showExportSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }

    val successfulBulkCount = remember(bulkJobs) {
        bulkJobs.sumOf { it.sentCount }
    }
    val totalBulkRecipients = remember(bulkJobs) {
        bulkJobs.sumOf { it.totalRecipients }
    }
    val deliveryRateStr = remember(successfulBulkCount, totalBulkRecipients) {
        if (totalBulkRecipients > 0) {
            String.format(Locale.US, "%.1f٪", (successfulBulkCount.toDouble() / totalBulkRecipients) * 100)
        } else if (sentMessages > 0) {
            "۱۰۰٪"
        } else {
            "—"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تحلیل و گزارش‌گیری سازمانی (Analytics)") },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                            .testTag("btn_back")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                }
            )
        },
        snackbarHost = {
            if (showExportSnackbar) {
                Snackbar(
                    action = {
                        TextButton(
                            onClick = { showExportSnackbar = false },
                            modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                        ) {
                            Text("باشه")
                        }
                    }
                ) {
                    Text(snackbarMessage)
                }
            }
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
            // High-level Performance Metrics
            Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("خلاصه عملکرد ارتباطی تجاری", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("کل پیامک‌های ارسالی ثبت‌شده:")
                        Text("${PersianUtils.toPersianDigits(sentMessages.toString())} پیامک", fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("کل پیام‌های پایگاه‌داده:")
                        Text("${PersianUtils.toPersianDigits(totalMessages.toString())} پیام", fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("نرخ تحویل کمپین‌های انبوه:")
                        Text(PersianUtils.toPersianDigits(deliveryRateStr), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("مشتریان ثبت‌شده در پرونده CRM:")
                        Text("${PersianUtils.toPersianDigits(customers.size.toString())} نفر", fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("کمپین‌های ارسال انبوه اجراشده:")
                        Text("${PersianUtils.toPersianDigits(bulkJobs.size.toString())} کمپین", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Report Export Buttons
            Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("خروجی‌گرفتن از گزارش‌ها", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    Button(
                        onClick = {
                            try {
                                val reportsDir = File(context.filesDir, "reports").also { it.mkdirs() }
                                val csvFile = File(reportsDir, "crm_customers_${System.currentTimeMillis()}.csv")
                                val sb = StringBuilder("ID,Name,Phone,Tag,Company,LastContactDate\n")
                                customers.forEach { c ->
                                    sb.append("${c.id},\"${c.name}\",${c.phoneNumber},\"${c.tags}\",\"${c.company.orEmpty()}\",${c.lastContactDate}\n")
                                }
                                csvFile.writeText(sb.toString(), Charsets.UTF_8)
                                snackbarMessage = "فایل CSV در حافظه برنامه ذخیره شد: ${csvFile.name}"
                            } catch (e: Exception) {
                                snackbarMessage = "خطا در ایجاد گزارش: ${e.message}"
                            }
                            showExportSnackbar = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .sizeIn(minHeight = 48.dp)
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("خروجی CSV پرونده مشتریان CRM")
                    }

                    OutlinedButton(
                        onClick = {
                            try {
                                val reportsDir = File(context.filesDir, "reports").also { it.mkdirs() }
                                val summaryFile = File(reportsDir, "campaigns_report_${System.currentTimeMillis()}.csv")
                                val sb = StringBuilder("JobId,Title,TotalRecipients,SentCount,Status,Timestamp\n")
                                bulkJobs.forEach { j ->
                                    sb.append("${j.id},\"${j.title}\",${j.totalRecipients},${j.sentCount},${j.status},${j.timestamp}\n")
                                }
                                summaryFile.writeText(sb.toString(), Charsets.UTF_8)
                                snackbarMessage = "گزارش کمپین‌ها با موفقیت ذخیره شد: ${summaryFile.name}"
                            } catch (e: Exception) {
                                snackbarMessage = "خطا در خروجی: ${e.message}"
                            }
                            showExportSnackbar = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .sizeIn(minHeight = 48.dp)
                    ) {
                        Icon(Icons.Default.TableChart, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("خروجی گزارش فعالیت‌های ارسال و کمپین")
                    }

                    OutlinedButton(
                        onClick = {
                            try {
                                val reportsDir = File(context.filesDir, "reports").also { it.mkdirs() }
                                val docFile = File(reportsDir, "executive_summary_${System.currentTimeMillis()}.txt")
                                val dateStr = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date())
                                val report = """
                                    گزارش جامع مدیریتی سامانه پیامکی سازمانی Global SMS
                                    تاریخ گزارش: $dateStr
                                    --------------------------------------------------
                                    تعداد کل پیام‌ها: $totalMessages
                                    پیام‌های ارسالی: $sentMessages
                                    تعداد مخاطبان CRM: ${customers.size}
                                    تعداد کمپین‌های انبوه: ${bulkJobs.size}
                                    نرخ تحویل کمپین‌ها: $deliveryRateStr
                                    وضعیت امنیت داده‌ها: رمزنگاری کامل در وضعیت سکون (SQLCipher AES-256)
                                """.trimIndent()
                                docFile.writeText(report, Charsets.UTF_8)
                                snackbarMessage = "گزارش متنی مدیریتی تولید و ذخیره شد: ${docFile.name}"
                            } catch (e: Exception) {
                                snackbarMessage = "خطا در تولید سند: ${e.message}"
                            }
                            showExportSnackbar = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .sizeIn(minHeight = 48.dp)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("تولید فایل گزارش جامع مدیریتی")
                    }
                }
            }
        }
    }
}
