package com.global.sms.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.global.sms.core.classifier.ClassificationResult
import com.global.sms.core.classifier.SmsClassifierEngine
import com.global.sms.data.entity.ClassificationRuleEntity
import com.global.sms.data.entity.MessageCategory
import com.global.sms.ui.viewmodels.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassificationRuleEditorScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val rules by viewModel.classificationRulesState.collectAsStateWithLifecycle()
    val progressState by viewModel.classificationProgressState.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }
    var ruleToEdit by remember { mutableStateOf<ClassificationRuleEntity?>(null) }
    var ruleToDelete by remember { mutableStateOf<ClassificationRuleEntity?>(null) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "موتور طبقه‌بندی هوشمند و قوانین پیامک",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            floatingActionButton = {
                if (selectedTab == 0) {
                    FloatingActionButton(
                        onClick = {
                            ruleToEdit = null
                            showAddDialog = true
                        },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.testTag("add_rule_fab")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "افزودن قانون جدید")
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Navigation Tabs
                PrimaryTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("قوانین و اولویت‌ها", style = MaterialTheme.typography.labelMedium) },
                        icon = { Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("تست با پیامک فارسی", style = MaterialTheme.typography.labelMedium) },
                        icon = { Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("پردازش در پس‌زمینه", style = MaterialTheme.typography.labelMedium) },
                        icon = { Icon(Icons.Default.Autorenew, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                when (selectedTab) {
                    0 -> RulesListTab(
                        rules = rules,
                        onEdit = { rule ->
                            ruleToEdit = rule
                            showAddDialog = true
                        },
                        onDelete = { rule -> ruleToDelete = rule },
                        onToggle = { id, isEnabled -> viewModel.toggleClassificationRule(id, isEnabled) }
                    )
                    1 -> ClassifierPlaygroundTab(rules = rules)
                    2 -> BackgroundProcessingTab(
                        progressState = progressState,
                        onStartReclassification = { viewModel.startBackgroundReclassification() }
                    )
                }
            }

            // Dialog for Add/Edit Rule
            if (showAddDialog) {
                RuleDialog(
                    initialRule = ruleToEdit,
                    onDismiss = {
                        showAddDialog = false
                        ruleToEdit = null
                    },
                    onSave = { name, targetCat, keywords, senderPattern, ruleType, priority, isEnabled ->
                        val targetRule = ruleToEdit
                        if (targetRule == null) {
                            viewModel.addClassificationRule(name, targetCat, keywords, senderPattern, ruleType, priority, isEnabled)
                        } else {
                            viewModel.updateClassificationRule(
                                targetRule.copy(
                                    name = name,
                                    targetCategory = targetCat,
                                    keywords = keywords,
                                    senderPattern = senderPattern,
                                    ruleType = ruleType,
                                    priority = priority,
                                    isEnabled = isEnabled
                                )
                            )
                        }
                        showAddDialog = false
                        ruleToEdit = null
                    }
                )
            }

            // Dialog for Delete Confirmation
            ruleToDelete?.let { rule ->
                AlertDialog(
                    onDismissRequest = { ruleToDelete = null },
                    title = { Text("حذف قانون طبقه‌بندی") },
                    text = { Text("آیا از حذف قانون «${rule.name}» اطمینان دارید؟") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.deleteClassificationRule(rule.id)
                                ruleToDelete = null
                            }
                        ) {
                            Text("حذف", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { ruleToDelete = null }) {
                            Text("انصراف")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun RulesListTab(
    rules: List<ClassificationRuleEntity>,
    onEdit: (ClassificationRuleEntity) -> Unit,
    onDelete: (ClassificationRuleEntity) -> Unit,
    onToggle: (Long, Boolean) -> Unit
) {
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }
    var showOnlyActive by remember { mutableStateOf(false) }

    val filteredRules = remember(rules, selectedCategoryFilter, showOnlyActive) {
        rules.filter { rule ->
            val matchesCat = selectedCategoryFilter == null || rule.targetCategory == selectedCategoryFilter
            val matchesActive = !showOnlyActive || rule.isEnabled
            matchesCat && matchesActive
        }
    }

    val availableCategories = remember(rules) {
        rules.map { it.targetCategory }.distinct()
    }

    if (rules.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Tune,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "هیچ قانونی تعریف نشده است",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "برای ایجاد قانون طبقه‌بندی هوشمند، روی دکمه + در پایین صفحه کلیک کنید.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "قوانین با اولویت بالاتر زودتر ارزیابی می‌شوند. در صورت عدم تطابق، هوش مصنوعی فعال می‌شود.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Quick Filter Row
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategoryFilter == null && !showOnlyActive,
                            onClick = {
                                selectedCategoryFilter = null
                                showOnlyActive = false
                            },
                            label = { Text("همه (${rules.size})") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = showOnlyActive,
                            onClick = { showOnlyActive = !showOnlyActive },
                            label = { Text("فقط فعال") },
                            leadingIcon = {
                                Icon(
                                    if (showOnlyActive) Icons.Default.CheckCircle else Icons.Default.Circle,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        )
                    }
                    items(availableCategories) { cat ->
                        FilterChip(
                            selected = selectedCategoryFilter == cat,
                            onClick = { selectedCategoryFilter = if (selectedCategoryFilter == cat) null else cat },
                            label = { Text(getCategoryPersianLabel(cat)) }
                        )
                    }
                }
            }

            if (filteredRules.isEmpty()) {
                item {
                    Text(
                        text = "قانونی مطابق با فیلتر انتخابی یافت نشد.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            } else {
                items(filteredRules, key = { it.id }) { rule ->
                    RuleCardItem(rule = rule, onEdit = onEdit, onDelete = onDelete, onToggle = onToggle)
                }
            }
        }
    }
}

@Composable
fun RuleCardItem(
    rule: ClassificationRuleEntity,
    onEdit: (ClassificationRuleEntity) -> Unit,
    onDelete: (ClassificationRuleEntity) -> Unit,
    onToggle: (Long, Boolean) -> Unit
) {
    val categoryColor = getCategoryColor(rule.targetCategory)
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("rule_card_${rule.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Tag
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = categoryColor.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, categoryColor)
                ) {
                    Text(
                        text = getCategoryPersianLabel(rule.targetCategory),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = categoryColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = rule.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1f)
                )

                // Priority Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = "اولویت: ${rule.priority}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Switch(
                    checked = rule.isEnabled,
                    onCheckedChange = { isChecked -> onToggle(rule.id, isChecked) },
                    modifier = Modifier.testTag("rule_toggle_${rule.id}")
                )
            }

            // Expandable conditions
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (rule.keywords.isNotBlank()) {
                        Text(
                            text = "کلمات کلیدی: ${rule.keywords}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (rule.senderPattern.isNotBlank()) {
                        Text(
                            text = "الگوی فرستنده: ${rule.senderPattern}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (rule.keywords.isNotBlank() || rule.senderPattern.isNotBlank()) {
                    TextButton(
                        onClick = { isExpanded = !isExpanded },
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = if (isExpanded) "بستن جزئیات" else "شرایط و الگوها",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Row {
                    IconButton(onClick = { onEdit(rule) }) {
                        Icon(Icons.Default.Edit, contentDescription = "ویرایش", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { onDelete(rule) }) {
                        Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
fun RuleDialog(
    initialRule: ClassificationRuleEntity?,
    onDismiss: () -> Unit,
    onSave: (name: String, targetCat: String, keywords: String, senderPattern: String, ruleType: String, priority: Int, isEnabled: Boolean) -> Unit
) {
    var name by remember { mutableStateOf(initialRule?.name ?: "") }
    var targetCategory by remember { mutableStateOf(initialRule?.targetCategory ?: "OTP") }
    var keywords by remember { mutableStateOf(initialRule?.keywords ?: "") }
    var senderPattern by remember { mutableStateOf(initialRule?.senderPattern ?: "") }
    var ruleType by remember { mutableStateOf(initialRule?.ruleType ?: "KEYWORD") }
    var priorityText by remember { mutableStateOf((initialRule?.priority ?: 50).toString()) }
    var isEnabled by remember { mutableStateOf(initialRule?.isEnabled ?: true) }
    var hasAttemptedSave by remember { mutableStateOf(false) }

    val isNameError = hasAttemptedSave && name.isBlank()

    val categoriesList = listOf(
        "OTP" to "کد تایید (OTP)",
        "BANK" to "بانک",
        "TRANSACTIONS" to "تراکنش مالی",
        "SPAM" to "اسپم",
        "ADVERTISEMENT" to "تبلیغات",
        "SHOPPING" to "خرید آنلاین",
        "DELIVERY" to "مرسوله و پست",
        "GOVERNMENT" to "سامانه دولتی",
        "BUSINESS" to "اداری و تجاری",
        "PERSONAL" to "شخصی",
        "UNKNOWN" to "نامشخص"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialRule == null) "افزودن قانون هوشمند جدید" else "ویرایش قانون طبقه‌بندی") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("نام قانون *") },
                    placeholder = { Text("مثلاً: فاکتورهای خریدهای آنلاین") },
                    singleLine = true,
                    isError = isNameError,
                    supportingText = if (isNameError) {
                        { Text("نام قانون الزامی است", color = MaterialTheme.colorScheme.error) }
                    } else null,
                    modifier = Modifier.fillMaxWidth().testTag("rule_name_input")
                )

                Text("دسته‌بندی هدف:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categoriesList) { (key, label) ->
                        val isSelected = targetCategory == key
                        FilterChip(
                            selected = isSelected,
                            onClick = { targetCategory = key },
                            label = { Text(label) }
                        )
                    }
                }

                OutlinedTextField(
                    value = priorityText,
                    onValueChange = { priorityText = it.filter { c -> c.isDigit() } },
                    label = { Text("وزن اولویت (1 تا 100)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Text("شرایط تطابق پیامک (اختیاری):", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = keywords,
                    onValueChange = { keywords = it },
                    label = { Text("کلمات کلیدی (با ویرگول)") },
                    placeholder = { Text("کد تایید, رمز پویا, واریز") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = senderPattern,
                    onValueChange = { senderPattern = it },
                    label = { Text("الگوی فرستنده") },
                    placeholder = { Text("Digikala, Tipax, 983000*") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    hasAttemptedSave = true
                    if (name.isNotBlank()) {
                        val prio = priorityText.toIntOrNull() ?: 50
                        onSave(name.trim(), targetCategory, keywords.trim(), senderPattern.trim(), ruleType, prio, isEnabled)
                    }
                },
                modifier = Modifier.testTag("save_rule_button")
            ) {
                Text("ذخیره")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف") }
        }
    )
}

@Composable
fun ClassifierPlaygroundTab(rules: List<ClassificationRuleEntity>) {
    val samplePersianSmsList = listOf(
        "بانک ملت" to "واریز به حساب: 6104****1234\nمبلغ: 2,500,000 ریال\nموجودی: 18,400,000 ریال\nبانک ملت",
        "دیجی‌کالا (OTP)" to "کد تایید ورود به دیجی‌کالا: 849201\nاعتبار: 3 دقیقه",
        "مرسوله تیپاکس" to "مرسوله تیپاکس با کد رهگیری 987123456 به مامور توزیع تحویل گردید.",
        "سفارش ترب" to "سفارش شما در فروشگاه آنلاین ترب ثبت گردید. مبلغ فاکتور: 450,000 تومان",
        "سامانه ثنا" to "ابلاغیه جدید در سامانه ثنا ثبت شد. جهت مشاهده به adliran.ir مراجعه کنید.",
        "تبلیغ رایگان" to "برنده 100 میلیون ریال جایزه قرعه‌کشی شوید! جهت فعالسازی رایگان کلیک کنید",
        "پیام شخصی" to "سلام علی جان، جلسه فردا ساعت ۱۰ برقرار هست؟"
    )

    var testSender by remember { mutableStateOf("Mellat") }
    var testBody by remember { mutableStateOf(samplePersianSmsList[0].second) }

    val classificationResult = remember(testSender, testBody, rules) {
        SmsClassifierEngine.classifyMessage(testSender, testBody, rules)
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                "نمونه‌های پیش‌فرض پیامک‌های فارسی برای آزمایش:",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(samplePersianSmsList) { (title, bodyText) ->
                    SuggestionChip(
                        onClick = {
                            testBody = bodyText
                            testSender = when {
                                title.contains("ملت") -> "Mellat"
                                title.contains("دیجی") -> "Digikala"
                                title.contains("تیپاکس") -> "Tipax"
                                title.contains("ترب") -> "Torob"
                                title.contains("ثنا") -> "SANA"
                                else -> "983000123"
                            }
                        },
                        label = { Text(title) }
                    )
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("آزمایشگاه هوش مصنوعی طبقه‌بندی پیامک", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

                    OutlinedTextField(
                        value = testSender,
                        onValueChange = { testSender = it },
                        label = { Text("فرستنده") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = testBody,
                        onValueChange = { testBody = it },
                        label = { Text("متن پیامک فارسی") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        item {
            ClassificationResultDisplayCard(result = classificationResult)
        }
    }
}

@Composable
fun ClassificationResultDisplayCard(result: ClassificationResult) {
    val categoryColor = getCategoryColor(result.category.name)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = categoryColor.copy(alpha = 0.08f)),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, categoryColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = getCategoryIcon(result.category),
                        contentDescription = null,
                        tint = categoryColor,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "دسته‌بندی شناسایی شده",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = getCategoryPersianLabel(result.category.name),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = categoryColor
                        )
                    }
                }

                // Confidence Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = categoryColor
                ) {
                    Text(
                        text = "اطمینان: ${(result.confidenceScore * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            HorizontalDivider(color = categoryColor.copy(alpha = 0.2f))

            if (result.matchedRuleName != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Verified, contentDescription = null, tint = categoryColor, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "منطبق با قانون: ${result.matchedRuleName} (اولویت: ${result.matchedRulePriority})",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Psychology, contentDescription = null, tint = categoryColor, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "تشخیص از طریق بردار ویژگی‌های آماری یادگیری ماشین (ML Score Vector)",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (result.detectedFeatures.isNotEmpty()) {
                Text(
                    text = "کلمات و نشانه‌های شناسایی شده: ${result.detectedFeatures.joinToString(" ، ")}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // ML Score Weights Matrix Breakdown
            if (result.featureWeightsMap.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("توزیع امتیاز احتمال دسته‌ها (ML Vector Weights):", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    result.featureWeightsMap.filter { it.value > 0.0f }.forEach { (cat, score) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = getCategoryPersianLabel(cat.name),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.width(110.dp)
                            )
                            LinearProgressIndicator(
                                progress = { (score / 3.0f).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = getCategoryColor(cat.name)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = String.format("%.1f", score),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BackgroundProcessingTab(
    progressState: com.global.sms.ui.viewmodels.ClassificationProgressState,
    onStartReclassification: () -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "موتور پردازش پس‌زمینه و دسته‌بندی انبوه",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "می‌توانید با اجرای این بخش، تمام پیامک‌های موجود در صندوق ورودی خود را بر اساس آخرین قوانین و مدل یادگیری ماشین به صورت خودکار طبقه‌بندی مجدد کنید.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = onStartReclassification,
                        enabled = !progressState.isRunning,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("start_batch_reclassify_button")
                    ) {
                        if (progressState.isRunning) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("در حال طبقه‌بندی در پس‌زمینه...")
                        } else {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("شروع طبقه‌بندی هوشمند صندوق ورودی")
                        }
                    }
                }
            }
        }

        if (progressState.isRunning || progressState.isCompleted || progressState.processedCount > 0) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("وضعیت پیشرفت پردازش:", style = MaterialTheme.typography.titleSmall)
                            Text(
                                "${progressState.processedCount} از ${progressState.totalCount} پیامک",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        val progressPct = if (progressState.totalCount > 0) progressState.processedCount.toFloat() / progressState.totalCount.toFloat() else 0f
                        LinearProgressIndicator(
                            progress = { progressPct },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                        )

                        if (progressState.isCompleted) {
                            Text("طبقه‌بندی همگانی پیامک‌ها با موفقیت به پایان رسید!", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }

                        if (progressState.categoryCounts.isNotEmpty()) {
                            Text("توزیع پیامک‌های طبقه‌بندی شده:", style = MaterialTheme.typography.labelMedium)
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                progressState.categoryCounts.forEach { (cat, count) ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(getCategoryPersianLabel(cat.name), style = MaterialTheme.typography.bodySmall)
                                        Text("$count پیامک", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
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

fun getCategoryColor(categoryName: String): Color {
    return when (categoryName.uppercase()) {
        "OTP" -> Color(0xFFD32F2F) // Red
        "BANK" -> Color(0xFF0288D1) // Blue
        "TRANSACTIONS" -> Color(0xFF388E3C) // Green
        "SPAM" -> Color(0xFFC2185B) // Pink/Red
        "ADVERTISEMENT" -> Color(0xFFF57C00) // Orange
        "SHOPPING" -> Color(0xFF00796B) // Teal
        "DELIVERY" -> Color(0xFF7B1FA2) // Purple
        "GOVERNMENT" -> Color(0xFF455A64) // Slate
        "BUSINESS" -> Color(0xFF512DA8) // Deep Purple
        "PERSONAL" -> Color(0xFF1976D2) // Blue
        else -> Color(0xFF757575)
    }
}

fun getCategoryPersianLabel(categoryName: String): String {
    return when (categoryName.uppercase()) {
        "OTP" -> "کد تایید (OTP)"
        "BANK" -> "بانک"
        "TRANSACTIONS" -> "تراکنش مالی"
        "SPAM" -> "اسپم"
        "ADVERTISEMENT" -> "تبلیغات"
        "SHOPPING" -> "خرید آنلاین"
        "DELIVERY" -> "مرسوله و پست"
        "GOVERNMENT" -> "سامانه دولتی"
        "BUSINESS" -> "اداری و تجاری"
        "PERSONAL" -> "شخصی"
        "WORK" -> "کاری"
        "IMPORTANT" -> "مهم"
        "PRIVATE" -> "خصوصی"
        else -> "نامشخص"
    }
}

fun getCategoryIcon(category: MessageCategory): androidx.compose.ui.graphics.vector.ImageVector {
    return when (category) {
        MessageCategory.OTP -> Icons.Default.Lock
        MessageCategory.BANK -> Icons.Default.AccountBalance
        MessageCategory.TRANSACTIONS -> Icons.Default.AttachMoney
        MessageCategory.SPAM -> Icons.Default.Report
        MessageCategory.ADVERTISEMENT -> Icons.Default.Campaign
        MessageCategory.SHOPPING -> Icons.Default.ShoppingCart
        MessageCategory.DELIVERY -> Icons.Default.LocalShipping
        MessageCategory.GOVERNMENT -> Icons.Default.Gavel
        MessageCategory.BUSINESS -> Icons.Default.BusinessCenter
        MessageCategory.WORK -> Icons.Default.Work
        MessageCategory.IMPORTANT -> Icons.Default.Star
        MessageCategory.PRIVATE -> Icons.Default.VpnKey
        else -> Icons.AutoMirrored.Filled.Chat
    }
}
