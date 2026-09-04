package com.global.sms.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.global.sms.data.entity.CrmCustomerEntity
import com.global.sms.ui.viewmodels.EnterpriseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrmCustomerManagementScreen(
    viewModel: EnterpriseViewModel,
    onBack: () -> Unit,
    onSendMessageToCustomer: (String) -> Unit,
    onOpenCustomer360: (Long) -> Unit = {}
) {
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var selectedTagFilter by remember { mutableStateOf("همه") }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingCustomer by remember { mutableStateOf<CrmCustomerEntity?>(null) }

    val allTags = listOf("همه", "VIP", "مشتری", "تامین‌کننده", "همکار", "مهم")

    val filteredCustomers = remember(customers, searchQuery, selectedTagFilter) {
        customers.filter { c ->
            val matchesQuery = searchQuery.isBlank() ||
                    c.name.contains(searchQuery, ignoreCase = true) ||
                    c.phoneNumber.contains(searchQuery) ||
                    (c.company?.contains(searchQuery, ignoreCase = true) == true)
            val matchesTag = selectedTagFilter == "همه" || c.tags.contains(selectedTagFilter)
            matchesQuery && matchesTag
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("مدیریت مشتریان و برچسب‌ها (CRM)") },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingCustomer = null
                    showAddDialog = true
                },
                modifier = Modifier.testTag("fab_add_customer")
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "افزودن مشتری")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("جستجوی مشتری بر اساس نام، شماره یا شرکت...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = if (searchQuery.isNotEmpty()) {
                    {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = null)
                        }
                    }
                } else null,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Tag Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(allTags) { tag ->
                    FilterChip(
                        selected = selectedTagFilter == tag,
                        onClick = { selectedTagFilter = tag },
                        label = { Text(tag) }
                    )
                }
            }

            // Customer List
            if (filteredCustomers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "هیچ مشتری با این مشخصات یافت نشد.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredCustomers, key = { it.id }) { customer ->
                        CustomerCardItem(
                            customer = customer,
                            onOpen360 = { onOpenCustomer360(customer.id) },
                            onEdit = {
                                editingCustomer = customer
                                showAddDialog = true
                            },
                            onDelete = { viewModel.deleteCustomer(customer) },
                            onSendSms = { onSendMessageToCustomer(customer.phoneNumber) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        CustomerEditDialog(
            customer = editingCustomer,
            onDismiss = { showAddDialog = false },
            onSave = { updated ->
                viewModel.saveCustomer(updated)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun CustomerCardItem(
    customer: CrmCustomerEntity,
    onOpen360: () -> Unit = {},
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSendSms: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        onClick = onOpen360,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = customer.name.take(1),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = customer.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    if (!customer.company.isNull_or_blank()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "(${customer.company})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(text = customer.phoneNumber, style = MaterialTheme.typography.bodyMedium)
                customer.notes?.let { note ->
                    Text(
                        text = note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                // Tags
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    customer.tags.split(",").map { it.trim() }.filter { it.isNotBlank() }.forEach { tag ->
                        AssistChip(
                            onClick = {},
                            label = { Text(tag, fontSize = 10.sp) },
                            modifier = Modifier.height(24.dp)
                        )
                    }
                }
            }

            IconButton(onClick = onSendSms) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "ارسال پیامک", tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "ویرایش")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

private fun String?.isNull_or_blank(): Boolean = this == null || this.isBlank()

@Composable
fun CustomerEditDialog(
    customer: CrmCustomerEntity?,
    onDismiss: () -> Unit,
    onSave: (CrmCustomerEntity) -> Unit
) {
    var name by remember { mutableStateOf(customer?.name ?: "") }
    var phone by remember { mutableStateOf(customer?.phoneNumber ?: "") }
    var company by remember { mutableStateOf(customer?.company ?: "") }
    var notes by remember { mutableStateOf(customer?.notes ?: "") }
    var tags by remember { mutableStateOf(customer?.tags ?: "مشتری") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (customer == null) "افزودن مشتری جدید" else "ویرایش مشخصات مشتری") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("نام و نام خانوادگی") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("شماره همراه") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = company,
                    onValueChange = { company = it },
                    label = { Text("نام شرکت / سازمان (اختیاری)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("برچسب‌ها (با کاما جدا کنید)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("یادداشت مشتری") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank() && phone.isNotBlank()) {
                        onSave(
                            (customer ?: CrmCustomerEntity(name = name, phoneNumber = phone)).copy(
                                name = name,
                                phoneNumber = phone,
                                company = company.ifBlank { null },
                                notes = notes.ifBlank { null },
                                tags = tags
                            )
                        )
                    }
                }
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
