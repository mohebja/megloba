package com.global.sms.di

import android.app.Application
import com.global.sms.core.ai.brain.LocalAIBrain
import com.global.sms.core.classifier.SmsClassifierEngine
import com.global.sms.data.db.GlobalSmsDatabase
import com.global.sms.data.repository.SettingsRepository
import com.global.sms.security.lock.AppLockManager
import com.global.sms.security.prefs.SecurePreferencesManager
import com.global.sms.security.vault.PrivateVaultSecurityManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * AppContainer — Manual Dependency Injection container.
 * Provides shared instances across the application without relying on heavy annotation processors.
 */
class AppContainer(private val application: Application) {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // Database & DAOs
    val database: GlobalSmsDatabase by lazy {
        GlobalSmsDatabase.getInstance(application)
    }

    val messageDao by lazy { database.messageDao() }
    val conversationDao by lazy { database.conversationDao() }
    val scheduledMessageDao by lazy { database.scheduledMessageDao() }
    val spamRuleDao by lazy { database.spamRuleDao() }
    val quickReplyDao by lazy { database.quickReplyDao() }
    val settingsDao by lazy { database.settingsDao() }
    val categoryDao by lazy { database.categoryDao() }

    // Security & Preferences
    val securePreferences: SecurePreferencesManager by lazy {
        SecurePreferencesManager(application)
    }

    val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(
            settingsDao = settingsDao,
            categoryDao = categoryDao,
            classificationRuleDao = database.classificationRuleDao()
        )
    }

    val appLockManager: AppLockManager by lazy {
        AppLockManager(application)
    }

    val vaultManager: PrivateVaultSecurityManager by lazy {
        PrivateVaultSecurityManager(application)
    }

    // Core AI & Classifier Singletons
    val classifierEngine = SmsClassifierEngine
    val aiBrain = LocalAIBrain
}
