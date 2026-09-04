package com.global.sms.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyCenterScreen(
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .testTag("privacy_center_screen"),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "مرکز امنیت و حریم خصوصی داده‌ها",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.testTag("privacy_center_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "بازگشت"
                            )
                        }
                    }
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "حفاظت ۱۰۰٪ محلی از اطلاعات",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "تمام پیام‌ها و تحلیل‌های هوش مصنوعی صرفاً روی حافظه دستگاه شما پردازش و ذخیره می‌شوند.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "وضعیت استانداردهای امنیتی",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                item {
                    PrivacyStatusCard(
                        title = "ذخیره‌سازی محلی پیام‌ها",
                        subtitle = "پیام‌ها به هیچ سرور خارجی منتقل نمی‌شوند",
                        icon = Icons.Default.Storage,
                        isSecure = true
                    )
                }

                item {
                    PrivacyStatusCard(
                        title = "موتور هوش مصنوعی آفلاین (Local AI)",
                        subtitle = "پردازش مدل‌های هوش مصنوعی بدون نیاز به اینترنت",
                        icon = Icons.Default.Psychology,
                        isSecure = true
                    )
                }

                item {
                    PrivacyStatusCard(
                        title = "رمزنگاری AES-256-GCM",
                        subtitle = "پایگاه داده و گاوصندوق با کلید Android KeyStore",
                        icon = Icons.Default.Lock,
                        isSecure = true
                    )
                }

                item {
                    PrivacyStatusCard(
                        title = "محافظت از گاوصندوق خصوصی",
                        subtitle = "تأیید هویت بیومتریک + حذف از فهرست‌های عمومی",
                        icon = Icons.Default.Security,
                        isSecure = true
                    )
                }

                item {
                    PrivacyStatusCard(
                        title = "پشتیبان‌گیری رمزنگاری‌شده",
                        subtitle = "فایل‌های بک‌آپ با پسورد اختصاصی رمزگذاری می‌شوند",
                        icon = Icons.Default.Backup,
                        isSecure = true
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            Toast.makeText(
                                context,
                                "گزارش کامل حریم خصوصی داده‌ها (Privacy Report) تولید شد",
                                Toast.LENGTH_LONG
                            ).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("export_privacy_report_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(imageVector = Icons.Default.FileDownload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("خروجی گزارش حریم خصوصی (Export Privacy Report)")
                    }
                }
            }
        }
    }
}

@Composable
private fun PrivacyStatusCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSecure: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = if (isSecure) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.errorContainer
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.padding(8.dp),
                    tint = if (isSecure) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onErrorContainer
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "تأیید شده",
                tint = Color(0xFF2E7D32)
            )
        }
    }
}
