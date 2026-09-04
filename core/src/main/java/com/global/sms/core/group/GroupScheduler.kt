package com.global.sms.core.group

import java.util.UUID

data class ScheduledGroupCampaign(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val groupId: String,
    val groupName: String,
    val templateText: String,
    val scheduledTime: Long,
    val simSlot: Int = 0,
    val isRecurring: Boolean = false,
    val recurrenceIntervalDays: Int = 0,
    val status: String = "SCHEDULED" // SCHEDULED, EXECUTING, COMPLETED, CANCELLED
)

class GroupScheduler {

    private val scheduledJobs = mutableListOf<ScheduledGroupCampaign>()

    fun scheduleCampaign(campaign: ScheduledGroupCampaign): ScheduledGroupCampaign {
        scheduledJobs.add(campaign)
        return campaign
    }

    fun getPendingCampaigns(): List<ScheduledGroupCampaign> {
        val now = System.currentTimeMillis()
        return scheduledJobs.filter { it.status == "SCHEDULED" && it.scheduledTime <= now }
    }

    fun cancelCampaign(campaignId: String): Boolean {
        val index = scheduledJobs.indexOfFirst { it.id == campaignId }
        if (index >= 0) {
            scheduledJobs[index] = scheduledJobs[index].copy(status = "CANCELLED")
            return true
        }
        return false
    }

    fun getAllScheduledCampaigns(): List<ScheduledGroupCampaign> {
        return scheduledJobs.toList()
    }
}
