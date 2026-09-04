package com.global.sms.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.global.sms.core.util.PersianUtils
import com.global.sms.ui.viewmodels.GlobalSmsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduledMessagesScreen(
    viewModel: GlobalSmsViewModel,
    onBack: () -> Unit
) {
    val scheduledMessages by viewModel.scheduledMessages.collectAsStateWithLifecycle()
    val usePersianDigits by viewModel.usePersianDigits.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "پیامک‌های زمان‌بندی شده",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.testTag("scheduled_messages_title")
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("scheduled_back_button")
                        ) {
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
                    modifier = Modifier.testTag("add_scheduled_message_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "افزودن زمان‌بندی")
                }
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                if (scheduledMessages.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp).padding(bottom = 16.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                text = "هیچ پیام زمان‌بندی شده‌ای ثبت نشده است",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "برای تنظیم ارسال خودکار پیامک در زمان مشخص، دکمه + را لمس کنید.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                            .testTag("scheduled_messages_list"),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(scheduledMessages, key = { it.id }) { msg ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("scheduled_card_${msg.id}"),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "گیرنده: ${if (usePersianDigits) PersianUtils.toPersianDigits(msg.address) else msg.address}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = if (usePersianDigits) PersianUtils.toPersianDigits(msg.body) else msg.body,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Alarm,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "زمان ارسال: ${PersianUtils.formatTimestamp(msg.scheduledTimestamp)}",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = {
                                            viewModel.cancelScheduledMessage(msg.id)
                                            Toast.makeText(context, "ارسال زمان‌بندی شده لغو گردید", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.testTag("delete_scheduled_button_${msg.id}")
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "حذف زمان‌بندی",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showAddDialog) {
            AddScheduledMessageDialog(
                onDismiss = { showAddDialog = false },
                onSchedule = { address, body, delayMillis, simSlot ->
                    val scheduledTime = System.currentTimeMillis() + delayMillis
                    viewModel.scheduleMessage(address, body, scheduledTime, simSlot)
                    showAddDialog = false
                    Toast.makeText(context, "پیامک برای زمان مشخص زمان‌بندی شد", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

@Composable
fun AddScheduledMessageDialog(
    onDismiss: () -> Unit,
    onSchedule: (address: String, body: String, delayMillis: Long, simSlot: Int) -> Unit
) {
    var address by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var selectedDelayMinutes by remember { mutableLongStateOf(15L) }
    var selectedSim by remember { mutableIntStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تنظیم پیامک زمان‌بندی شده") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("شماره گیرنده") },
                    placeholder = { Text("مثلاً 09123456789") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("schedule_address_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    label = { Text("متن پیامک") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("schedule_body_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("زمان ارسال بعد از:", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        5L to "۵ دقیقه",
                        15L to "۱۵ دقیقه",
                        60L to "۱ ساعت",
                        1440L to "۱ روز"
                    ).forEach { (minutes, label) ->
                        FilterChip(
                            selected = selectedDelayMinutes == minutes,
                            onClick = { selectedDelayMinutes = minutes },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.SimCard,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("سیم‌کارت ارسال:", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = selectedSim == 0,
                        onClick = { selectedSim = 0 },
                        label = { Text("سیم ۱", fontSize = 11.sp) }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    FilterChip(
                        selected = selectedSim == 1,
                        onClick = { selectedSim = 1 },
                        label = { Text("سیم ۲", fontSize = 11.sp) }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (address.isNotBlank() && body.isNotBlank()) {
                        onSchedule(address.trim(), body.trim(), selectedDelayMinutes * 60 * 1000L, selectedSim)
                    }
                },
                enabled = address.isNotBlank() && body.isNotBlank(),
                modifier = Modifier.testTag("confirm_schedule_button")
            ) {
                Text("ثبت و زمان‌بندی")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_schedule_button")
            ) {
                Text("انصراف")
            }
        }
    )
}
