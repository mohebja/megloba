package com.global.sms.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.global.sms.core.trash.TrashManager
import com.global.sms.core.trash.TrashedConversation
import com.global.sms.core.util.PersianUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(
    usePersianDigits: Boolean = true,
    usePersianCalendar: Boolean = true,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        TrashManager.init(context)
    }

    val trashedItems by TrashManager.trashedItems.collectAsStateWithLifecycle()
    var showEmptyTrashDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "سطل بازیافت پیام‌ها",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            text = if (usePersianDigits) PersianUtils.toPersianDigits("${trashedItems.size} گفتگوی حذف‌شده")
                            else "${trashedItems.size} گفتگوی حذف‌شده",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_back_trash")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                },
                actions = {
                    if (trashedItems.isNotEmpty()) {
                        IconButton(
                            onClick = { showEmptyTrashDialog = true },
                            modifier = Modifier.testTag("btn_empty_trash")
                        ) {
                            Icon(
                                Icons.Default.DeleteSweep,
                                contentDescription = "تخلیه کامل سطل زباله",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
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
            // Informational Notice Banner
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "پیام‌های حذف‌شده تا ۳۰ روز در این بخش نگهداری می‌شوند و در هر زمان قابل بازیابی هستند.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }

            if (trashedItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "سطل زباله خالی است.",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "گفتگوهای حذف‌شده شما به مدت ۳۰ روز در اینجا باقی خواهند ماند.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(trashedItems, key = { it.threadId }) { item ->
                        TrashedItemCard(
                            item = item,
                            usePersianDigits = usePersianDigits,
                            usePersianCalendar = usePersianCalendar,
                            onRestore = {
                                coroutineScope.launch {
                                    TrashManager.restoreFromTrash(context, item.threadId)
                                    Toast.makeText(context, "گفتگو به صندوق اصلی بازگردانده شد", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onDeletePermanently = {
                                TrashManager.permanentlyDelete(context, item.threadId)
                                Toast.makeText(context, "گفتگو برای همیشه حذف شد", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }

    if (showEmptyTrashDialog) {
        AlertDialog(
            onDismissRequest = { showEmptyTrashDialog = false },
            title = { Text("تخلیه کامل سطل زباله") },
            text = { Text("آیا از حذف دائمی تمام پیام‌ها و گفتگوهای موجود در سطل زباله اطمینان دارید؟ این عملیات غیرقابل بازگشت است.") },
            confirmButton = {
                Button(
                    onClick = {
                        TrashManager.emptyTrash(context)
                        showEmptyTrashDialog = false
                        Toast.makeText(context, "سطل زباله کاملاً تخلیه شد", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("حذف دائمی همه")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmptyTrashDialog = false }) {
                    Text("انصراف")
                }
            }
        )
    }
}

@Composable
fun TrashedItemCard(
    item: TrashedConversation,
    usePersianDigits: Boolean,
    usePersianCalendar: Boolean,
    onRestore: () -> Unit,
    onDeletePermanently: () -> Unit
) {
    val displayName = item.contactName ?: item.address
    val remainingDaysStr = if (usePersianDigits) PersianUtils.toPersianDigits("${item.remainingDays} روز") else "${item.remainingDays} روز"

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (usePersianDigits) PersianUtils.toPersianDigits(displayName) else displayName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (usePersianDigits) PersianUtils.toPersianDigits(item.address) else item.address,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f)
                ) {
                    Text(
                        text = "$remainingDaysStr تا حذف خودکار",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (usePersianDigits) PersianUtils.toPersianDigits(item.lastMessage) else item.lastMessage,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onDeletePermanently,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier.height(32.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("حذف نهایی", fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.width(8.dp))

                FilledTonalButton(
                    onClick = onRestore,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("بازیابی گفتگو", fontSize = 11.sp)
                }
            }
        }
    }
}
