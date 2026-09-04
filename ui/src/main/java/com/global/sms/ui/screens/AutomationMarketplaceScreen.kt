package com.global.sms.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.global.sms.ui.viewmodels.GlobalSmsViewModel

data class AutomationMarketplaceTemplate(
    val templateId: String,
    val title: String,
    val category: String,
    val description: String,
    val triggerPattern: String,
    val actionSummary: String,
    val icon: ImageVector,
    val isActivated: Boolean = false,
    val executionCount: Int = 0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutomationMarketplaceScreen(
    viewModel: GlobalSmsViewModel? = null,
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") }

    var templates by remember {
        mutableStateOf(
            listOf(
                AutomationMarketplaceTemplate(
                    templateId = "tmpl_bank_mon",
                    title = "پایش هوشمند تراکنش‌های بانکی",
                    category = "FINANCE",
                    description = "ثبت، تحلیل و دسته‌بندی خودکار پیامک‌های واریز/برداشت تمام بانک‌های کشور بدون نیاز به اینترنت.",
                    triggerPattern = "کارت / واریز / برداشت / برداشت از حساب",
                    actionSummary = "ثبت در بیلان مالی و هشدارهای حاد",
                    icon = Icons.Default.AccountBalance,
                    isActivated = false,
                    executionCount = 0
                ),
                AutomationMarketplaceTemplate(
                    templateId = "tmpl_cust_followup",
                    title = "پیگیری خودکار سرنخ و مشتریان (CRM)",
                    category = "CRM",
                    description = "ارسال خودکار پیام تشکر، کاتالوگ و استعلام رضایت‌سنجی ۲۴ ساعت پس از اولین پیامک مشتری.",
                    triggerPattern = "قیمت / سفارش / استعلام / ثبت نام",
                    actionSummary = "ایجاد اقدام در AI Copilot و پیامک زمان‌بندی شده",
                    icon = Icons.Default.People,
                    isActivated = false,
                    executionCount = 0
                ),
                AutomationMarketplaceTemplate(
                    templateId = "tmpl_otp_assistant",
                    title = "دستیار رمز پویا و کدهای تایید (OTP)",
                    category = "UTILITY",
                    description = "کپی خودکار رمزهای یکبارمصرف در حافظه، پاکسازی زمان‌بندی شده کدهای منقضی جهت حفظ امنیت.",
                    triggerPattern = "کد تایید / رمز پویا / OTP / Verification Code",
                    actionSummary = "کپی در Clipboard و درج در نوار اعلان",
                    icon = Icons.Default.LockReset,
                    isActivated = false,
                    executionCount = 0
                ),
                AutomationMarketplaceTemplate(
                    templateId = "tmpl_meeting_remind",
                    title = "یادآور خودکار جلسات و رویدادها",
                    category = "PRODUCTIVITY",
                    description = "تشخیص خودکار قرار ملاقات‌ها در متن پیامک‌ها و تنظیم یادآور در تقویم محلی هوش مصنوعی.",
                    triggerPattern = "جلسه / قرار / ساعت / فردا / جلسه هماهنگی",
                    actionSummary = "افزودن به لیست یادآورها و ارسال هشدار ۳۰ دقیقه قبل",
                    icon = Icons.Default.Event,
                    isActivated = false,
                    executionCount = 0
                ),
                AutomationMarketplaceTemplate(
                    templateId = "tmpl_fraud_protect",
                    title = "سپر ضدکلاهبرداری و فیشینگ (Anti-Smishing)",
                    category = "SECURITY",
                    description = "بررسی لینک‌ها و شماره‌های ناشناس، مسدودسازی خودکار پیامک‌های جعلی یارانه، سهام عدالت و عدل‌ایران.",
                    triggerPattern = "لینک آلوده / adliran / darchal / ثبت نام یارانه",
                    actionSummary = "انتقال فوری به پوشه اسپم و ارسال هشدار امنیتی",
                    icon = Icons.Default.Security,
                    isActivated = false,
                    executionCount = 0
                )
            )
        )
    }

    val filteredTemplates = templates.filter { tmpl ->
        (selectedCategory == "ALL" || tmpl.category == selectedCategory) &&
                (tmpl.title.contains(searchQuery) || tmpl.description.contains(searchQuery))
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .testTag("automation_marketplace_screen"),
            topBar = {
                TopAppBar(
                    title = { Text("بازارگاه سناریوهای خودکارسازی AI (آفلاین)", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("marketplace_back_button")) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                // Header Info Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("marketplace_info_card")
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Storefront,
                            contentDescription = null,
                            modifier = Modifier.size(36.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "AI Automation Marketplace (100% Offline)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                "قالب‌های آماده خودکارسازی جهت ارتقای بهره‌وری سازمانی، امنیت مالی و مدیریت ارتباط با مشتری.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("marketplace_search_input"),
                    placeholder = { Text("جستجوی قالب خودکارسازی...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Category Filter Chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilterChip(
                        selected = selectedCategory == "ALL",
                        onClick = { selectedCategory = "ALL" },
                        label = { Text("همه") },
                        modifier = Modifier.testTag("filter_all")
                    )
                    FilterChip(
                        selected = selectedCategory == "FINANCE",
                        onClick = { selectedCategory = "FINANCE" },
                        label = { Text("مالی و بانکی") },
                        modifier = Modifier.testTag("filter_finance")
                    )
                    FilterChip(
                        selected = selectedCategory == "SECURITY",
                        onClick = { selectedCategory = "SECURITY" },
                        label = { Text("امنیتی") },
                        modifier = Modifier.testTag("filter_security")
                    )
                    FilterChip(
                        selected = selectedCategory == "CRM",
                        onClick = { selectedCategory = "CRM" },
                        label = { Text("مشتریان") },
                        modifier = Modifier.testTag("filter_crm")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Templates List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredTemplates, key = { it.templateId }) { template ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("template_card_${template.templateId}"),
                            colors = CardDefaults.cardColors(
                                containerColor = if (template.isActivated)
                                    MaterialTheme.colorScheme.surface
                                else
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            template.icon,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(28.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            template.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                    Switch(
                                        checked = template.isActivated,
                                        onCheckedChange = { checked ->
                                            templates = templates.map {
                                                if (it.templateId == template.templateId) it.copy(isActivated = checked) else it
                                            }
                                            viewModel?.toggleAutomationTemplate(template.templateId, checked)
                                            Toast.makeText(
                                                context,
                                                if (checked) "الگوی '${template.title}' فعال شد" else "الگو غیرفعال شد",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        },
                                        modifier = Modifier.testTag("switch_${template.templateId}")
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(template.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider()
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            "الگوی محرک: ${template.triggerPattern}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            "دفعات اجرای موفق: ${template.executionCount}",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                    Badge(
                                        containerColor = if (template.isActivated) Color(0xFF2E7D32) else Color.Gray
                                    ) {
                                        Text(
                                            if (template.isActivated) "فعال در سیستم" else "غیرفعال",
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
