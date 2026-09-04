package com.global.sms.core.contact.crm

enum class InteractionType {
    SMS_RECEIVED,
    SMS_SENT,
    SCHEDULED_SMS,
    CAMPAIGN_SMS,
    NOTE_ADDED,
    CALL_LOG,
    STATUS_CHANGE
}

data class ContactHistoryItem(
    val id: String,
    val contactId: String,
    val interactionType: InteractionType,
    val title: String,
    val snippet: String,
    val timestamp: Long,
    val category: String = "GENERAL",
    val simSlot: Int = 0,
    val status: String = "DELIVERED"
)

data class ContactTimeline(
    val contactId: String,
    val totalSent: Int,
    val totalReceived: Int,
    val totalScheduled: Int,
    val historyItems: List<ContactHistoryItem>
)
