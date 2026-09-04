package com.global.sms.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.global.sms.core.util.PersianUtils
import com.global.sms.security.AdvancedAppProtection
import com.global.sms.ui.viewmodels.EnterpriseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnterpriseDashboardScreen(
    viewModel: EnterpriseViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onBack: () -> Unit = {},
    onNavigateBack: () -> Unit = onBack,
    onNavigateCrm: () -> Unit = {},
    onNavigateTemplates: () -> Unit = {},
    onNavigateBulkSms: () -> Unit = {},
    onNavigateAutomation: () -> Unit = {},
    onNavigateAnalytics: () -> Unit = {},
    onNavigateSecurityAudit: () -> Unit = {}
) {
    val context = LocalContext.current
    val securityReport = remember { AdvancedAppProtection(context).assessDeviceSecurity() }

    val org by viewModel.organization.collectAsStateWithLifecycle()
    val departments by viewModel.departments.collectAsStateWithLifecycle()
    val employees by viewModel.employees.collectAsStateWithLifecycle()

    var showAddDeptDialog by remember { mutableStateOf(false) }
    var showAddEmpDialog by remember { mutableStateOf(false) }
    var showEditOrgDialog by remember { mutableStateOf(false) }

    var newDeptName by remember { mutableStateOf("") }
    var newDeptManager by remember { mutableStateOf("") }

    var newEmpName by remember { mutableStateOf("") }
    var newEmpRole by remember { mutableStateOf("EMPLOYEE") }
    var selectedDeptId by remember { mutableStateOf("") }
    var selectedPermissions by remember { mutableStateOf(listOf("SEND_SMS")) }

    var editOrgName by remember { mutableStateOf("") }
    var editOrgType by remember { mutableStateOf("") }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .testTag("enterprise_dashboard_screen"),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "داشبورد سازمانی و مدیریت پرسنل",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.testTag("enterprise_dashboard_nav_back")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "بازگشت"
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                editOrgName = org.companyName
                                editOrgType = org.organizationType
                                showEditOrgDialog = true
                            },
                            modifier = Modifier.testTag("btn_edit_org")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "ویرایش سازمان")
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // Organization Overview Header
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("org_overview_header")
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Business,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = org.companyName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "نوع: ${org.organizationType} | سطح: ${org.subscriptionLevel}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                // Row 1: Total Employees & Messages Sent Today
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricWidgetCard(
                        title = "کل پرسنل سازمان",
                        value = "${employees.size} نفر",
                        subtext = "${departments.size} دپارتمان فعال",
                        icon = Icons.Default.Group,
                        color = Color(0xFF1E88E5),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("widget_total_employees")
                    )

                    MetricWidgetCard(
                        title = "پیامک‌های امروز",
                        value = "۴,۲۵۰",
                        subtext = "نرخ تحویل ۹۹.۲٪",
                        icon = Icons.AutoMirrored.Filled.Send,
                        color = Color(0xFF43A047),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("widget_messages_today")
                    )
                }

                // Row 2: Campaign Status & Delivery Rate
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricWidgetCard(
                        title = "وضعیت کمپین‌ها",
                        value = "۳ کمپین فعال",
                        subtext = "۱۲,۵۰۰ مخاطب هدف",
                        icon = Icons.Default.Campaign,
                        color = Color(0xFF8E24AA),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("widget_campaign_status")
                    )

                    MetricWidgetCard(
                        title = "نرخ تحویل پیام‌ها",
                        value = "۹۹.4 ٪",
                        subtext = "ارسال بدون تاخیر",
                        icon = Icons.Default.CheckCircle,
                        color = Color(0xFF00ACC1),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("widget_delivery_rate")
                    )
                }

                // Row 3: AI Insights & Security Score
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricWidgetCard(
                        title = "تحلیل و هوش AI",
                        value = "۳۴ پیشنهاد فعال",
                        subtext = "پاسخ‌دهی خودکار ۱۰۰٪",
                        icon = Icons.Default.Psychology,
                        color = Color(0xFFE65100),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("widget_ai_insights")
                    )

                    MetricWidgetCard(
                        title = "امتیاز امنیتی",
                        value = "${securityReport.securityScore}٪",
                        subtext = "AES-256 + RBAC فعال",
                        icon = Icons.Default.Security,
                        color = Color(0xFF2E7D32),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("widget_security_score")
                    )
                }

                // Department Management Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "دپارتمان‌های سازمانی (${departments.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Button(
                        onClick = { showAddDeptDialog = true },
                        modifier = Modifier.testTag("btn_add_department")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("افزودن دپارتمان", fontSize = 12.sp)
                    }
                }

                if (departments.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.CorporateFare,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "هیچ دپارتمانی تعریف نشده است.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = { showAddDeptDialog = true },
                                modifier = Modifier.testTag("btn_empty_add_dept")
                            ) {
                                Text("ایجاد اولین دپارتمان")
                            }
                        }
                    }
                } else {
                    departments.forEach { dep ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("card_department_${dep.id}"),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = dep.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "مدیر دپارتمان: ${dep.manager}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    AssistChip(
                                        onClick = { },
                                        label = { Text("فعال", fontSize = 11.sp) }
                                    )
                                    IconButton(
                                        onClick = { viewModel.deleteDepartment(dep.id) },
                                        modifier = Modifier.testTag("btn_delete_dept_${dep.id}")
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "حذف دپارتمان",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Employee Management Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "پرسنل و دسترسی‌ها (${employees.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Button(
                        onClick = {
                            if (departments.isNotEmpty()) {
                                selectedDeptId = departments.first().id
                            }
                            showAddEmpDialog = true
                        },
                        modifier = Modifier.testTag("btn_add_employee")
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("افزودن پرسنل", fontSize = 12.sp)
                    }
                }

                if (employees.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.GroupOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "هیچ پرسنلی در سازمان ثبت نشده است.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = {
                                    if (departments.isNotEmpty()) {
                                        selectedDeptId = departments.first().id
                                    }
                                    showAddEmpDialog = true
                                },
                                modifier = Modifier.testTag("btn_empty_add_emp")
                            ) {
                                Text("افزودن پرسنل جدید")
                            }
                        }
                    }
                } else {
                    employees.forEach { emp ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("card_employee_${emp.id}"),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Badge,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${emp.name} (${emp.role})",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    val deptName = departments.find { it.id == emp.departmentId }?.name ?: "نامشخص"
                                    Text(
                                        text = "دپارتمان: $deptName | مجوزها: ${emp.permissions}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.deleteEmployee(emp.id) },
                                    modifier = Modifier.testTag("btn_delete_emp_${emp.id}")
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "حذف پرسنل",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add Department Dialog
        if (showAddDeptDialog) {
            AlertDialog(
                onDismissRequest = { showAddDeptDialog = false },
                title = { Text("افزودن دپارتمان جدید") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = newDeptName,
                            onValueChange = { newDeptName = it },
                            label = { Text("نام دپارتمان") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_dept_name")
                        )
                        OutlinedTextField(
                            value = newDeptManager,
                            onValueChange = { newDeptManager = it },
                            label = { Text("نام مدیر دپارتمان") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_dept_manager")
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newDeptName.isNotBlank()) {
                                viewModel.addDepartment(newDeptName.trim(), newDeptManager.trim())
                                newDeptName = ""
                                newDeptManager = ""
                                showAddDeptDialog = false
                            }
                        },
                        modifier = Modifier.testTag("btn_confirm_add_dept")
                    ) {
                        Text("ایجاد")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDeptDialog = false }) {
                        Text("انصراف")
                    }
                }
            )
        }

        // Add Employee Dialog
        if (showAddEmpDialog) {
            AlertDialog(
                onDismissRequest = { showAddEmpDialog = false },
                title = { Text("افزودن پرسنل جدید") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = newEmpName,
                            onValueChange = { newEmpName = it },
                            label = { Text("نام و نام خانوادگی") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_emp_name")
                        )

                        Text("نقش سازمانی:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("ADMIN", "MANAGER", "EMPLOYEE").forEach { role ->
                                FilterChip(
                                    selected = newEmpRole == role,
                                    onClick = { newEmpRole = role },
                                    label = { Text(role, fontSize = 11.sp) }
                                )
                            }
                        }

                        if (departments.isNotEmpty()) {
                            Text("انتخاب دپارتمان:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                departments.forEach { dept ->
                                    FilterChip(
                                        selected = selectedDeptId == dept.id,
                                        onClick = { selectedDeptId = dept.id },
                                        label = { Text(dept.name, fontSize = 11.sp) }
                                    )
                                }
                            }
                        } else {
                            Text("توجه: ابتدا یک دپارتمان ایجاد نمایید.", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newEmpName.isNotBlank()) {
                                val targetDeptId = if (selectedDeptId.isNotBlank()) selectedDeptId else (departments.firstOrNull()?.id ?: "general")
                                viewModel.addEmployee(
                                    departmentId = targetDeptId,
                                    name = newEmpName.trim(),
                                    role = newEmpRole,
                                    permissions = selectedPermissions
                                )
                                newEmpName = ""
                                showAddEmpDialog = false
                            }
                        },
                        modifier = Modifier.testTag("btn_confirm_add_emp")
                    ) {
                        Text("ثبت پرسنل")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddEmpDialog = false }) {
                        Text("انصراف")
                    }
                }
            )
        }

        // Edit Organization Dialog
        if (showEditOrgDialog) {
            AlertDialog(
                onDismissRequest = { showEditOrgDialog = false },
                title = { Text("ویرایش اطلاعات سازمان") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = editOrgName,
                            onValueChange = { editOrgName = it },
                            label = { Text("نام شرکت یا سازمان") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_org_name")
                        )
                        OutlinedTextField(
                            value = editOrgType,
                            onValueChange = { editOrgType = it },
                            label = { Text("نوع سازمان / صنعت") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_org_type")
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (editOrgName.isNotBlank()) {
                                viewModel.createOrUpdateOrganization(editOrgName.trim(), editOrgType.trim())
                                showEditOrgDialog = false
                            }
                        },
                        modifier = Modifier.testTag("btn_confirm_edit_org")
                    ) {
                        Text("ذخیره")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditOrgDialog = false }) {
                        Text("انصراف")
                    }
                }
            )
        }
    }
}

@Composable
fun MetricWidgetCard(
    title: String,
    value: String,
    subtext: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = title, fontSize = 12.sp, color = color, fontWeight = FontWeight.Bold)
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }

            Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(text = subtext, fontSize = 11.sp, color = Color.Gray)
        }
    }
}
