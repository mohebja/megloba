package com.global.sms.core.campaign

import com.global.sms.core.group.CampaignReport
import com.global.sms.core.group.DeliveryState
import com.global.sms.core.group.RecipientDeliveryStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

data class Campaign(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val recipients: List<String>,
    val templateBody: String,
    val scheduledTime: Long = System.currentTimeMillis(),
    val simSlot: Int = 0,
    val status: String = "QUEUED", // QUEUED, RUNNING, COMPLETED, PAUSED
    val totalSent: Int = 0,
    val deliveredCount: Int = 0,
    val failedCount: Int = 0,
    val pendingCount: Int = 0,
    val createdTimestamp: Long = System.currentTimeMillis()
)

class CampaignRepository {

    private val _campaigns = MutableStateFlow<List<Campaign>>(
        listOf(
            Campaign(
                id = "camp_101",
                name = "اطلاع‌رسانی جشنواره تخفیف تابستانه",
                recipients = listOf("09121111111", "09122222222", "09353333333"),
                templateBody = "سلام {name} عزیز، ۲۰٪ تخفیف ویژه خریدهای تابستانه در انتظار شماست.",
                status = "COMPLETED",
                totalSent = 3,
                deliveredCount = 3,
                failedCount = 0,
                pendingCount = 0
            ),
            Campaign(
                id = "camp_102",
                name = "ارسال تبریک عید به مشتریان VIP",
                recipients = listOf("09121111111"),
                templateBody = "عید شما مبارک باد. با احترام، شرکت Global SMS",
                status = "QUEUED",
                totalSent = 0,
                deliveredCount = 0,
                failedCount = 0,
                pendingCount = 1
            )
        )
    )

    val campaigns: Flow<List<Campaign>> = _campaigns.asStateFlow()

    suspend fun createCampaign(
        name: String,
        recipients: List<String>,
        templateBody: String,
        scheduledTime: Long,
        simSlot: Int
    ): Campaign {
        val newCampaign = Campaign(
            name = name,
            recipients = recipients,
            templateBody = templateBody,
            scheduledTime = scheduledTime,
            simSlot = simSlot,
            status = "QUEUED",
            pendingCount = recipients.size
        )
        _campaigns.value = listOf(newCampaign) + _campaigns.value
        return newCampaign
    }

    suspend fun runCampaign(campaignId: String): CampaignReport {
        val current = _campaigns.value.toMutableList()
        val index = current.indexOfFirst { it.id == campaignId }
        if (index >= 0) {
            val c = current[index]
            val updated = c.copy(
                status = "COMPLETED",
                totalSent = c.recipients.size,
                deliveredCount = c.recipients.size,
                pendingCount = 0
            )
            current[index] = updated
            _campaigns.value = current

            val statuses = c.recipients.map {
                RecipientDeliveryStatus(
                    recipientPhone = it,
                    recipientName = "مخاطب ($it)",
                    status = DeliveryState.DELIVERED
                )
            }

            return CampaignReport(
                campaignId = c.id,
                campaignName = c.name,
                groupName = "ارسال گروهی",
                totalRecipients = c.recipients.size,
                deliveredCount = c.recipients.size,
                failedCount = 0,
                pendingCount = 0,
                successRatePercentage = 100f,
                startTimestamp = System.currentTimeMillis(),
                recipientStatuses = statuses
            )
        }
        throw IllegalArgumentException("Campaign not found: $campaignId")
    }
}
