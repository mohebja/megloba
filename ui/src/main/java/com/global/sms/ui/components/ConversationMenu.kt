package com.global.sms.ui.components

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.global.sms.data.entity.ConversationEntity
import com.global.sms.data.entity.MessageCategory
import com.global.sms.ui.viewmodels.GlobalSmsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationMenuBottomSheet(
    conversation: ConversationEntity,
    viewModel: GlobalSmsViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var showBlockDialog by remember { mutableStateOf(false) }

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
                .testTag("conversation_menu_bottom_sheet")
        ) {
            Text(
                text = conversation.contactName ?: conversation.address,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            HorizontalDivider()

            Spacer(modifier = Modifier.height(8.dp))

            // 1. Mark Read / Unread
            val isUnread = conversation.unreadCount > 0
            ConversationMenuItem(
                icon = if (isUnread) Icons.Default.MarkEmailRead else Icons.Default.MarkEmailUnread,
                title = if (isUnread) "علامت‌گذاری به عنوان خوانده شده" else "علامت‌گذاری به عنوان خوانده نشده",
                testTag = "action_mark_read_unread",
                onClick = {
                    viewModel.toggleReadUnread(conversation)
                    Toast.makeText(context, if (isUnread) "خوانده شد" else "خوانده نشده شد", Toast.LENGTH_SHORT).show()
                    onDismiss()
                }
            )

            // 2. Pin / Unpin
            ConversationMenuItem(
                icon = if (conversation.isPinned) Icons.Default.PushPin else Icons.Default.PushPin,
                title = if (conversation.isPinned) "برداشتن پین" else "پین کردن گفتگو",
                testTag = "action_pin_conversation",
                onClick = {
                    viewModel.togglePinConversation(conversation.threadId, conversation.isPinned)
                    onDismiss()
                }
            )

            // 3. Archive Conversation
            ConversationMenuItem(
                icon = Icons.Default.Archive,
                title = if (conversation.isArchived) "خروج از آرشیو" else "آرشیو کردن گفتگو",
                testTag = "action_archive_conversation",
                onClick = {
                    viewModel.archiveConversation(conversation.threadId, !conversation.isArchived)
                    Toast.makeText(context, if (conversation.isArchived) "از آرشیو خارج شد" else "آرشیو شد", Toast.LENGTH_SHORT).show()
                    onDismiss()
                }
            )

            // 4. Hide Conversation
            ConversationMenuItem(
                icon = Icons.Default.VisibilityOff,
                title = "انتقال به صندوق مخفی (Private Vault)",
                testTag = "action_hide_conversation",
                onClick = {
                    viewModel.hideConversation(conversation.threadId, true)
                    Toast.makeText(context, "گفتگو مخفی شد", Toast.LENGTH_SHORT).show()
                    onDismiss()
                }
            )

            // 5. Add / Change Category
            ConversationMenuItem(
                icon = Icons.Default.Category,
                title = "دسته‌بندی گفتگو (دسته: ${conversation.category.name})",
                testTag = "action_set_category",
                onClick = {
                    showCategoryDialog = true
                }
            )

            // 6. Block Sender
            ConversationMenuItem(
                icon = Icons.Default.Block,
                title = "مسدودسازی شماره",
                testTag = "action_block_conversation",
                tint = MaterialTheme.colorScheme.error,
                onClick = {
                    showBlockDialog = true
                }
            )

            // 7. Delete Conversation
            ConversationMenuItem(
                icon = Icons.Default.Delete,
                title = "حذف کامل گفتگو",
                testTag = "action_delete_conversation",
                tint = MaterialTheme.colorScheme.error,
                onClick = {
                    showDeleteDialog = true
                }
            )
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("حذف گفتگو") },
            text = { Text("آیا از حذف این گفتگو اطمینان دارید؟") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteConversation(conversation.threadId)
                    Toast.makeText(context, "گفتگو حذف شد", Toast.LENGTH_SHORT).show()
                    onDismiss()
                }) {
                    Text("حذف", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("انصراف") }
            }
        )
    }

    if (showBlockDialog) {
        AlertDialog(
            onDismissRequest = { showBlockDialog = false },
            title = { Text("مسدودسازی فرستنده") },
            text = { Text("آیا می‌خواهید فرستنده ${conversation.address} مسدود شود؟") },
            confirmButton = {
                TextButton(onClick = {
                    showBlockDialog = false
                    viewModel.blockSender(conversation.address)
                    Toast.makeText(context, "فرستنده مسدود شد", Toast.LENGTH_SHORT).show()
                    onDismiss()
                }) {
                    Text("مسدودسازی", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBlockDialog = false }) { Text("انصراف") }
            }
        )
    }

    if (showCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showCategoryDialog = false },
            title = { Text("انتخاب دسته‌بندی") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    MessageCategory.values().forEach { cat ->
                        TextButton(
                            onClick = {
                                viewModel.setConversationCategory(conversation.threadId, cat)
                                showCategoryDialog = false
                                Toast.makeText(context, "دسته‌بندی به ${cat.name} تغییر یافت", Toast.LENGTH_SHORT).show()
                                onDismiss()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(cat.name, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCategoryDialog = false }) { Text("انصراف") }
            }
        )
    }
}

@Composable
private fun ConversationMenuItem(
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
