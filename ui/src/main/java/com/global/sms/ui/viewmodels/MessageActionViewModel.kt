package com.global.sms.ui.viewmodels

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.provider.ContactsContract
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.global.sms.data.db.GlobalSmsDatabase
import com.global.sms.data.entity.MessageCategory
import com.global.sms.data.entity.MessageEntity
import com.global.sms.data.entity.SpamRuleEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MessageActionViewModel(application: Application) : AndroidViewModel(application) {
    private val db = GlobalSmsDatabase.getInstance(application)
    private val messageDao = db.messageDao()
    private val conversationDao = db.conversationDao()
    private val spamRuleDao = db.spamRuleDao()

    fun deleteMessages(messageIds: Collection<Long>, onComplete: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            messageIds.forEach { id ->
                messageDao.deleteMessage(id)
            }
            launch(Dispatchers.Main) { onComplete() }
        }
    }

    fun deleteConversation(threadId: Long, onComplete: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            conversationDao.deleteConversation(threadId)
            messageDao.deleteThreadMessages(threadId)
            launch(Dispatchers.Main) { onComplete() }
        }
    }

    fun archiveConversation(threadId: Long, isArchived: Boolean = true, onComplete: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            conversationDao.setConversationArchived(threadId, isArchived)
            launch(Dispatchers.Main) { onComplete() }
        }
    }

    fun hideMessages(messageIds: Collection<Long>, isHidden: Boolean = true, onComplete: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            messageIds.forEach { id ->
                messageDao.setMessageHidden(id, isHidden)
            }
            launch(Dispatchers.Main) { onComplete() }
        }
    }

    fun hideConversation(threadId: Long, isHidden: Boolean = true, onComplete: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            conversationDao.setConversationHidden(threadId, isHidden)
            launch(Dispatchers.Main) { onComplete() }
        }
    }

    fun blockSender(address: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            spamRuleDao.insertSpamRule(
                SpamRuleEntity(pattern = address.trim(), ruleType = "SENDER")
            )
            launch(Dispatchers.Main) { onComplete() }
        }
    }

    fun reportSpam(address: String, body: String, messageId: Long? = null, onComplete: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            spamRuleDao.insertSpamRule(
                SpamRuleEntity(pattern = address.trim(), ruleType = "SENDER")
            )
            if (messageId != null) {
                val msg = messageDao.getMessageById(messageId)
                if (msg != null) {
                    messageDao.updateMessage(msg.copy(category = MessageCategory.SPAM))
                }
            }
            launch(Dispatchers.Main) { onComplete() }
        }
    }

    fun copyTextToClipboard(context: Context, text: String, label: String = "متن پیامک") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
    }

    fun shareMessage(context: Context, text: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "اشتراک‌گذاری پیامک")
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }

    fun addSenderToContacts(context: Context, address: String) {
        val intent = Intent(Intent.ACTION_INSERT).apply {
            type = ContactsContract.Contacts.CONTENT_TYPE
            putExtra(ContactsContract.Intents.Insert.PHONE, address)
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun addBookmark(messageId: Long, threadId: Long, note: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            db.bookmarkDao().insertBookmark(
                com.global.sms.data.entity.BookmarkEntity(
                    messageId = messageId,
                    threadId = threadId,
                    note = note
                )
            )
        }
    }

    fun exportMessages(context: Context, messages: List<MessageEntity>) {
        viewModelScope.launch(Dispatchers.IO) {
            val exportEngine = com.global.sms.core.engine.MessageExportEngine(context)
            exportEngine.exportMessages(messages, com.global.sms.core.engine.ExportFormat.TXT)
        }
    }
}
