package com.global.sms.ui.screens

import com.global.sms.ui.viewmodels.SettingsViewModel

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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.global.sms.core.rule.CategoryRuleEngine
import com.global.sms.data.entity.CategoryEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManagementScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val categories by viewModel.categoriesState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var categoryToEdit by remember { mutableStateOf<CategoryEntity?>(null) }
    var categoryToDelete by remember { mutableStateOf<CategoryEntity?>(null) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "مدیریت دسته‌بندی‌های پیامک",
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
                FloatingActionButton(
                    onClick = {
                        categoryToEdit = null
                        showAddDialog = true
                    },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("add_category_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "افزودن دسته‌بندی جدید")
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                if (categories.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "هیچ دسته‌بندی یافت نشد.\nبرای افزودن دکمه + را بزنید.",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            CategoryRuleTesterCard(categories = categories)
                        }

                        item {
                            Text(
                                "دسته‌بندی‌ها و قوانین اختصاص خودکار پیامک‌ها",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            )
                        }

                        items(categories, key = { it.id }) { category ->
                            CategoryCardItem(
                                category = category,
                                onEdit = {
                                    categoryToEdit = category
                                    showAddDialog = true
                                },
                                onDelete = {
                                    categoryToDelete = category
                                }
                            )
                        }
                    }
                }
            }

            // Dialog for Create / Edit Category
            if (showAddDialog) {
                CategoryDialog(
                    initialCategory = categoryToEdit,
                    onDismiss = {
                        showAddDialog = false
                        categoryToEdit = null
                    },
                    onSave = { name, description, icon, color, priority, autoRule ->
                        val targetCat = categoryToEdit
                        if (targetCat == null) {
                            viewModel.addCategory(name, description, icon, color, priority, autoRule)
                        } else {
                            viewModel.updateCategory(
                                targetCat.copy(
                                    name = name,
                                    description = description,
                                    icon = icon,
                                    color = color,
                                    priority = priority,
                                    autoRule = autoRule,
                                    updatedDate = System.currentTimeMillis()
                                )
                            )
                        }
                        showAddDialog = false
                        categoryToEdit = null
                    }
                )
            }

            // Confirmation dialog for Delete
            categoryToDelete?.let { cat ->
                AlertDialog(
                    onDismissRequest = { categoryToDelete = null },
                    title = { Text("حذف دسته‌بندی") },
                    text = { Text("آیا از حذف دسته‌بندی «${cat.name}» اطمینان دارید؟") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.deleteCategory(cat.id)
                                categoryToDelete = null
                            }
                        ) {
                            Text("حذف", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { categoryToDelete = null }) {
                            Text("انصراف")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun CategoryCardItem(
    category: CategoryEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val categoryColor = Color(category.color)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("category_item_${category.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon with Color Badge Background
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(categoryColor.copy(alpha = 0.15f))
                    .border(1.5.dp, categoryColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getIconVector(category.icon),
                    contentDescription = category.name,
                    tint = categoryColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Text info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = "اولویت: ${category.priority}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                if (category.description.isNotBlank()) {
                    Text(
                        text = category.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                if (category.autoRule.isNotBlank()) {
                    Text(
                        text = "قوانین خودکار: ${category.autoRule}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text(
                        text = "بدون قانون اختصاص خودکار",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            // Action Buttons
            IconButton(onClick = onEdit) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "ویرایش دسته‌بندی",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "حذف دسته‌بندی",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun CategoryDialog(
    initialCategory: CategoryEntity?,
    onDismiss: () -> Unit,
    onSave: (name: String, description: String, icon: String, color: Long, priority: Int, autoRule: String) -> Unit
) {
    var name by remember { mutableStateOf(initialCategory?.name ?: "") }
    var description by remember { mutableStateOf(initialCategory?.description ?: "") }
    var selectedIcon by remember { mutableStateOf(initialCategory?.icon ?: "Folder") }
    var selectedColor by remember { mutableStateOf(initialCategory?.color ?: 0xFF1A73E8) }
    var priorityText by remember { mutableStateOf((initialCategory?.priority ?: 10).toString()) }
    var autoRule by remember { mutableStateOf(initialCategory?.autoRule ?: "") }

    val iconOptions = listOf(
        "Folder" to Icons.Default.Folder,
        "AccountBalance" to Icons.Default.AccountBalance,
        "Work" to Icons.Default.Work,
        "Star" to Icons.Default.Star,
        "ShoppingCart" to Icons.Default.ShoppingCart,
        "People" to Icons.Default.People,
        "Notifications" to Icons.Default.Notifications,
        "Lock" to Icons.Default.Lock
    )

    val colorOptions = listOf(
        0xFF1A73E8, // Blue
        0xFF00658F, // Dark Blue/Teal
        0xFF006A6A, // Teal
        0xFF1E88E5, // Light Blue
        0xFF388E3C, // Emerald Green
        0xFFF57C00, // Orange
        0xFF70538C, // Purple
        0xFFBA1A1A, // Red
        0xFF984061  // Magenta
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (initialCategory == null) "ایجاد دسته‌بندی جدید" else "ویرایش دسته‌بندی")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("نام دسته‌بندی") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("category_name_input")
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("توضیحات کوتاه") },
                    placeholder = { Text("مثال: پیامک‌های مربوط به بانک‌ها و رمز پویا") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("انتخاب آیکون:", style = MaterialTheme.typography.bodySmall)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(iconOptions) { (iconKey, vector) ->
                        val isSelected = selectedIcon == iconKey
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .border(
                                    width = if (isSelected) 2.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedIcon = iconKey },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = vector,
                                contentDescription = iconKey,
                                tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Text("انتخاب رنگ:", style = MaterialTheme.typography.bodySmall)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(colorOptions) { colorLong ->
                        val isSelected = selectedColor == colorLong
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(colorLong))
                                .border(
                                    width = if (isSelected) 3.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = colorLong },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "انتخاب شده",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = priorityText,
                    onValueChange = { priorityText = it.filter { char -> char.isDigit() } },
                    label = { Text("اولویت (عدد بالاتر = اولویت بیشتر)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = autoRule,
                    onValueChange = { autoRule = it },
                    label = { Text("کلمات کلیدی اختصاص خودکار (با ویرگول)") },
                    placeholder = { Text("مثال: بانک, رمز, واریز, فاکتور") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val prio = priorityText.toIntOrNull() ?: 0
                        onSave(name.trim(), description.trim(), selectedIcon, selectedColor, prio, autoRule.trim())
                    }
                },
                modifier = Modifier.testTag("save_category_button")
            ) {
                Text("ذخیره")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("انصراف")
            }
        }
    )
}

fun getIconVector(iconKey: String): ImageVector {
    return when (iconKey) {
        "AccountBalance" -> Icons.Default.AccountBalance
        "Work" -> Icons.Default.Work
        "Star" -> Icons.Default.Star
        "ShoppingCart" -> Icons.Default.ShoppingCart
        "People" -> Icons.Default.People
        "Notifications" -> Icons.Default.Notifications
        "Lock" -> Icons.Default.Lock
        else -> Icons.Default.Folder
    }
}

@Composable
fun CategoryRuleTesterCard(categories: List<CategoryEntity>) {
    var testSender by remember { mutableStateOf("") }
    var testBody by remember { mutableStateOf("") }

    val testResult = remember(testSender, testBody, categories) {
        if (testBody.isBlank() && testSender.isBlank()) null
        else CategoryRuleEngine.classifyMessage(
            sender = testSender,
            body = testBody,
            customCategories = categories
        )
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Psychology,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "تست زنده قوانین دسته‌بندی",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            OutlinedTextField(
                value = testSender,
                onValueChange = { testSender = it },
                label = { Text("فرستنده نمونه (مثلاً: 20001234 یا Melli)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = testBody,
                onValueChange = { testBody = it },
                label = { Text("متن پیامک نمونه (مثلاً: واریز 500,000 ریال به حساب)") },
                modifier = Modifier.fillMaxWidth()
            )

            testResult?.let { res ->
                val matchedCategory = categories.firstOrNull { it.id == res.customCategoryId }
                val chipColor = matchedCategory?.color?.let { Color(it) } ?: MaterialTheme.colorScheme.primary

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = chipColor.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, chipColor)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = matchedCategory?.icon?.let { getIconVector(it) } ?: Icons.Default.Check,
                            contentDescription = null,
                            tint = chipColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "دسته‌بندی شناسایی شده: ${res.customCategoryName}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = chipColor
                        )
                    }
                }
            }
        }
    }
}
