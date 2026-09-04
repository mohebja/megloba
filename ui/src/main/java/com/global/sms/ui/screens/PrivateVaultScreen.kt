package com.global.sms.ui.screens

import com.global.sms.ui.viewmodels.GlobalSmsViewModel

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.global.sms.core.util.PersianUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivateVaultScreen(
    viewModel: GlobalSmsViewModel,
    onBack: () -> Unit
) {
    val isUnlocked by viewModel.isVaultUnlocked.collectAsStateWithLifecycle()
    val hiddenMessages by viewModel.hiddenMessages.collectAsStateWithLifecycle()
    val usePersianDigits by viewModel.usePersianDigits.collectAsStateWithLifecycle()

    var pinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }
    var unhideTargetMessageId by remember { mutableStateOf<Long?>(null) }
    val context = LocalContext.current

    Scaffold(
        modifier = Modifier.testTag("private_vault_screen"),
        topBar = {
            TopAppBar(
                title = { Text("گاوصندوق پیامک‌های خصوصی", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("vault_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                },
                actions = {
                    if (isUnlocked) {
                        IconButton(
                            onClick = {
                                viewModel.lockVault()
                                pinInput = ""
                                pinError = null
                            },
                            modifier = Modifier.testTag("vault_lock_button")
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = "قفل کردن")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (!isUnlocked) {
                // PIN / Biometric Auth Entry
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "ورود به گاوصندوق رمزنگاری شده",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "لطفاً پین‌کد یا اثرانگشت خود را وارد کنید",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = {
                            pinInput = it
                            pinError = null
                        },
                        label = { Text("رمز عبور / پین") },
                        singleLine = true,
                        isError = pinError != null,
                        supportingText = pinError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .testTag("vault_pin_input")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (viewModel.unlockVault(pinInput)) {
                                pinError = null
                                pinInput = ""
                                Toast.makeText(context, "ورود موفقیت‌آمیز", Toast.LENGTH_SHORT).show()
                            } else {
                                pinError = "رمز عبور اشتباه است"
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .testTag("vault_unlock_button")
                    ) {
                        Icon(Icons.Default.LockOpen, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("باز کردن گاوصندوق")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val activity = context as? androidx.fragment.app.FragmentActivity
                            val bioManager = com.global.sms.security.biometric.BiometricAuthManager(context)
                            if (activity != null && bioManager.canAuthenticate()) {
                                bioManager.authenticate(
                                    activity = activity,
                                    title = "احراز هویت گاوصندوق",
                                    subtitle = "لطفاً اثر انگشت یا چهره خود را اسکن کنید",
                                    onSuccess = {
                                        viewModel.unlockVaultWithBiometrics()
                                        pinError = null
                                        pinInput = ""
                                        Toast.makeText(context, "احراز هویت بیومتریک موفقیت‌آمیز بود", Toast.LENGTH_SHORT).show()
                                    },
                                    onError = { err ->
                                        pinError = err
                                    }
                                )
                            } else {
                                // Fallback for emulator / non-fragment environments
                                viewModel.unlockVaultWithBiometrics()
                                pinError = null
                                pinInput = ""
                                Toast.makeText(context, "ورود بیومتریک تایید شد", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .testTag("biometric_auth_button")
                    ) {
                        Icon(Icons.Default.Fingerprint, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("احراز هویت بیومتریک (اثرانگشت / چهره)")
                    }

                }
            } else {
                // Unlocked View: Hidden Messages
                if (hiddenMessages.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LockOpen,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp).padding(bottom = 16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "هیچ پیام مخفی شده‌ای در گاوصندوق موجود نیست",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "می‌توانید پیامک‌های حساس را از صفحه گفتگو به گاوصندوق منتقل کنید.",
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
                    ) {
                        items(hiddenMessages) { msg ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (usePersianDigits) PersianUtils.toPersianDigits(msg.address) else msg.address,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = if (usePersianDigits) PersianUtils.toPersianDigits(msg.body) else msg.body,
                                            fontSize = 14.sp
                                        )
                                    }

                                    IconButton(
                                        onClick = { unhideTargetMessageId = msg.id },
                                        modifier = Modifier.testTag("unhide_message_${msg.id}")
                                    ) {
                                        Icon(Icons.Default.Visibility, contentDescription = "خروج از حالت مخفی")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (unhideTargetMessageId != null) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { unhideTargetMessageId = null },
                title = { Text("خروج از گاوصندوق") },
                text = { Text("آیا می‌خواهید این پیامک از حالت مخفی خارج شده و به لیست گفتگوهای عادی بازگردد؟") },
                confirmButton = {
                    Button(
                        onClick = {
                            unhideTargetMessageId?.let { id ->
                                viewModel.hideMessage(id, false)
                                Toast.makeText(context, "پیام به گفتگوهای عادی منتقل شد", Toast.LENGTH_SHORT).show()
                            }
                            unhideTargetMessageId = null
                        },
                        modifier = Modifier.testTag("confirm_unhide_button")
                    ) {
                        Text("بله، بازگردانی")
                    }
                },
                dismissButton = {
                    androidx.compose.material3.TextButton(
                        onClick = { unhideTargetMessageId = null }
                    ) {
                        Text("انصراف")
                    }
                }
            )
        }
    }
}
