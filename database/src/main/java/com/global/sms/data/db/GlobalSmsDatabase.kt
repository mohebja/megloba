package com.global.sms.data.db

import com.global.sms.data.dao.*
import com.global.sms.data.entity.*

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        MessageEntity::class,
        MessageFtsEntity::class,
        ConversationEntity::class,
        ScheduledMessageEntity::class,
        SpamRuleEntity::class,
        QuickReplyEntity::class,
        CategoryEntity::class,
        SettingsEntity::class,
        SearchHistoryEntity::class,
        ClassificationRuleEntity::class,
        ContactGroupEntity::class,
        AiSettingsEntity::class,
        AiMetadataEntity::class,
        FinancialTransactionEntity::class,
        AiFeedbackEntity::class,
        EnterpriseProfileEntity::class,
        BulkSmsJobEntity::class,
        ContactEntity::class,
        ContactGroupMemberEntity::class,
        ReminderEntity::class,
        ConversationTagEntity::class,
        BookmarkEntity::class,
        AIMessageAnalysisEntity::class,
        OtpEntity::class,
        SmartReplyEntity::class,
        SmsImportLogEntity::class,
        ContactProfileEntity::class,
        CampaignEntity::class,
        CampaignRecipientEntity::class,
        AiInsightEntity::class,
        BackupEntity::class,
        AiConversationInsightEntity::class,
        ThemeEntity::class,
        NotificationRuleEntity::class,
        VoiceCommandEntity::class,
        TaskEntity::class,
        TaskReminderEntity::class,
        CalendarSuggestionEntity::class,
        ContactInsightEntity::class,
        AiMemoryEntity::class,
        ConversationInsightEntity::class,
        SemanticIndexEntity::class,
        EmotionAnalysisEntity::class,
        CrmCustomerEntity::class,
        BusinessTemplateEntity::class,
        AutomationRuleEntity::class,
        SecurityAuditLogEntity::class,
        AiAgentActionEntity::class,
        WorkflowRuleEntity::class,
        CommunicationProfileEntity::class,
        AgentApprovalEntity::class,
        ReliabilityLogEntity::class,
        DeviceProfileEntity::class,
        UserProfileEntity::class,
        AnalyticsEntity::class,
        BackupHistoryEntity::class,
        OrganizationEntity::class,
        DepartmentEntity::class,
        EmployeeEntity::class,
        PermissionEntity::class,
        SyncEntity::class,
        AuditEntity::class,
        AiAgentEntity::class,
        WorkflowEntity::class,
        WorkflowExecutionEntity::class,
        EnterpriseReportEntity::class,
        ApiAccessLogEntity::class,
        SecurityAuditEntity::class,
        AIAgentMemoryEntity::class,
        PluginEntity::class,
        DeviceEntity::class,
        SyncSessionEntity::class,
        AutomationTemplateEntity::class,
        LicenseEntity::class,
        PluginMarketplaceEntity::class,
        EnterpriseUserEntity::class,
        CloudConnectorEntity::class,
        MigrationHistoryEntity::class
    ],
    version = 29,
    exportSchema = true
)
abstract class GlobalSmsDatabase : RoomDatabase() {

    abstract fun messageDao(): MessageDao
    abstract fun conversationDao(): ConversationDao
    abstract fun scheduledMessageDao(): ScheduledMessageDao
    abstract fun spamRuleDao(): SpamRuleDao
    abstract fun quickReplyDao(): QuickReplyDao
    abstract fun categoryDao(): CategoryDao
    abstract fun settingsDao(): SettingsDao
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun classificationRuleDao(): ClassificationRuleDao
    abstract fun contactGroupDao(): ContactGroupDao
    abstract fun contactDao(): ContactDao
    abstract fun contactGroupMemberDao(): ContactGroupMemberDao
    abstract fun aiSettingsDao(): AiSettingsDao
    abstract fun aiMetadataDao(): AiMetadataDao
    abstract fun financialTransactionDao(): FinancialTransactionDao
    abstract fun aiFeedbackDao(): AiFeedbackDao
    abstract fun enterpriseProfileDao(): EnterpriseProfileDao
    abstract fun otpDao(): OtpDao
    abstract fun smartReplyDao(): SmartReplyDao
    abstract fun smsImportLogDao(): SmsImportLogDao
    abstract fun contactProfileDao(): ContactProfileDao
    abstract fun campaignDao(): CampaignDao
    abstract fun campaignRecipientDao(): CampaignRecipientDao
    abstract fun aiInsightDao(): AiInsightDao
    abstract fun backupDao(): BackupDao
    abstract fun aiConversationInsightDao(): AiConversationInsightDao
    abstract fun themeDao(): ThemeDao
    abstract fun notificationRuleDao(): NotificationRuleDao
    abstract fun voiceCommandDao(): VoiceCommandDao
    abstract fun taskDao(): TaskDao
    abstract fun calendarSuggestionDao(): CalendarSuggestionDao
    abstract fun contactInsightDao(): ContactInsightDao
    abstract fun aiMemoryDao(): AiMemoryDao
    abstract fun conversationInsightDao(): ConversationInsightDao
    abstract fun semanticIndexDao(): SemanticIndexDao
    abstract fun emotionAnalysisDao(): EmotionAnalysisDao
    abstract fun crmCustomerDao(): CrmCustomerDao
    abstract fun businessTemplateDao(): BusinessTemplateDao
    abstract fun automationRuleDao(): AutomationRuleDao
    abstract fun securityAuditLogDao(): SecurityAuditLogDao
    abstract fun bulkSmsJobDao(): BulkSmsJobDao
    abstract fun reminderDao(): ReminderDao
    abstract fun conversationTagDao(): ConversationTagDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun aiAgentActionDao(): AiAgentActionDao
    abstract fun workflowRuleDao(): WorkflowRuleDao
    abstract fun communicationProfileDao(): CommunicationProfileDao
    abstract fun agentApprovalDao(): AgentApprovalDao
    abstract fun aiAgentDao(): AiAgentDao
    abstract fun workflowDao(): WorkflowDao
    abstract fun workflowExecutionDao(): WorkflowExecutionDao
    abstract fun enterpriseReportDao(): EnterpriseReportDao
    abstract fun apiAccessLogDao(): ApiAccessLogDao
    abstract fun securityAuditDao(): SecurityAuditDao
    abstract fun aiAgentMemoryV2Dao(): AIAgentMemoryV2Dao
    abstract fun pluginDao(): PluginDao
    abstract fun deviceDao(): DeviceDao
    abstract fun syncSessionDao(): SyncSessionDao
    abstract fun automationTemplateDao(): AutomationTemplateDao
    abstract fun licenseDao(): LicenseDao
    abstract fun pluginMarketplaceDao(): PluginMarketplaceDao
    abstract fun enterpriseUserDao(): EnterpriseUserDao
    abstract fun cloudConnectorDao(): CloudConnectorDao
    abstract fun migrationHistoryDao(): MigrationHistoryDao
    abstract fun organizationDao(): OrganizationDao
    abstract fun departmentDao(): DepartmentDao
    abstract fun employeeDao(): EmployeeDao
    abstract fun permissionDao(): PermissionDao
    abstract fun syncDao(): SyncDao
    abstract fun auditTrailDao(): AuditTrailDao

    companion object {
        @Volatile
        private var INSTANCE: GlobalSmsDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE messages ADD COLUMN isPinned INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE messages ADD COLUMN isEncrypted INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE messages ADD COLUMN otpCode TEXT DEFAULT NULL")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `ai_metadata` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `messageId` INTEGER NOT NULL,
                        `sentiment` TEXT NOT NULL,
                        `extractedEntities` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `financial_transactions` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `messageId` INTEGER NOT NULL,
                        `bankName` TEXT NOT NULL,
                        `transactionType` TEXT NOT NULL,
                        `amount` REAL NOT NULL,
                        `cardOrAccount` TEXT,
                        `balanceAfter` REAL,
                        `timestamp` INTEGER NOT NULL,
                        `category` TEXT NOT NULL
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `ai_feedback` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `messageId` INTEGER NOT NULL,
                        `originalCategory` TEXT NOT NULL,
                        `userCorrectedCategory` TEXT NOT NULL,
                        `timestamp` INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `enterprise_profiles` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `companyName` TEXT NOT NULL,
                        `apiKey` TEXT NOT NULL,
                        `autoReplyEnabled` INTEGER NOT NULL,
                        `customFooter` TEXT NOT NULL
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `bulk_sms_jobs` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `jobName` TEXT NOT NULL,
                        `totalRecipients` INTEGER NOT NULL,
                        `sentCount` INTEGER NOT NULL,
                        `failedCount` INTEGER NOT NULL,
                        `status` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `contacts` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `lookupKey` TEXT NOT NULL,
                        `displayName` TEXT NOT NULL,
                        `phoneNumber` TEXT NOT NULL,
                        `photoUri` TEXT,
                        `starred` INTEGER NOT NULL DEFAULT 0,
                        `customRingtone` TEXT,
                        `notes` TEXT,
                        `lastUpdated` INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_contacts_phoneNumber` ON `contacts` (`phoneNumber`)")
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `contact_group_members` (
                        `groupId` INTEGER NOT NULL,
                        `contactId` INTEGER NOT NULL,
                        `addedTimestamp` INTEGER NOT NULL,
                        PRIMARY KEY(`groupId`, `contactId`)
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `reminders` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `messageId` INTEGER NOT NULL,
                        `threadId` INTEGER NOT NULL,
                        `reminderTime` INTEGER NOT NULL,
                        `note` TEXT NOT NULL,
                        `isCompleted` INTEGER NOT NULL DEFAULT 0,
                        `createdAt` INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `conversation_tags` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `threadId` INTEGER NOT NULL,
                        `tag` TEXT NOT NULL,
                        `colorHex` TEXT NOT NULL
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `bookmarks` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `messageId` INTEGER NOT NULL,
                        `label` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `ai_message_analyses` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `messageId` INTEGER NOT NULL,
                        `category` TEXT NOT NULL,
                        `sentiment` TEXT NOT NULL,
                        `urgency` INTEGER NOT NULL,
                        `summary` TEXT NOT NULL,
                        `suggestedAction` TEXT NOT NULL
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `otps` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `messageId` INTEGER NOT NULL,
                        `code` TEXT NOT NULL,
                        `sender` TEXT NOT NULL,
                        `expiresAt` INTEGER NOT NULL,
                        `isUsed` INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_14_15 = object : Migration(14, 15) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `smart_replies` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `replyText` TEXT NOT NULL,
                        `category` TEXT NOT NULL,
                        `usageCount` INTEGER NOT NULL DEFAULT 0,
                        `isCustom` INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_15_16 = object : Migration(15, 16) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `sms_import_logs` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `totalImported` INTEGER NOT NULL,
                        `status` TEXT NOT NULL,
                        `timestamp` INTEGER NOT NULL
                    )
                """.trimIndent())

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `contact_profiles` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `phoneNumber` TEXT NOT NULL,
                        `displayName` TEXT NOT NULL,
                        `avatarUrl` TEXT,
                        `email` TEXT,
                        `isVip` INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `campaigns` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `messageTemplate` TEXT NOT NULL,
                        `scheduledTime` INTEGER NOT NULL,
                        `status` TEXT NOT NULL
                    )
                """.trimIndent())

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `campaign_recipients` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `campaignId` INTEGER NOT NULL,
                        `phoneNumber` TEXT NOT NULL,
                        `status` TEXT NOT NULL
                    )
                """.trimIndent())

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `ai_insights` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `type` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `timestamp` INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_16_17 = object : Migration(16, 17) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `backups` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `fileName` TEXT NOT NULL,
                        `sizeBytes` INTEGER NOT NULL,
                        `messageCount` INTEGER NOT NULL,
                        `timestamp` INTEGER NOT NULL
                    )
                """.trimIndent())

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `ai_conversation_insights` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `threadId` INTEGER NOT NULL,
                        `summary` TEXT NOT NULL,
                        `keyPoints` TEXT NOT NULL,
                        `sentiment` TEXT NOT NULL,
                        `updatedAt` INTEGER NOT NULL
                    )
                """.trimIndent())

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `themes` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `primaryColorHex` TEXT NOT NULL,
                        `isDark` INTEGER NOT NULL
                    )
                """.trimIndent())

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `notification_rules` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `keyword` TEXT NOT NULL,
                        `soundUri` TEXT,
                        `isMuted` INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_17_18 = object : Migration(17, 18) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `voice_commands` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `command` TEXT NOT NULL,
                        `action` TEXT NOT NULL,
                        `timestamp` INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_18_19 = object : Migration(18, 19) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `tasks` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `messageId` INTEGER NOT NULL,
                        `title` TEXT NOT NULL,
                        `dueDate` INTEGER,
                        `isCompleted` INTEGER NOT NULL DEFAULT 0,
                        `createdAt` INTEGER NOT NULL
                    )
                """.trimIndent())

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `task_reminders` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `taskId` INTEGER NOT NULL,
                        `reminderTime` INTEGER NOT NULL,
                        `isFired` INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_19_20 = object : Migration(19, 20) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `tasks` ADD COLUMN `status` TEXT NOT NULL DEFAULT 'NEW'")
                db.execSQL("ALTER TABLE `tasks` ADD COLUMN `source` TEXT NOT NULL DEFAULT 'USER_CREATED'")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_tasks_status` ON `tasks` (`status`)")
            }
        }

        val MIGRATION_20_21 = object : Migration(20, 21) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `calendar_suggestions` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `messageId` INTEGER NOT NULL,
                        `title` TEXT NOT NULL,
                        `eventDateMillis` INTEGER NOT NULL,
                        `timeString` TEXT NOT NULL DEFAULT '',
                        `location` TEXT NOT NULL DEFAULT '',
                        `isAccepted` INTEGER NOT NULL DEFAULT 0,
                        `createdAt` INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_calendar_suggestions_messageId` ON `calendar_suggestions` (`messageId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_calendar_suggestions_eventDateMillis` ON `calendar_suggestions` (`eventDateMillis`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_calendar_suggestions_isAccepted` ON `calendar_suggestions` (`isAccepted`)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `contact_insights` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `address` TEXT NOT NULL,
                        `smartCategory` TEXT NOT NULL DEFAULT 'PERSONAL',
                        `interactionCount` INTEGER NOT NULL DEFAULT 1,
                        `lastContactMillis` INTEGER NOT NULL,
                        `priorityScore` INTEGER NOT NULL DEFAULT 50
                    )
                """.trimIndent())
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_contact_insights_address` ON `contact_insights` (`address`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_contact_insights_smartCategory` ON `contact_insights` (`smartCategory`)")
            }
        }

        val MIGRATION_21_22 = object : Migration(21, 22) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `ai_memories` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `address` TEXT NOT NULL,
                        `category` TEXT NOT NULL,
                        `memoryKey` TEXT NOT NULL,
                        `memoryValue` TEXT NOT NULL,
                        `confidence` REAL NOT NULL DEFAULT 0.9,
                        `updatedAt` INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_ai_memories_address` ON `ai_memories` (`address`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_ai_memories_category` ON `ai_memories` (`category`)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `conversation_insights` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `threadId` INTEGER NOT NULL,
                        `topicSummary` TEXT NOT NULL,
                        `userIntention` TEXT NOT NULL,
                        `emotion` TEXT NOT NULL,
                        `urgencyLevel` TEXT NOT NULL,
                        `decisionsCount` INTEGER NOT NULL DEFAULT 0,
                        `actionsCount` INTEGER NOT NULL DEFAULT 0,
                        `createdAt` INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_conversation_insights_threadId` ON `conversation_insights` (`threadId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_conversation_insights_urgencyLevel` ON `conversation_insights` (`urgencyLevel`)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `semantic_indices` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `messageId` INTEGER NOT NULL,
                        `keyword` TEXT NOT NULL,
                        `weight` REAL NOT NULL DEFAULT 1.0,
                        `createdAt` INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_semantic_indices_messageId` ON `semantic_indices` (`messageId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_semantic_indices_keyword` ON `semantic_indices` (`keyword`)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `emotion_analyses` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `messageId` INTEGER NOT NULL,
                        `primaryEmotion` TEXT NOT NULL,
                        `intensityScore` INTEGER NOT NULL,
                        `priorityBoost` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_emotion_analyses_messageId` ON `emotion_analyses` (`messageId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_emotion_analyses_primaryEmotion` ON `emotion_analyses` (`primaryEmotion`)")
            }
        }

        val MIGRATION_22_23 = object : Migration(22, 23) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `ai_agent_actions` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `actionType` TEXT NOT NULL,
                        `targetId` TEXT NOT NULL DEFAULT '',
                        `description` TEXT NOT NULL,
                        `status` TEXT NOT NULL DEFAULT 'SUGGESTED',
                        `urgency` INTEGER NOT NULL DEFAULT 50,
                        `timestamp` INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_ai_agent_actions_status` ON `ai_agent_actions` (`status`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_ai_agent_actions_targetId` ON `ai_agent_actions` (`targetId`)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `workflow_rules` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `ruleName` TEXT NOT NULL,
                        `triggerType` TEXT NOT NULL,
                        `triggerValue` TEXT NOT NULL,
                        `actionType` TEXT NOT NULL,
                        `actionValue` TEXT NOT NULL DEFAULT '',
                        `requiresApproval` INTEGER NOT NULL DEFAULT 1,
                        `isEnabled` INTEGER NOT NULL DEFAULT 1,
                        `createdAt` INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_workflow_rules_triggerType` ON `workflow_rules` (`triggerType`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_workflow_rules_isEnabled` ON `workflow_rules` (`isEnabled`)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `communication_profiles` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `contactAddress` TEXT NOT NULL,
                        `communicationStyle` TEXT NOT NULL DEFAULT 'FORMAL',
                        `priorityScore` INTEGER NOT NULL DEFAULT 50,
                        `averageResponseTimeMinutes` INTEGER NOT NULL DEFAULT 30,
                        `preferredChannel` TEXT NOT NULL DEFAULT 'SMS',
                        `workHoursOnly` INTEGER NOT NULL DEFAULT 0,
                        `lastAnalyzed` INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_communication_profiles_contactAddress` ON `communication_profiles` (`contactAddress`)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `agent_approvals` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `actionId` INTEGER NOT NULL,
                        `actionSummary` TEXT NOT NULL,
                        `requestedAt` INTEGER NOT NULL,
                        `decidedAt` INTEGER,
                        `status` TEXT NOT NULL DEFAULT 'PENDING',
                        `isKillSwitchActive` INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_agent_approvals_status` ON `agent_approvals` (`status`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_agent_approvals_actionId` ON `agent_approvals` (`actionId`)")
            }
        }

        val MIGRATION_23_24 = object : Migration(23, 24) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `reliability_logs` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `timestamp` INTEGER NOT NULL,
                        `eventType` TEXT NOT NULL,
                        `details` TEXT NOT NULL,
                        `healthScore` INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `device_profiles` (
                        `id` INTEGER PRIMARY KEY NOT NULL,
                        `manufacturer` TEXT NOT NULL,
                        `brand` TEXT NOT NULL,
                        `isAutoStartEnabled` INTEGER NOT NULL,
                        `isBatteryOptimized` INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `user_profiles` (
                        `id` INTEGER PRIMARY KEY NOT NULL,
                        `activeProfileName` TEXT NOT NULL,
                        `isAutoReplyActive` INTEGER NOT NULL,
                        `autoReplyMessage` TEXT NOT NULL,
                        `isMuteActive` INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `analytics_records` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `timestamp` INTEGER NOT NULL,
                        `totalMessages` INTEGER NOT NULL,
                        `totalSpamBlocked` INTEGER NOT NULL,
                        `avgResponseTimeMinutes` INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `backup_history` (
                        `backupId` TEXT PRIMARY KEY NOT NULL,
                        `timestamp` INTEGER NOT NULL,
                        `formattedDate` TEXT NOT NULL,
                        `messageCount` INTEGER NOT NULL,
                        `contactCount` INTEGER NOT NULL,
                        `sizeBytes` INTEGER NOT NULL,
                        `isEncrypted` INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_24_25 = object : Migration(24, 25) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `organizations` (
                        `id` TEXT PRIMARY KEY NOT NULL,
                        `companyName` TEXT NOT NULL,
                        `organizationType` TEXT NOT NULL,
                        `createdDate` INTEGER NOT NULL,
                        `securityPolicy` TEXT NOT NULL,
                        `subscriptionLevel` TEXT NOT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `departments` (
                        `id` TEXT PRIMARY KEY NOT NULL,
                        `organizationId` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `manager` TEXT NOT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `employees` (
                        `id` TEXT PRIMARY KEY NOT NULL,
                        `departmentId` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `role` TEXT NOT NULL,
                        `permissions` TEXT NOT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `permissions` (
                        `id` TEXT PRIMARY KEY NOT NULL,
                        `roleName` TEXT NOT NULL,
                        `allowedPermissions` TEXT NOT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `sync_logs` (
                        `syncId` TEXT PRIMARY KEY NOT NULL,
                        `deviceType` TEXT NOT NULL,
                        `timestamp` INTEGER NOT NULL,
                        `status` TEXT NOT NULL,
                        `encryptedPayloadSize` INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `audit_trail` (
                        `id` TEXT PRIMARY KEY NOT NULL,
                        `timestamp` INTEGER NOT NULL,
                        `action` TEXT NOT NULL,
                        `actor` TEXT NOT NULL,
                        `details` TEXT NOT NULL,
                        `isSecurityViolation` INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_25_26 = object : Migration(25, 26) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `ai_agents` (
                        `id` TEXT PRIMARY KEY NOT NULL,
                        `agentName` TEXT NOT NULL,
                        `roleType` TEXT NOT NULL,
                        `departmentId` TEXT NOT NULL,
                        `executionMode` TEXT NOT NULL,
                        `isActive` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `workflows` (
                        `id` TEXT PRIMARY KEY NOT NULL,
                        `name` TEXT NOT NULL,
                        `triggerType` TEXT NOT NULL,
                        `conditionJson` TEXT NOT NULL,
                        `actionJson` TEXT NOT NULL,
                        `isEnabled` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `workflow_executions` (
                        `id` TEXT PRIMARY KEY NOT NULL,
                        `workflowId` TEXT NOT NULL,
                        `triggerEvent` TEXT NOT NULL,
                        `status` TEXT NOT NULL,
                        `logDetails` TEXT NOT NULL,
                        `executedAt` INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `enterprise_reports` (
                        `id` TEXT PRIMARY KEY NOT NULL,
                        `reportTitle` TEXT NOT NULL,
                        `reportType` TEXT NOT NULL,
                        `format` TEXT NOT NULL,
                        `contentPayload` TEXT NOT NULL,
                        `generatedAt` INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `api_access_logs` (
                        `id` TEXT PRIMARY KEY NOT NULL,
                        `apiKeyId` TEXT NOT NULL,
                        `endpoint` TEXT NOT NULL,
                        `httpMethod` TEXT NOT NULL,
                        `responseStatus` INTEGER NOT NULL,
                        `clientIp` TEXT NOT NULL,
                        `timestamp` INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `security_audits` (
                        `id` TEXT PRIMARY KEY NOT NULL,
                        `eventType` TEXT NOT NULL,
                        `severity` TEXT NOT NULL,
                        `sourceModule` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `signatureHash` TEXT NOT NULL,
                        `timestamp` INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_26_27 = object : Migration(26, 27) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `ai_agent_memories_v2` (
                        `id` TEXT PRIMARY KEY NOT NULL,
                        `agentRole` TEXT NOT NULL,
                        `memoryKey` TEXT NOT NULL,
                        `memoryValue` TEXT NOT NULL,
                        `sensitivityLevel` INTEGER NOT NULL DEFAULT 1,
                        `timestamp` INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `plugins` (
                        `id` TEXT PRIMARY KEY NOT NULL,
                        `pluginId` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `category` TEXT NOT NULL,
                        `isInstalled` INTEGER NOT NULL DEFAULT 0,
                        `permissionsJson` TEXT NOT NULL,
                        `installedTimestamp` INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `companion_devices` (
                        `id` TEXT PRIMARY KEY NOT NULL,
                        `deviceId` TEXT NOT NULL,
                        `deviceName` TEXT NOT NULL,
                        `platform` TEXT NOT NULL,
                        `lastActiveMs` INTEGER NOT NULL,
                        `isTrusted` INTEGER NOT NULL DEFAULT 1
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `sync_sessions` (
                        `id` TEXT PRIMARY KEY NOT NULL,
                        `sessionId` TEXT NOT NULL,
                        `deviceId` TEXT NOT NULL,
                        `syncType` TEXT NOT NULL,
                        `status` TEXT NOT NULL,
                        `timestamp` INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `automation_templates` (
                        `id` TEXT PRIMARY KEY NOT NULL,
                        `templateId` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `category` TEXT NOT NULL,
                        `triggerPattern` TEXT NOT NULL,
                        `actionSummary` TEXT NOT NULL,
                        `isActivated` INTEGER NOT NULL DEFAULT 0,
                        `executionCount` INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_27_28 = object : Migration(27, 28) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `reliability_diagnostics` (
                        `id` TEXT PRIMARY KEY NOT NULL,
                        `category` TEXT NOT NULL,
                        `severity` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `autoResolved` INTEGER NOT NULL DEFAULT 1,
                        `timestamp` INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `ai_memory_v3` (
                        `id` TEXT PRIMARY KEY NOT NULL,
                        `type` TEXT NOT NULL,
                        `subjectKey` TEXT NOT NULL,
                        `memoryContent` TEXT NOT NULL,
                        `importanceScore` REAL NOT NULL DEFAULT 0.8,
                        `expiresAtMs` INTEGER,
                        `createdTimestampMs` INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `workflow_approvals_v2` (
                        `id` TEXT PRIMARY KEY NOT NULL,
                        `workflowName` TEXT NOT NULL,
                        `actionType` TEXT NOT NULL,
                        `requiresApproval` INTEGER NOT NULL DEFAULT 1,
                        `status` TEXT NOT NULL,
                        `timestamp` INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_28_29 = object : Migration(28, 29) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `licenses` (
                        `licenseId` TEXT PRIMARY KEY NOT NULL,
                        `licenseKey` TEXT NOT NULL,
                        `tier` TEXT NOT NULL,
                        `organizationName` TEXT NOT NULL,
                        `maxSeats` INTEGER NOT NULL,
                        `expiresAtMs` INTEGER,
                        `isActivatedOffline` INTEGER NOT NULL,
                        `issuedTimestampMs` INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `plugin_marketplace` (
                        `pluginId` TEXT PRIMARY KEY NOT NULL,
                        `nameFa` TEXT NOT NULL,
                        `nameEn` TEXT NOT NULL,
                        `category` TEXT NOT NULL,
                        `version` TEXT NOT NULL,
                        `author` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `requiredPermissions` TEXT NOT NULL,
                        `isInstalled` INTEGER NOT NULL,
                        `isEnabled` INTEGER NOT NULL,
                        `isSandboxed` INTEGER NOT NULL,
                        `rating` REAL NOT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `enterprise_users` (
                        `userId` TEXT PRIMARY KEY NOT NULL,
                        `name` TEXT NOT NULL,
                        `emailOrPhone` TEXT NOT NULL,
                        `role` TEXT NOT NULL,
                        `department` TEXT NOT NULL,
                        `isActive` INTEGER NOT NULL,
                        `createdTimestampMs` INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `cloud_connectors` (
                        `connectorId` TEXT PRIMARY KEY NOT NULL,
                        `providerType` TEXT NOT NULL,
                        `serverEndpoint` TEXT NOT NULL,
                        `isEnabled` INTEGER NOT NULL,
                        `autoSyncEnabled` INTEGER NOT NULL,
                        `lastSyncTimestampMs` INTEGER,
                        `requiresTls` INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `migration_history` (
                        `migrationId` TEXT PRIMARY KEY NOT NULL,
                        `sourceAppVersion` TEXT NOT NULL,
                        `schemaVersion` INTEGER NOT NULL,
                        `totalMessages` INTEGER NOT NULL,
                        `totalContacts` INTEGER NOT NULL,
                        `totalWorkflows` INTEGER NOT NULL,
                        `checksumSha256` TEXT NOT NULL,
                        `status` TEXT NOT NULL,
                        `timestampMs` INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        fun getInstance(context: Context): GlobalSmsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GlobalSmsDatabase::class.java,
                    "global_sms_encrypted_db"
                )
                    .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
                    .addMigrations(
                        MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5,
                        MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9,
                        MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13, MIGRATION_13_14, MIGRATION_14_15,
                        MIGRATION_15_16, MIGRATION_16_17, MIGRATION_17_18, MIGRATION_18_19, MIGRATION_19_20, MIGRATION_20_21,
                        MIGRATION_21_22, MIGRATION_22_23, MIGRATION_23_24, MIGRATION_24_25, MIGRATION_25_26, MIGRATION_26_27,
                        MIGRATION_27_28, MIGRATION_28_29
                    )

                    .addCallback(object : RoomDatabase.Callback() {

                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            try {
                                db.query("PRAGMA synchronous=NORMAL;").close()
                                db.query("PRAGMA temp_store=MEMORY;").close()
                                db.query("PRAGMA mmap_size=268435456;").close() // 256MB memory mapping
                            } catch (e: Exception) {
                                Log.w("GlobalSmsDatabase", "Failed to apply optimization PRAGMAs on database open", e)
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
