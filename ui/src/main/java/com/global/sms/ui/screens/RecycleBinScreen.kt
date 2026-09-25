package com.global.sms.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.global.sms.core.haptic.HapticFeedbackManager
import com.global.sms.core.trash.RecycleBinManager
import com.global.sms.core.trash.TrashedMessageItem
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecycleBinScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val trashedMessages by RecycleBinManager.trashedItems.collectAsState()
    var showEmptyConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        RecycleBinManager.loadTrash(context)
    }

    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("سطل بازیافت پیام‌ها (۳۰ روزه)") },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("btn_back_recycle_bin")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                },
                actions = {
                    if (trashedMessages.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    RecycleBinManager.restoreAll(context)
                                    HapticFeedbackManager.vibrateClick(context)
                                    Toast.makeText(context, "تمامی پیام‌ها بازیابی شدند", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.testTag("btn_restore_all_trash")
                        ) {
                            Icon(Icons.Default.RestoreFromTrash, contentDescription = "بازیابی همه")
                        }

                        IconButton(
                            onClick = { showEmptyConfirmDialog = true },
                            modifier = Modifier.testTag("btn_empty_trash")
                        ) {
                            Icon(Icons.Default.DeleteForever, contentDescription = "خالی کردن سطل زباله")
                        }
                    }
                }
            )
        },
        modifier = modifier.testTag("recycle_bin_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Explanatory Banner
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "پیام‌های حذف‌شده به مدت ۳۰ روز در این بخش ذخیره شده و سپس برای حفظ حریم خصوصی به صورت دائمی پاک می‌شوند.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (trashedMessages.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "سطل بازیافت خالی است",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(trashedMessages, key = { it.id }) { item ->
                        TrashedItemCard(
                            item = item,
                            dateFormat = dateFormat,
                            onRestore = {
                                scope.launch {
                                    RecycleBinManager.restoreFromTrash(context, item)
                                    HapticFeedbackManager.vibrateClick(context)
                                    Toast.makeText(context, "پیام بازیابی شد", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onDeletePermanently = {
                                scope.launch {
                                    RecycleBinManager.permanentlyDelete(context, item.id)
                                    HapticFeedbackManager.vibrateClick(context)
                                    Toast.makeText(context, "پیام برای همیشه حذف شد", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showEmptyConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showEmptyConfirmDialog = false },
            title = { Text("پاک‌سازی دائمی سطل زباله") },
            text = { Text("آیا از پاک‌سازی تمامی پیام‌های سطل زباله اطمینان دارید؟ این عمل غیرقابل بازگشت است.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            RecycleBinManager.emptyTrash(context)
                            showEmptyConfirmDialog = false
                            HapticFeedbackManager.vibrateSecurityAlert(context)
                            Toast.makeText(context, "سطل زباله کاملاً خالی شد", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("حذف دائمی", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmptyConfirmDialog = false }) {
                    Text("انصراف")
                }
            }
        )
    }
}

@Composable
private fun TrashedItemCard(
    item: TrashedMessageItem,
    dateFormat: SimpleDateFormat,
    onRestore: () -> Unit,
    onDeletePermanently: () -> Unit
) {
    val now = System.currentTimeMillis()
    val daysRemaining = maxOf(0, TimeUnit.MILLISECONDS.toDays(item.expiresAt - now))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.address,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "$daysRemaining روز تا حذف خودکار",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.body,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "زمان حذف: ${dateFormat.format(Date(item.trashedAt))}",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.outline
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onDeletePermanently,
                    modifier = Modifier.testTag("btn_delete_perm_${item.id}")
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text("حذف دائم", fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.size(8.dp))
                OutlinedButton(
                    onClick = onRestore,
                    modifier = Modifier.testTag("btn_restore_${item.id}")
                ) {
                    Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text("بازیابی", fontSize = 11.sp)
                }
            }
        }
    }
}
