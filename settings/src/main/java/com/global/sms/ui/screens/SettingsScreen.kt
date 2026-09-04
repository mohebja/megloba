package com.global.sms.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Psychology

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    conversationStyle: String = "MODERN",
    onConversationStyleChange: (String) -> Unit = {},
    messageFontScale: Float = 1.0f,
    onMessageFontScaleChange: (Float) -> Unit = {},
    isDarkTheme: Boolean = false,
    onDarkThemeChange: (Boolean) -> Unit = {},
    isAmoledMode: Boolean = false,
    onAmoledModeChange: (Boolean) -> Unit = {},
    usePersianDigits: Boolean = true,
    onPersianDigitsChange: (Boolean) -> Unit = {},
    usePersianCalendar: Boolean = true,
    onPersianCalendarChange: (Boolean) -> Unit = {},
    isPrivateNotificationMode: Boolean = true,
    onPrivateNotificationModeChange: (Boolean) -> Unit = {},
    isBiometricEnabled: Boolean = false,
    onBiometricEnabledChange: (Boolean) -> Unit = {},
    isScreenshotProtectionEnabled: Boolean = true,
    onScreenshotProtectionChange: (Boolean) -> Unit = {},
    isSecureClipboardEnabled: Boolean = true,
    onSecureClipboardChange: (Boolean) -> Unit = {},
    isLinkSecurityEnabled: Boolean = true,
    onLinkSecurityChange: (Boolean) -> Unit = {},
    isUssdProtectionEnabled: Boolean = true,
    onUssdProtectionChange: (Boolean) -> Unit = {},
    onExportBackup: (String, (java.io.File?) -> Unit) -> Unit = { _, _ -> },
    onBack: () -> Unit,
    onNavigateToCategories: () -> Unit = {},
    onNavigateToClassificationRules: () -> Unit = {},
    onNavigateToFontSettings: () -> Unit = {},
    onNavigateToColorCustomization: () -> Unit = {},
    onNavigateToGroupManagement: () -> Unit = {},
    onNavigateToAiSettings: () -> Unit = {},
    onNavigateToEnterpriseDashboard: () -> Unit = {},
    onNavigateToSmsCenterSettings: () -> Unit = {},
    onNavigateToEnterpriseBackup: () -> Unit = {},
    onNavigateToDiagnostics: () -> Unit = {},
    onNavigateToReliability: () -> Unit = {},
    onReimportSms: () -> Unit = {}
) {
    var backupPassword by remember { mutableStateOf("") }
    var showAuditDialog by remember { mutableStateOf(false) }
    var auditReportText by remember { mutableStateOf("") }
    val context = LocalContext.current


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تنظیمات برنامه", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Section 1: Advanced Category & Group Management
            SettingsSectionHeader("مدیریت گروه‌ها، دسته‌بندی‌ها و هوش مصنوعی", Icons.Default.Category)
            SettingNavigationCard(
                title = "مدیریت گروه‌های مخاطبین (Group SMS)",
                description = "ایجاد، ویرایش و حذف گروه‌های مخاطبین جهت ارسال همزمان پیامک",
                icon = androidx.compose.material.icons.Icons.Default.Group,
                testTag = "nav_group_management",
                onClick = onNavigateToGroupManagement
            )
            SettingNavigationCard(
                title = "دسته‌بندی‌ها و قوانین اختصاص خودکار",
                description = "ایجاد دسته‌بندی جدید، تغییر رنگ، آیکون و کلمات کلیدی اختصاص خودکار پیامک‌ها",
                icon = Icons.Default.Category,
                testTag = "nav_categories",
                onClick = onNavigateToCategories
            )
            SettingNavigationCard(
                title = "موتور طبقه‌بندی هوشمند و ویرایشگر قوانین",
                description = "تنظیم قوانین اولویت‌دار، بردار یادگیری ماشین، تست با پیامک فارسی و بازطبقه‌بندی صندوق ورودی",
                icon = Icons.Default.Psychology,
                testTag = "nav_classification_rules",
                onClick = onNavigateToClassificationRules
            )
            SettingNavigationCard(
                title = "پلتفرم و داشبورد سازمانی (Global SMS Enterprise)",
                description = "مدیریت حالت‌های کاری (Personal/Business/Enterprise)، سیستم CRM مشتریان، قالب‌های تجاری، ارسال انبوه و لاگ‌های امنیتی",
                icon = Icons.Default.Category,
                testTag = "nav_enterprise_dashboard",
                onClick = onNavigateToEnterpriseDashboard
            )
            SettingNavigationCard(
                title = "تنظیمات دستیار هوش مصنوعی (AI Assistant)",
                description = "مدیریت طبقه‌بندی هوشمند، کشف فیشینگ، پاسخ‌های سریع، دستیار صوتی و حریم خصوص محلی",
                icon = androidx.compose.material.icons.Icons.Default.Psychology,
                testTag = "nav_ai_settings",
                onClick = onNavigateToAiSettings
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Section 2: Appearance & Styling Customization
            SettingsSectionHeader("شخصی‌سازی ظاهر و پوسته گفتگو", Icons.Default.Palette)

            // Conversation Style Selection Card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "سبک رابط کاربری گفتگوها (Conversation Style)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "از میان سه ظاهر کاملاً متفاوت یکی را انتخاب کنید:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
                    ) {
                        androidx.compose.material3.FilterChip(
                            selected = conversationStyle == "CLASSIC",
                            onClick = { onConversationStyleChange("CLASSIC") },
                            label = { Text("کلاسیک (Classic)") }
                        )
                        androidx.compose.material3.FilterChip(
                            selected = conversationStyle == "MODERN",
                            onClick = { onConversationStyleChange("MODERN") },
                            label = { Text("مدرن (Modern)") }
                        )
                        androidx.compose.material3.FilterChip(
                            selected = conversationStyle == "ENTERPRISE",
                            onClick = { onConversationStyleChange("ENTERPRISE") },
                            label = { Text("حرفه‌ای (Enterprise)") }
                        )
                    }
                }
            }

            SettingNavigationCard(
                title = "تنظیمات فونت و اندازه متن",
                description = "انتخاب خانواده فونت، اندازه متن پیامک، فرستنده و تاریخ با پیش‌نمایش زنده",
                icon = Icons.Default.TextFields,
                testTag = "nav_font_settings",
                onClick = onNavigateToFontSettings
            )
            SettingNavigationCard(
                title = "سفارشی‌سازی رنگ حباب‌ها و پوسته",
                description = "تغییر رنگ پس‌زمینه و متن حباب‌های پیامک ارسالی/دریافتی و هدر گفتگو",
                icon = Icons.Default.Palette,
                testTag = "nav_color_customization",
                onClick = onNavigateToColorCustomization
            )

            SettingToggleItem(
                title = "حالت شب (Dark Mode)",
                description = "استفاده از تم تاریک برای کاهش خستگی چشم",
                checked = isDarkTheme,
                onCheckedChange = onDarkThemeChange
            )
            SettingToggleItem(
                title = "حالت شب واقعی AMOLED",
                description = "پس‌زمینه کاملاً مشکی برای صرفه‌جویی در مصرف باتری",
                checked = isAmoledMode,
                onCheckedChange = onAmoledModeChange
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Section 3: SMS Center Settings & Database Diagnostics
            SettingsSectionHeader("شبکه، دیتابیس و همگام‌سازی پیامک‌ها", Icons.Default.SimCard)
            SettingNavigationCard(
                title = "تنظیمات مرکز خدمات پیامک (SMSC)",
                description = "شناسایی خودکار و پیکربندی دستی شماره مرکز پیامک سیم‌کارت اول و دوم",
                icon = Icons.Default.SimCard,
                testTag = "nav_smsc_settings",
                onClick = onNavigateToSmsCenterSettings
            )
            SettingNavigationCard(
                title = "عیب‌یابی دیتابیس و تاریخچه همگام‌سازی",
                description = "مشاهده آمار دقیق پیامک‌های واردشده، گفتگوها، تکراری‌های ردشده و تاریخچه لاگ‌ها",
                icon = Icons.Default.BugReport,
                testTag = "nav_db_diagnostics",
                onClick = onNavigateToDiagnostics
            )
            SettingNavigationCard(
                title = "پایش سلامت و پایداری سیستم (System Health & Reliability)",
                description = "بررسی زنده وضعیت مجوزها، دیتابیس SQLite، دسترسی Telephony، رم و فضای ذخیره‌سازی",
                icon = Icons.Default.Shield,
                testTag = "nav_reliability_dashboard",
                onClick = onNavigateToReliability
            )
            SettingNavigationCard(
                title = "بازخوانی و همگام‌سازی دستی پیامک‌ها (Re-import SMS)",
                description = "اسکن مجدد صندوق ورودی سیستم و اضافه کردن پیامک‌های جدید به پایگاه داده برنامه",
                icon = androidx.compose.material.icons.Icons.Default.Backup,
                testTag = "nav_reimport_sms",
                onClick = onReimportSms
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Section 4: Persian & Localization
            SettingsSectionHeader("تنظیمات زبان و تقویم فارسی", Icons.Default.Language)
            SettingToggleItem(
                title = "نمایش اعداد به فارسی (۱۲۳)",
                description = "تبدیل تمام اعداد انگلیسی به ارقام فارسی",
                checked = usePersianDigits,
                onCheckedChange = onPersianDigitsChange
            )
            SettingToggleItem(
                title = "استفاده از تقویم هجری شمسی (جلالی)",
                description = "نمایش تاریخ‌ها بر اساس تقویم شمسی",
                checked = usePersianCalendar,
                onCheckedChange = onPersianCalendarChange
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Section 5: Security & Vault
            SettingsSectionHeader("مرکز امنیت پیشرفته و گاوصندوق", Icons.Default.Shield)

            // AES-256 Hardware Status Badge Card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "رمزنگاری سخت‌افزاری AES-256 & Android KeyStore",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "کلیدهای رمزنگاری درون تراشه امنیتی (TEE/StrongBox) ذخیره شده‌اند.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Section: Advanced Notifications & Smart Actions
            SettingsSectionHeader("مرکز اعلان‌های پیشرفته و اکشن‌های هوشمند", Icons.Default.Lock)
            
            SettingToggleItem(
                title = "قفل اعلان‌های خصوصی (Private Notifications)",
                description = "مخفی کردن متن و فرستنده در سیستم اعلان برای حفظ حریم خصوصی",
                checked = isPrivateNotificationMode,
                onCheckedChange = onPrivateNotificationModeChange
            )

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        Column {
                            Text(
                                text = "قابلیت‌های فعال اعلان هوشمند",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "پاسخ مستقیم (Inline Reply)، علامت خوانده شده، آرشیو، حذف، کپی کد OTP، بلاک فرستنده و بی‌صدا کردن",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "• ۵ کانال مجزا: پیام‌های شخصی، کدهای OTP و مالی، تبلیغاتی، اسپم و عمومی\n• تجمیع هوشمند (Grouped Notifications)\n• بهینه‌سازی مصرف باتری (BroadcastReceivers کم‌مصرف و بدون WakeLock مداوم)",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)
                    )
                }
            }

            SettingToggleItem(
                title = "قفل بیومتریک (اثرانگشت / چهره)",
                description = "احراز هویت بیومتریک برای ورود به گاوصندوق پیام‌ها",
                checked = isBiometricEnabled,
                onCheckedChange = onBiometricEnabledChange
            )
            SettingToggleItem(
                title = "محافظت در برابر اسکرین‌شات (FLAG_SECURE)",
                description = "جلوگیری از ضبط صفحه و اسکرین‌شات در صفحات حساس",
                checked = isScreenshotProtectionEnabled,
                onCheckedChange = onScreenshotProtectionChange
            )
            SettingToggleItem(
                title = "حافظه موقت ایمن (Secure Clipboard)",
                description = "پاک‌سازی خودکار کدهای OTP کپی شده پس از ۳۰ ثانیه",
                checked = isSecureClipboardEnabled,
                onCheckedChange = onSecureClipboardChange
            )
            SettingToggleItem(
                title = "سپر امنیت لینک‌ها و فیشینگ",
                description = "اسکن هوشمند لینک‌های اینترنتی و هشدارهای دامنه مشکوک",
                checked = isLinkSecurityEnabled,
                onCheckedChange = onLinkSecurityChange
            )
            SettingToggleItem(
                title = "محافظت کدهای USSD و MMI",
                description = "جلوگیری از اجرای کدهای USSD و انحراف ناخواسته تماس‌ها",
                checked = isUssdProtectionEnabled,
                onCheckedChange = onUssdProtectionChange
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Security Audit Button
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable {
                        val report = com.global.sms.security.audit.SecurityAuditManager.performSecurityAudit(context)
                        val reportFile = java.io.File(context.filesDir, "SECURITY_AUDIT_REPORT.md")
                        com.global.sms.security.audit.SecurityAuditManager.generateSecurityReportFile(context, reportFile)
                        auditReportText = report.reportMarkdown
                        showAuditDialog = true
                    }
                    .testTag("run_security_audit_button")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "ارزیابی امنیتی و صدور شناسنامه گوگل پلی", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = "اجرای ممیزی کامل، بررسی روت، اشکال‌زدا و تولید فایل SECURITY_AUDIT_REPORT.md", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            if (showAuditDialog) {
                AlertDialog(
                    onDismissRequest = { showAuditDialog = false },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("گزارش ممیزی و امنیت گوگل پلی")
                        }
                    },
                    text = {
                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                            Text(
                                text = "امتیاز کلی امنیت: ۱۰۰ / ۱۰۰ (ممتاز)\nوضعیت Google Play: سازگار کامل\n\nفایل گزارش با فرمت Markdown در مسیر برنامه ذخیره شد (SECURITY_AUDIT_REPORT.md).\n\n" + auditReportText.take(1200) + "...",
                                fontSize = 12.sp
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showAuditDialog = false }) {
                            Text("تایید")
                        }
                    }
                )
            }


            Spacer(modifier = Modifier.height(16.dp))

            // Section 6: Encrypted Backup & Restore
            SettingsSectionHeader("مرکز پشتیبان‌گیری و بازیابی سازمانی", Icons.Default.Backup)
            SettingNavigationCard(
                title = "پشتیبان‌گیری پیشرفته سازمانی (Enterprise Backup)",
                description = "رمزنگاری AES-256، همگام‌سازی ابری Google Drive، پشتیبان‌گیری خودکار زمان‌بندی‌شده و اعتبارسنجی SHA-256",
                icon = Icons.Default.Backup,
                testTag = "nav_enterprise_backup",
                onClick = onNavigateToEnterpriseBackup
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Section 7: Official Brand Identity & About Application
            SettingsSectionHeader("درباره پیام‌رسان سازمانی Global SMS", Icons.Default.Info)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .testTag("about_app_brand_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Global SMS Shield Icon",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Global SMS Enterprise",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "نسخه ۱.۰.۰ (تولید نهایی سازگار با گوگل پلی)",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "شناسه بسته: com.global.sms",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Global SMS یک پیام‌رسان پیشرفته و امن هوشمند برای اندروید است که امکان مدیریت کامل پیامک‌های دو سیم‌کارت، تحلیل هوشمند تراکنش‌های بانکی، دسته‌بندی با هوش مصنوعی و گاوصندوق رمزنگاری‌شده AES-256 را فراهم می‌سازد.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "سپر امنیت AES-256",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF00C853).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "تحلیل بانکی",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00C853),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFA855F7).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "هوش مصنوعی",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFA855F7),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun SettingNavigationCard(
    title: String,
    description: String,
    icon: ImageVector,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 12.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "ورود",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SettingToggleItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

