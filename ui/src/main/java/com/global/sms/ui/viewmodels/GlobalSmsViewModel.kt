package com.global.sms.ui.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.global.sms.data.dao.*
import com.global.sms.data.db.GlobalSmsDatabase
import com.global.sms.data.entity.*
import com.global.sms.data.repository.SettingsRepository
import com.global.sms.engine.sim.DualSimManager
import com.global.sms.engine.worker.SmsSchedulerWorker
import com.global.sms.engine.tts.TtsManager
import com.global.sms.security.lock.AppLockManager
import com.global.sms.security.backup.BackupManager
import com.global.sms.security.backup.EncryptedBackupManager
import com.global.sms.security.prefs.SecurePreferencesManager
import com.global.sms.security.vault.PrivateVaultSecurityManager
import com.global.sms.security.device.DeviceSecurityScanner
import com.global.sms.security.device.SecurityReport
import com.global.sms.security.network.LinkAndUssdSecurityManager
import com.global.sms.security.network.LinkScanResult
import com.global.sms.security.network.UssdScanResult
import com.global.sms.security.audit.SecurityAuditManager
import com.global.sms.security.audit.FullSecurityAuditReport
import com.global.sms.security.clipboard.SecureClipboardManager
import com.global.sms.core.parser.BankSmsAnalysis
import com.global.sms.core.parser.BankTransactionParser
import com.global.sms.core.analytics.LocalAnalyticsSummary
import com.global.sms.core.analytics.MessageAnalyticsEngine
import com.global.sms.core.engine.*
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.global.sms.data.db.PerformanceReportManager
import com.global.sms.data.db.SystemPerformanceMetrics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.TimeUnit

data class SearchFilterState(
    val query: String = "",
    val senderQuery: String = "",
    val selectedCategory: MessageCategory? = null,
    val isOtpOnly: Boolean = false,
    val hasAttachmentOnly: Boolean = false,
    val isUnreadOnly: Boolean = false,
    val isFavoritesOnly: Boolean = false,
    val isPinnedOnly: Boolean = false,
    val isBankOnly: Boolean = false,
    val startDate: Long? = null,
    val endDate: Long? = null,
    val includeHidden: Boolean = false
) {
    val activeFilterCount: Int
        get() {
            var count = 0
            if (senderQuery.isNotBlank()) count++
            if (selectedCategory != null) count++
            if (isOtpOnly) count++
            if (hasAttachmentOnly) count++
            if (isUnreadOnly) count++
            if (isFavoritesOnly) count++
            if (isPinnedOnly) count++
            if (isBankOnly) count++
            if (startDate != null || endDate != null) count++
            if (includeHidden) count++
            return count
        }

    val isEmpty: Boolean get() = query.isBlank() && activeFilterCount == 0
}

class GlobalSmsViewModel(application: Application) : AndroidViewModel(application) {

    private val db = GlobalSmsDatabase.getInstance(application)
    private val messageDao = db.messageDao()
    private val conversationDao = db.conversationDao()
    private val scheduledMessageDao = db.scheduledMessageDao()
    private val quickReplyDao = db.quickReplyDao()
    private val spamRuleDao = db.spamRuleDao()
    private val _isDbInitialized = MutableStateFlow(false)
    val isDbInitialized: StateFlow<Boolean> = _isDbInitialized.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Ensure room database instance and core tables are ready
                db.openHelper.writableDatabase
                _isDbInitialized.value = true
            } catch (e: Exception) {
                Log.e("GlobalSmsViewModel", "Database warmup initialization error", e)
                _isDbInitialized.value = true
            }
        }
    }

    private val searchHistoryDao = db.searchHistoryDao()
    private val contactDao = db.contactDao()
    private val contactGroupDao = db.contactGroupDao()
    private val otpDao = db.otpDao()
    private val aiSettingsDao = db.aiSettingsDao()
    private val smsImportLogDao = db.smsImportLogDao()
    private val aiMemoryDao = db.aiMemoryDao()
    private val taskDao = db.taskDao()
    private val automationTemplateDao = db.automationTemplateDao()

    val activeOtpsFlow = otpDao.getActiveOtpsFlow()
    val allOtpsFlow = otpDao.getAllOtpsFlow()
    val aiSettingsFlow = aiSettingsDao.getAiSettingsFlow()

    val aiMemoriesFlow: StateFlow<List<com.global.sms.data.entity.AiMemoryEntity>> = aiMemoryDao.getAllMemoriesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTasksFlow: StateFlow<List<com.global.sms.data.entity.TaskEntity>> = taskDao.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val automationTemplatesFlow: StateFlow<List<com.global.sms.data.entity.AutomationTemplateEntity>> = automationTemplateDao.getAllTemplates()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun insertAiMemory(memory: com.global.sms.data.entity.AiMemoryEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            aiMemoryDao.insertOrUpdateMemory(memory)
        }
    }

    fun deleteAiMemory(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            aiMemoryDao.deleteMemory(id)
        }
    }

    fun clearAllAiMemories() {
        viewModelScope.launch(Dispatchers.IO) {
            aiMemoryDao.deleteAllMemories()
        }
    }

    fun insertTask(task: com.global.sms.data.entity.TaskEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            taskDao.insertTask(task)
        }
    }

    fun setTaskCompleted(taskId: Long, isCompleted: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            taskDao.setTaskCompleted(taskId, isCompleted)
        }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            taskDao.deleteTask(taskId)
        }
    }

    fun toggleAutomationTemplate(templateId: String, activated: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            automationTemplateDao.updateTemplateActivation(templateId, activated)
        }
    }

    fun insertAutomationTemplate(template: com.global.sms.data.entity.AutomationTemplateEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            automationTemplateDao.insertTemplate(template)
        }
    }

    val importLogs: StateFlow<List<com.global.sms.data.entity.SmsImportLogEntity>> = smsImportLogDao.getAllImportLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalImportedSmsCount: StateFlow<Int> = messageDao.getTotalMessageCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalConversationsCount: StateFlow<Int> = conversationDao.getTotalConversationsCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun markOtpAsUsed(otpId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            otpDao.markAsUsed(otpId)
        }
    }

    fun deleteOtp(otpId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            otpDao.deleteOtp(otpId)
        }
    }

    fun updateAiSettings(settings: AiSettingsEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            aiSettingsDao.saveAiSettings(settings)
        }
    }


    val contactGroups: StateFlow<List<ContactGroupEntity>> = contactGroupDao.getAllGroupsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createContactGroup(name: String, description: String?, members: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            val group = ContactGroupEntity(
                name = name,
                description = description,
                members = members.joinToString(",")
            )
            contactGroupDao.insertGroup(group)
        }
    }

    fun updateContactGroup(group: ContactGroupEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            contactGroupDao.updateGroup(group)
        }
    }

    fun deleteContactGroup(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            contactGroupDao.deleteGroupById(id)
        }
    }

    fun sendGroupSms(recipients: List<String>, body: String, simSlot: Int = 0) {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>()
            recipients.forEach { rawAddress ->
                val address = rawAddress.trim()
                if (address.isNotBlank()) {
                    val threadId = address.hashCode().toLong()
                    val msg = MessageEntity(
                        threadId = threadId,
                        address = address,
                        body = body,
                        type = MessageType.OUTBOX.code,
                        simSlot = simSlot,
                        deliveryStatus = MessageStatus.PENDING.code
                    )
                    val insertedId = messageDao.insertMessage(msg)
                    com.global.sms.engine.sender.SmsSender.sendSms(
                        context = context,
                        messageId = insertedId,
                        address = address,
                        body = body,
                        simSlot = simSlot
                    )
                }
            }
        }
    }

    fun updateConversationStyle(style: String) {
        viewModelScope.launch {
            val current = settingsRepository.getSettingsOnce()
            settingsRepository.saveSettings(current.copy(conversationStyle = style))
        }
    }

    fun updateMessageFontScale(scale: Float) {
        viewModelScope.launch {
            val current = settingsRepository.getSettingsOnce()
            val clamped = scale.coerceIn(0.7f, 2.2f)
            settingsRepository.saveSettings(current.copy(messageFontScale = clamped))
        }
    }


    val appLockManager = AppLockManager(application)
    val securePrefsManager = SecurePreferencesManager(application)
    val vaultSecurityManager = PrivateVaultSecurityManager(application)
    val ttsManager = TtsManager(application)

    // Security & Compliance Audit State
    val deviceSecurityReport: StateFlow<SecurityReport> = MutableStateFlow(
        DeviceSecurityScanner.scanDevice(application)
    ).asStateFlow()

    fun runSecurityAudit(): FullSecurityAuditReport {
        val context = getApplication<Application>()
        val report = SecurityAuditManager.performSecurityAudit(context)
        val reportFile = File(context.filesDir, "SECURITY_AUDIT_REPORT.md")
        SecurityAuditManager.generateSecurityReportFile(context, reportFile)
        return report
    }

    fun copyToSecureClipboard(label: String, text: String, isSensitive: Boolean = true) {
        SecureClipboardManager.copyToClipboard(
            context = getApplication(),
            label = label,
            text = text,
            isSensitive = isSensitive
        )
    }

    fun scanUrl(url: String): LinkScanResult {
        return LinkAndUssdSecurityManager.scanUrl(url)
    }

    fun inspectUssdCode(code: String): UssdScanResult {
        return LinkAndUssdSecurityManager.inspectUssdCode(code)
    }

    val settingsRepository = SettingsRepository(db.settingsDao(), db.categoryDao())
    val settingsState: StateFlow<SettingsEntity> = settingsRepository.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsEntity())
    val categoriesState: StateFlow<List<CategoryEntity>> = settingsRepository.categoriesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bankAnalyses: StateFlow<List<BankSmsAnalysis>> = messageDao.getBankMessages()
        .map { messages ->
            messages.map { msg ->
                BankTransactionParser.analyzeMessage(
                    sender = msg.address,
                    body = msg.body,
                    messageId = msg.id,
                    timestamp = msg.timestamp
                )
            }
        }
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val messageAnalyticsFlow: StateFlow<LocalAnalyticsSummary> = messageDao.getAllMessagesFlow()
        .map { messages ->
            MessageAnalyticsEngine.calculateSummary(messages)
        }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LocalAnalyticsSummary())

    // UI Navigation State
    private val _selectedThreadId = MutableStateFlow<Long?>(null)
    val selectedThreadId: StateFlow<Long?> = _selectedThreadId.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<MessageCategory?>(null)
    val selectedCategory: StateFlow<MessageCategory?> = _selectedCategory.asStateFlow()

    // Professional Search Engine State & Flow
    val recentSearches: StateFlow<List<SearchHistoryEntity>> = searchHistoryDao.getRecentSearches()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchFilterState = MutableStateFlow(SearchFilterState())
    val searchFilterState: StateFlow<SearchFilterState> = _searchFilterState.asStateFlow()

    fun updateSearchFilter(transform: (SearchFilterState) -> SearchFilterState) {
        _searchFilterState.value = transform(_searchFilterState.value)
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        _searchFilterState.value = _searchFilterState.value.copy(query = query)
    }

    fun setCategoryFilter(category: MessageCategory?) {
        _selectedCategory.value = category
        _searchFilterState.value = _searchFilterState.value.copy(selectedCategory = category)
    }


    fun resetSearchFilters() {
        _searchFilterState.value = SearchFilterState(query = _searchFilterState.value.query)
    }

    fun saveCurrentSearchToHistory() {
        val q = _searchFilterState.value.query.trim()
        if (q.isNotBlank()) {
            viewModelScope.launch(Dispatchers.IO) {
                searchHistoryDao.insertSearch(SearchHistoryEntity(query = q))
            }
        }
    }

    fun deleteRecentSearch(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            searchHistoryDao.deleteSearchById(id)
        }
    }

    fun clearRecentSearches() {
        viewModelScope.launch(Dispatchers.IO) {
            searchHistoryDao.clearSearchHistory()
        }
    }

    val searchEngine = com.global.sms.core.search.SearchEngine(messageDao, contactDao, searchHistoryDao)

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val searchResults: StateFlow<List<MessageEntity>> = _searchFilterState
        .debounce(200)
        .flatMapLatest { filter ->
            if (filter.isEmpty) {
                flowOf(emptyList())
            } else {
                val criteria = com.global.sms.core.search.SearchFilterCriteria(
                    query = filter.query,
                    categories = filter.selectedCategory?.let { setOf(it) } ?: emptySet(),
                    isUnreadOnly = filter.isUnreadOnly,
                    isFavoritesOnly = filter.isFavoritesOnly,
                    isPinnedOnly = filter.isPinnedOnly,
                    hasAttachmentOnly = filter.hasAttachmentOnly,
                    isOtpOnly = filter.isOtpOnly,
                    isBankOnly = filter.isBankOnly,
                    isHiddenOnly = filter.includeHidden && _isVaultUnlocked.value,
                    startDate = filter.startDate,
                    endDate = filter.endDate,
                    senderFilter = filter.senderQuery.ifBlank { null }
                )
                searchEngine.search(criteria).map { items -> items.map { it.message } }
            }
        }
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    // Private Vault Auth State
    private val _isVaultUnlocked = MutableStateFlow(false)
    val isVaultUnlocked: StateFlow<Boolean> = _isVaultUnlocked.asStateFlow()

    // Default SMS Handler & Import State
    val isDefaultSmsApp = MutableStateFlow(true)
    val showDefaultSmsDialog = MutableStateFlow(false)
    val isImportingSms = MutableStateFlow(false)
    val smsImportProgress = MutableStateFlow(0f)
    val smsImportStatusText = MutableStateFlow("")

    fun checkDefaultSmsApp() {
        val context = getApplication<Application>()
        val isDefault = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(android.app.role.RoleManager::class.java)
            roleManager?.isRoleHeld(android.app.role.RoleManager.ROLE_SMS) == true
        } else {
            android.provider.Telephony.Sms.getDefaultSmsPackage(context) == context.packageName
        }
        isDefaultSmsApp.value = isDefault
        if (!isDefault) {
            showDefaultSmsDialog.value = true
        }
    }

    fun startHistoricalSmsImport(force: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>()
            val hasReadPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_SMS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED

            if (!hasReadPermission) {
                Log.d("GlobalSmsViewModel", "READ_SMS permission not granted yet, skipping auto SMS import")
                return@launch
            }

            val existingCount = messageDao.getTotalMessageCountOnce()
            if (force || existingCount == 0) {
                isImportingSms.value = true
                smsImportStatusText.value = "در حال شروع انتقال پیامک‌ها..."
                smsImportProgress.value = 0f

                try {
                    val importedTotal = com.global.sms.engine.importer.SmsImporter.importSystemSms(
                        context = getApplication()
                    ) { progress, current, total ->
                        smsImportProgress.value = progress
                        smsImportStatusText.value = "در حال دریافت $current از $total پیامک..."
                    }
                    if (importedTotal > 0) {
                        smsImportStatusText.value = "همگام‌سازی $importedTotal پیامک با موفقیت انجام شد."
                    } else {
                        smsImportStatusText.value = ""
                    }
                } catch (e: SecurityException) {
                    Log.w("GlobalSmsViewModel", "Permission denied during system SMS import", e)
                    smsImportStatusText.value = ""
                } catch (e: Exception) {
                    Log.e("GlobalSmsViewModel", "Error importing system SMS messages", e)
                    smsImportStatusText.value = "خطا در دریافت پیامک‌ها"
                } finally {
                    isImportingSms.value = false
                }
            }
        }
    }

    // Settings State
    val isDarkTheme = MutableStateFlow(false)
    val isAmoledMode = MutableStateFlow(false)
    val isRtlPersian = MutableStateFlow(true)
    val usePersianDigits = MutableStateFlow(true)
    val usePersianCalendar = MutableStateFlow(true)

    // Reactive Data Flows
    @OptIn(ExperimentalCoroutinesApi::class)
    val conversations: StateFlow<List<ConversationEntity>> = _selectedCategory
        .flatMapLatest { cat ->
            if (cat == null) {
                conversationDao.getAllConversations()
            } else {
                conversationDao.getConversationsByCategory(cat)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeThreadMessages: StateFlow<List<MessageEntity>> = _selectedThreadId
        .flatMapLatest { threadId ->
            if (threadId != null) {
                messageDao.getMessagesForThread(threadId)
            } else {
                MutableStateFlow(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val hiddenMessages: StateFlow<List<MessageEntity>> = messageDao.getHiddenMessages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val scheduledMessages: StateFlow<List<ScheduledMessageEntity>> = scheduledMessageDao.getAllScheduledMessages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val quickReplies: StateFlow<List<QuickReplyEntity>> = quickReplyDao.getAllQuickReplies()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val spamMessages: StateFlow<List<MessageEntity>> = messageDao.getSpamMessages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Paging 3 Flow for Conversations List
    @OptIn(ExperimentalCoroutinesApi::class)
    val conversationsPagingFlow: kotlinx.coroutines.flow.Flow<PagingData<ConversationEntity>> = _selectedCategory
        .flatMapLatest { cat ->
            Pager(
                config = PagingConfig(
                    pageSize = 30,
                    prefetchDistance = 15,
                    enablePlaceholders = false,
                    initialLoadSize = 30
                )
            ) {
                if (cat == null) {
                    conversationDao.getAllConversationsPagingSource()
                } else {
                    conversationDao.getConversationsByCategoryPagingSource(cat)
                }
            }.flow
        }
        .cachedIn(viewModelScope)

    // Paging 3 Flow for Active Thread Messages List
    @OptIn(ExperimentalCoroutinesApi::class)
    val activeThreadMessagesPagingFlow: kotlinx.coroutines.flow.Flow<PagingData<MessageEntity>> = _selectedThreadId
        .flatMapLatest { threadId ->
            if (threadId != null) {
                Pager(
                    config = PagingConfig(
                        pageSize = 50,
                        prefetchDistance = 20,
                        enablePlaceholders = false,
                        initialLoadSize = 50
                    )
                ) {
                    messageDao.getMessagesForThreadPagingSource(threadId)
                }.flow
            } else {
                kotlinx.coroutines.flow.flowOf(PagingData.empty())
            }
        }
        .cachedIn(viewModelScope)

    // Performance Report Flow & Benchmark Methods
    private val _performanceReportMarkdown = MutableStateFlow<String?>(null)
    val performanceReportMarkdown: StateFlow<String?> = _performanceReportMarkdown.asStateFlow()

    private val _isBenchmarking = MutableStateFlow(false)
    val isBenchmarking: StateFlow<Boolean> = _isBenchmarking.asStateFlow()

    fun generatePerformanceReport() {
        viewModelScope.launch(Dispatchers.IO) {
            val markdown = PerformanceReportManager.generatePerformanceReportMarkdown(db)
            _performanceReportMarkdown.value = markdown
        }
    }

    fun seedBenchmarkMessages(count: Int, onComplete: (Long) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            _isBenchmarking.value = true
            val elapsedMs = PerformanceReportManager.seedBenchmarkMessages(db, count)
            _isBenchmarking.value = false
            generatePerformanceReport()
            withContext(Dispatchers.Main) {
                onComplete(elapsedMs)
            }
        }
    }

    val totalCount = messageDao.getTotalMessageCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val sentCount = messageDao.getSentMessageCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val receivedCount = messageDao.getReceivedMessageCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val spamCount = messageDao.getSpamCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val bankCount = messageDao.getBankCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        // Seed default Quick Reply templates if empty
        viewModelScope.launch {
            seedDefaultData()
        }
    }

    private suspend fun seedDefaultData() {
        val defaultReplies = listOf(
            QuickReplyEntity(title = "بعداً تماس می‌گیرم", content = "سلام، الان در جلسه هستم. بعداً با شما تماس خواهم گرفت."),
            QuickReplyEntity(title = "دریافت شد", content = "پیام شما دریافت شد، با تشکر."),
            QuickReplyEntity(title = "در حال حرکت", content = "در حال رانندگی هستم، رسیدم تماس می‌گیرم."),
            QuickReplyEntity(title = "آدرس و اطلاعات", content = "اطلاعات مورد نظر برای شما ارسال خواهد شد.")
        )
        defaultReplies.forEach { quickReplyDao.insertQuickReply(it) }
    }

    fun selectThread(threadId: Long?) {
        _selectedThreadId.value = threadId
        if (threadId != null) {
            viewModelScope.launch {
                messageDao.markThreadAsRead(threadId)
            }
        }
    }

    fun unlockVault(pin: String): Boolean {
        val success = appLockManager.verifyPin(pin)
        _isVaultUnlocked.value = success
        return success
    }

    fun unlockVaultWithBiometrics() {
        _isVaultUnlocked.value = true
    }

    fun lockVault() {
        _isVaultUnlocked.value = false
    }

    val pendingQueue: StateFlow<List<MessageEntity>> = com.global.sms.engine.queue.SmsQueueManager.getPendingQueue(application)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val retryQueue: StateFlow<List<MessageEntity>> = com.global.sms.engine.queue.SmsQueueManager.getRetryQueue(application)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val failedQueue: StateFlow<List<MessageEntity>> = com.global.sms.engine.queue.SmsQueueManager.getFailedQueue(application)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun sendMessage(address: String, body: String, simSlot: Int = 0) {
        viewModelScope.launch {
            com.global.sms.engine.dispatcher.MessageDispatcher.dispatchSendMessage(
                context = getApplication(),
                address = address,
                body = body,
                simSlot = simSlot,
                threadId = _selectedThreadId.value
            )
        }
    }

    fun scheduleMessage(address: String, body: String, scheduledTimeMillis: Long, simSlot: Int = 0) {
        viewModelScope.launch {
            com.global.sms.engine.dispatcher.MessageDispatcher.dispatchScheduledMessage(
                context = getApplication(),
                address = address,
                body = body,
                scheduledTimestamp = scheduledTimeMillis,
                simSlot = simSlot
            )
        }
    }

    fun cancelScheduledMessage(id: Long) {
        viewModelScope.launch {
            com.global.sms.engine.queue.SmsQueueManager.cancelScheduledMessage(getApplication(), id)
            scheduledMessageDao.deleteScheduledMessage(id)
        }
    }

    fun retryFailedMessage(messageId: Long) {
        viewModelScope.launch {
            com.global.sms.engine.queue.SmsQueueManager.retryFailedMessage(getApplication(), messageId)
        }
    }

    fun clearFailedQueue() {
        viewModelScope.launch {
            com.global.sms.engine.queue.SmsQueueManager.clearFailedQueue(getApplication())
        }
    }

    fun hideMessage(messageId: Long, isHidden: Boolean) {
        viewModelScope.launch {
            messageDao.setMessageHidden(messageId, isHidden)
        }
    }

    fun hideConversation(threadId: Long, isHidden: Boolean) {
        viewModelScope.launch {
            conversationDao.setConversationHidden(threadId, isHidden)
        }
    }

    fun archiveConversation(threadId: Long, isArchived: Boolean = true) {
        viewModelScope.launch {
            conversationDao.setConversationArchived(threadId, isArchived)
        }
    }

    fun markConversationRead(threadId: Long) {
        viewModelScope.launch {
            conversationDao.markConversationRead(threadId)
            messageDao.markThreadAsRead(threadId)
        }
    }

    fun markConversationUnread(threadId: Long) {
        viewModelScope.launch {
            conversationDao.markConversationUnread(threadId)
        }
    }

    fun toggleReadUnread(conversation: ConversationEntity) {
        viewModelScope.launch {
            if (conversation.unreadCount > 0) {
                conversationDao.markConversationRead(conversation.threadId)
                messageDao.markThreadAsRead(conversation.threadId)
            } else {
                conversationDao.markConversationUnread(conversation.threadId)
            }
        }
    }

    fun setConversationCategory(threadId: Long, category: MessageCategory) {
        viewModelScope.launch {
            conversationDao.setConversationCategory(threadId, category)
            messageDao.setThreadCategory(threadId, category)
        }
    }

    fun blockSender(address: String) {
        viewModelScope.launch {
            spamRuleDao.insertSpamRule(SpamRuleEntity(pattern = address.trim(), ruleType = "SENDER"))
        }
    }

    val reminderDao = db.reminderDao()
    val conversationTagDao = db.conversationTagDao()
    val bookmarkDao = db.bookmarkDao()

    val actionEngine = MessageActionEngine(application, messageDao, conversationDao, bookmarkDao)
    val exportEngine = MessageExportEngine(application)

    val bookmarkedMessages: StateFlow<List<MessageEntity>> = bookmarkDao.getBookmarkedMessages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteConversations: StateFlow<List<ConversationEntity>> = conversationDao.getFavoriteConversations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pinnedConversations: StateFlow<List<ConversationEntity>> = conversationDao.getPinnedConversations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadConversations: StateFlow<List<ConversationEntity>> = conversationDao.getUnreadConversations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val archivedConversations: StateFlow<List<ConversationEntity>> = conversationDao.getArchivedConversations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun togglePinConversation(threadId: Long, currentIsPinned: Boolean) {
        viewModelScope.launch {
            conversationDao.setConversationPinnedWithTimestamp(threadId, !currentIsPinned, System.currentTimeMillis())
        }
    }

    fun toggleFavoriteConversation(threadId: Long, currentIsFavorite: Boolean) {
        viewModelScope.launch {
            conversationDao.setConversationFavorite(threadId, !currentIsFavorite)
        }
    }

    fun setConversationMuteUntil(threadId: Long, muteUntil: Long) {
        viewModelScope.launch {
            conversationDao.setConversationMuteUntil(threadId, muteUntil)
        }
    }

    fun executeMessageAction(
        action: MessageActionType,
        messages: List<MessageEntity>,
        extraData: Map<String, Any> = emptyMap(),
        onResult: (ActionResult) -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = actionEngine.executeAction(action, messages, extraData)
            onResult(result)
        }
    }

    fun exportMessagesFormatted(
        messages: List<MessageEntity>,
        format: ExportFormat,
        passphrase: String? = null,
        onResult: (File?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val file = exportEngine.exportMessages(messages, format, passphrase)
                onResult(file)
            } catch (e: Exception) {
                onResult(null)
            }
        }
    }

    fun addBookmark(messageId: Long, threadId: Long, note: String? = null) {
        viewModelScope.launch {
            bookmarkDao.insertBookmark(BookmarkEntity(messageId = messageId, threadId = threadId, note = note))
        }
    }

    fun removeBookmark(messageId: Long) {
        viewModelScope.launch {
            bookmarkDao.deleteBookmarkByMessageId(messageId)
        }
    }

    fun deleteConversation(threadId: Long) {
        viewModelScope.launch {
            conversationDao.deleteConversation(threadId)
            messageDao.deleteThreadMessages(threadId)
        }
    }

    fun deleteMessage(messageId: Long) {
        viewModelScope.launch {
            messageDao.deleteMessage(messageId)
        }
    }

    fun restoreSpamMessage(messageId: Long, threadId: Long) {
        viewModelScope.launch {
            messageDao.setMessageCategory(messageId, MessageCategory.PERSONAL)
            conversationDao.setConversationCategory(threadId, MessageCategory.PERSONAL)
        }
    }

    fun clearAllSpamMessages() {
        viewModelScope.launch {
            messageDao.deleteAllSpamMessages()
        }
    }

    fun speakMessage(body: String) {
        ttsManager.speakMessage(body, appLockManager.isPrivateNotificationMode)
    }

    fun exportBackup(password: String, onResult: (File?) -> Unit) {
        viewModelScope.launch {
            val allMessages = hiddenMessages.value + activeThreadMessages.value
            val file = BackupManager.exportBackup(getApplication(), allMessages, password)
            onResult(file)
        }
    }

    fun importBackup(file: File, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val imported = BackupManager.importBackup(file, password)
            if (imported != null) {
                imported.forEach { messageDao.insertMessage(it) }
                onResult(true)
            } else {
                onResult(false)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.shutdown()
    }
}
