package com.global.sms.core.search

import com.global.sms.data.entity.MessageEntity

interface SemanticSearchEngine {
    suspend fun semanticSearch(query: String, limit: Int = 20): List<MessageEntity>
    suspend fun indexMessage(message: MessageEntity)
}

class DefaultSemanticSearchEngine : SemanticSearchEngine {
    override suspend fun semanticSearch(query: String, limit: Int): List<MessageEntity> {
        // Fallback placeholder for local vector search / LLM embeddings
        return emptyList()
    }

    override suspend fun indexMessage(message: MessageEntity) {
        // No-op for base engine
    }
}
