package com.global.sms.di

import android.content.Context
import com.global.sms.data.dao.*
import com.global.sms.data.db.GlobalSmsDatabase
import com.global.sms.data.db.PerformanceReportManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideGlobalSmsDatabase(@ApplicationContext context: Context): GlobalSmsDatabase {
        return GlobalSmsDatabase.getInstance(context)
    }

    @Provides
    fun provideMessageDao(database: GlobalSmsDatabase): MessageDao = database.messageDao()

    @Provides
    fun provideConversationDao(database: GlobalSmsDatabase): ConversationDao = database.conversationDao()

    @Provides
    fun provideScheduledMessageDao(database: GlobalSmsDatabase): ScheduledMessageDao = database.scheduledMessageDao()

    @Provides
    fun provideQuickReplyDao(database: GlobalSmsDatabase): QuickReplyDao = database.quickReplyDao()

    @Provides
    fun provideSpamRuleDao(database: GlobalSmsDatabase): SpamRuleDao = database.spamRuleDao()

    @Provides
    fun provideSearchHistoryDao(database: GlobalSmsDatabase): SearchHistoryDao = database.searchHistoryDao()

    @Provides
    fun provideContactGroupDao(database: GlobalSmsDatabase): ContactGroupDao = database.contactGroupDao()

    @Provides
    fun provideSettingsDao(database: GlobalSmsDatabase): SettingsDao = database.settingsDao()

    @Provides
    fun provideClassificationRuleDao(database: GlobalSmsDatabase): ClassificationRuleDao = database.classificationRuleDao()

    @Provides
    fun provideCategoryDao(database: GlobalSmsDatabase): CategoryDao = database.categoryDao()

    @Provides
    fun provideFinancialTransactionDao(database: GlobalSmsDatabase): FinancialTransactionDao = database.financialTransactionDao()

    @Provides
    fun provideEnterpriseProfileDao(database: GlobalSmsDatabase): EnterpriseProfileDao = database.enterpriseProfileDao()

    @Provides
    fun provideCrmCustomerDao(database: GlobalSmsDatabase): CrmCustomerDao = database.crmCustomerDao()

    @Provides
    fun provideBusinessTemplateDao(database: GlobalSmsDatabase): BusinessTemplateDao = database.businessTemplateDao()

    @Provides
    fun provideBulkSmsJobDao(database: GlobalSmsDatabase): BulkSmsJobDao = database.bulkSmsJobDao()

    @Provides
    fun provideAutomationRuleDao(database: GlobalSmsDatabase): AutomationRuleDao = database.automationRuleDao()

    @Provides
    fun provideSecurityAuditLogDao(database: GlobalSmsDatabase): SecurityAuditLogDao = database.securityAuditLogDao()

    @Provides
    fun provideAiMetadataDao(database: GlobalSmsDatabase): AiMetadataDao = database.aiMetadataDao()

    @Provides
    fun provideAiSettingsDao(database: GlobalSmsDatabase): AiSettingsDao = database.aiSettingsDao()

    @Provides
    fun provideAiFeedbackDao(database: GlobalSmsDatabase): AiFeedbackDao = database.aiFeedbackDao()

    @Provides
    @Singleton
    fun providePerformanceReportManager(): PerformanceReportManager {
        return PerformanceReportManager
    }
}
