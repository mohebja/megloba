package com.global.sms.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
import com.global.sms.data.entity.OtpEntity
import com.global.sms.security.clipboard.SecureClipboardManager
import com.global.sms.ui.viewmodels.GlobalSmsViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpScreen(
    viewModel: GlobalSmsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeOtps by viewModel.activeOtpsFlow.collectAsStateWithLifecycle(initialValue = emptyList())
    val allOtps by viewModel.allOtpsFlow.collectAsStateWithLifecycle(initialValue = emptyList())
    val usePersianDigits by viewModel.usePersianDigits.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(0) } // 0: Active, 1: History

    val context = LocalContext.current

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "مرکز مدیریت کدهای تایید (OTP Center)",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.testTag("otp_screen_title")
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.testTag("otp_back_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            },
            modifier = modifier
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("کدهای فعال (${if (usePersianDigits) PersianUtils.toPersianDigits(activeOtps.size.toString()) else activeOtps.size})") },
                        icon = { Icon(Icons.Default.Key, contentDescription = null) },
                        modifier = Modifier.testTag("otp_tab_active")
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("تاریخچه کل (${if (usePersianDigits) PersianUtils.toPersianDigits(allOtps.size.toString()) else allOtps.size})") },
                        icon = { Icon(Icons.Default.LockClock, contentDescription = null) },
                        modifier = Modifier.testTag("otp_tab_history")
                    )
                }

                val currentList = if (selectedTab == 0) activeOtps else allOtps

                if (currentList.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                modifier = Modifier.padding(16.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                text = if (selectedTab == 0) "هیچ کد تایید فعالی موجود نیست." else "تاریخچه کدها خالی است.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .testTag("otp_list"),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(currentList, key = { it.id }) { otp ->
                            OtpCardItem(
                                otp = otp,
                                usePersianDigits = usePersianDigits,
                                onCopy = { code ->
                                    SecureClipboardManager.copyToClipboard(
                                        context = context,
                                        label = "OTP Code",
                                        text = code,
                                        isSensitive = true
                                    )
                                    val formattedCode = if (usePersianDigits) PersianUtils.toPersianDigits(code) else code
                                    Toast.makeText(context, "کد $formattedCode با امنیت در حافظه موقت کپی شد (پاکسازی خودکار ۳۰ ثانیه)", Toast.LENGTH_SHORT).show()
                                },
                                onMarkAsUsed = { viewModel.markOtpAsUsed(it.id) },
                                onDelete = { viewModel.deleteOtp(it.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OtpCardItem(
    otp: OtpEntity,
    usePersianDigits: Boolean = false,
    onCopy: (String) -> Unit,
    onMarkAsUsed: (OtpEntity) -> Unit,
    onDelete: (OtpEntity) -> Unit
) {
    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTime = System.currentTimeMillis()
        }
    }

    val remainingMillis = (otp.expiresTimestamp - currentTime).coerceAtLeast(0)
    val isExpired = remainingMillis == 0L || otp.isUsed
    val secondsLeft = (remainingMillis / 1000) % 60
    val minutesLeft = (remainingMillis / 1000) / 60
    val rawTimerText = String.format("%02d:%02d", minutesLeft, secondsLeft)
    val timerText = if (usePersianDigits) PersianUtils.toPersianDigits(rawTimerText) else rawTimerText

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isExpired) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("otp_card_${otp.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = otp.serviceName,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = if (usePersianDigits) PersianUtils.toPersianDigits(otp.address) else otp.address,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (!isExpired) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LockClock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text(
                            text = timerText,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    Text(
                        text = if (otp.isUsed) "استفاده شده" else "منقضی شده",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Code Display
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (usePersianDigits) PersianUtils.toPersianDigits(otp.code) else otp.code,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 4.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.testTag("otp_code_text_${otp.id}")
                )

                Button(
                    onClick = { onCopy(otp.code) },
                    enabled = !isExpired,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("copy_otp_button_${otp.id}")
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("کپی کد")
                }
            }

            // Security Warning if High Level
            AnimatedVisibility(visible = otp.securityLevel == "HIGH") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .background(
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "هشدار: پیامک حاوی لینک مشکوک است. کد را در سایت‌های غیررسمی وارد نکنید.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (!otp.isUsed) {
                    OutlinedButton(
                        onClick = { onMarkAsUsed(otp) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("mark_used_button_${otp.id}")
                    ) {
                        Icon(Icons.Default.Done, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("علامت به عنوان استفاده شده")
                    }
                }

                IconButton(
                    onClick = { onDelete(otp) },
                    modifier = Modifier.testTag("delete_otp_button_${otp.id}")
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "حذف کد",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
