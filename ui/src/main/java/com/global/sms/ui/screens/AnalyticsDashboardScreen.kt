package com.global.sms.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.global.sms.core.analytics.LocalAnalyticsSummary
import com.global.sms.core.analytics.MessageAnalyticsEngine
import com.global.sms.core.util.PersianUtils
import com.global.sms.ui.viewmodels.GlobalSmsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsDashboardScreen(
    viewModel: GlobalSmsViewModel? = null,
    onNavigateBack: () -> Unit = {}
) {
    val fallbackEngine = remember { MessageAnalyticsEngine() }
    val data by if (viewModel != null) {
        viewModel.messageAnalyticsFlow.collectAsStateWithLifecycle()
    } else {
        fallbackEngine.summary.collectAsStateWithLifecycle()
    }

    val usePersianDigits by if (viewModel != null) {
        viewModel.usePersianDigits.collectAsStateWithLifecycle()
    } else {
        remember { mutableStateOf(true) }
    }

    fun formatNum(num: Int): String {
        val str = num.toString()
        return if (usePersianDigits) PersianUtils.toPersianDigits(str) else str
    }

    val maxHourlyCount = remember(data.hourlyDistribution) {
        data.hourlyDistribution.maxOfOrNull { it.second }?.coerceAtLeast(1) ?: 1
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .testTag("analytics_dashboard_screen"),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "تحلیل و آمار پیشرفته پیام‌ها",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.testTag("analytics_back_button")
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MetricCard(
                            title = "کل پیام‌ها",
                            value = formatNum(data.totalMessagesCount),
                            icon = Icons.Default.Email,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            title = "اسپم مسدودشده",
                            value = formatNum(data.totalSpamBlocked),
                            icon = Icons.Default.Block,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MetricCard(
                            title = "میانگین زمان پاسخ",
                            value = if (data.averageResponseTimeMinutes > 0) {
                                "${formatNum(data.averageResponseTimeMinutes)} دقیقه"
                            } else {
                                "بدون داده"
                            },
                            icon = Icons.Default.Timer,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            title = "ساعت اوج پیام‌ها",
                            value = if (data.peakHourOfDay != "-") {
                                if (usePersianDigits) PersianUtils.toPersianDigits(data.peakHourOfDay) else data.peakHourOfDay
                            } else {
                                "بدون داده"
                            },
                            icon = Icons.Default.AccessTime,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MetricCard(
                            title = "پیام‌های دریافتی",
                            value = formatNum(data.incomingMessagesCount),
                            icon = Icons.Default.CallReceived,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            title = "پیام‌های ارسالی",
                            value = formatNum(data.outgoingMessagesCount),
                            icon = Icons.Default.CallMade,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Text(
                        text = "توزیع زمانی پیام‌ها در طول روز",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            data.hourlyDistribution.forEach { (timeRange, count) ->
                                val displayTimeRange = if (usePersianDigits) PersianUtils.toPersianDigits(timeRange) else timeRange
                                val fraction = if (count > 0) {
                                    (count.toFloat() / maxHourlyCount).coerceIn(0.05f, 1.0f)
                                } else {
                                    0f
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = displayTimeRange,
                                        modifier = Modifier.width(60.dp),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(16.dp)
                                            .background(
                                                color = MaterialTheme.colorScheme.primaryContainer,
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                    ) {
                                        if (fraction > 0f) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxHeight()
                                                    .fillMaxWidth(fraction = fraction)
                                                    .background(
                                                        color = MaterialTheme.colorScheme.primary,
                                                        shape = RoundedCornerShape(4.dp)
                                                    )
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = formatNum(count),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}
