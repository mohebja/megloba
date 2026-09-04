package com.global.sms.core.ai.assistant

class AiCommunicationAssistant(
    private val insightEngine: ConversationInsightEngine = ConversationInsightEngine(),
    private val smartReplyEngine: SmartReplyLearningEngine = SmartReplyLearningEngine()
) {

    /**
     * 100% On-Device Processing.
     * No message data or customer conversation text ever leaves the local device.
     */
    fun processConversation(
        conversationId: String,
        messages: List<String>
    ): Pair<ConversationInsight, List<SmartReplySuggestion>> {
        val insight = insightEngine.analyzeConversation(conversationId, messages)
        val lastMessage = messages.lastOrNull() ?: ""
        val suggestions = smartReplyEngine.getSuggestedReplies(insight.detectedIntent, lastMessage)

        return Pair(insight, suggestions)
    }

    fun learnUserResponse(replyText: String) {
        smartReplyEngine.learnUserReply(replyText)
    }
}
