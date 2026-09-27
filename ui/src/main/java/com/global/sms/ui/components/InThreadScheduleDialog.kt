package com.global.sms.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.global.sms.core.util.PersianUtils
import java.util.Calendar
import java.util.concurrent.TimeUnit

enum class SchedulePreset(val label: String, val delayMillis: Long) {
    MINUTES_10("۱۰ دقیقه دیگر", TimeUnit.MINUTES.toMillis(10)),
    MINUTES_30("۳۰ دقیقه دیگر", TimeUnit.MINUTES.toMillis(30)),
    HOUR_1("۱ ساعت دیگر", TimeUnit.HOURS.toMillis(1)),
    TONIGHT_9PM("امشب ساعت ۲۱", calculateTonight9PMMillis()),
    TOMORROW_9AM("فردا صبح ساعت ۹", calculateTomorrow9AMMillis())
}

private fun calculateTonight9PMMillis(): Long {
    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, 21)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    if (cal.timeInMillis <= System.currentTimeMillis()) {
        cal.add(Calendar.DAY_OF_YEAR, 1)
    }
    return cal.timeInMillis - System.currentTimeMillis()
}

private fun calculateTomorrow9AMMillis(): Long {
    val cal = Calendar.getInstance()
    cal.add(Calendar.DAY_OF_YEAR, 1)
    cal.set(Calendar.HOUR_OF_DAY, 9)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    return cal.timeInMillis - System.currentTimeMillis()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InThreadScheduleDialog(
    recipientAddress: String,
    messageDraft: String,
    selectedSimSlot: Int,
    usePersianDigits: Boolean,
    usePersianCalendar: Boolean,
    onDismiss: () -> Unit,
    onConfirmSchedule: (scheduledTimestamp: Long, simSlot: Int) -> Unit
) {
    var selectedPreset by remember { mutableStateOf<SchedulePreset?>(SchedulePreset.MINUTES_30) }
    var currentSimSlot by remember { mutableIntStateOf(selectedSimSlot) }
    var customDelayMinutes by remember { mutableFloatStateOf(60f) }
    var isCustomMode by remember { mutableStateOf(false) }

    val targetTimestamp = remember(selectedPreset, isCustomMode, customDelayMinutes) {
        val now = System.currentTimeMillis()
        if (isCustomMode) {
            now + (customDelayMinutes.toLong() * 60 * 1000L)
        } else {
            now + (selectedPreset?.delayMillis ?: TimeUnit.MINUTES.toMillis(30))
        }
    }

    val formattedTargetTime = PersianUtils.formatTimestamp(
        targetTimestamp,
        usePersianCalendar = usePersianCalendar,
        usePersianDigits = usePersianDigits
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .padding(16.dp)
                .testTag("in_thread_schedule_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "زمان‌بندی ارسال پیامک",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "ارسال خودکار در ساعت تعیین‌شده",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "بستن")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // Preview Box
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "زمان ارسال: $formattedTargetTime",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "گیرنده: ${if (usePersianDigits) PersianUtils.toPersianDigits(recipientAddress) else recipientAddress}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "انتخاب بازه زمانی پیشنهادی:",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Presets Flow/Chips
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = !isCustomMode && selectedPreset == SchedulePreset.MINUTES_10,
                            onClick = { isCustomMode = false; selectedPreset = SchedulePreset.MINUTES_10 },
                            label = { Text(SchedulePreset.MINUTES_10.label, fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = !isCustomMode && selectedPreset == SchedulePreset.MINUTES_30,
                            onClick = { isCustomMode = false; selectedPreset = SchedulePreset.MINUTES_30 },
                            label = { Text(SchedulePreset.MINUTES_30.label, fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = !isCustomMode && selectedPreset == SchedulePreset.HOUR_1,
                            onClick = { isCustomMode = false; selectedPreset = SchedulePreset.HOUR_1 },
                            label = { Text(SchedulePreset.HOUR_1.label, fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = !isCustomMode && selectedPreset == SchedulePreset.TONIGHT_9PM,
                            onClick = { isCustomMode = false; selectedPreset = SchedulePreset.TONIGHT_9PM },
                            label = { Text(SchedulePreset.TONIGHT_9PM.label, fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = !isCustomMode && selectedPreset == SchedulePreset.TOMORROW_9AM,
                            onClick = { isCustomMode = false; selectedPreset = SchedulePreset.TOMORROW_9AM },
                            label = { Text(SchedulePreset.TOMORROW_9AM.label, fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = isCustomMode,
                            onClick = { isCustomMode = true },
                            label = { Text("زمان دلخواه", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                if (isCustomMode) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "تاخیر: ${customDelayMinutes.toInt()} دقیقه بعد",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Slider(
                        value = customDelayMinutes,
                        onValueChange = { customDelayMinutes = it },
                        valueRange = 5f..720f,
                        steps = 23
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // SIM Selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("سیم‌کارت ارسال:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = currentSimSlot == 0,
                            onClick = { currentSimSlot = 0 },
                            label = { Text("سیم ۱") }
                        )
                        FilterChip(
                            selected = currentSimSlot == 1,
                            onClick = { currentSimSlot = 1 },
                            label = { Text("سیم ۲") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("انصراف")
                    }

                    Button(
                        onClick = {
                            onConfirmSchedule(targetTimestamp, currentSimSlot)
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("confirm_schedule_btn"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Alarm, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ثبت زمان‌بندی", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
