package com.global.sms.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.global.sms.core.campaign.Campaign
import com.global.sms.ui.viewmodels.CampaignViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampaignDashboardScreen(
    viewModel: CampaignViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val campaigns by viewModel.campaigns.collectAsStateWithLifecycle()
    var showCreateDialog by remember { mutableStateOf(false) }

    var campaignName by remember { mutableStateOf("") }
    var recipientsText by remember { mutableStateOf("") }
    var templateBody by remember { mutableStateOf("") }
    var selectedSimSlot by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "مدیریت کمپین‌های پیامکی سازمانی",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("campaign_nav_back")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "بازگشت"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showCreateDialog = true },
                        modifier = Modifier.testTag("btn_add_campaign")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "کمپین جدید"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateDialog = true },
                icon = { Icon(Icons.Default.Campaign, contentDescription = null) },
                text = { Text("کمپین جدید") },
                modifier = Modifier.testTag("fab_create_campaign")
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // High-Level Analytics Overview Banner
            val totalSentAll = campaigns.sumOf { it.totalSent }
            val totalDeliveredAll = campaigns.sumOf { it.deliveredCount }
            val totalFailedAll = campaigns.sumOf { it.failedCount }
            val totalPendingAll = campaigns.sumOf { it.pendingCount }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .testTag("campaign_overview_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "آمار کل پیامک‌های گروهی و کمپین‌ها",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        AnalyticsStatBadge("ارسال کل", totalSentAll.toString(), Color(0xFF1A73E8))
                        AnalyticsStatBadge("تحویل شده", totalDeliveredAll.toString(), Color(0xFF00C853))
                        AnalyticsStatBadge("ناموفق", totalFailedAll.toString(), Color(0xFFE53935))
                        AnalyticsStatBadge("در انتظار", totalPendingAll.toString(), Color(0xFFFF9800))
                    }
                }
            }

            Text(
                text = "لیست کمپین‌های فعال و زمان‌بندی شده",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            if (campaigns.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "هیچ کمپینی ثبت نشده است. جهت ارسال انبوه دکمه «کمپین جدید» را لمس کنید.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(campaigns, key = { it.id }) { item ->
                        CampaignItemCard(
                            campaign = item,
                            onExecute = { viewModel.executeCampaignNow(item.id) }
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("ایجاد کمپین ارسال پیامک گروهی") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = campaignName,
                        onValueChange = { campaignName = it },
                        label = { Text("نام کمپین") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_campaign_name")
                    )
                    OutlinedTextField(
                        value = recipientsText,
                        onValueChange = { recipientsText = it },
                        label = { Text("شماره‌های گیرندگان (با کاما یا خط جدید)") },
                        placeholder = { Text("09121111111, 09352222222") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_campaign_recipients")
                    )
                    OutlinedTextField(
                        value = templateBody,
                        onValueChange = { templateBody = it },
                        label = { Text("متن قالب پیامک") },
                        placeholder = { Text("سلام {name} عزیز، ...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .testTag("input_campaign_template")
                    )

                    Text("سیم‌کارت ارسال کننده:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = selectedSimSlot == 0,
                            onClick = { selectedSimSlot = 0 },
                            label = { Text("سیم‌کارت ۱") }
                        )
                        FilterChip(
                            selected = selectedSimSlot == 1,
                            onClick = { selectedSimSlot = 1 },
                            label = { Text("سیم‌کارت ۲") }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.createCampaign(
                            name = campaignName,
                            recipientsText = recipientsText,
                            templateBody = templateBody,
                            simSlot = selectedSimSlot
                        )
                        showCreateDialog = false
                        campaignName = ""
                        recipientsText = ""
                        templateBody = ""
                    },
                    modifier = Modifier.testTag("btn_submit_campaign")
                ) {
                    Text("ثبت و شروع ارسال")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("انصراف")
                }
            }
        )
    }
}

@Composable
fun AnalyticsStatBadge(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = color)
        Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun CampaignItemCard(
    campaign: Campaign,
    onExecute: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_campaign_${campaign.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = campaign.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                val (statusText, statusColor) = when (campaign.status) {
                    "COMPLETED" -> "تکمیل شده" to Color(0xFF00C853)
                    "RUNNING" -> "در حال ارسال" to Color(0xFF1A73E8)
                    else -> "در صف ارسال" to Color(0xFFFF9800)
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = statusText,
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = campaign.templateBody,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "تعداد گیرندگان: ${campaign.recipients.size} نفر | تحویل: ${campaign.deliveredCount}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (campaign.status == "QUEUED") {
                    Button(
                        onClick = onExecute,
                        modifier = Modifier.testTag("btn_run_campaign_${campaign.id}")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ارسال مستقیم", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
