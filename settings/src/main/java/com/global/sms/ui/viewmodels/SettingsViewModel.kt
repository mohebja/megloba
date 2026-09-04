package com.global.sms.ui.viewmodels

import android.app.Application
import android.content.Context
import android.telephony.SmsManager
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.global.sms.data.db.GlobalSmsDatabase
import com.global.sms.data.entity.AiSettingsEntity
import com.global.sms.data.entity.CategoryEntity
import com.global.sms.data.entity.ClassificationRuleEntity
import com.global.sms.data.entity.MessageCategory
import com.global.sms.data.entity.MessageEntity
import com.global.sms.data.entity.SettingsEntity
import com.global.sms.data.repository.SettingsRepository
import com.global.sms.core.sim.DualSimManager
import com.global.sms.core.sim.SimCardInfo
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ClassificationProgressState(
    val isRunning: Boolean = false,
    val processedCount: Int = 0,
    val totalCount: Int = 0,
    val categoryCounts: Map<MessageCategory, Int> = emptyMap(),
    val isCompleted: Boolean = false
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val database = GlobalSmsDatabase.getInstance(application)
    private val aiSettingsDao = database.aiSettingsDao()
    private val aiMetadataDao = database.aiMetadataDao()
    private val financialTransactionDao = database.financialTransactionDao()
    private val aiFeedbackDao = database.aiFeedbackDao()

    private val settingsRepository = SettingsRepository(
        settingsDao = database.settingsDao(),
        categoryDao = database.categoryDao(),
        classificationRuleDao = database.classificationRuleDao()
    )

    val aiSettingsState: StateFlow<AiSettingsEntity> = aiSettingsDao.getAiSettingsFlow()
        .map { it ?: AiSettingsEntity() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AiSettingsEntity()
        )

    fun updateAiSettings(settings: AiSettingsEntity) {
        viewModelScope.launch {
            aiSettingsDao.saveAiSettings(settings)
        }
    }

    fun clearAiData() {
        viewModelScope.launch {
            aiMetadataDao.clearAllMetadata()
            financialTransactionDao.clearAllTransactions()
            aiFeedbackDao.clearAllFeedback()
        }
    }

    val settingsState: StateFlow<SettingsEntity> = settingsRepository.settingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsEntity()
        )

    val categoriesState: StateFlow<List<CategoryEntity>> = settingsRepository.categoriesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val classificationRulesState: StateFlow<List<ClassificationRuleEntity>> = settingsRepository.classificationRulesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _classificationProgressState = kotlinx.coroutines.flow.MutableStateFlow(ClassificationProgressState())
    val classificationProgressState: StateFlow<ClassificationProgressState> = _classificationProgressState

    init {
        viewModelScope.launch {
            // Seed default categories and rules if empty
            val currentCats = settingsRepository.categoriesFlow.first()
            if (currentCats.isEmpty()) {
                settingsRepository.seedDefaultCategoriesIfEmpty()
            }
            settingsRepository.seedDefaultRulesIfEmpty()
        }
    }

    // --- Classification Rules Management ---
    fun addClassificationRule(
        name: String,
        targetCategory: String,
        keywords: String,
        senderPattern: String,
        ruleType: String,
        priority: Int,
        isEnabled: Boolean = true
    ) {
        viewModelScope.launch {
            val newRule = ClassificationRuleEntity(
                name = name,
                targetCategory = targetCategory,
                keywords = keywords,
                senderPattern = senderPattern,
                ruleType = ruleType,
                priority = priority,
                isEnabled = isEnabled
            )
            settingsRepository.insertClassificationRule(newRule)
        }
    }

    fun updateClassificationRule(rule: ClassificationRuleEntity) {
        viewModelScope.launch {
            settingsRepository.updateClassificationRule(rule)
        }
    }

    fun deleteClassificationRule(id: Long) {
        viewModelScope.launch {
            settingsRepository.deleteClassificationRule(id)
        }
    }

    fun toggleClassificationRule(id: Long, isEnabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setClassificationRuleEnabled(id, isEnabled)
        }
    }

    // --- Background Batch Re-Classification Engine ---
    fun startBackgroundReclassification() {
        if (_classificationProgressState.value.isRunning) return

        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            _classificationProgressState.value = ClassificationProgressState(isRunning = true)

            val rules = database.classificationRuleDao().getEnabledRulesSync()
            val messageDao = database.messageDao()
            val conversationDao = database.conversationDao()

            val allMessages: List<MessageEntity> = messageDao.getAllMessagesSync()
            val total = allMessages.size
            val catCounts = mutableMapOf<MessageCategory, Int>()

            _classificationProgressState.value = ClassificationProgressState(
                isRunning = true,
                processedCount = 0,
                totalCount = total,
                categoryCounts = emptyMap()
            )

            allMessages.chunked(100).forEachIndexed { chunkIndex, chunk ->
                chunk.forEach { msg: MessageEntity ->
                    val plainBody = com.global.sms.core.security.FieldEncryptionManager.decrypt(msg.body)
                    val classificationResult = com.global.sms.core.classifier.SmsClassifierEngine.classifyMessage(
                        sender = msg.address,
                        body = plainBody,
                        customRules = rules
                    )

                    val newCat = classificationResult.category
                    catCounts[newCat] = (catCounts[newCat] ?: 0) + 1

                    if (msg.category != newCat) {
                        val updatedMsg = msg.copy(
                            category = newCat,
                            isHidden = (newCat == MessageCategory.SPAM)
                        )
                        messageDao.updateMessage(updatedMsg)

                        // Update conversation category as well
                        val conv = conversationDao.getConversationByThreadId(msg.threadId)
                        if (conv != null && conv.lastTimestamp <= msg.timestamp) {
                            conversationDao.insertOrUpdateConversation(
                                conv.copy(
                                    category = newCat,
                                    isHidden = (newCat == MessageCategory.SPAM)
                                )
                            )
                        }
                    }
                }

                val currentProcessed = kotlin.math.min((chunkIndex + 1) * 100, total)
                _classificationProgressState.value = ClassificationProgressState(
                    isRunning = true,
                    processedCount = currentProcessed,
                    totalCount = total,
                    categoryCounts = HashMap(catCounts)
                )
            }

            _classificationProgressState.value = ClassificationProgressState(
                isRunning = false,
                processedCount = total,
                totalCount = total,
                categoryCounts = HashMap(catCounts),
                isCompleted = true
            )
        }
    }


    // --- Font Settings ---
    fun updateFontSettings(
        fontFamily: String,
        messageTextSizeSp: Int,
        senderNameSizeSp: Int,
        dateTextSizeSp: Int
    ) {
        viewModelScope.launch {
            val current = settingsRepository.getSettingsOnce()
            settingsRepository.saveSettings(
                current.copy(
                    fontFamily = fontFamily,
                    messageTextSizeSp = messageTextSizeSp,
                    senderNameSizeSp = senderNameSizeSp,
                    dateTextSizeSp = dateTextSizeSp
                )
            )
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
            val clampedScale = scale.coerceIn(0.7f, 2.2f)
            settingsRepository.saveSettings(current.copy(messageFontScale = clampedScale))
        }
    }

    // --- Color Customization ---
    fun updateBubbleAndHeaderColors(
        incomingBg: Long,
        incomingText: Long,
        outgoingBg: Long,
        outgoingText: Long,
        headerColor: Long,
        timestampColor: Long
    ) {
        viewModelScope.launch {
            val current = settingsRepository.getSettingsOnce()
            settingsRepository.saveSettings(
                current.copy(
                    incomingBubbleBgColor = incomingBg,
                    incomingBubbleTextColor = incomingText,
                    outgoingBubbleBgColor = outgoingBg,
                    outgoingBubbleTextColor = outgoingText,
                    headerColor = headerColor,
                    timestampColor = timestampColor
                )
            )
        }
    }

    fun resetColorsToDefault() {
        viewModelScope.launch {
            val current = settingsRepository.getSettingsOnce()
            settingsRepository.saveSettings(
                current.copy(
                    incomingBubbleBgColor = 0xFFE9EEF6,
                    incomingBubbleTextColor = 0xFF1B1F2A,
                    outgoingBubbleBgColor = 0xFF1A73E8,
                    outgoingBubbleTextColor = 0xFFFFFFFF,
                    headerColor = 0xFF1A73E8,
                    timestampColor = 0xFF707784
                )
            )
        }
    }

    // --- SMS Center Settings ---
    fun updateSmscSettings(sim1Smsc: String, sim2Smsc: String, autoDetect: Boolean) {
        viewModelScope.launch {
            val current = settingsRepository.getSettingsOnce()
            settingsRepository.saveSettings(
                current.copy(
                    sim1SmscAddress = sim1Smsc,
                    sim2SmscAddress = sim2Smsc,
                    autoDetectSmsc = autoDetect
                )
            )
        }
    }

    fun autoDetectSmscForSims(context: Context): Pair<String, String> {
        var sim1 = "+9891100500" // MCI default SMSC
        var sim2 = "+989350000000" // Irancell default SMSC

        try {
            val activeSims: List<SimCardInfo> = if (androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.READ_PHONE_STATE
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                DualSimManager.getActiveSimCards(context)
            } else {
                emptyList()
            }
            if (activeSims.isNotEmpty()) {
                val smsManager = context.getSystemService(SmsManager::class.java)
                // Attempt system lookup safely (Android 11+ / API standard)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R &&
                    androidx.core.content.ContextCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.READ_PHONE_STATE
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                ) {
                    try {
                        val smsc = smsManager?.smscAddress
                        if (!smsc.isNullOrBlank()) {
                            sim1 = smsc
                        }
                    } catch (e: Exception) {
                        Log.d("SettingsViewModel", "Unable to read SMSC address from SmsManager directly", e)
                    }
                }
            }
        } catch (e: Exception) {
            Log.d("SettingsViewModel", "Exception checking SMSC telephony service, falling back to Persian defaults", e)
        }

        return Pair(sim1, sim2)
    }

    // --- Category Management ---
    fun addCategory(name: String, description: String, icon: String, color: Long, priority: Int, autoRule: String) {
        viewModelScope.launch {
            val newCategory = CategoryEntity(
                name = name,
                description = description,
                icon = icon,
                color = color,
                priority = priority,
                autoRule = autoRule
            )
            settingsRepository.insertCategory(newCategory)
        }
    }

    fun updateCategory(category: CategoryEntity) {
        viewModelScope.launch {
            settingsRepository.updateCategory(category)
        }
    }

    fun deleteCategory(id: Long) {
        viewModelScope.launch {
            settingsRepository.deleteCategory(id)
        }
    }

    // --- Theme & Persian Settings ---
    fun updateAppearanceTheme(
        isDarkTheme: Boolean,
        isAmoledMode: Boolean,
        isDynamicColors: Boolean
    ) {
        viewModelScope.launch {
            val current = settingsRepository.getSettingsOnce()
            settingsRepository.saveSettings(
                current.copy(
                    isDarkTheme = isDarkTheme,
                    isAmoledMode = isAmoledMode,
                    isDynamicColors = isDynamicColors
                )
            )
        }
    }
}
