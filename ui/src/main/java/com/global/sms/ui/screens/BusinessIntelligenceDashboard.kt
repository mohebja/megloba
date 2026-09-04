package com.global.sms.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.global.sms.core.bi.EnterpriseBIEngine
import com.global.sms.core.bi.EnterpriseBiReport

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessIntelligenceDashboard(
    onNavigateBack: () -> Unit = {}
) {
    val biEngine = remember { EnterpriseBIEngine() }
    val metrics by biEngine.biMetrics.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Business Intelligence / هوش تجاری سازمانی", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("bi_dashboard_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { biEngine.refreshMetrics() },
                        modifier = Modifier.testTag("bi_refresh_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sentiment Trend Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("bi_card_sentiment"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Psychology, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Sentiment Trend / تحلیل احساسات", fontWeight = FontWeight.Bold)
                            }
                            Text("${metrics.sentiment.overallScore}/10", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            SentimentMeter("مثبت (Positive)", "${metrics.sentiment.positivePercent}%", Color(0xFF2E7D32))
                            SentimentMeter("خنثی (Neutral)", "${metrics.sentiment.neutralPercent}%", Color(0xFFF57C00))
                            SentimentMeter("منفی (Negative)", "${metrics.sentiment.negativePercent}%", Color(0xFFC62828))
                        }
                    }
                }
            }

            // Campaign ROI Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("bi_card_roi"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Campaign ROI / بازدهی کمپین‌ها", fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            BiMetricTile("نرخ تبدیل", "${metrics.roi.conversionRatePercent}%")
                            BiMetricTile("درآمد تقریبی", "$${metrics.roi.estimatedRevenueDollars}")
                            BiMetricTile("کمپین‌ها", "${metrics.roi.totalCampaignsCount}")
                        }
                    }
                }
            }

            // Customer Churn Risk Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("bi_card_churn"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Churn Risk Radar / هشدار ریزش مشتریان", fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "مشتریان در معرض ریزش بالا: ${metrics.churn.highRiskCount} نفر",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "عامل اصلی ریزش: ${metrics.churn.primaryRiskDriver}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // Team Response Efficiency Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("bi_card_efficiency"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Speed, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Team Efficiency / سرعت پاسخگویی تیم", fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            BiMetricTile("میانگین زمان", "${metrics.efficiency.avgResponseTimeMinutes} دقیقه")
                            BiMetricTile("حل خودکار AI", "${metrics.efficiency.autoResolvedPercent}%")
                            BiMetricTile("رضایت مشتری", "★ ${metrics.efficiency.customerSatisfactionRating}")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SentimentMeter(label: String, valStr: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(valStr, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(label, fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
private fun BiMetricTile(title: String, value: String) {
    Column {
        Text(title, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}
