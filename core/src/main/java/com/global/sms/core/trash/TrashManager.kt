package com.global.sms.core.trash

import android.content.Context
import android.util.Log
import com.global.sms.data.db.GlobalSmsDatabase
import com.global.sms.data.entity.ConversationEntity
import com.global.sms.data.entity.MessageEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

data class TrashedConversation(
    val threadId: Long,
    val address: String,
    val contactName: String?,
    val lastMessage: String,
    val messageCount: Int,
    val trashedTimestamp: Long = System.currentTimeMillis(),
    val messagesJson: String = "[]",
    val conversationJson: String = "{}"
) {
    val remainingDays: Int
        get() {
            val elapsedMs = System.currentTimeMillis() - trashedTimestamp
            val elapsedDays = (elapsedMs / (1000L * 60 * 60 * 24)).toInt()
            return (30 - elapsedDays).coerceAtLeast(0)
        }
}

object TrashManager {
    private const val TAG = "TrashManager"
    private const val PREFS_NAME = "global_sms_trash_store"
    private const val KEY_TRASH_ITEMS = "trashed_conversations_v1"
    private const val RETENTION_MILLIS = 30L * 24 * 60 * 60 * 1000L // 30 days

    private val _trashedItems = MutableStateFlow<List<TrashedConversation>>(emptyList())
    val trashedItems: StateFlow<List<TrashedConversation>> = _trashedItems.asStateFlow()

    private var isInitialized = false

    fun init(context: Context) {
        if (isInitialized) return
        isInitialized = true
        loadFromPrefs(context)
        purgeExpiredItems(context)
    }

    private fun loadFromPrefs(context: Context) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val jsonStr = prefs.getString(KEY_TRASH_ITEMS, null) ?: return
            val array = JSONArray(jsonStr)
            val list = mutableListOf<TrashedConversation>()

            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    TrashedConversation(
                        threadId = obj.getLong("threadId"),
                        address = obj.getString("address"),
                        contactName = if (obj.has("contactName") && !obj.isNull("contactName")) obj.getString("contactName") else null,
                        lastMessage = obj.optString("lastMessage", ""),
                        messageCount = obj.optInt("messageCount", 0),
                        trashedTimestamp = obj.optLong("trashedTimestamp", System.currentTimeMillis()),
                        messagesJson = obj.optString("messagesJson", "[]"),
                        conversationJson = obj.optString("conversationJson", "{}")
                    )
                )
            }
            _trashedItems.value = list
        } catch (e: Exception) {
            Log.e(TAG, "Error loading trash items", e)
        }
    }

    private fun saveToPrefs(context: Context) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val array = JSONArray()
            _trashedItems.value.forEach { item ->
                val obj = JSONObject().apply {
                    put("threadId", item.threadId)
                    put("address", item.address)
                    put("contactName", item.contactName)
                    put("lastMessage", item.lastMessage)
                    put("messageCount", item.messageCount)
                    put("trashedTimestamp", item.trashedTimestamp)
                    put("messagesJson", item.messagesJson)
                    put("conversationJson", item.conversationJson)
                }
                array.put(obj)
            }
            prefs.edit().putString(KEY_TRASH_ITEMS, array.toString()).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving trash items", e)
        }
    }

    fun purgeExpiredItems(context: Context) {
        val now = System.currentTimeMillis()
        val validItems = _trashedItems.value.filter { now - it.trashedTimestamp <= RETENTION_MILLIS }
        if (validItems.size != _trashedItems.value.size) {
            _trashedItems.value = validItems
            saveToPrefs(context)
            Log.i(TAG, "Purged expired trash items older than 30 days")
        }
    }

    suspend fun moveToTrash(
        context: Context,
        threadId: Long,
        conversation: ConversationEntity?,
        messages: List<MessageEntity>
    ) {
        init(context)
        try {
            val convJson = if (conversation != null) {
                JSONObject().apply {
                    put("threadId", conversation.threadId)
                    put("address", conversation.address)
                    put("contactName", conversation.contactName)
                    put("lastMessage", conversation.lastMessage)
                    put("lastTimestamp", conversation.lastTimestamp)
                    put("unreadCount", conversation.unreadCount)
                    put("category", conversation.category.name)
                    put("isPinned", conversation.isPinned)
                    put("isHidden", conversation.isHidden)
                }.toString()
            } else "{}"

            val messagesArray = JSONArray()
            messages.forEach { msg ->
                val msgObj = JSONObject().apply {
                    put("id", msg.id)
                    put("threadId", msg.threadId)
                    put("address", msg.address)
                    put("body", msg.body)
                    put("timestamp", msg.timestamp)
                    put("type", msg.type)
                    put("simSlot", msg.simSlot)
                    put("isRead", msg.isRead)
                    put("category", msg.category.name)
                }
                messagesArray.put(msgObj)
            }

            val address = conversation?.address ?: messages.firstOrNull()?.address ?: "ناشناس"
            val contactName = conversation?.contactName
            val lastMessage = conversation?.lastMessage ?: messages.lastOrNull()?.body ?: ""

            val trashed = TrashedConversation(
                threadId = threadId,
                address = address,
                contactName = contactName,
                lastMessage = lastMessage,
                messageCount = messages.size,
                trashedTimestamp = System.currentTimeMillis(),
                messagesJson = messagesArray.toString(),
                conversationJson = convJson
            )

            val current = _trashedItems.value.toMutableList()
            current.removeAll { it.threadId == threadId }
            current.add(0, trashed)
            _trashedItems.value = current
            saveToPrefs(context)
            Log.i(TAG, "Moved conversation $threadId to trash. Total in trash: ${current.size}")
        } catch (e: Exception) {
            Log.e(TAG, "Error moving conversation to trash", e)
        }
    }

    suspend fun restoreFromTrash(context: Context, threadId: Long) {
        val target = _trashedItems.value.find { it.threadId == threadId } ?: return
        try {
            val db = GlobalSmsDatabase.getInstance(context)

            // Reconstruct Conversation
            if (target.conversationJson.isNotBlank() && target.conversationJson != "{}") {
                val obj = JSONObject(target.conversationJson)
                val conv = ConversationEntity(
                    threadId = obj.getLong("threadId"),
                    address = obj.getString("address"),
                    contactName = if (obj.has("contactName") && !obj.isNull("contactName")) obj.getString("contactName") else null,
                    lastMessage = obj.optString("lastMessage", ""),
                    lastTimestamp = obj.optLong("lastTimestamp", System.currentTimeMillis()),
                    unreadCount = obj.optInt("unreadCount", 0),
                    isPinned = obj.optBoolean("isPinned", false),
                    isHidden = obj.optBoolean("isHidden", false)
                )
                db.conversationDao().insertOrUpdateConversation(conv)
            }

            // Reconstruct Messages
            if (target.messagesJson.isNotBlank() && target.messagesJson != "[]") {
                val array = JSONArray(target.messagesJson)
                val messages = mutableListOf<MessageEntity>()
                for (i in 0 until array.length()) {
                    val msgObj = array.getJSONObject(i)
                    messages.add(
                        MessageEntity(
                            id = msgObj.optLong("id", 0L),
                            threadId = msgObj.getLong("threadId"),
                            address = msgObj.getString("address"),
                            body = msgObj.getString("body"),
                            timestamp = msgObj.getLong("timestamp"),
                            type = msgObj.optInt("type", 1),
                            simSlot = msgObj.optInt("simSlot", 0),
                            isRead = msgObj.optBoolean("isRead", true)
                        )
                    )
                }
                db.messageDao().insertMessagesBatch(messages)
            }

            // Remove from trash list
            val updated = _trashedItems.value.filter { it.threadId != threadId }
            _trashedItems.value = updated
            saveToPrefs(context)
            Log.i(TAG, "Restored conversation $threadId from trash successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring from trash", e)
        }
    }

    fun permanentlyDelete(context: Context, threadId: Long) {
        val updated = _trashedItems.value.filter { it.threadId != threadId }
        _trashedItems.value = updated
        saveToPrefs(context)
        Log.i(TAG, "Permanently deleted conversation $threadId from trash")
    }

    fun emptyTrash(context: Context) {
        _trashedItems.value = emptyList()
        saveToPrefs(context)
        Log.i(TAG, "Emptied trash completely")
    }
}
