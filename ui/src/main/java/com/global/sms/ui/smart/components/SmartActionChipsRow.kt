package com.global.sms.ui.smart.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.global.sms.core.haptic.HapticFeedbackManager
import com.global.sms.core.smart.SmartAction

@Composable
fun SmartActionChipsRow(
    actions: List<SmartAction>,
    modifier: Modifier = Modifier
) {
    if (actions.isEmpty()) return

    val context = LocalContext.current

    Row(
        modifier = modifier.padding(top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        actions.forEach { action ->
            when (action) {
                is SmartAction.CopyOtp -> {
                    AssistChip(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("OTP Code", action.code))
                            HapticFeedbackManager.vibrateOtpCopied(context)
                            Toast.makeText(context, "کد تایید کپی شد: ${action.code}", Toast.LENGTH_SHORT).show()
                        },
                        label = { Text("کپی کد: ${action.code}", fontSize = 11.sp) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.ContentCopy,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                            labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.testTag("action_chip_copy_otp")
                    )
                }

                is SmartAction.PayBill -> {
                    AssistChip(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val billInfo = "شناسه قبض: ${action.billId}\nشناسه پرداخت: ${action.paymentId}"
                            clipboard.setPrimaryClip(ClipData.newPlainText("Bill IDs", billInfo))
                            HapticFeedbackManager.vibrateClick(context)
                            Toast.makeText(context, "شناسه‌های قبض کپی شد", Toast.LENGTH_SHORT).show()
                        },
                        label = { Text("پرداخت قبض", fontSize = 11.sp) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Payment,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                            labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        modifier = Modifier.testTag("action_chip_pay_bill")
                    )
                }

                is SmartAction.TrackDelivery -> {
                    AssistChip(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Tracking Number", action.trackingNumber))
                            HapticFeedbackManager.vibrateClick(context)
                            Toast.makeText(context, "کد رهگیری کپی شد", Toast.LENGTH_SHORT).show()
                        },
                        label = { Text("پیگیری مرسوله", fontSize = 11.sp) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.LocalShipping,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                        },
                        modifier = Modifier.testTag("action_chip_tracking")
                    )
                }

                is SmartAction.CalendarReminder -> {
                    AssistChip(
                        onClick = {
                            HapticFeedbackManager.vibrateClick(context)
                            Toast.makeText(context, "یادآوری: ${action.dateOrTime}", Toast.LENGTH_SHORT).show()
                        },
                        label = { Text("تقویم / یادآور", fontSize = 11.sp) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                        },
                        modifier = Modifier.testTag("action_chip_calendar")
                    )
                }
            }
        }
    }
}
