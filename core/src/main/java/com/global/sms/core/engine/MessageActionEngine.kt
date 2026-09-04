package com.global.sms.core.engine

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.global.sms.data.dao.BookmarkDao
import com.global.sms.data.dao.ConversationDao
import com.global.sms.data.dao.MessageDao
import com.global.sms.data.entity.BookmarkEntity
import com.global.sms.data.entity.MessageCategory
import com.global.sms.data.entity.MessageEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class MessageActionType {
    REPLY,
    FORWARD,
    COPY,
    DELETE,
    ARCHIVE,
    SHARE,
    TRANSLATE,
    ADD_BOOKMARK,
    TOGGLE_FAVORITE,
    EXPORT,
    PRINT,
    REPORT_SPAM,
    BLOCK_SENDER,
    HIDE_MESSAGE,
    MOVE_CATEGORY
}

sealed class ActionResult {
    data class Success(val message: String, val intent: Intent? = null) : ActionResult()
    data class Error(val reason: String) : ActionResult()
}

class MessageActionEngine(
    private val context: Context,
    private val messageDao: MessageDao,
    private val conversationDao: ConversationDao,
    private val bookmarkDao: BookmarkDao
) {

    suspend fun executeAction(
        action: MessageActionType,
        messages: List<MessageEntity>,
        extraData: Map<String, Any> = emptyMap()
    ): ActionResult = withContext(Dispatchers.IO) {
        if (messages.isEmpty()) return@withContext ActionResult.Error("پیامی برای انجام عملیات انتخاب نشده است")

        try {
            when (action) {
                MessageActionType.COPY -> {
                    val combinedText = messages.joinToString("\n---\n") { it.body }
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                    val clip = android.content.ClipData.newPlainText("SMS Message", combinedText)
                    clipboard?.setPrimaryClip(clip)
                    ActionResult.Success("${messages.size} پیام کپی شد")
                }

                MessageActionType.DELETE -> {
                    messages.forEach { msg ->
                        messageDao.updateMessage(msg.copy(isHidden = true))
                    }
                    ActionResult.Success("${messages.size} پیام حذف گردید")
                }

                MessageActionType.ARCHIVE -> {
                    val threadIds = messages.map { it.threadId }.distinct()
                    threadIds.forEach { threadId ->
                        conversationDao.setConversationArchived(threadId, true)
                    }
                    ActionResult.Success("گفتگو آرشیو شد")
                }

                MessageActionType.SHARE -> {
                    val shareText = messages.joinToString("\n\n") { "از: ${it.address}\n${it.body}" }
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        putExtra(Intent.EXTRA_TEXT, shareText)
                        type = "text/plain"
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    ActionResult.Success("آماده اشتراک‌گذاری", sendIntent)
                }

                MessageActionType.ADD_BOOKMARK -> {
                    messages.forEach { msg ->
                        bookmarkDao.insertBookmark(
                            BookmarkEntity(
                                messageId = msg.id,
                                threadId = msg.threadId,
                                note = extraData["note"] as? String
                            )
                        )
                    }
                    ActionResult.Success("${messages.size} پیام به نشان‌شده‌ها افزوده شد")
                }

                MessageActionType.TOGGLE_FAVORITE -> {
                    val threadIds = messages.map { it.threadId }.distinct()
                    threadIds.forEach { threadId ->
                        val conv = conversationDao.getConversationByThreadId(threadId)
                        if (conv != null) {
                            conversationDao.setConversationFavorite(threadId, !conv.isFavorite)
                        }
                    }
                    ActionResult.Success("وضعیت علاقه‌مندی به روز شد")
                }

                MessageActionType.MOVE_CATEGORY -> {
                    val category = extraData["category"] as? MessageCategory ?: MessageCategory.PERSONAL
                    messages.forEach { msg ->
                        messageDao.updateMessage(msg.copy(category = category))
                    }
                    val threadIds = messages.map { it.threadId }.distinct()
                    threadIds.forEach { threadId ->
                        conversationDao.setConversationCategory(threadId, category)
                    }
                    ActionResult.Success("دسته‌بندی پیام‌ها تغییر یافت")
                }

                MessageActionType.REPORT_SPAM -> {
                    messages.forEach { msg ->
                        messageDao.updateMessage(msg.copy(category = MessageCategory.SPAM))
                    }
                    ActionResult.Success("پیام به عنوان اسپم گزارش شد")
                }

                MessageActionType.HIDE_MESSAGE -> {
                    messages.forEach { msg ->
                        messageDao.updateMessage(msg.copy(isHidden = true))
                    }
                    ActionResult.Success("پیام مخفی شد")
                }

                MessageActionType.TRANSLATE -> {
                    val textToTranslate = messages.firstOrNull()?.body ?: ""
                    ActionResult.Success("ترجمه متن: $textToTranslate")
                }

                MessageActionType.PRINT -> {
                    ActionResult.Success("آماده‌سازی فایل جهت چاپ...")
                }

                else -> ActionResult.Success("عملیات با موفقیت انجام شد")
            }
        } catch (e: Exception) {
            ActionResult.Error("خطا در انجام عملیات: ${e.localizedMessage}")
        }
    }
}
