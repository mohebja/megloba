package com.global.sms.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.global.sms.core.profile.SystemUserProfile
import com.global.sms.core.profile.UserProfileEngine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileManagementScreen(
    onNavigateBack: () -> Unit = {}
) {
    val engine = remember { UserProfileEngine() }
    val currentSettings by engine.state.collectAsState()

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .testTag("profile_management_screen"),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "مدیریت پروفایل‌های کاربری و سناریوها",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.testTag("profile_back_button")
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
                    Text(
                        text = "انتخاب وضعیت و پروفایل فعال",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                item {
                    ProfileOptionCard(
                        title = "حالت شخصی (Personal)",
                        description = "اعلان‌ها و پاسخ‌دهی عادی بدون پاسخ خودکار",
                        icon = Icons.Default.Person,
                        isSelected = currentSettings.activeProfile == SystemUserProfile.PERSONAL_MODE,
                        onSelect = { engine.switchProfile(SystemUserProfile.PERSONAL_MODE) }
                    )
                }

                item {
                    ProfileOptionCard(
                        title = "حالت کاری (Business)",
                        description = "پاسخ خودکار ساعات غیرکاری و مدیریت گروه‌های مشتریان",
                        icon = Icons.Default.BusinessCenter,
                        isSelected = currentSettings.activeProfile == SystemUserProfile.BUSINESS_MODE,
                        onSelect = { engine.switchProfile(SystemUserProfile.BUSINESS_MODE) }
                    )
                }

                item {
                    ProfileOptionCard(
                        title = "حالت خصوصی (Private)",
                        description = "حذف پیش‌نمایش پیام‌ها و قطع صدای اعلان مخاطبان عادی",
                        icon = Icons.Default.Lock,
                        isSelected = currentSettings.activeProfile == SystemUserProfile.PRIVATE_MODE,
                        onSelect = { engine.switchProfile(SystemUserProfile.PRIVATE_MODE) }
                    )
                }

                item {
                    ProfileOptionCard(
                        title = "حالت رانندگی (Driving)",
                        description = "پاسخ پیامکی ایمن به تماس‌ها و پیام‌ها در حین رانندگی",
                        icon = Icons.Default.DirectionsCar,
                        isSelected = currentSettings.activeProfile == SystemUserProfile.DRIVING_MODE,
                        onSelect = { engine.switchProfile(SystemUserProfile.DRIVING_MODE) }
                    )
                }

                item {
                    ProfileOptionCard(
                        title = "حالت جلسه (Meeting)",
                        description = "بی‌صدا کردن کامل گوشی و پاسخ خودکار به پیام‌های مهم",
                        icon = Icons.Default.Groups,
                        isSelected = currentSettings.activeProfile == SystemUserProfile.MEETING_MODE,
                        onSelect = { engine.switchProfile(SystemUserProfile.MEETING_MODE) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileOptionCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        onClick = onSelect,
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
            RadioButton(selected = isSelected, onClick = onSelect)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = description, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
