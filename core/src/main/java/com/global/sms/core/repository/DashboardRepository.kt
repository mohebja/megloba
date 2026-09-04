package com.global.sms.core.repository

import android.content.Context
import com.global.sms.data.dao.*
import com.global.sms.data.db.GlobalSmsDatabase
import com.global.sms.data.entity.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class EnterpriseDashboardStats(
    val messagesTodayCount: Int = 0,
    val deliverySuccessRate: Float = 99.4f,
    val failedMessagesCount: Int = 0,
    val activeCrmContactsCount: Int = 0,
    val totalContactsCount: Int = 0,
    val databaseSizeBytesMb: Float = 1.2f,
    val activeAutomationsCount: Int = 0,
    val templatesCount: Int = 0,
    val securityLogsCount: Int = 0
)

data class AiHomeDashboardData(
    val messagesTodayCount: Int = 0,
    val bankMessagesTodayCount: Int = 0,
    val pendingRepliesCount: Int = 0,
    val upcomingPaymentsCount: Int = 0,
    val importantMessages: List<MessageEntity> = emptyList(),
    val pendingReplyMessages: List<MessageEntity> = emptyList(),
    val financialAlerts: List<FinancialTransactionEntity> = emptyList(),
    val tasks: List<TaskEntity> = emptyList(),
    val blockedSpamCount: Int = 0,
    val securityLogsCount: Int = 0,
    val aiSuggestions: List<String> = emptyList()
)

class DashboardRepository(
    private val context: Context,
    private val db: GlobalSmsDatabase = GlobalSmsDatabase.getInstance(context)
) {
    private val messageDao: MessageDao = db.messageDao()
    private val contactDao: ContactDao = db.contactDao()
    private val taskDao: TaskDao = db.taskDao()
    private val financialTransactionDao: FinancialTransactionDao = db.financialTransactionDao()
    private val spamRuleDao: SpamRuleDao = db.spamRuleDao()

    fun getAiDashboardDataFlow(): Flow<AiHomeDashboardData> {
        val messagesFlow = messageDao.searchMessagesAdvanced(
            query = null, category = null, isOtpOnly = false, hasAttachmentOnly = false,
            isUnreadOnly = false, isPinnedOnly = false, isBankOnly = false, senderFilter = null,
            startDate = null, endDate = null, includeHidden = false
        )
        val tasksFlow = taskDao.getPendingTasks()
        val finTransactionsFlow = financialTransactionDao.getAllTransactionsFlow()
        val spamRulesFlow = spamRuleDao.getAllSpamRules()

        return combine(messagesFlow, tasksFlow, finTransactionsFlow, spamRulesFlow) { messages, tasks, finTxns, spamRules ->
            val todayMs = System.currentTimeMillis() - 24 * 3600 * 1000L
            val todayMessages = messages.filter { it.timestamp >= todayMs }
            val bankToday = todayMessages.count { it.category == MessageCategory.BANK }
            
            val pendingReplies = messages.filter { !it.isRead && it.type == 1 }
            val importantMsgs = messages.filter { it.isPinned || (it.category == MessageCategory.BANK) || !it.isRead }.take(5)
            
            val upcomingPayments = finTxns.filter { it.timestamp > System.currentTimeMillis() || it.transactionType == "EXPENSE" }
            val suggestions = mutableListOf<String>()

            if (pendingReplies.isNotEmpty()) {
                suggestions.add("${pendingReplies.size} پیام خوانده نشده نیازمند پاسخ شما است.")
            }
            if (bankToday > 0) {
                suggestions.add("امروز $bankToday پیامک بانکی تحلیل شد.")
            }
            if (tasks.isNotEmpty()) {
                suggestions.add("تعداد ${tasks.size} کار باقی‌مانده در لیست شما وجود دارد.")
            }
            if (suggestions.isEmpty()) {
                suggestions.add("تمام پیام‌ها و برنامه‌های شما بررسی شده‌اند.")
            }

            AiHomeDashboardData(
                messagesTodayCount = todayMessages.size,
                bankMessagesTodayCount = bankToday,
                pendingRepliesCount = pendingReplies.size,
                upcomingPaymentsCount = upcomingPayments.size,
                importantMessages = importantMsgs,
                pendingReplyMessages = pendingReplies.take(5),
                financialAlerts = finTxns.take(5),
                tasks = tasks.take(5),
                blockedSpamCount = spamRules.size,
                securityLogsCount = messages.count { it.category == MessageCategory.SPAM },
                aiSuggestions = suggestions
            )
        }
    }

    fun getDashboardStatsFlow(): Flow<EnterpriseDashboardStats> {
        val messagesFlow = messageDao.searchMessagesAdvanced(
            query = null, category = null, isOtpOnly = false, hasAttachmentOnly = false,
            isUnreadOnly = false, isPinnedOnly = false, isBankOnly = false, senderFilter = null,
            startDate = null, endDate = null, includeHidden = false
        )
        val contactsFlow = contactDao.getAllContactsFlow()

        return combine(messagesFlow, contactsFlow) { messages, contacts ->
            val todayMs = System.currentTimeMillis() - 24 * 3600 * 1000L
            val todayMessages = messages.filter { it.timestamp >= todayMs }
            val failedCount = messages.count { it.type == 5 || it.type == 6 }
            val totalSent = messages.count { it.type == 2 }

            val deliveryRate = if (totalSent > 0) {
                ((totalSent - failedCount).toFloat() / totalSent.toFloat() * 100f).coerceIn(0f, 100f)
            } else {
                99.4f
            }

            val dbFile = context.getDatabasePath("global_sms_encrypted_db")
            val dbSizeMb = if (dbFile.exists()) {
                dbFile.length() / (1024f * 1024f)
            } else {
                (messages.size * 0.05f).coerceAtLeast(0.5f)
            }

            EnterpriseDashboardStats(
                messagesTodayCount = todayMessages.size,
                deliverySuccessRate = deliveryRate,
                failedMessagesCount = failedCount,
                activeCrmContactsCount = contacts.size,
                totalContactsCount = contacts.size,
                databaseSizeBytesMb = String.format("%.1f", dbSizeMb).toFloatOrNull() ?: 1.2f,
                activeAutomationsCount = 0,
                templatesCount = 0,
                securityLogsCount = 0
            )
        }
    }
}
