package com.global.sms.ui.screens

import com.global.sms.ui.viewmodels.GlobalSmsViewModel

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.global.sms.core.util.PersianUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageStatsScreen(
    viewModel: GlobalSmsViewModel,
    onBack: () -> Unit
) {
    val totalCount by viewModel.totalCount.collectAsStateWithLifecycle()
    val sentCount by viewModel.sentCount.collectAsStateWithLifecycle()
    val receivedCount by viewModel.receivedCount.collectAsStateWithLifecycle()
    val spamCount by viewModel.spamCount.collectAsStateWithLifecycle()
    val bankCount by viewModel.bankCount.collectAsStateWithLifecycle()
    val usePersianDigits by viewModel.usePersianDigits.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.testTag("message_stats_screen"),
        topBar = {
            TopAppBar(
                title = { Text("آمار و تحلیل پیامک‌ها", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("message_stats_back_button")
                    ) {
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
                .padding(16.dp)
        ) {
            Text(
                text = "خلاصه آمار عملکرد پیام‌رسان",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    StatCard(
                        title = "کل پیامک‌ها",
                        count = totalCount,
                        icon = Icons.AutoMirrored.Filled.Message,
                        usePersianDigits = usePersianDigits
                    )
                }
                item {
                    StatCard(
                        title = "ارسال شده",
                        count = sentCount,
                        icon = Icons.AutoMirrored.Filled.CallMade,
                        usePersianDigits = usePersianDigits
                    )
                }
                item {
                    StatCard(
                        title = "دریافت شده",
                        count = receivedCount,
                        icon = Icons.AutoMirrored.Filled.CallReceived,
                        usePersianDigits = usePersianDigits
                    )
                }
                item {
                    StatCard(
                        title = "پیامک‌های بانکی",
                        count = bankCount,
                        icon = Icons.Default.AccountBalance,
                        usePersianDigits = usePersianDigits
                    )
                }
                item {
                    StatCard(
                        title = "اسپم و آگهی",
                        count = spamCount,
                        icon = Icons.Default.Security,
                        usePersianDigits = usePersianDigits
                    )
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    count: Int,
    icon: ImageVector,
    usePersianDigits: Boolean
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(text = title, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (usePersianDigits) PersianUtils.toPersianDigits(count.toString()) else count.toString(),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
