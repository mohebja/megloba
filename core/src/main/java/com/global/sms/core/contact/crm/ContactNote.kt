package com.global.sms.core.contact.crm

data class ContactNote(
    val id: String,
    val contactId: String,
    val authorName: String = "اپراتور سیستم",
    val noteText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false
)
