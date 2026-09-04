package com.global.sms.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.global.sms.core.repository.AiHomeDashboardData
import com.global.sms.core.repository.DashboardRepository
import com.global.sms.core.repository.EnterpriseDashboardStats
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DashboardRepository(application)

    val statsState: StateFlow<EnterpriseDashboardStats> = repository.getDashboardStatsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = EnterpriseDashboardStats()
        )

    val aiDashboardState: StateFlow<AiHomeDashboardData> = repository.getAiDashboardDataFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AiHomeDashboardData()
        )
}
