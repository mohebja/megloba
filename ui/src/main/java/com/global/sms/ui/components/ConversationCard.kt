package com.global.sms.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.global.sms.core.util.PersianUtils
import com.global.sms.data.entity.ConversationEntity
import com.global.sms.data.entity.MessageCategory
import com.global.sms.ui.theme.BankCategoryColor
import com.global.sms.ui.theme.ImportantCategoryColor
import com.global.sms.ui.theme.PrivateCategoryColor
import com.global.sms.ui.theme.SpamCategoryColor
import com.global.sms.ui.theme.WorkCategoryColor

@Composable
fun ConversationCard(
    conversation: ConversationEntity,
    style: String = "MODERN",
    usePersianDigits: Boolean,
    usePersianCalendar: Boolean,
    onClick: () -> Unit,
    onPinToggle: () -> Unit,
    onHideToVault: () -> Unit,
    onDelete: () -> Unit
) {
    val resolved = rememberContactInfo(conversation.address)
    val displayName = resolved.name ?: conversation.contactName ?: conversation.address

    val categoryColor = when (conversation.category) {
        MessageCategory.BANK -> BankCategoryColor
        MessageCategory.SPAM -> SpamCategoryColor
        MessageCategory.PRIVATE -> PrivateCategoryColor
        MessageCategory.WORK -> WorkCategoryColor
        MessageCategory.IMPORTANT -> ImportantCategoryColor
        else -> MaterialTheme.colorScheme.primary
    }

    when (style) {
        "CLASSIC" -> {
            // UI STYLE 1: Classic SMS Layout (Compact, rectangular borders, traditional SMS list)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 2.dp)
                    .clickable { onClick() }
                    .testTag("conversation_card_${conversation.threadId}"),
                colors = CardDefaults.cardColors(
                    containerColor = if (conversation.isPinned) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ContactAvatar(
                        photoUri = resolved.photoUri,
                        displayName = displayName,
                        size = 36.dp
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (usePersianDigits) PersianUtils.toPersianDigits(displayName) else displayName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = PersianUtils.formatTimestamp(conversation.lastTimestamp, usePersianCalendar, usePersianDigits),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = if (usePersianDigits) PersianUtils.toPersianDigits(conversation.lastMessage) else conversation.lastMessage,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (conversation.unreadCount > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Badge(containerColor = MaterialTheme.colorScheme.error) {
                            Text(if (usePersianDigits) PersianUtils.toPersianDigits(conversation.unreadCount.toString()) else conversation.unreadCount.toString())
                        }
                    }
                }
            }
        }

        "ENTERPRISE" -> {
            // UI STYLE 3: Professional Enterprise Style (Info-rich, visible category chips, status indicators)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .clickable { onClick() }
                    .testTag("conversation_card_${conversation.threadId}"),
                colors = CardDefaults.cardColors(
                    containerColor = if (conversation.isPinned) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.5.dp, categoryColor.copy(alpha = 0.6f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            ContactAvatar(
                                photoUri = resolved.photoUri,
                                displayName = displayName,
                                size = 40.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (usePersianDigits) PersianUtils.toPersianDigits(displayName) else displayName,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Category Tag Badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = categoryColor.copy(alpha = 0.15f),
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            Text(
                                text = when (conversation.category) {
                                    MessageCategory.BANK -> "بانکی"
                                    MessageCategory.OTP -> "کد تایید"
                                    MessageCategory.TRANSACTIONS -> "تراکنش"
                                    MessageCategory.SPAM -> "اسپم"
                                    MessageCategory.WORK -> "کاری"
                                    MessageCategory.IMPORTANT -> "مهم"
                                    else -> "شخصی"
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = categoryColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (usePersianDigits) PersianUtils.toPersianDigits(conversation.lastMessage) else conversation.lastMessage,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        Text(
                            text = PersianUtils.formatTimestamp(conversation.lastTimestamp, usePersianCalendar, usePersianDigits),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (conversation.unreadCount > 0 || conversation.isPinned) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (conversation.unreadCount > 0) {
                                Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                    Text("${conversation.unreadCount} پیام خوانده‌نشده")
                                }
                            }
                            if (conversation.isPinned) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.Default.PushPin, contentDescription = "Sanjagh", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }

        else -> {
            // UI STYLE 2: Modern Google Messages Inspired (Default)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .clickable { onClick() }
                    .testTag("conversation_card_${conversation.threadId}"),
                colors = CardDefaults.cardColors(
                    containerColor = if (conversation.isPinned) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ContactAvatar(
                        photoUri = resolved.photoUri,
                        displayName = displayName,
                        size = 48.dp
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (usePersianDigits) PersianUtils.toPersianDigits(displayName) else displayName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = PersianUtils.formatTimestamp(conversation.lastTimestamp, usePersianCalendar, usePersianDigits),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (usePersianDigits) PersianUtils.toPersianDigits(conversation.lastMessage) else conversation.lastMessage,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )

                            if (conversation.unreadCount > 0) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                    Text(if (usePersianDigits) PersianUtils.toPersianDigits(conversation.unreadCount.toString()) else conversation.unreadCount.toString())
                                }
                            }

                            if (conversation.isPinned) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(Icons.Default.PushPin, contentDescription = "Sanjagh", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}
