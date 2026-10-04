package com.global.sms.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.global.sms.core.financial.DigitalPaymentReceipt

/**
 * Digital Receipt Modal Dialog.
 * Displays a verified, beautifully formatted Iranian banking receipt with one-tap
 * copy and system share functionality.
 */
@Composable
fun DigitalReceiptDialog(
    receipt: DigitalPaymentReceipt,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        androidx.compose.runtime.CompositionLocalProvider(
            LocalLayoutDirection provides LayoutDirection.Rtl
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(24.dp))
                    .testTag("digital_receipt_dialog"),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Icon & Status
                    val badgeColor = if (receipt.isCredit) Color(0xFF2E7D32) else Color(0xFFC62828)
                    val badgeBg = if (receipt.isCredit) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)

                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(badgeBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (receipt.isCredit) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                            contentDescription = null,
                            tint = badgeColor,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = receipt.bankName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = receipt.transactionTypeTitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = badgeColor,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Amount Display
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = receipt.formattedAmountTomans,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "معادل: ${receipt.formattedAmountRials}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Details Table
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val cardNum = receipt.maskedCardNumber
                        if (cardNum != null) {
                            ReceiptRow(label = "شماره کارت / حساب", value = cardNum)
                        }
                        val bal = receipt.formattedBalanceTomans
                        if (bal != null) {
                            ReceiptRow(label = "مانده موجودی", value = bal)
                        }
                        ReceiptRow(label = "شماره پیگیری", value = receipt.trackingNumber)
                        ReceiptRow(label = "زمان تراکنش", value = receipt.formattedDatePersian)
                        ReceiptRow(label = "کد اعتبارسنجی", value = receipt.verificationHash.take(12), isMonospace = true)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("رسید بانکی", receipt.shareableText)
                                cm.setPrimaryClip(clip)
                                Toast.makeText(context, "متن رسید کپی شد", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_copy_receipt")
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("کپی متن")
                        }

                        Button(
                            onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "رسید بانکی ${receipt.bankName}")
                                    putExtra(Intent.EXTRA_TEXT, receipt.shareableText)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "اشتراک‌گذاری رسید"))
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_share_receipt")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("اشتراک")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_close_receipt")
                    ) {
                        Text("بستن")
                    }
                }
            }
        }
    }
}

@Composable
private fun ReceiptRow(
    label: String,
    value: String,
    isMonospace: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            fontFamily = if (isMonospace) FontFamily.Monospace else FontFamily.Default,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
