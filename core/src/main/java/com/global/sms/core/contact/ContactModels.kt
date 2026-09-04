package com.global.sms.core.contact

data class ContactInfo(
    val id: String,
    val name: String,
    val rawName: String,
    val phoneNumber: String,
    val normalizedNumber: String,
    val photoUri: String? = null,
    val lookupKey: String? = null,
    val groupNames: List<String> = emptyList(),
    val isDuplicate: Boolean = false,
    val duplicateNumbers: List<String> = emptyList()
)

data class ContactGroup(
    val id: String,
    val title: String,
    val accountName: String? = null,
    val count: Int = 0
)

enum class ContactPermissionState {
    GRANTED,
    NEEDS_EXPLANATION,
    DENIED,
    PERMANENTLY_DENIED,
    NOT_REQUESTED
}

data class ContactDuplicateGroup(
    val primaryContact: ContactInfo,
    val duplicates: List<ContactInfo>,
    val reason: String // "شماره یکسان" or "نام یکسان"
)

data class SelectedRecipientsState(
    val selectedContacts: List<ContactInfo> = emptyList(),
    val customNumbers: List<String> = emptyList()
) {
    val allNumbers: List<String>
        get() = (selectedContacts.map { it.phoneNumber } + customNumbers).distinct()

    val totalCount: Int
        get() = allNumbers.size
}
