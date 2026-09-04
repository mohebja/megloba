package com.global.sms.ui.screens

import com.global.sms.ui.viewmodels.GlobalSmsViewModel

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Quickreply
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material.icons.filled.Task
import androidx.compose.material.icons.filled.Translate
import com.global.sms.core.ai.brain.LocalAIBrain
import com.global.sms.core.ai.brain.ChatMessage
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.global.sms.core.util.PersianUtils
import com.global.sms.core.security.PhishingDetector
import com.global.sms.core.util.SmsSegmenter
import com.global.sms.data.entity.MessageEntity
import com.global.sms.data.entity.MessageType
import com.global.sms.data.entity.SettingsEntity
import com.global.sms.ui.components.ContactAvatar
import com.global.sms.ui.components.rememberContactInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageThreadScreen(
    viewModel: GlobalSmsViewModel,
    threadId: Long,
    onBack: () -> Unit
) {
    val messages by viewModel.activeThreadMessages.collectAsStateWithLifecycle()
    val quickReplies by viewModel.quickReplies.collectAsStateWithLifecycle()
    val settings by viewModel.settingsState.collectAsStateWithLifecycle()
    val usePersianDigits by viewModel.usePersianDigits.collectAsStateWithLifecycle()
    val usePersianCalendar by viewModel.usePersianCalendar.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var messageText by remember { mutableStateOf("") }
    var selectedSimSlot by remember { mutableIntStateOf(0) }
    var showQuickReplySheet by remember { mutableStateOf(false) }
    var showEmojiPicker by remember { mutableStateOf(false) }
    var showAttachmentPicker by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    var selectedMessageForAction by remember { mutableStateOf<com.global.sms.data.entity.MessageEntity?>(null) }

    var fontScale by remember(settings.messageFontScale) { mutableFloatStateOf(settings.messageFontScale) }

    val segmentInfo = SmsSegmenter.calculateSegments(messageText)
    val rawAddress = messages.firstOrNull()?.address ?: "گیرنده"
    val contactInfo = rememberContactInfo(rawAddress)
    val address = contactInfo.name ?: rawAddress

    val popularEmojis = listOf("😊", "😂", "❤️", "👍", "🙏", "📱", "💳", "🔑", "⏰", "📍", "🛍️", "💼", "📌", "✉️", "✅", "🔥", "🎉", "💐")

    val lastReceivedMessage = remember(messages) {
        messages.lastOrNull { it.type == MessageType.INBOX.code }
    }
    val smartReplySuggestions = remember(lastReceivedMessage) {
        if (lastReceivedMessage != null) {
            com.global.sms.core.ai.smartreply.SmartReplyEngine.generateSmartReplies(lastReceivedMessage.body)
        } else emptyList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ContactAvatar(
                            photoUri = contactInfo.photoUri,
                            displayName = address,
                            size = 38.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (usePersianDigits) PersianUtils.toPersianDigits(address) else address,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = if (selectedSimSlot == 0) "سیم ۱ • امنیتی فعال" else "سیم ۲ • امنیتی فعال",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("thread_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                },
                actions = {
                    // Call Button
                    IconButton(
                        onClick = {
                            try {
                                val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                                    data = android.net.Uri.parse("tel:$address")
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "امکان تماس مستقیم وجود ندارد", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.testTag("thread_call_button")
                    ) {
                        Icon(Icons.Default.Call, contentDescription = "تماس تلفنی", tint = MaterialTheme.colorScheme.primary)
                    }

                    // SIM Switcher
                    IconButton(
                        onClick = { selectedSimSlot = if (selectedSimSlot == 0) 1 else 0 },
                        modifier = Modifier.testTag("thread_sim_switch_button")
                    ) {
                        Icon(Icons.Default.SimCard, contentDescription = "تغییر سیم کارت")
                    }

                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.testTag("thread_more_menu_button")
                    ) {
                        Icon(Icons.Default.MoreVert, contentDescription = "منو")
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("انتقال گفتگو به گاوصندوق") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                viewModel.hideConversation(threadId, true)
                                onBack()
                            }
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val pagedMessages = viewModel.activeThreadMessagesPagingFlow.collectAsLazyPagingItems()

            // Message List with Pinch-to-Zoom Font Resize Support & Feedback Badge
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, _, zoom, _ ->
                            if (zoom != 1f) {
                                val newScale = (fontScale * zoom).coerceIn(0.7f, 2.2f)
                                fontScale = newScale
                                viewModel.updateMessageFontScale(newScale)
                            }
                        }
                    }
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    reverseLayout = true
                ) {
                    if (pagedMessages.itemCount > 0) {
                        items(
                            count = pagedMessages.itemCount,
                            key = pagedMessages.itemKey { it.id },
                            contentType = { index -> pagedMessages[index]?.type ?: 0 }
                        ) { index ->
                            val message = pagedMessages[index]
                            if (message != null) {
                                MessageBubble(
                                    message = message,
                                    settings = settings,
                                    fontScale = fontScale,
                                    usePersianDigits = usePersianDigits,
                                    usePersianCalendar = usePersianCalendar,
                                    onCopyOtp = { code ->
                                        clipboardManager.setText(AnnotatedString(code))
                                        Toast.makeText(context, "کد تایید کپی شد: $code", Toast.LENGTH_SHORT).show()
                                    },
                                    onSpeak = { viewModel.speakMessage(message.body) },
                                    onHideMessage = { viewModel.hideMessage(message.id, true) },
                                    onLongClick = { selectedMessageForAction = message }
                                )
                            }
                        }
                    } else {
                        items(
                            items = messages,
                            key = { it.id },
                            contentType = { message -> message.type }
                        ) { message ->
                            MessageBubble(
                                message = message,
                                settings = settings,
                                fontScale = fontScale,
                                usePersianDigits = usePersianDigits,
                                usePersianCalendar = usePersianCalendar,
                                onCopyOtp = { code ->
                                    clipboardManager.setText(AnnotatedString(code))
                                    Toast.makeText(context, "کد تایید کپی شد: $code", Toast.LENGTH_SHORT).show()
                                },
                                onSpeak = { viewModel.speakMessage(message.body) },
                                onHideMessage = { viewModel.hideMessage(message.id, true) },
                                onLongClick = { selectedMessageForAction = message }
                            )
                        }
                    }
                }
            }

            // Message Long-Press Actions BottomSheet
            selectedMessageForAction?.let { msg ->
                com.global.sms.ui.components.MessageActionBottomSheet(
                    message = msg,
                    selectedMessages = listOf(msg),
                    onDismiss = { selectedMessageForAction = null },
                    onForward = { text -> messageText = text }
                )
            }

            // Full Unicode Emoji Picker Sheet
            if (showEmojiPicker) {
                com.global.sms.ui.emoji.EmojiPicker(
                    onEmojiSelected = { emoji ->
                        messageText += emoji
                    },
                    onDismiss = { showEmojiPicker = false }
                )
            }

            // Quick Reply Suggestions Bar
            if (showQuickReplySheet) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(8.dp)
                ) {
                    items(quickReplies) { reply ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    messageText = reply.content
                                    showQuickReplySheet = false
                                },
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Text(
                                text = reply.title + ": " + reply.content,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }

            // Attachment Picker Sheet
            if (showAttachmentPicker) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    IconButton(onClick = {
                        messageText += " [پیوست تصویر]"
                        showAttachmentPicker = false
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "تصویر")
                    }
                    IconButton(onClick = {
                        messageText += " [کارت مخاطب vCard]"
                        showAttachmentPicker = false
                    }) {
                        Icon(Icons.Default.Person, contentDescription = "مخاطب")
                    }
                    IconButton(onClick = {
                        messageText += " [موقعیت مکانی 📍]"
                        showAttachmentPicker = false
                    }) {
                        Icon(Icons.Default.Place, contentDescription = "موقعیت مکانی")
                    }
                }
            }

            // Smart Reply AI Suggestions Chips
            if (smartReplySuggestions.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(smartReplySuggestions) { suggestion ->
                        androidx.compose.material3.AssistChip(
                            onClick = { messageText = suggestion },
                            label = { Text(suggestion, fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.Quickreply, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary) }
                        )
                    }
                }
            }

            // Input Bar
            Surface(
                tonalElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    // Segment Counter info bar
                    // AI Action Bar Row above input
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = false,
                                onClick = {
                                    val summary = LocalAIBrain.summarizeConversation(
                                        messages.map { ChatMessage(sender = if (it.type == 1) "مخاطب" else "من", body = it.body) }
                                    )
                                    Toast.makeText(context, "خلاصه گفتگو: $summary", Toast.LENGTH_LONG).show()
                                },
                                label = { Text("خلاصه گفتگو", fontSize = 11.sp) },
                                leadingIcon = { Icon(Icons.Default.Summarize, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            )
                        }
                        item {
                            FilterChip(
                                selected = false,
                                onClick = {
                                    val smartReply = smartReplySuggestions.firstOrNull() ?: "درود، پیام شما دریافت شد."
                                    messageText = smartReply
                                },
                                label = { Text("پیشنهاد پاسخ", fontSize = 11.sp) },
                                leadingIcon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            )
                        }
                        item {
                            FilterChip(
                                selected = false,
                                onClick = {
                                    val extractedTask = LocalAIBrain.extractTask(
                                        lastReceivedMessage?.body ?: ""
                                    )
                                    if (extractedTask != null) {
                                        Toast.makeText(context, "وظیفه استخراج شد: $extractedTask", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "هیچ وظیفه‌ای در پیام یافت نشد", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                label = { Text("استخراج کار", fontSize = 11.sp) },
                                leadingIcon = { Icon(Icons.Default.Task, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            )
                        }
                        item {
                            FilterChip(
                                selected = false,
                                onClick = {
                                    val translated = LocalAIBrain.translateToPersian(
                                        lastReceivedMessage?.body ?: ""
                                    )
                                    Toast.makeText(context, "ترجمه: $translated", Toast.LENGTH_LONG).show()
                                },
                                label = { Text("ترجمه پیام", fontSize = 11.sp) },
                                leadingIcon = { Icon(Icons.Default.Translate, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val counterText = "${segmentInfo.remainingInSegment}/${if (segmentInfo.isUnicode) 70 else 160} (${segmentInfo.segmentCount} پیامک)"
                        Text(
                            text = if (usePersianDigits) PersianUtils.toPersianDigits(counterText) else counterText,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (segmentInfo.isUnicode) "پشتیبانی کامل از زبان فارسی" else "Standard GSM",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { showAttachmentPicker = !showAttachmentPicker },
                            modifier = Modifier.testTag("thread_attachment_button")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "پیوست")
                        }

                        IconButton(
                            onClick = { showEmojiPicker = !showEmojiPicker },
                            modifier = Modifier.testTag("thread_emoji_button")
                        ) {
                            Icon(Icons.Default.Face, contentDescription = "ایموجی")
                        }

                        IconButton(
                            onClick = { showQuickReplySheet = !showQuickReplySheet },
                            modifier = Modifier.testTag("thread_quick_reply_button")
                        ) {
                            Icon(Icons.Default.Quickreply, contentDescription = "پاسخ سریع")
                        }

                        OutlinedTextField(
                            value = messageText,
                            onValueChange = { messageText = it },
                            placeholder = { Text("نوشتن پیامک...") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("message_input_field"),
                            maxLines = 4
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        IconButton(
                            onClick = {
                                if (messageText.isNotBlank()) {
                                    viewModel.sendMessage(address, messageText, selectedSimSlot)
                                    messageText = ""
                                }
                            },
                            modifier = Modifier.testTag("send_button")
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Send,
                                contentDescription = "ارسال",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: MessageEntity,
    settings: SettingsEntity,
    fontScale: Float = 1.0f,
    usePersianDigits: Boolean,
    usePersianCalendar: Boolean,
    onCopyOtp: (String) -> Unit,
    onSpeak: () -> Unit,
    onHideMessage: () -> Unit,
    onLongClick: () -> Unit = {}
) {
    val isSent = message.type == MessageType.SENT.code
    val alignment = if (isSent) Alignment.End else Alignment.Start

    val bubbleColor = if (isSent) androidx.compose.ui.graphics.Color(settings.outgoingBubbleBgColor)
    else androidx.compose.ui.graphics.Color(settings.incomingBubbleBgColor)

    val textColor = if (isSent) androidx.compose.ui.graphics.Color(settings.outgoingBubbleTextColor)
    else androidx.compose.ui.graphics.Color(settings.incomingBubbleTextColor)

    val timestampColor = androidx.compose.ui.graphics.Color(settings.timestampColor)

    val customFontFamily = when (settings.fontFamily) {
        "SansSerif" -> androidx.compose.ui.text.font.FontFamily.SansSerif
        "Serif" -> androidx.compose.ui.text.font.FontFamily.Serif
        "Monospace" -> androidx.compose.ui.text.font.FontFamily.Monospace
        else -> androidx.compose.ui.text.font.FontFamily.Default
    }

    val scan = remember(message.body) { PhishingDetector.scanMessage(message.address, message.body) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = alignment
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = bubbleColor),
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .combinedClickable(
                    onLongClick = onLongClick,
                    onClick = {}
                )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Threat warning if detected
                val warningReason = scan.warningReason
                if (scan.isSpamOrPhishing && warningReason != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                    ) {
                        Text(
                            text = warningReason,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(6.dp)
                        )
                    }
                }

                Text(
                    text = if (usePersianDigits) PersianUtils.toPersianDigits(message.body) else message.body,
                    color = textColor,
                    fontSize = (settings.messageTextSizeSp * fontScale).sp,
                    lineHeight = com.global.sms.ui.theme.DynamicTypography.getScaledLineHeight(settings.messageTextSizeSp.toFloat(), fontScale),
                    fontFamily = customFontFamily
                )

                // OTP Copy button if applicable
                message.otpCode?.let { otp ->
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedButton(
                        onClick = { onCopyOtp(otp) },
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "کپی کد تایید: ${if (usePersianDigits) PersianUtils.toPersianDigits(otp) else otp}",
                            fontFamily = customFontFamily
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = onSpeak,
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "خوانش صوتی",
                            tint = textColor.copy(alpha = 0.7f),
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = PersianUtils.formatTimestamp(message.timestamp, usePersianCalendar, usePersianDigits),
                        color = timestampColor,
                        fontSize = settings.dateTextSizeSp.sp,
                        fontFamily = customFontFamily
                    )
                }
            }
        }
    }
}

private fun String?.isNull_or_empty(): Boolean = this == null || this.trim().isEmpty()
