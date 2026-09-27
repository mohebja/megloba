package com.global.sms.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.global.sms.core.util.PersianUtils
import com.global.sms.data.entity.ConversationEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationQuickActionsSheet(
    conversation: ConversationEntity,
    usePersianDigits: Boolean,
    onDismiss: () -> Unit,
    onOpenThread: () -> Unit,
    onSendQuickReply: (String) -> Unit,
    onPinToggle: () -> Unit,
    onHideToVault: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val contactInfo = rememberContactInfo(conversation.address)
    val displayName = contactInfo.name ?: conversation.contactName ?: conversation.address

    var replyText by remember { mutableStateOf("") }

    val quickPresets = listOf(
        "درود، پیام شما دریافت شد.",
        "در حال حاضر در جلسه هستم، بعداً تماس می‌گیرم.",
        "در حال رانندگی هستم.",
        "بله، مورد تایید است.",
        "بسیار عالی، سپاسگزارم.",
        "لطفاً با من تماس بگیرید."
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        tonalElevation = 8.dp,
        modifier = Modifier.testTag("conversation_quick_actions_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Header Info Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ContactAvatar(
                    photoUri = contactInfo.photoUri,
                    displayName = displayName,
                    size = 52.dp
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (usePersianDigits) PersianUtils.toPersianDigits(displayName) else displayName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (usePersianDigits) PersianUtils.toPersianDigits(conversation.address) else conversation.address,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onOpenThread) {
                    Icon(
                        Icons.Default.Chat,
                        contentDescription = "مشاهده کامل گفتگو",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Last message quote card
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (usePersianDigits) PersianUtils.toPersianDigits(conversation.lastMessage) else conversation.lastMessage,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(12.dp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons Row (Call, Pin, Vault, Delete)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                // Call
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    FilledTonalIconButton(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:${conversation.address}")
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "امکان تماس مستقیم وجود ندارد", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Icon(Icons.Default.Call, contentDescription = "تماس تلفنی", tint = MaterialTheme.colorScheme.primary)
                    }
                    Text("تماس", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                // Pin
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    FilledTonalIconButton(
                        onClick = {
                            onPinToggle()
                            onDismiss()
                        }
                    ) {
                        Icon(
                            Icons.Default.PushPin,
                            contentDescription = if (conversation.isPinned) "حذف سنجاق" else "سنجاق",
                            tint = if (conversation.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(if (conversation.isPinned) "برداشتن سنجاق" else "سنجاق", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                // Vault
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    FilledTonalIconButton(
                        onClick = {
                            onHideToVault()
                            onDismiss()
                            Toast.makeText(context, "گفتگو به گاوصندوق منتقل شد", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = "انتقال به گاوصندوق", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text("گاوصندوق", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                // Delete
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    FilledTonalIconButton(
                        onClick = {
                            onDelete()
                            onDismiss()
                            Toast.makeText(context, "گفتگو حذف شد", Toast.LENGTH_SHORT).show()
                        },
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                        )
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "حذف گفتگو", tint = MaterialTheme.colorScheme.error)
                    }
                    Text("حذف", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp))

            // Quick Reply Presets
            Text(
                text = "پاسخ سریع با یک لمس:",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(quickPresets) { preset ->
                    AssistChip(
                        onClick = { replyText = preset },
                        label = { Text(preset, fontSize = 11.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Reply Input Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = replyText,
                    onValueChange = { replyText = it },
                    placeholder = { Text("ارسال پاسخ سریع...") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_reply_input"),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    enabled = replyText.isNotBlank(),
                    onClick = {
                        onSendQuickReply(replyText.trim())
                        onDismiss()
                        Toast.makeText(context, "پاسخ سریع ارسال شد", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.testTag("btn_send_quick_reply")
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "ارسال",
                        tint = if (replyText.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
