package com.global.sms.core.group

data class RecipientDeliveryStatus(
    val recipientPhone: String,
    val recipientName: String,
    val status: DeliveryState,
    val sentTimestamp: Long = System.currentTimeMillis(),
    val errorMessage: String? = null
)

enum class DeliveryState {
    PENDING,
    SENDING,
    DELIVERED,
    FAILED,
    RETRYING
}

data class CampaignReport(
    val campaignId: String,
    val campaignName: String,
    val groupName: String,
    val totalRecipients: Int,
    val deliveredCount: Int,
    val failedCount: Int,
    val pendingCount: Int,
    val successRatePercentage: Float,
    val startTimestamp: Long,
    val endTimestamp: Long? = null,
    val recipientStatuses: List<RecipientDeliveryStatus> = emptyList()
)
