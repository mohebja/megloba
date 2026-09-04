package com.global.sms.ui.components

import android.Manifest
import android.app.Activity
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.global.sms.core.contact.ContactPermissionState
import com.global.sms.ui.viewmodels.ContactViewModel

@Composable
fun ContactPermissionCard(
    permissionState: ContactPermissionState,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = if (permissionState == ContactPermissionState.PERMANENTLY_DENIED) Icons.Default.Lock else Icons.Default.Contacts,
                contentDescription = "دسترسی مخاطبین",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = when (permissionState) {
                    ContactPermissionState.PERMANENTLY_DENIED -> "دسترسی به مخاطبین غیرفعال است"
                    ContactPermissionState.NEEDS_EXPLANATION -> "توضیح لزوم دسترسی مخاطبین"
                    else -> "اجازه دسترسی به مخاطبین"
                },
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = when (permissionState) {
                    ContactPermissionState.PERMANENTLY_DENIED -> "شما دسترسی مخاطبین را به صورت دائمی رد کرده‌اید. برای نمایش نام و عکس مخاطبین در پیامک‌ها و ارسال گروهی، از تنظیمات گوشی دسترسی را فعال کنید."
                    ContactPermissionState.NEEDS_EXPLANATION -> "برنامه Global SMS برای نمایش اسامی و تصاویر مخاطبین، سازماندهی گروه‌ها و ارسال سریع پیامک به دسترسی به لیست مخاطبین نیاز دارد."
                    else -> "برای جستجو و انتخاب مخاطبین جهت ارسال پیامک، لطفاً دسترسی به مخاطبین دستگاه را تأیید کنید."
                },
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 19.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (permissionState == ContactPermissionState.PERMANENTLY_DENIED) {
                Button(
                    onClick = onOpenSettings,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("باز کردن تنظیمات برنامه", fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = onRequestPermission,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Contacts, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("اعطای دسترسی به مخاطبین", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun rememberContactPermissionLauncher(
    viewModel: ContactViewModel
): () -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        val activity = context as? Activity
        val shouldShowRationale = activity?.let {
            ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.READ_CONTACTS)
        } ?: false
        viewModel.onPermissionResult(isGranted, shouldShowRationale)
    }

    return {
        launcher.launch(Manifest.permission.READ_CONTACTS)
    }
}
