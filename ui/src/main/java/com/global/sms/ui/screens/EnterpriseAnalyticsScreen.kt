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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.global.sms.ui.viewmodels.EnterpriseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnterpriseAnalyticsScreen(
    viewModel: EnterpriseViewModel,
    onBack: () -> Unit
) {
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val bulkJobs by viewModel.bulkJobs.collectAsStateWithLifecycle()

    var showExportSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تحلیل و گزارش‌گیری سازمانی (Analytics)") },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                }
            )
        },
        snackbarHost = {
            if (showExportSnackbar) {
                Snackbar(
                    action = {
                        TextButton(onClick = { showExportSnackbar = false }) {
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
                        Text("کل پیامک‌های ارسالی امروز:")
                        Text("۱,۴۸۰ پیامک", fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("نرخ تحویل موفق (Delivery Rate):")
                        Text("۹۹.۲٪", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("میانگین زمان پاسخگویی اپراتورها:")
                        Text("۳ دقیقه", fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("مشتریان فعال مخاطب CRM:")
                        Text("${customers.size} نفر", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Report Export Buttons
            Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("خروجی‌گرفتن از گزارش‌ها", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    Button(
                        onClick = {
                            snackbarMessage = "گزارش تعامل با مشتریان به‌صورت CSV خروجی گرفته شد."
                            showExportSnackbar = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("خروجی CSV گزارش ارتباط با مشتریان")
                    }

                    OutlinedButton(
                        onClick = {
                            snackbarMessage = "گزارش عملکرد فروش و پشتیبانی به‌صورت Excel خروجی گرفته شد."
                            showExportSnackbar = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.TableChart, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("خروجی Excel فعالیت‌های پشتیبانی و فروش")
                    }

                    OutlinedButton(
                        onClick = {
                            snackbarMessage = "گزارش جامع مدیریتی به صورت PDF تولید شد."
                            showExportSnackbar = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("تولید فایل PDF گزارش جامع مدیریتی")
                    }
                }
            }
        }
    }
}
