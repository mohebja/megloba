package com.global.sms.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.global.sms.data.db.GlobalSmsDatabase
import com.global.sms.data.entity.MessageEntity
import com.global.sms.security.backup.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnterpriseBackupScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var password by remember { mutableStateOf("") }
    var backupStatus by remember { mutableStateOf<String?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var lastBackupFile by remember { mutableStateOf<File?>(null) }

    val restoreProgress by BackgroundRestoreEngine.restoreProgress.collectAsState()

    LaunchedEffect(Unit) {
        AutoBackupManager.init(context)
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "پشتیبان‌گیری رمزنگاری‌شده سازمانی",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Encryption Badge Card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        Column {
                            Text(
                                text = "رمزنگاری تاییدشده AES-256-GCM + PBKDF2",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "کلیه نسخه‌های پشتیبان با کلید مشتق‌شده از گذرواژه (PBKDF2 با ۲۱۰,۰۰۰ تکرار)، سالت و IV تصادفی و تگ احراز اصالت ۱۲۸ بیتی رمزنگاری می‌شوند.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                            )
                        }
                    }
                }

                // Backup Creation Section
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "ایجاد نسخه پشتیبان محافظت‌شده",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "گذرواژه اختصاصی برای رمزنگاری داده‌ها وارد کنید:",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("گذرواژه پشتیبان") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth().testTag("backup_password_input"),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (password.length < 4) {
                                    Toast.makeText(context, "گذرواژه باید حداقل ۴ کاراکتر باشد", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                scope.launch {
                                    isProcessing = true
                                    backupStatus = "در حال ایجاد نسخه پشتیبان رمزنگاری‌شده..."
                                    withContext(Dispatchers.IO) {
                                        try {
                                            val db = GlobalSmsDatabase.getInstance(context)
                                            val messages = db.messageDao().getAllMessagesSync()
                                            val backupItems = messages.map {
                                                BackupMessageItem(
                                                    id = it.id,
                                                    threadId = it.threadId,
                                                    address = it.address,
                                                    body = it.body,
                                                    date = it.timestamp,
                                                    type = it.type,
                                                    read = if (it.isRead) 1 else 0,
                                                    status = it.deliveryStatus
                                                )
                                            }
                                            val model = EnterpriseBackupModel(
                                                version = 1,
                                                timestamp = System.currentTimeMillis(),
                                                messages = backupItems
                                            )
                                            val file = EncryptedBackupManager.createEncryptedBackup(context, model, password)
                                            lastBackupFile = file
                                            withContext(Dispatchers.Main) {
                                                backupStatus = "پشتیبان با موفقیت ایجاد و رمزنگاری شد (${file.name} - ${file.length()} بایت)"
                                                Toast.makeText(context, "نسخه پشتیبان رمزنگاری‌شده ذخیره شد", Toast.LENGTH_LONG).show()
                                            }
                                        } catch (e: Exception) {
                                            withContext(Dispatchers.Main) {
                                                backupStatus = "خطا در رمزنگاری: ${e.message}"
                                            }
                                        }
                                    }
                                    isProcessing = false
                                }
                            },
                            enabled = !isProcessing,
                            modifier = Modifier.fillMaxWidth().testTag("create_backup_button")
                        ) {
                            Icon(Icons.Default.EnhancedEncryption, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isProcessing) "در حال پردازش..." else "ایجاد پشتیبان رمزنگاری‌شده")
                        }

                        if (backupStatus != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = backupStatus ?: "",
                                color = if (backupStatus?.contains("خطا") == true) MaterialTheme.colorScheme.error else Color(0xFF00897B),
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                // Restore Section
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "بازیابی اطلاعات از فایل رمزنگاری‌شده",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "برای بازیابی اطلاعات، گذرواژه فایل پشتیبان را وارد نمایید.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedButton(
                            onClick = {
                                val target = lastBackupFile
                                if (target == null || !target.exists()) {
                                    Toast.makeText(context, "ابتدا یک فایل پشتیبان ایجاد نمایید یا فایل را انتخاب کنید", Toast.LENGTH_SHORT).show()
                                    return@OutlinedButton
                                }
                                if (password.isEmpty()) {
                                    Toast.makeText(context, "لطفاً گذرواژه فایل را وارد کنید", Toast.LENGTH_SHORT).show()
                                    return@OutlinedButton
                                }
                                BackgroundRestoreEngine.restoreArchiveInBackground(
                                    context = context,
                                    backupFile = target,
                                    password = password
                                ) { restoredModel ->
                                    val db = GlobalSmsDatabase.getInstance(context)
                                    restoredModel.messages.forEach { item ->
                                        db.messageDao().insertMessage(
                                            MessageEntity(
                                                id = item.id,
                                                threadId = item.threadId,
                                                address = item.address,
                                                body = item.body,
                                                timestamp = item.date,
                                                type = item.type,
                                                isRead = item.read == 1,
                                                deliveryStatus = item.status
                                            )
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().testTag("restore_backup_button")
                        ) {
                            Icon(Icons.Default.Restore, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("بازیابی و احراز اصالت داده‌ها")
                        }

                        when (val stage = restoreProgress) {
                            is RestoreProgressStage.DECRYPTING -> {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("در حال رمزگشایی و بررسی تگ احراز اصالت...", color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                            }
                            is RestoreProgressStage.COMPLETED -> {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "بازیابی با موفقیت انجام شد: ${stage.restoredMessages} پیام بازیابی شدند.",
                                    color = Color(0xFF00897B),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            is RestoreProgressStage.ERROR -> {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "خطا در بازیابی: ${stage.reason}",
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 13.sp
                                )
                            }
                            else -> {}
                        }
                    }
                }
            }
        }
    }
}
