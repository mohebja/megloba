package com.global.sms.core.ai.agent

import com.global.sms.data.entity.MessageEntity
import com.global.sms.data.entity.AiAgentActionEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AgentObservation(
    val messageId: Long,
    val sender: String,
    val body: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class AgentReasoning(
    val detectedIntent: String,
    val urgencyScore: Int,
    val reasoningText: String,
    val needsApproval: Boolean = true
)

enum class AgentState {
    IDLE,
    OBSERVING,
    REASONING,
    SUGGESTING,
    AWAITING_CONFIRMATION,
    EXECUTING
}

object CommunicationAgent {

    private val _agentState = MutableStateFlow(AgentState.IDLE)
    val agentState: StateFlow<AgentState> = _agentState.asStateFlow()

    private val _isKillSwitchActive = MutableStateFlow(false)
    val isKillSwitchActive: StateFlow<Boolean> = _isKillSwitchActive.asStateFlow()

    fun setKillSwitch(active: Boolean) {
        _isKillSwitchActive.value = active
    }

    suspend fun processIncomingMessage(
        message: MessageEntity,
        onActionSuggested: suspend (AiAgentActionEntity) -> Unit
    ): AgentReasoning? {
        if (_isKillSwitchActive.value) {
            _agentState.value = AgentState.IDLE
            return null
        }

        // 1. Observation
        _agentState.value = AgentState.OBSERVING
        val observation = AgentObservation(
            messageId = message.id,
            sender = message.address,
            body = message.body,
            timestamp = message.timestamp
        )

        // 2. Reasoning
        _agentState.value = AgentState.REASONING
        val reasoning = ActionRecommendationEngine.reasonAboutMessage(observation.body)

        // 3. Suggestion
        _agentState.value = AgentState.SUGGESTING
        val suggestions = ActionRecommendationEngine.generateRecommendations(message)

        for (action in suggestions) {
            // ALWAYS requires user confirmation before execution
            _agentState.value = AgentState.AWAITING_CONFIRMATION
            onActionSuggested(action)
        }

        if (suggestions.isEmpty()) {
            _agentState.value = AgentState.IDLE
        }

        return reasoning
    }

    suspend fun executeConfirmedAction(
        action: AiAgentActionEntity,
        onExecute: suspend (AiAgentActionEntity) -> Boolean
    ): Boolean {
        if (_isKillSwitchActive.value) return false

        _agentState.value = AgentState.EXECUTING
        val success = onExecute(action)
        _agentState.value = AgentState.IDLE
        return success
    }
}
