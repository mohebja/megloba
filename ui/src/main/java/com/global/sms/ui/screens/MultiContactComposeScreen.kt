package com.global.sms.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.global.sms.core.util.SmsSegmenter
import com.global.sms.ui.components.ContactAvatar
import com.global.sms.ui.components.ContactPermissionCard
import com.global.sms.ui.components.rememberContactPermissionLauncher
import com.global.sms.ui.viewmodels.ContactViewModel
import com.global.sms.ui.viewmodels.GlobalSmsViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MultiContactComposeScreen(
    viewModel: GlobalSmsViewModel,
    contactViewModel: ContactViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    initialNumbers: List<String> = emptyList(),
    onOpenContactPicker: () -> Unit = {},
    onBack: () -> Unit,
    onSent: () -> Unit
) {
    val context = LocalContext.current
    val contactUiState by contactViewModel.uiState.collectAsStateWithLifecycle()
    val selectedRecipientsState by contactViewModel.selectedRecipientsState.collectAsStateWithLifecycle()
    val groups by viewModel.contactGroups.collectAsStateWithLifecycle()
    val usePersianDigits by viewModel.usePersianDigits.collectAsStateWithLifecycle()

    val requestPermission = rememberContactPermissionLauncher(contactViewModel)

    var messageText by remember { mutableStateOf("") }
    var selectedSimSlot by remember { mutableIntStateOf(0) }
    var manualPhoneInput by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        contactViewModel.checkPermissionState()
        if (initialNumbers.isNotEmpty()) {
            initialNumbers.forEach { num ->
                contactViewModel.addCustomNumberRecipient(num)
            }
        }
    }

    val allRecipients = selectedRecipientsState.allNumbers
    val segmentInfo = SmsSegmenter.calculateSegments(messageText)

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "ارسال پیامک جدید / گروهی",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier.testTag("multi_contact_title")
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { selectedSimSlot = if (selectedSimSlot == 0) 1 else 0 },
                            modifier = Modifier.testTag("toggle_sim_button")
                        ) {
                            Icon(Icons.Default.SimCard, contentDescription = "تغییر سیم‌کارت")
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
                    .padding(12.dp)
            ) {
                // Recipient Header & Select Contact Action
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "گیرندگان پیامک (${if (usePersianDigits) PersianUtils.toPersianDigits(allRecipients.size.toString()) else allRecipients.size}):",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            FilledTonalButton(
                                onClick = onOpenContactPicker,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("select_contact_button")
                            ) {
                                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("انتخاب مخاطب", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Manual number entry box
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = manualPhoneInput,
                                onValueChange = { manualPhoneInput = it },
                                placeholder = { Text("تایپ شماره مستقیم (مثلاً 0912...)") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("manual_phone_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = {
                                    if (manualPhoneInput.isNotBlank()) {
                                        contactViewModel.addCustomNumberRecipient(manualPhoneInput)
                                        manualPhoneInput = ""
                                    }
                                },
                                modifier = Modifier.testTag("add_manual_number_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "افزودن شماره", tint = MaterialTheme.colorScheme.primary)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Selected Recipient Chips
                        if (allRecipients.isNotEmpty()) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Selected Contacts
                                selectedRecipientsState.selectedContacts.forEach { contact ->
                                    AssistChip(
                                        onClick = { contactViewModel.removeContactRecipient(contact) },
                                        label = {
                                            Text(
                                                text = "${contact.name} (${if (usePersianDigits) PersianUtils.toPersianDigits(contact.phoneNumber) else contact.phoneNumber})",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        },
                                        trailingIcon = {
                                            Icon(Icons.Default.Close, contentDescription = "حذف", modifier = Modifier.size(14.dp))
                                        },
                                        colors = AssistChipDefaults.assistChipColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                                            labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                        ),
                                        modifier = Modifier.testTag("recipient_chip_${contact.phoneNumber}")
                                    )
                                }
                                // Custom Numbers
                                selectedRecipientsState.customNumbers.forEach { num ->
                                    AssistChip(
                                        onClick = { contactViewModel.removeCustomNumberRecipient(num) },
                                        label = {
                                            Text(
                                                text = if (usePersianDigits) PersianUtils.toPersianDigits(num) else num,
                                                fontSize = 12.sp
                                            )
                                        },
                                        trailingIcon = {
                                            Icon(Icons.Default.Close, contentDescription = "حذف", modifier = Modifier.size(14.dp))
                                        },
                                        modifier = Modifier.testTag("recipient_chip_$num")
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = "هنوز مخاطبی انتخاب نشده است. روی دکمه «انتخاب مخاطب» کلیک کنید یا شماره تایپ نمایید.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Quick Group Pick Chips
                if (groups.isNotEmpty()) {
                    Text(
                        text = "افزودن سریع گروه‌ها:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        groups.forEach { group ->
                            val members = group.members.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                            AssistChip(
                                onClick = {
                                    members.forEach { num ->
                                        contactViewModel.addCustomNumberRecipient(num)
                                    }
                                },
                                label = { Text("+ گروه ${group.name} (${if (usePersianDigits) PersianUtils.toPersianDigits(members.size.toString()) else members.size})") },
                                leadingIcon = { Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            )
                        }
                    }
                }

                if (contactUiState.permissionState != ContactPermissionState.GRANTED) {
                    ContactPermissionCard(
                        permissionState = contactUiState.permissionState,
                        onRequestPermission = requestPermission,
                        onOpenSettings = { contactViewModel.openAppSettings(context) }
                    )
                } else {
                    // Inline Contact List with Checkboxes for quick toggling
                    OutlinedTextField(
                        value = contactUiState.searchQuery,
                        onValueChange = { contactViewModel.onSearchQueryChanged(it) },
                        placeholder = { Text("جستجوی سریع مخاطبین...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("contact_search_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .testTag("quick_contacts_list")
                    ) {
                        items(contactUiState.contacts, key = { it.id + "_" + it.phoneNumber }) { contact ->
                            val isSelected = selectedRecipientsState.selectedContacts.any { it.id == contact.id || it.phoneNumber == contact.phoneNumber }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { contactViewModel.toggleContactRecipient(contact) }
                                    .padding(vertical = 6.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ContactAvatar(photoUri = contact.photoUri, displayName = contact.name, size = 38.dp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = contact.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = if (usePersianDigits) PersianUtils.toPersianDigits(contact.phoneNumber) else contact.phoneNumber,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { contactViewModel.toggleContactRecipient(contact) }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Message Composer Section
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    tonalElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (selectedSimSlot == 0) "ارسال از سیم‌کارت ۱" else "ارسال از سیم‌کارت ۲",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            val segmentCountText = if (usePersianDigits) PersianUtils.toPersianDigits(segmentInfo.segmentCount.toString()) else segmentInfo.segmentCount.toString()
                            val charCountText = if (usePersianDigits) PersianUtils.toPersianDigits(segmentInfo.charCount.toString()) else segmentInfo.charCount.toString()
                            Text(
                                text = "ارسال در $segmentCountText پیامک ($charCountText کاراکتر - ${if (segmentInfo.isUnicode) "یونیکد" else "GSM"})",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (segmentInfo.segmentCount > 1) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (segmentInfo.segmentCount > 1) FontWeight.Bold else FontWeight.Normal
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = messageText,
                            onValueChange = { messageText = it },
                            placeholder = { Text("متن پیامک را بنویسید...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                                .testTag("group_message_input"),
                            maxLines = 4
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                if (allRecipients.isEmpty()) {
                                    Toast.makeText(context, "لطفاً حداقل یک گیرنده انتخاب کنید", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (messageText.isBlank()) {
                                    Toast.makeText(context, "متن پیامک نمی‌تواند خالی باشد", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }

                                viewModel.sendGroupSms(allRecipients, messageText, selectedSimSlot)
                                val recipientCountStr = if (usePersianDigits) PersianUtils.toPersianDigits(allRecipients.size.toString()) else allRecipients.size.toString()
                                Toast.makeText(context, "پیامک به $recipientCountStr گیرنده ارسال شد", Toast.LENGTH_LONG).show()
                                contactViewModel.clearAllRecipients()
                                onSent()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("send_group_sms_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            val recipientCountStr = if (usePersianDigits) PersianUtils.toPersianDigits(allRecipients.size.toString()) else allRecipients.size.toString()
                            Text(if (allRecipients.size <= 1) "ارسال پیامک" else "ارسال همزمان به $recipientCountStr نفر")
                        }
                    }
                }
            }
        }
    }
}
