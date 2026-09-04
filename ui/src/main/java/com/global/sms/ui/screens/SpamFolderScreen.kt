package com.global.sms.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.global.sms.core.util.PersianUtils
import com.global.sms.ui.viewmodels.GlobalSmsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpamFolderScreen(
    viewModel: GlobalSmsViewModel,
    onBack: () -> Unit
) {
    val spamMessages by viewModel.spamMessages.collectAsStateWithLifecycle()
    val usePersianDigits by viewModel.usePersianDigits.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showClearAllConfirmDialog by remember { mutableStateOf(false) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "پوشه اسپم و تبلیغات مشکوک",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.testTag("spam_folder_title")
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("spam_back_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                        }
                    },
                    actions = {
                        if (spamMessages.isNotEmpty()) {
                            IconButton(
                                onClick = { showClearAllConfirmDialog = true },
                                modifier = Modifier.testTag("clear_all_spam_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteSweep,
                                    contentDescription = "پاکسازی همه اسپم‌ها",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                if (spamMessages.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp).padding(bottom = 16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "هیچ پیام اسپم یا آگهی کلاهبرداری یافت نشد",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "سیستم هوشمند محافظت از هرزنامه تمام پیامک‌های ورودی را به صورت خودکار پایش می‌کند.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                            .testTag("spam_messages_list"),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(spamMessages, key = { it.id }) { msg ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("spam_card_${msg.id}"),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "فرستنده: ${if (usePersianDigits) PersianUtils.toPersianDigits(msg.address) else msg.address}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                        Text(
                                            text = PersianUtils.formatTimestamp(msg.timestamp),
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = if (usePersianDigits) PersianUtils.toPersianDigits(msg.body) else msg.body,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                viewModel.restoreSpamMessage(msg.id, msg.threadId)
                                                Toast.makeText(context, "پیامک از اسپم خارج و به پیام‌های عادی منتقل شد", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.testTag("restore_spam_button_${msg.id}")
                                        ) {
                                            Icon(
                                                Icons.Default.Restore,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                                            Text("بازیابی پیامک (اسپم نیست)", fontSize = 12.sp)
                                        }

                                        Spacer(modifier = Modifier.padding(horizontal = 4.dp))

                                        IconButton(
                                            onClick = {
                                                viewModel.deleteMessage(msg.id)
                                                Toast.makeText(context, "پیامک حذف شد", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.testTag("delete_spam_button_${msg.id}")
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "حذف دائمی اسپم",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showClearAllConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showClearAllConfirmDialog = false },
                title = { Text("پاکسازی تمام اسپم‌ها") },
                text = { Text("آیا مطمئن هستید که می‌خواهید تمام ${spamMessages.size} پیامک اسپم را به صورت دائمی حذف کنید؟") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.clearAllSpamMessages()
                            showClearAllConfirmDialog = false
                            Toast.makeText(context, "تمامی پیامک‌های اسپم حذف شدند", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.testTag("confirm_clear_all_spam_button")
                    ) {
                        Text("حذف همه")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showClearAllConfirmDialog = false },
                        modifier = Modifier.testTag("cancel_clear_all_spam_button")
                    ) {
                        Text("انصراف")
                    }
                }
            )
        }
    }
}
