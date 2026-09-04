package com.global.sms.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.global.sms.core.repository.SmartSummaryRepository
import com.global.sms.data.db.GlobalSmsDatabase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel responsible for dynamic AI conversation summaries.
 */
class ConversationSummaryViewModel(application: Application) : AndroidViewModel(application) {

    private val db = GlobalSmsDatabase.getInstance(application)
    private val repository = SmartSummaryRepository(db.messageDao())

    val overallSummary: StateFlow<String> = repository.getOverallSummaryFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "در حال تحلیل پیامک‌های دریافت شده..."
        )

    fun getThreadSummary(threadId: Long): StateFlow<String> {
        return repository.getThreadSummaryFlow(threadId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = "در حال خلاصه‌سازی گفتگو..."
            )
    }
}
