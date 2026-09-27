package com.global.sms.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.global.sms.core.security.PhishingScanResult
import com.global.sms.core.security.ThreatLevel

@Composable
fun PhishingWarningCard(
    scanResult: PhishingScanResult,
    onBlockSender: () -> Unit,
    onReportSpam: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCritical = scanResult.threatLevel == ThreatLevel.CRITICAL || scanResult.threatLevel == ThreatLevel.HIGH
    val containerColor = if (isCritical) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)
    else MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.25f)

    val contentColor = if (isCritical) MaterialTheme.colorScheme.error
    else MaterialTheme.colorScheme.tertiary

    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .testTag("phishing_warning_card"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, contentColor.copy(alpha = 0.7f))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isCritical) Icons.Default.Warning else Icons.Default.Security,
                        contentDescription = "هشدار امنیتی",
                        tint = contentColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isCritical) "هشدار امنیتی: فیشینگ و کلاهبرداری احتمالی!" else "هشدارهای امنیتی لینک",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = contentColor
                    )
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = contentColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = when (scanResult.threatLevel) {
                            ThreatLevel.CRITICAL -> "خطر بالا"
                            ThreatLevel.HIGH -> "مشکوک"
                            ThreatLevel.MEDIUM -> "متوسط"
                            ThreatLevel.LOW -> "عادی"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = contentColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            scanResult.warningReason?.let { reason ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = reason,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (scanResult.detectedUrls.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "لینک‌های شناسایی شده:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                scanResult.detectedUrls.forEach { url ->
                    Text(
                        text = "• $url",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.outline,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onReportSpam,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(Icons.Default.ReportProblem, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("گزارش هرزنامه", fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onBlockSender,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = contentColor),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("مسدودسازی فرستنده", fontSize = 11.sp)
                }
            }
        }
    }
}
