package com.global.sms.ui.managers

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MessageSelectionManager {
    private val _selectedMessageIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedMessageIds: StateFlow<Set<Long>> = _selectedMessageIds.asStateFlow()

    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode.asStateFlow()

    fun toggleSelection(messageId: Long) {
        _selectedMessageIds.update { current ->
            val next = if (current.contains(messageId)) current - messageId else current + messageId
            _isSelectionMode.value = next.isNotEmpty()
            next
        }
    }

    fun selectMessage(messageId: Long) {
        _selectedMessageIds.update { current ->
            val next = current + messageId
            _isSelectionMode.value = true
            next
        }
    }

    fun selectAll(allIds: List<Long>) {
        _selectedMessageIds.value = allIds.toSet()
        _isSelectionMode.value = allIds.isNotEmpty()
    }

    fun clearSelection() {
        _selectedMessageIds.value = emptySet()
        _isSelectionMode.value = false
    }

    fun isSelected(messageId: Long): Boolean = _selectedMessageIds.value.contains(messageId)
}
