package com.global.sms.core.trash

import android.content.Context
import com.global.sms.data.db.GlobalSmsDatabase
import com.global.sms.data.entity.MessageEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

data class TrashedMessageItem(
    val id: Long,
    val threadId: Long,
    val address: String,
    val body: String,
    val originalDate: Long,
    val trashedAt: Long,
    val expiresAt: Long
)

object RecycleBinManager {

    private const val TRASH_PREFS_NAME = "global_sms_recycle_bin"
    private const val KEY_TRASHED_IDS = "trashed_message_ids"

    private val _trashedItems = MutableStateFlow<List<TrashedMessageItem>>(emptyList())
    val trashedItems: StateFlow<List<TrashedMessageItem>> = _trashedItems.asStateFlow()

    suspend fun loadTrash(context: Context) = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences(TRASH_PREFS_NAME, Context.MODE_PRIVATE)
        val rawEntries = prefs.getStringSet(KEY_TRASHED_IDS, emptySet()) ?: emptySet()
        val now = System.currentTimeMillis()

        val parsed = rawEntries.mapNotNull { entry ->
            val parts = entry.split("|")
            if (parts.size >= 6) {
                val id = parts[0].toLongOrNull() ?: return@mapNotNull null
                val threadId = parts[1].toLongOrNull() ?: 0L
                val address = parts[2]
                val body = parts[3]
                val originalDate = parts[4].toLongOrNull() ?: now
                val trashedAt = parts[5].toLongOrNull() ?: now
                val expiresAt = trashedAt + TimeUnit.DAYS.toMillis(30)
                TrashedMessageItem(id, threadId, address, body, originalDate, trashedAt, expiresAt)
            } else null
        }.filter { it.expiresAt > now } // Filter expired

        _trashedItems.value = parsed.sortedByDescending { it.trashedAt }
    }

    suspend fun moveToTrash(context: Context, message: MessageEntity) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val item = TrashedMessageItem(
            id = message.id,
            threadId = message.threadId,
            address = message.address,
            body = message.body,
            originalDate = message.timestamp,
            trashedAt = now,
            expiresAt = now + TimeUnit.DAYS.toMillis(30)
        )

        val db = GlobalSmsDatabase.getInstance(context)
        db.messageDao().deleteMessageById(message.id)

        val prefs = context.getSharedPreferences(TRASH_PREFS_NAME, Context.MODE_PRIVATE)
        val currentSet = prefs.getStringSet(KEY_TRASHED_IDS, emptySet())?.toMutableSet() ?: mutableSetOf()
        // Format: id|threadId|address|body|originalDate|trashedAt
        val sanitizedBody = message.body.replace("|", "/")
        currentSet.add("${message.id}|${message.threadId}|${message.address}|$sanitizedBody|${message.timestamp}|$now")
        prefs.edit().putStringSet(KEY_TRASHED_IDS, currentSet).apply()

        loadTrash(context)
    }

    suspend fun restoreFromTrash(context: Context, item: TrashedMessageItem) = withContext(Dispatchers.IO) {
        val db = GlobalSmsDatabase.getInstance(context)
        val restoredEntity = MessageEntity(
            id = item.id,
            threadId = item.threadId,
            address = item.address,
            body = item.body,
            timestamp = item.originalDate
        )
        db.messageDao().insertMessage(restoredEntity)

        permanentlyDelete(context, item.id)
    }

    suspend fun restoreAll(context: Context) = withContext(Dispatchers.IO) {
        val currentList = _trashedItems.value
        val db = GlobalSmsDatabase.getInstance(context)
        val entities = currentList.map { item ->
            MessageEntity(
                id = item.id,
                threadId = item.threadId,
                address = item.address,
                body = item.body,
                timestamp = item.originalDate
            )
        }
        db.messageDao().insertMessagesBatch(entities)
        emptyTrash(context)
    }

    suspend fun permanentlyDelete(context: Context, messageId: Long) = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences(TRASH_PREFS_NAME, Context.MODE_PRIVATE)
        val currentSet = prefs.getStringSet(KEY_TRASHED_IDS, emptySet())?.toMutableSet() ?: mutableSetOf()
        currentSet.removeAll { it.startsWith("$messageId|") }
        prefs.edit().putStringSet(KEY_TRASHED_IDS, currentSet).apply()
        loadTrash(context)
    }

    suspend fun emptyTrash(context: Context) = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences(TRASH_PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_TRASHED_IDS).apply()
        _trashedItems.value = emptyList()
    }
}
