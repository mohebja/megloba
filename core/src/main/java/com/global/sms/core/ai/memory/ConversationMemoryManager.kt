package com.global.sms.core.ai.memory

import com.global.sms.data.dao.AiMemoryDao
import com.global.sms.data.entity.AiMemoryEntity
import kotlinx.coroutines.flow.Flow

data class LocalFactMemory(
    val contactAddress: String,
    val factCategory: String, // "RELATIONSHIP", "PREFERENCE", "DECISION", "BUSINESS_ROLE"
    val factKey: String,
    val factValue: String,
    val confidenceScore: Float = 0.9f
)

object ConversationMemoryManager {

    private var memoryDao: AiMemoryDao? = null

    fun initialize(dao: AiMemoryDao) {
        memoryDao = dao
    }

    suspend fun extractAndSaveFact(address: String, messageText: String) {
        val text = messageText.trim()

        val fact = when {
            text.contains("مدیر") || text.contains("رئیس") || text.contains("همکار") -> {
                LocalFactMemory(address, "BUSINESS_ROLE", "سمت کاری", text)
            }
            text.contains("دوست داری") || text.contains("ترجیح") || text.contains("علاقه") -> {
                LocalFactMemory(address, "PREFERENCE", "ترجیحات کاربر", text)
            }
            text.contains("تصمیم گرفتیم") || text.contains("توافق شد") -> {
                LocalFactMemory(address, "DECISION", "تصمیم ثبت‌شده", text)
            }
            else -> null
        }

        fact?.let {
            val entity = AiMemoryEntity(
                address = it.contactAddress,
                category = it.factCategory,
                memoryKey = it.factKey,
                memoryValue = it.factValue,
                confidence = it.confidenceScore,
                updatedAt = System.currentTimeMillis()
            )
            memoryDao?.insertOrUpdateMemory(entity)
        }
    }

    fun getMemoriesForContact(address: String): Flow<List<AiMemoryEntity>>? {
        return memoryDao?.getMemoriesByAddress(address)
    }

    suspend fun getContextSummaryForContact(address: String): String {
        val memories = memoryDao?.getMemoriesByAddressList(address) ?: emptyList()
        if (memories.isEmpty()) return "هیچ حافظه قبلی ثبت نشده است."
        return memories.joinToString("؛ ") { "${it.memoryKey}: ${it.memoryValue}" }
    }
}
