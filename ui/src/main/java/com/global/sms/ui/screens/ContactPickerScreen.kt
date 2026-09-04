package com.global.sms.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.global.sms.core.contact.ContactInfo
import com.global.sms.core.contact.ContactPermissionState
import com.global.sms.core.util.PersianUtils
import com.global.sms.ui.components.ContactAvatar
import com.global.sms.ui.components.ContactPermissionCard
import com.global.sms.ui.components.rememberContactPermissionLauncher
import com.global.sms.ui.viewmodels.ContactViewModel
import com.global.sms.ui.viewmodels.GlobalSmsViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ContactPickerScreen(
    contactViewModel: ContactViewModel,
    globalViewModel: GlobalSmsViewModel,
    isMultiSelect: Boolean = true,
    onBack: () -> Unit,
    onContactsSelected: (List<ContactInfo>) -> Unit
) {
    val context = LocalContext.current
    val contactUiState by contactViewModel.uiState.collectAsStateWithLifecycle()
    val selectedRecipientsState by contactViewModel.selectedRecipientsState.collectAsStateWithLifecycle()
    val usePersianDigits by globalViewModel.usePersianDigits.collectAsStateWithLifecycle()

    val requestPermission = rememberContactPermissionLauncher(contactViewModel)

    val tempSelected = remember {
        mutableStateListOf<ContactInfo>().apply {
            addAll(selectedRecipientsState.selectedContacts)
        }
    }

    LaunchedEffect(Unit) {
        contactViewModel.checkPermissionState()
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "انتخاب مخاطبین",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                modifier = Modifier.testTag("contact_picker_title")
                            )
                            val countStr = if (usePersianDigits) PersianUtils.toPersianDigits(tempSelected.size.toString()) else tempSelected.size.toString()
                            Text(
                                text = if (tempSelected.isEmpty()) "هیچ مخاطبی انتخاب نشده"
                                else "$countStr مخاطب انتخاب شده",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack, modifier = Modifier.testTag("contact_picker_back_button")) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                        }
                    },
                    actions = {
                        if (isMultiSelect && tempSelected.isNotEmpty()) {
                            TextButton(
                                onClick = { tempSelected.clear() },
                                modifier = Modifier.testTag("clear_all_selection_button")
                            ) {
                                Text("حذف همه", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            },
            bottomBar = {
                Surface(
                    shadowElevation = 8.dp,
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onBack,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("cancel_contact_picker_button")
                        ) {
                            Text("انصراف")
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        val selectedCount = if (usePersianDigits) PersianUtils.toPersianDigits(tempSelected.size.toString()) else tempSelected.size.toString()
                        Button(
                            onClick = {
                                contactViewModel.setSelectedContacts(tempSelected.toList())
                                onContactsSelected(tempSelected.toList())
                            },
                            enabled = tempSelected.isNotEmpty(),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("confirm_contact_selection_button")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("تایید ($selectedCount)")
                        }
                    }
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 12.dp)
            ) {
                if (contactUiState.permissionState != ContactPermissionState.GRANTED) {
                    ContactPermissionCard(
                        permissionState = contactUiState.permissionState,
                        onRequestPermission = requestPermission,
                        onOpenSettings = { contactViewModel.openAppSettings(context) }
                    )
                } else {
                    // Search Input
                    OutlinedTextField(
                        value = contactUiState.searchQuery,
                        onValueChange = { contactViewModel.onSearchQueryChanged(it) },
                        placeholder = { Text("جستجوی نام یا شماره مخاطب (فارسی / انگلیسی)...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (contactUiState.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { contactViewModel.onSearchQueryChanged("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "پاک کردن")
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .testTag("contact_picker_search_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Group filter chips
                    if (contactUiState.systemGroups.isNotEmpty()) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            val totalContactsCount = if (usePersianDigits) PersianUtils.toPersianDigits(contactUiState.contacts.size.toString()) else contactUiState.contacts.size.toString()
                            FilterChip(
                                selected = contactUiState.selectedGroupFilter == null,
                                onClick = { contactViewModel.onGroupFilterChanged(null) },
                                label = { Text("همه مخاطبین ($totalContactsCount)") }
                            )
                            contactUiState.systemGroups.forEach { group ->
                                val groupCount = if (usePersianDigits) PersianUtils.toPersianDigits(group.count.toString()) else group.count.toString()
                                FilterChip(
                                    selected = contactUiState.selectedGroupFilter == group.title,
                                    onClick = {
                                        val newFilter = if (contactUiState.selectedGroupFilter == group.title) null else group.title
                                        contactViewModel.onGroupFilterChanged(newFilter)
                                    },
                                    label = { Text("${group.title} ($groupCount)") },
                                    leadingIcon = { Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                )
                            }
                        }
                    }

                    // Selected chips row
                    if (tempSelected.isNotEmpty()) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            tempSelected.forEach { contact ->
                                AssistChip(
                                    onClick = { tempSelected.removeAll { it.id == contact.id || it.phoneNumber == contact.phoneNumber } },
                                    label = { Text(contact.name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                                    trailingIcon = {
                                        Icon(Icons.Default.Close, contentDescription = "حذف", modifier = Modifier.size(14.dp))
                                    },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    ),
                                    modifier = Modifier.testTag("selected_chip_${contact.phoneNumber}")
                                )
                            }
                        }
                    }

                    if (contactUiState.isLoading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else if (contactUiState.contacts.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = if (contactUiState.searchQuery.isBlank()) "هیچ مخاطبی یافت نشد" else "مخاطبی با عبارت «${contactUiState.searchQuery}» پیدا نشد",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("contact_picker_list")
                        ) {
                            items(
                                items = contactUiState.contacts,
                                key = { it.id + "_" + it.phoneNumber }
                            ) { contact ->
                                val isSelected = tempSelected.any { it.id == contact.id || it.phoneNumber == contact.phoneNumber }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("contact_picker_row_${contact.phoneNumber}")
                                        .clickable {
                                            if (isMultiSelect) {
                                                if (isSelected) {
                                                    tempSelected.removeAll { it.id == contact.id || it.phoneNumber == contact.phoneNumber }
                                                } else {
                                                    tempSelected.add(contact)
                                                }
                                            } else {
                                                tempSelected.clear()
                                                tempSelected.add(contact)
                                                contactViewModel.setSelectedContacts(listOf(contact))
                                                onContactsSelected(listOf(contact))
                                            }
                                        }
                                        .padding(vertical = 10.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    ContactAvatar(photoUri = contact.photoUri, displayName = contact.name, size = 44.dp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = contact.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        Text(
                                            text = if (usePersianDigits) PersianUtils.toPersianDigits(contact.phoneNumber) else contact.phoneNumber,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (isMultiSelect) {
                                        Checkbox(
                                            checked = isSelected,
                                            onCheckedChange = { checked ->
                                                if (checked) {
                                                    tempSelected.add(contact)
                                                } else {
                                                    tempSelected.removeAll { it.id == contact.id || it.phoneNumber == contact.phoneNumber }
                                                }
                                            }
                                        )
                                    } else if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "انتخاب شده",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            }
                        }
                    }
                }
            }
        }
    }
}
