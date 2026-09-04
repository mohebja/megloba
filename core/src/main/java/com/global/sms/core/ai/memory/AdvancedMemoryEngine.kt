package com.global.sms.core.ai.memory

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

enum class MemoryType {
    LONG_TERM,
    SHORT_TERM,
    USER_PREFERENCE,
    CONTACT_RELATIONSHIP
}

data class AIMemoryRecord(
    val memoryId: String = UUID.randomUUID().toString(),
    val type: MemoryType,
    val subjectKey: String,
    val memoryContent: String,
    val importanceScore: Float = 0.8f, // 0.0 to 1.0
    val expiresAtMs: Long? = null,
    val createdTimestampMs: Long = System.currentTimeMillis()
)

class AdvancedMemoryEngine {

    private val _memories = MutableStateFlow<List<AIMemoryRecord>>(emptyList())
    val memories: StateFlow<List<AIMemoryRecord>> = _memories.asStateFlow()

    fun storeMemory(
        type: MemoryType,
        subjectKey: String,
        content: String,
        importanceScore: Float = 0.8f,
        ttlMinutes: Long? = null
    ): AIMemoryRecord {
        val expiry = ttlMinutes?.let { System.currentTimeMillis() + (it * 60_000L) }
        val record = AIMemoryRecord(
            type = type,
            subjectKey = subjectKey,
            memoryContent = content,
            importanceScore = importanceScore,
            expiresAtMs = expiry
        )
        _memories.value = listOf(record) + _memories.value
        return record
    }

    fun updateMemory(memoryId: String, newContent: String): Boolean {
        _memories.value = _memories.value.map {
            if (it.memoryId == memoryId) it.copy(memoryContent = newContent) else it
        }
        return true
    }

    fun deleteMemory(memoryId: String): Boolean {
        _memories.value = _memories.value.filter { it.memoryId != memoryId }
        return true
    }

    fun purgeExpiredMemories(): Int {
        val now = System.currentTimeMillis()
        val beforeCount = _memories.value.size
        _memories.value = _memories.value.filter { it.expiresAtMs == null || it.expiresAtMs > now }
        return beforeCount - _memories.value.size
    }

    fun resetAllAiMemories(): Boolean {
        _memories.value = emptyList()
        return true
    }

    fun getHighImportanceMemories(): List<AIMemoryRecord> {
        return _memories.value.filter { it.importanceScore >= 0.8f }
    }
}
