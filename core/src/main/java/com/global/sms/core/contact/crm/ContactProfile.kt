package com.global.sms.core.contact.crm

data class ContactProfile(
    val id: String,
    val fullName: String,
    val phoneNumbers: List<String>,
    val primaryPhoneNumber: String,
    val email: String? = null,
    val company: String? = null,
    val jobTitle: String? = null,
    val avatarUri: String? = null,
    val category: String = "مشتریان",
    val tags: List<ContactTag> = emptyList(),
    val notes: List<ContactNote> = emptyList(),
    val isVip: Boolean = false,
    val isFavorite: Boolean = false,
    val createdTimestamp: Long = System.currentTimeMillis(),
    val lastContactTimestamp: Long = System.currentTimeMillis(),
    val customFields: Map<String, String> = emptyMap()
)
