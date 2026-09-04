package com.global.sms.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.global.sms.core.enterprise.EnterprisePermission
import com.global.sms.core.enterprise.EnterpriseRole
import com.global.sms.core.enterprise.RolePermissionEngine

data class RuntimePermissionItem(
    val permission: String,
    val titlePersian: String,
    val description: String,
    val isCritical: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionManagementScreen(
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val engine = remember { RolePermissionEngine() }
    val currentRole by engine.activeUserRole.collectAsState()

    var permissionCheckTrigger by remember { mutableIntStateOf(0) }

    val runtimePermissions = remember {
        listOf(
            RuntimePermissionItem(
                permission = Manifest.permission.READ_SMS,
                titlePersian = "خواندن پیامک‌ها (READ_SMS)",
                description = "نیازمند به عنوان پیش‌فرض و نمایش لیست گفتگوها",
                isCritical = true
            ),
            RuntimePermissionItem(
                permission = Manifest.permission.RECEIVE_SMS,
                titlePersian = "دریافت پیامک‌ها (RECEIVE_SMS)",
                description = "دریافت لحظه‌ای پیامک‌های جدید و کدهای تأیید",
                isCritical = true
            ),
            RuntimePermissionItem(
                permission = Manifest.permission.SEND_SMS,
                titlePersian = "ارسال پیامک (SEND_SMS)",
                description = "ارسال پیامک‌های جدید، زمان‌بندی شده و گروهی",
                isCritical = true
            ),
            RuntimePermissionItem(
                permission = Manifest.permission.READ_PHONE_STATE,
                titlePersian = "وضعیت سیم‌کارت‌ها (READ_PHONE_STATE)",
                description = "شناسایی دو سیم‌کارت و دریافت مرکز پیامک (SMSC)",
                isCritical = false
            ),
            RuntimePermissionItem(
                permission = Manifest.permission.READ_CONTACTS,
                titlePersian = "مخاطبین (READ_CONTACTS)",
                description = "نمایش نام و تصویر مخاطبین در گفتگوها",
                isCritical = false
            )
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        permissionCheckTrigger++
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .testTag("permission_management_screen"),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "مدیریت مجوزهای سیستم و نقش‌ها (RBAC)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.testTag("permission_back_button")
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
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Section 1: Android System Runtime Permissions
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "مجوزهای سیستمی اندروید (Runtime Permissions)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "وضعیت زنده دسترسی‌های امنیتی سیستم",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Button(
                            onClick = {
                                permissionLauncher.launch(
                                    runtimePermissions.map { it.permission }.toTypedArray()
                                )
                            },
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                        ) {
                            Text("درخواست مجوزها")
                        }
                    }
                }

                items(runtimePermissions) { item ->
                    key(permissionCheckTrigger, item.permission) {
                        val isGranted = ContextCompat.checkSelfPermission(
                            context,
                            item.permission
                        ) == PackageManager.PERMISSION_GRANTED

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isGranted) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                            ),
                            elevation = CardDefaults.cardElevation(1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = if (isGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = item.titlePersian,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = item.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Surface(
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp),
                                    color = if (isGranted) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
                                ) {
                                    Text(
                                        text = if (isGranted) "فعال (Granted)" else "مسدود (Denied)",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isGranted) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }

                // Section 2: Enterprise RBAC Roles
                item {
                    Text(
                        text = "انتخاب نقش فعال برای تست دسترسی‌ها",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                items(EnterpriseRole.values().toList()) { role ->
                    val def = engine.getRoleDefinition(role)
                    val isSelected = currentRole == role

                    Card(
                        onClick = { engine.switchRole(role) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(if (isSelected) 4.dp else 1.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { engine.switchRole(role) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = when (role) {
                                    EnterpriseRole.OWNER -> Icons.Default.VerifiedUser
                                    EnterpriseRole.ADMIN -> Icons.Default.AdminPanelSettings
                                    EnterpriseRole.MANAGER -> Icons.Default.SupervisorAccount
                                    EnterpriseRole.EMPLOYEE -> Icons.Default.Person
                                    EnterpriseRole.VIEW_ONLY -> Icons.Default.Visibility
                                },
                                contentDescription = null,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${def.titlePersian} (${role.name})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "مجوزها: ${def.defaultPermissions.joinToString { it.name }}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "بررسی زنده مجوزهای نقش فعال (${engine.getRoleDefinition(currentRole).titlePersian})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                items(EnterprisePermission.values().toList()) { perm ->
                    val isAllowed = engine.hasPermission(currentRole, perm)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isAllowed) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                contentDescription = null,
                                tint = if (isAllowed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = perm.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = if (isAllowed) "مجاز" else "غیرمجاز (مسدود)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isAllowed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
