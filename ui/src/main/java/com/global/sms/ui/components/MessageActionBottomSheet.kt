package com.global.sms.ui.components

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.global.sms.data.entity.MessageEntity
import com.global.sms.ui.viewmodels.MessageActionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageActionBottomSheet(
    message: MessageEntity,
    selectedMessages: List<MessageEntity> = listOf(message),
    onDismiss: () -> Unit,
    onForward: (String) -> Unit,
    messageActionViewModel: MessageActionViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showDeleteConversationDialog by remember { mutableStateOf(false) }
    var showBlockConfirmDialog by remember { mutableStateOf(false) }
    var showReportSpamConfirmDialog by remember { mutableStateOf(false) }

    val isMultiSelect = selectedMessages.size > 1
    val combinedText = selectedMessages.joinToString("\n---\n") { it.body }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
                .testTag("message_action_bottom_sheet")
        ) {
            Text(
                text = if (isMultiSelect) "${selectedMessages.size} پیامک انتخاب شده" else "عملیات پیامک",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            HorizontalDivider()

            Spacer(modifier = Modifier.height(8.dp))

            // 1. Copy text
            MessageActionItem(
                icon = Icons.Default.ContentCopy,
                title = "کپی متن پیامک",
                testTag = "action_copy_text",
                onClick = {
                    messageActionViewModel.copyTextToClipboard(context, combinedText)
                    Toast.makeText(context, "متن کپی شد", Toast.LENGTH_SHORT).show()
                    onDismiss()
                }
            )

            // 2. Share message
            MessageActionItem(
                icon = Icons.Default.Share,
                title = "اشتراک‌گذاری پیامک",
                testTag = "action_share_message",
                onClick = {
                    messageActionViewModel.shareMessage(context, combinedText)
                    onDismiss()
                }
            )

            // 3. Forward message
            MessageActionItem(
                icon = Icons.AutoMirrored.Filled.Send,
                title = "ارسال مجدد (Forward)",
                testTag = "action_forward_message",
                onClick = {
                    onForward(combinedText)
                    onDismiss()
                }
            )

            // 3b. Bookmark / Pin message
            MessageActionItem(
                icon = Icons.Default.PushPin,
                title = "سنجاق کردن پیامک",
                testTag = "action_pin_message",
                onClick = {
                    messageActionViewModel.addBookmark(message.id, message.threadId)
                    Toast.makeText(context, "پیامک سنجاق شد", Toast.LENGTH_SHORT).show()
                    onDismiss()
                }
            )

            // 3c. AI Analyze message
            MessageActionItem(
                icon = Icons.Default.Psychology,
                title = "تحلیل هوشمند پیامک (AI Analyze)",
                testTag = "action_ai_analyze",
                onClick = {
                    val summary = com.global.sms.core.ai.brain.LocalAIBrain.summarizeMessage(message.body)
                    Toast.makeText(context, "تحلیل AI: $summary", Toast.LENGTH_LONG).show()
                    onDismiss()
                }
            )

            // 3c. Export message
            MessageActionItem(
                icon = Icons.Default.FileDownload,
                title = "خروجی فایل (TXT / PDF / Backup)",
                testTag = "action_export_message",
                onClick = {
                    messageActionViewModel.exportMessages(context, selectedMessages)
                    Toast.makeText(context, "فایل خروجی آماده شد", Toast.LENGTH_SHORT).show()
                    onDismiss()
                }
            )

            // 4. Add sender to contacts
            if (!isMultiSelect && message.address.isNotBlank()) {
                MessageActionItem(
                    icon = Icons.Default.PersonAdd,
                    title = "افزودن فرستنده به مخاطبین",
                    testTag = "action_add_contact",
                    onClick = {
                        messageActionViewModel.addSenderToContacts(context, message.address)
                        onDismiss()
                    }
                )
            }

            // 5. Hide message
            MessageActionItem(
                icon = Icons.Default.VisibilityOff,
                title = if (isMultiSelect) "مخفی کردن پیامک‌های انتخاب شده" else "مخفی کردن پیامک",
                testTag = "action_hide_message",
                onClick = {
                    messageActionViewModel.hideMessages(selectedMessages.map { it.id }) {
                        Toast.makeText(context, "پیامک به صندوق مخفی منتقل شد", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }
                }
            )

            // 6. Archive conversation
            MessageActionItem(
                icon = Icons.Default.Archive,
                title = "آرشیو کردن گفتگو",
                testTag = "action_archive_conversation",
                onClick = {
                    messageActionViewModel.archiveConversation(message.threadId) {
                        Toast.makeText(context, "گفتگو آرشیو شد", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }
                }
            )

            // 7. Block sender
            MessageActionItem(
                icon = Icons.Default.Block,
                title = "مسدودسازی فرستنده (${message.address})",
                testTag = "action_block_sender",
                tint = MaterialTheme.colorScheme.error,
                onClick = {
                    showBlockConfirmDialog = true
                }
            )

            // 8. Report spam
            MessageActionItem(
                icon = Icons.Default.Report,
                title = "گزارش پیامک اسپم / تبلیغاتی",
                testTag = "action_report_spam",
                tint = MaterialTheme.colorScheme.tertiary,
                onClick = {
                    showReportSpamConfirmDialog = true
                }
            )

            // 9. Delete message
            MessageActionItem(
                icon = Icons.Default.Delete,
                title = if (isMultiSelect) "حذف ${selectedMessages.size} پیامک" else "حذف پیامک",
                testTag = "action_delete_message",
                tint = MaterialTheme.colorScheme.error,
                onClick = {
                    showDeleteConfirmDialog = true
                }
            )

            // 10. Delete conversation
            MessageActionItem(
                icon = Icons.Default.DeleteForever,
                title = "حذف کل گفتگو",
                testTag = "action_delete_conversation",
                tint = MaterialTheme.colorScheme.error,
                onClick = {
                    showDeleteConversationDialog = true
                }
            )
        }
    }

    // Confirmation Dialogs
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("حذف پیامک") },
            text = { Text("آیا از حذف ${if (isMultiSelect) "${selectedMessages.size} پیامک" else "این پیامک"} اطمینان دارید؟") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirmDialog = false
                    messageActionViewModel.deleteMessages(selectedMessages.map { it.id }) {
                        Toast.makeText(context, "پیامک حذف شد", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }
                }) {
                    Text("حذف", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("انصراف")
                }
            }
        )
    }

    if (showDeleteConversationDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConversationDialog = false },
            title = { Text("حذف گفتگو") },
            text = { Text("آیا از حذف کامل این گفتگو و تمامی پیامک‌های آن اطمینان دارید؟") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConversationDialog = false
                    messageActionViewModel.deleteConversation(message.threadId) {
                        Toast.makeText(context, "گفتگو حذف شد", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }
                }) {
                    Text("حذف کامل", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConversationDialog = false }) {
                    Text("انصراف")
                }
            }
        )
    }

    if (showBlockConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showBlockConfirmDialog = false },
            title = { Text("مسدودسازی فرستنده") },
            text = { Text("آیا می‌خواهید شماره ${message.address} مسدود شود و پیامک‌های جدید آن در لیست اسپم قرار گیرد؟") },
            confirmButton = {
                TextButton(onClick = {
                    showBlockConfirmDialog = false
                    messageActionViewModel.blockSender(message.address) {
                        Toast.makeText(context, "فرستنده مسدود شد", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }
                }) {
                    Text("مسدودسازی", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBlockConfirmDialog = false }) {
                    Text("انصراف")
                }
            }
        )
    }

    if (showReportSpamConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showReportSpamConfirmDialog = false },
            title = { Text("گزارش اسپم") },
            text = { Text("آیا پیامک ${message.address} به عنوان پیامک مزاحم / اسپم علامت‌گذاری شود؟") },
            confirmButton = {
                TextButton(onClick = {
                    showReportSpamConfirmDialog = false
                    messageActionViewModel.reportSpam(message.address, message.body, message.id) {
                        Toast.makeText(context, "گزارش اسپم ثبت شد", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }
                }) {
                    Text("ثبت گزارش")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportSpamConfirmDialog = false }) {
                    Text("انصراف")
                }
            }
        )
    }
}

@Composable
private fun MessageActionItem(
    icon: ImageVector,
    title: String,
    testTag: String,
    tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = title, fontSize = 15.sp, color = tint, fontWeight = FontWeight.Medium)
    }
}
