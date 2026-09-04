package com.global.sms.ui.viewmodels

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.global.sms.core.contact.*
import com.global.sms.ui.components.ResolvedContact
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ContactUiState(
    val contacts: List<ContactInfo> = emptyList(),
    val systemGroups: List<ContactGroup> = emptyList(),
    val searchQuery: String = "",
    val selectedGroupFilter: String? = null,
    val isLoading: Boolean = false,
    val permissionState: ContactPermissionState = ContactPermissionState.NOT_REQUESTED,
    val duplicates: List<ContactDuplicateGroup> = emptyList(),
    val resolvedContactsMap: Map<String, ResolvedContact> = emptyMap()
)

class ContactViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ContactRepository = ContactRepositoryImpl(application)

    private val _uiState = MutableStateFlow(ContactUiState())
    val uiState: StateFlow<ContactUiState> = _uiState.asStateFlow()

    private val _selectedRecipientsState = MutableStateFlow(SelectedRecipientsState())
    val selectedRecipientsState: StateFlow<SelectedRecipientsState> = _selectedRecipientsState.asStateFlow()

    val syncState: StateFlow<ContactSyncState> = repository.syncState
    val syncStats: StateFlow<ContactSyncStats> = repository.syncStats

    fun toggleContactRecipient(contact: ContactInfo) {
        _selectedRecipientsState.update { current ->
            val list = current.selectedContacts.toMutableList()
            if (list.any { it.id == contact.id || it.phoneNumber == contact.phoneNumber }) {
                list.removeAll { it.id == contact.id || it.phoneNumber == contact.phoneNumber }
            } else {
                list.add(contact)
            }
            current.copy(selectedContacts = list)
        }
    }

    fun addContactRecipient(contact: ContactInfo) {
        _selectedRecipientsState.update { current ->
            if (current.selectedContacts.none { it.id == contact.id || it.phoneNumber == contact.phoneNumber }) {
                current.copy(selectedContacts = current.selectedContacts + contact)
            } else current
        }
    }

    fun removeContactRecipient(contact: ContactInfo) {
        _selectedRecipientsState.update { current ->
            current.copy(selectedContacts = current.selectedContacts.filterNot { it.id == contact.id || it.phoneNumber == contact.phoneNumber })
        }
    }

    fun addCustomNumberRecipient(number: String) {
        val trimmed = number.trim()
        if (trimmed.isBlank()) return
        _selectedRecipientsState.update { current ->
            if (!current.customNumbers.contains(trimmed) && !current.selectedContacts.any { it.phoneNumber == trimmed }) {
                current.copy(customNumbers = current.customNumbers + trimmed)
            } else current
        }
    }

    fun removeCustomNumberRecipient(number: String) {
        _selectedRecipientsState.update { current ->
            current.copy(customNumbers = current.customNumbers.filterNot { it == number })
        }
    }

    fun setSelectedContacts(contacts: List<ContactInfo>) {
        _selectedRecipientsState.update { current ->
            current.copy(selectedContacts = contacts)
        }
    }

    fun clearAllRecipients() {
        _selectedRecipientsState.update { SelectedRecipientsState() }
    }

    init {
        checkPermissionState()
    }

    fun checkPermissionState() {
        val hasPerm = repository.hasPermission()
        _uiState.update {
            it.copy(
                permissionState = if (hasPerm) ContactPermissionState.GRANTED else ContactPermissionState.NOT_REQUESTED
            )
        }
        if (hasPerm) {
            repository.registerContentObserver()
            loadContactsAndGroups()
        }
    }

    fun onPermissionResult(isGranted: Boolean, shouldShowRationale: Boolean) {
        if (isGranted) {
            _uiState.update { it.copy(permissionState = ContactPermissionState.GRANTED) }
            repository.registerContentObserver()
            loadContactsAndGroups()
        } else {
            val state = if (shouldShowRationale) {
                ContactPermissionState.NEEDS_EXPLANATION
            } else {
                ContactPermissionState.PERMANENTLY_DENIED
            }
            _uiState.update { it.copy(permissionState = state) }
        }
    }

    fun triggerManualSync() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.syncContactsNow()
            loadContactsAndGroups(forceRefresh = true)
        }
    }

    fun togglePeriodicSync(enabled: Boolean, intervalHours: Long = 24) {
        if (enabled) {
            repository.schedulePeriodicSync(intervalHours)
        } else {
            repository.cancelPeriodicSync()
        }
    }

    override fun onCleared() {
        super.onCleared()
        repository.unregisterContentObserver()
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        performSearch()
    }

    fun onGroupFilterChanged(groupName: String?) {
        _uiState.update { it.copy(selectedGroupFilter = groupName) }
        performSearch()
    }

    private fun performSearch() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val currentQuery = _uiState.value.searchQuery
            val currentGroup = _uiState.value.selectedGroupFilter
            val results = repository.searchContacts(currentQuery, currentGroup)
            _uiState.update {
                it.copy(
                    contacts = results,
                    isLoading = false
                )
            }
        }
    }

    fun loadContactsAndGroups(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            if (!repository.hasPermission()) return@launch

            _uiState.update { it.copy(isLoading = true) }

            val groups = repository.getSystemContactGroups()
            val contacts = repository.searchContacts(
                query = _uiState.value.searchQuery,
                groupFilter = _uiState.value.selectedGroupFilter
            )

            _uiState.update {
                it.copy(
                    contacts = contacts,
                    systemGroups = groups,
                    isLoading = false
                )
            }
        }
    }

    fun checkForDuplicates() {
        viewModelScope.launch {
            val dups = repository.getDuplicateGroups()
            _uiState.update { it.copy(duplicates = dups) }
        }
    }

    fun resolvePhoneNumber(phoneNumber: String) {
        if (phoneNumber.isBlank() || _uiState.value.resolvedContactsMap.containsKey(phoneNumber)) return

        viewModelScope.launch {
            val resolved = repository.resolveContact(phoneNumber)
            val resContact = ResolvedContact(
                name = resolved?.name,
                photoUri = resolved?.photoUri
            )

            _uiState.update { state ->
                val newMap = state.resolvedContactsMap.toMutableMap()
                newMap[phoneNumber] = resContact
                state.copy(resolvedContactsMap = newMap)
            }
        }
    }

    fun openAppSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun refreshCache() {
        repository.invalidateCache()
        loadContactsAndGroups(forceRefresh = true)
    }
}
