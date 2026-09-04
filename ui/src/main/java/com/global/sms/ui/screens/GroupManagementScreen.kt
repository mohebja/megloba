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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.global.sms.core.contact.ContactInfo
import com.global.sms.core.contact.ContactPermissionState
import com.global.sms.core.util.PersianUtils
import com.global.sms.data.entity.ContactGroupEntity
import com.global.sms.ui.components.ContactAvatar
import com.global.sms.ui.components.ContactPermissionCard
import com.global.sms.ui.components.rememberContactPermissionLauncher
import com.global.sms.ui.viewmodels.ContactViewModel
import com.global.sms.ui.viewmodels.GlobalSmsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupManagementScreen(
    viewModel: GlobalSmsViewModel,
    onBack: () -> Unit,
    onOpenMultiCompose: (List<String>) -> Unit = {}
) {
    val groups by viewModel.contactGroups.collectAsStateWithLifecycle()
    val usePersianDigits by viewModel.usePersianDigits.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showCreateDialog by remember { mutableStateOf(false) }
    var editingGroup by remember { mutableStateOf<ContactGroupEntity?>(null) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "مدیریت گروه‌های مخاطبین",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.testTag("group_management_title")
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack, modifier = Modifier.testTag("group_management_back_button")) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    modifier = Modifier.testTag("create_group_fab"),
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "ایجاد گروه جدید")
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                if (groups.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Group,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "هنوز هیچ گروهی ساخته نشده است",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "برای ارسال پیامک گروهی، ابتدا یک گروه ایجاد کنید.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("group_management_list"),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(groups, key = { it.id }) { group ->
                            val memberList = remember(group.members) {
                                group.members.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                            }

                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("group_card_${group.id}")
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    Icons.Default.Group,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    text = group.name,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 17.sp
                                                )
                                                val memberCountStr = if (usePersianDigits) PersianUtils.toPersianDigits(memberList.size.toString()) else memberList.size.toString()
                                                Text(
                                                    text = "$memberCountStr عضو",
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }

                                        Row {
                                            IconButton(
                                                onClick = { editingGroup = group },
                                                modifier = Modifier.testTag("edit_group_button_${group.id}")
                                            ) {
                                                Icon(Icons.Default.Edit, contentDescription = "ویرایش")
                                            }
                                            IconButton(
                                                onClick = {
                                                    viewModel.deleteContactGroup(group.id)
                                                    Toast.makeText(context, "گروه حذف گردید", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.testTag("delete_group_button_${group.id}")
                                            ) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    contentDescription = "حذف",
                                                    tint = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }
                                    }

                                    if (!group.description.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = group.description ?: "",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Button(
                                        onClick = {
                                            if (memberList.isNotEmpty()) {
                                                onOpenMultiCompose(memberList)
                                            } else {
                                                Toast.makeText(context, "این گروه هیچ عضوی ندارد", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("send_group_sms_${group.id}")
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("ارسال پیامک به اعضای این گروه")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showCreateDialog) {
            GroupEditDialog(
                group = null,
                onDismiss = { showCreateDialog = false },
                onSave = { name, desc, members ->
                    viewModel.createContactGroup(name, desc, members)
                    showCreateDialog = false
                    Toast.makeText(context, "گروه جدید با موفقیت ایجاد شد", Toast.LENGTH_SHORT).show()
                }
            )
        }

        editingGroup?.let { group ->
            GroupEditDialog(
                group = group,
                onDismiss = { editingGroup = null },
                onSave = { name, desc, members ->
                    viewModel.updateContactGroup(group.copy(name = name, description = desc, members = members.joinToString(",")))
                    editingGroup = null
                    Toast.makeText(context, "اطلاعات گروه به‌روزرسانی شد", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

@Composable
fun GroupEditDialog(
    group: ContactGroupEntity?,
    onDismiss: () -> Unit,
    onSave: (name: String, description: String?, members: List<String>) -> Unit
) {
    var name by remember { mutableStateOf(group?.name ?: "") }
    var description by remember { mutableStateOf(group?.description ?: "") }
    var membersText by remember { mutableStateOf(group?.members ?: "") }
    var showContactPicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (group == null) "ایجاد گروه جدید" else "ویرایش گروه") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("نام گروه") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("group_name_input")
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("توضیحات (اختیاری)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("group_desc_input")
                )
                OutlinedTextField(
                    value = membersText,
                    onValueChange = { membersText = it },
                    label = { Text("شماره‌های اعضا (با کاما جدا کنید)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("group_members_input"),
                    maxLines = 3
                )
                OutlinedButton(
                    onClick = { showContactPicker = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("group_pick_contacts_button")
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("انتخاب از مخاطبین دستگاه")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val memberList = membersText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        onSave(name.trim(), description.ifBlank { null }, memberList)
                    }
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.testTag("group_save_button")
            ) {
                Text("ذخیره")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("group_cancel_button")
            ) {
                Text("انصراف")
            }
        }
    )

    if (showContactPicker) {
        GroupContactPickerDialog(
            onDismiss = { showContactPicker = false },
            onContactsSelected = { selected ->
                val current = membersText.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableSet()
                current.addAll(selected.map { it.phoneNumber })
                membersText = current.joinToString(",")
                showContactPicker = false
            }
        )
    }
}

@Composable
fun GroupContactPickerDialog(
    onDismiss: () -> Unit,
    onContactsSelected: (List<ContactInfo>) -> Unit,
    contactViewModel: ContactViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current
    val contactUiState by contactViewModel.uiState.collectAsStateWithLifecycle()
    val selectedContacts = remember { mutableStateListOf<ContactInfo>() }
    val requestPermission = rememberContactPermissionLauncher(contactViewModel)

    LaunchedEffect(Unit) {
        contactViewModel.checkPermissionState()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("انتخاب مخاطبین") },
        text = {
            Column {
                if (contactUiState.permissionState != ContactPermissionState.GRANTED) {
                    ContactPermissionCard(
                        permissionState = contactUiState.permissionState,
                        onRequestPermission = requestPermission,
                        onOpenSettings = { contactViewModel.openAppSettings(context) }
                    )
                } else {
                    OutlinedTextField(
                        value = contactUiState.searchQuery,
                        onValueChange = { contactViewModel.onSearchQueryChanged(it) },
                        placeholder = { Text("جستجوی نام یا شماره...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("group_picker_search_input")
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyColumn(modifier = Modifier.height(260.dp)) {
                        items(contactUiState.contacts, key = { it.id + "_" + it.phoneNumber }) { contact ->
                            val isSelected = selectedContacts.any { it.id == contact.id || it.phoneNumber == contact.phoneNumber }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isSelected) selectedContacts.removeAll { it.id == contact.id || it.phoneNumber == contact.phoneNumber }
                                        else selectedContacts.add(contact)
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ContactAvatar(photoUri = contact.photoUri, displayName = contact.name, size = 36.dp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(contact.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(contact.phoneNumber, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                androidx.compose.material3.Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { checked ->
                                        if (checked) selectedContacts.add(contact)
                                        else selectedContacts.removeAll { it.id == contact.id || it.phoneNumber == contact.phoneNumber }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onContactsSelected(selectedContacts.toList()) },
                enabled = selectedContacts.isNotEmpty(),
                modifier = Modifier.testTag("confirm_group_contact_picker")
            ) {
                Text("افزودن (${selectedContacts.size})")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_group_contact_picker")
            ) {
                Text("بستن")
            }
        }
    )
}
