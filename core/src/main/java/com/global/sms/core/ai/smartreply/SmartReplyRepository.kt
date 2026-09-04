package com.global.sms.core.ai.smartreply

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class CustomSmartReply(
    val id: Long,
    val text: String,
    val isUserDefined: Boolean = true
)

class SmartReplyRepository {

    private val _customReplies = MutableStateFlow<List<CustomSmartReply>>(
        listOf(
            CustomSmartReply(1, "بله، در خدمتم."),
            CustomSmartReply(2, "خیر، لطفا زمان دیگری پیشنهاد دهید."),
            CustomSmartReply(3, "بررسی می‌کنم و خبر می‌دهم.")
        )
    )

    fun getCustomRepliesFlow(): Flow<List<CustomSmartReply>> = _customReplies.asStateFlow()

    fun getSuggestions(incomingMessage: String): List<String> {
        val engineSuggestions = SmartReplyEngine.generateSmartReplies(incomingMessage)
        val userCustom = _customReplies.value.map { it.text }
        return (engineSuggestions + userCustom).distinct().take(5)
    }

    fun addCustomReply(text: String) {
        if (text.isBlank()) return
        val current = _customReplies.value.toMutableList()
        val newId = (current.maxOfOrNull { it.id } ?: 0) + 1
        current.add(CustomSmartReply(newId, text.trim()))
        _customReplies.value = current
    }

    fun removeCustomReply(id: Long) {
        _customReplies.value = _customReplies.value.filter { it.id != id }
    }
}
