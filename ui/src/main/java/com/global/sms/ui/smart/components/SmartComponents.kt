package com.global.sms.ui.smart.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.global.sms.data.entity.MessageCategory

data class AiDashboardStats(
    val unreadCount: Int = 0,
    val importantCount: Int = 0,
    val bankTxCount: Int = 0,
    val otpCount: Int = 0,
    val spamCount: Int = 0
)

@Composable
fun AiDashboardCard(
    stats: AiDashboardStats,
    onCategoryClick: (MessageCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + slideInVertically()
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "داشبورد هوش مصنوعی",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "داشبورد هوشمند پیامک‌ها (AI Dashboard)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        DashboardMetricBadge(
                            title = "خوانده‌نشده",
                            value = "${stats.unreadCount}",
                            icon = Icons.Default.MarkChatUnread,
                            badgeColor = MaterialTheme.colorScheme.primary,
                            onClick = {}
                        )
                    }
                    item {
                        DashboardMetricBadge(
                            title = "کد ورود (OTP)",
                            value = "${stats.otpCount}",
                            icon = Icons.Default.Key,
                            badgeColor = MaterialTheme.colorScheme.tertiary,
                            onClick = { onCategoryClick(MessageCategory.OTP) }
                        )
                    }
                    item {
                        DashboardMetricBadge(
                            title = "تراکنش بانکی",
                            value = "${stats.bankTxCount}",
                            icon = Icons.Default.AccountBalance,
                            badgeColor = Color(0xFF2E7D32),
                            onClick = { onCategoryClick(MessageCategory.BANK) }
                        )
                    }
                    item {
                        DashboardMetricBadge(
                            title = "مهم و ضروری",
                            value = "${stats.importantCount}",
                            icon = Icons.Default.NotificationImportant,
                            badgeColor = Color(0xFFD84315),
                            onClick = { onCategoryClick(MessageCategory.IMPORTANT) }
                        )
                    }
                    item {
                        DashboardMetricBadge(
                            title = "اسپم و مشکوک",
                            value = "${stats.spamCount}",
                            icon = Icons.Default.Shield,
                            badgeColor = MaterialTheme.colorScheme.error,
                            onClick = { onCategoryClick(MessageCategory.SPAM) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardMetricBadge(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    badgeColor: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = badgeColor.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = badgeColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = badgeColor
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SmartCategoryChipRow(
    selectedCategory: MessageCategory?,
    onSelectCategory: (MessageCategory?) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf(
        null to "همه پیام‌ها",
        MessageCategory.PERSONAL to "شخصی",
        MessageCategory.OTP to "رمز و OTP",
        MessageCategory.BANK to "تراکنش بانکی",
        MessageCategory.IMPORTANT to "مهم و ضروری",
        MessageCategory.SPAM to "اسپم و تبلیغات"
    )

    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { (cat, label) ->
            FilterChip(
                selected = selectedCategory == cat,
                onClick = { onSelectCategory(cat) },
                label = { Text(label, fontSize = 13.sp) },
                leadingIcon = if (cat == MessageCategory.OTP) {
                    { Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp)) }
                } else if (cat == MessageCategory.SPAM) {
                    { Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(16.dp)) }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}

@Composable
fun AiSummaryCard(
    summaryText: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "خلاصه هوشمند AI",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "خلاصه هوشمند گفتگو",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = summaryText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
fun SmartReplyChipRow(
    replies: List<String>,
    onSelectReply: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (replies.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "پاسخ‌های هوشمند پیشنهادی:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(replies) { reply ->
                SuggestionChip(
                    onClick = { onSelectReply(reply) },
                    label = { Text(reply, fontSize = 12.sp) },
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }
    }
}

@Composable
fun OtpQuickCopyBanner(
    otpCode: String,
    onCopy: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Key,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "کد تایید شناسایی شده: $otpCode",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }

            Button(
                onClick = onCopy,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = "کپی", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("کپی کد", fontSize = 12.sp)
            }
        }
    }
}
