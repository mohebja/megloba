package com.global.sms.ui.classic.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.global.sms.data.entity.MessageType
import com.global.sms.ui.classic.components.ClassicMessageBubble
import com.global.sms.ui.classic.components.ClassicTopBar
import com.global.sms.ui.viewmodels.GlobalSmsViewModel

@Composable
fun ClassicMessageThreadScreen(
    viewModel: GlobalSmsViewModel,
    threadId: Long,
    onBack: () -> Unit
) {
    val activeThread by viewModel.activeThreadMessages.collectAsStateWithLifecycle()
    val usePersianDigits by viewModel.usePersianDigits.collectAsStateWithLifecycle()
    var inputText by remember { mutableStateOf("") }

    val contactTitle = remember(activeThread) {
        activeThread.firstOrNull()?.address ?: "گفتگوی کلاسیک"
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            modifier = Modifier.testTag("classic_thread_screen"),
            topBar = {
                ClassicTopBar(
                    title = contactTitle,
                    onBackClick = onBack
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                        .testTag("classic_message_list"),
                    reverseLayout = true
                ) {
                    items(
                        items = activeThread,
                        key = { it.id }
                    ) { msg ->
                        val isOutgoing = msg.type == MessageType.SENT.code || msg.type == MessageType.OUTBOX.code
                        ClassicMessageBubble(
                            message = msg.body,
                            isOutgoing = isOutgoing,
                            timestamp = msg.timestamp,
                            modifier = Modifier.testTag("classic_bubble_${msg.id}")
                        )
                    }
                }

                Surface(
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("classic_message_input"),
                            placeholder = { Text("نوشتن پیامک...") },
                            shape = RoundedCornerShape(24.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = {
                                if (inputText.isNotBlank()) {
                                    viewModel.sendMessage(
                                        address = contactTitle,
                                        body = inputText,
                                        simSlot = 0
                                    )
                                    inputText = ""
                                }
                            },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.testTag("classic_send_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "ارسال"
                            )
                        }
                    }
                }
            }
        }
    }
}
