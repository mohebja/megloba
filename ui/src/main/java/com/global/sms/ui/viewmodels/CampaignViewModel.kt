package com.global.sms.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.global.sms.core.campaign.Campaign
import com.global.sms.core.campaign.CampaignRepository
import com.global.sms.core.group.CampaignReport
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CampaignViewModel(
    private val repository: CampaignRepository = CampaignRepository()
) : ViewModel() {

    private val _campaigns = MutableStateFlow<List<Campaign>>(emptyList())
    val campaigns: StateFlow<List<Campaign>> = _campaigns.asStateFlow()

    private val _lastReport = MutableStateFlow<CampaignReport?>(null)
    val lastReport: StateFlow<CampaignReport?> = _lastReport.asStateFlow()

    init {
        viewModelScope.launch {
            repository.campaigns.collect {
                _campaigns.value = it
            }
        }
    }

    fun createCampaign(
        name: String,
        recipientsText: String,
        templateBody: String,
        scheduledTime: Long = System.currentTimeMillis(),
        simSlot: Int = 0
    ) {
        val recipients = recipientsText.split(",", "\n", ";")
            .map { it.trim() }
            .filter { it.isNotBlank() }

        if (name.isBlank() || recipients.isEmpty() || templateBody.isBlank()) return

        viewModelScope.launch {
            repository.createCampaign(
                name = name,
                recipients = recipients,
                templateBody = templateBody,
                scheduledTime = scheduledTime,
                simSlot = simSlot
            )
        }
    }

    fun executeCampaignNow(campaignId: String) {
        viewModelScope.launch {
            try {
                val report = repository.runCampaign(campaignId)
                _lastReport.value = report
            } catch (e: Exception) {
                Log.e("CampaignViewModel", "Error executing campaign $campaignId", e)
            }
        }
    }
}
