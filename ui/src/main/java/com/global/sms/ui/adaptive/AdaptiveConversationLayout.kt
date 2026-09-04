package com.global.sms.ui.adaptive

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.global.sms.data.entity.MessageEntity
import com.global.sms.ui.smart.screens.SmartConversationsScreen
import com.global.sms.ui.viewmodels.GlobalSmsViewModel

enum class ScreenWindowType {
    COMPACT,    // Mobile portrait (< 600dp width)
    MEDIUM,     // Foldables unfolded / portrait tablets (600dp - 840dp)
    EXPANDED    // Large tablets / Chromebooks (> 840dp)
}

@Composable
fun rememberWindowType(maxWidth: Int): ScreenWindowType {
    return when {
        maxWidth < 600 -> ScreenWindowType.COMPACT
        maxWidth in 600..840 -> ScreenWindowType.MEDIUM
        else -> ScreenWindowType.EXPANDED
    }
}

/**
 * Responsive Adaptive Layout for Global SMS.
 * Automatically switches between Single-Pane (Compact Phones) and
 * Two-Pane List-Detail Split View (Foldables, Tablets, ChromeOS).
 */
@Composable
fun AdaptiveConversationLayout(
    viewModel: GlobalSmsViewModel,
    onOpenThread: (Long) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenBankDashboard: () -> Unit,
    onOpenOtpCenter: () -> Unit,
    onOpenSettings: () -> Unit,
    onComposeNew: () -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val windowType = rememberWindowType(maxWidth.value.toInt())

        if (windowType == ScreenWindowType.COMPACT) {
            // Single-Pane Phone Layout
            SmartConversationsScreen(
                viewModel = viewModel,
                onOpenThread = onOpenThread,
                onOpenSearch = onOpenSearch,
                onOpenBankDashboard = onOpenBankDashboard,
                onOpenOtpCenter = onOpenOtpCenter,
                onOpenSettings = onOpenSettings,
                onComposeNew = onComposeNew
            )
        } else {
            // Two-Pane Tablet / Foldable Split View
            Row(modifier = Modifier.fillMaxSize()) {
                // Left Pane: Conversations & Navigation List (40% width or fixed 380dp)
                Box(
                    modifier = Modifier
                        .width(380.dp)
                        .fillMaxHeight()
                ) {
                    SmartConversationsScreen(
                        viewModel = viewModel,
                        onOpenThread = { threadId ->
                            viewModel.selectThread(threadId)
                        },
                        onOpenSearch = onOpenSearch,
                        onOpenBankDashboard = onOpenBankDashboard,
                        onOpenOtpCenter = onOpenOtpCenter,
                        onOpenSettings = onOpenSettings,
                        onComposeNew = onComposeNew
                    )
                }

                VerticalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)

                // Right Pane: Active Thread Detail View & Compose Panel
                val activeThreadId by viewModel.selectedThreadId.collectAsState()

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    val currentThreadId = activeThreadId
                    if (currentThreadId != null && currentThreadId != 0L) {
                        com.global.sms.ui.screens.MessageThreadScreen(
                            viewModel = viewModel,
                            threadId = currentThreadId,
                            onBack = {
                                viewModel.selectThread(0L)
                            }
                        )
                    } else {
                        // Empty Detail State
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.Chat,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "یک گفتگو را از لیست سمت راست انتخاب کنید",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "نمایش دو پنله مخصوص تبلت، نمایشگرهای عریض و گوشی‌های تاشو",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        }
    }
}
