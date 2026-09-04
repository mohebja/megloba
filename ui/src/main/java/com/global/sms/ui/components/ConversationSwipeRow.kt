package com.global.sms.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.MarkEmailUnread
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.global.sms.data.entity.ConversationEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationSwipeRow(
    conversation: ConversationEntity,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onSwipeRightToMarkReadUnread: () -> Unit,
    onSwipeLeftToArchive: () -> Unit,
    content: @Composable () -> Unit
) {
    val isUnread = conversation.unreadCount > 0

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    onSwipeRightToMarkReadUnread()
                    false
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    onSwipeLeftToArchive()
                    false
                }
                else -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val color by animateColorAsState(
                targetValue = when (direction) {
                    SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.primaryContainer
                    SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.tertiaryContainer
                    else -> Color.Transparent
                },
                label = "swipe_bg_color"
            )

            val alignment = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                else -> Alignment.Center
            }

            val icon = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> if (isUnread) Icons.Default.MarkEmailRead else Icons.Default.MarkEmailUnread
                SwipeToDismissBoxValue.EndToStart -> Icons.Default.Archive
                else -> null
            }

            val scale by animateFloatAsState(
                targetValue = if (dismissState.targetValue != SwipeToDismissBoxValue.Settled) 1.2f else 0.8f,
                label = "swipe_icon_scale"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color, shape = RoundedCornerShape(8.dp))
                    .padding(horizontal = 20.dp),
                contentAlignment = alignment
            ) {
                icon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        modifier = Modifier
                            .scale(scale)
                            .size(24.dp),
                        tint = when (direction) {
                            SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.onPrimaryContainer
                            SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.onTertiaryContainer
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .testTag("conversation_swipe_row_${conversation.threadId}")
    ) {
        content()
    }
}
