# Global SMS — Database Design Document (Room Schema v31)

---

## 1. Overview & Architecture

The persistence layer of Global SMS is implemented using **Android Room Database** with **SQLite Write-Ahead Logging (WAL)** mode.
- **Current Database Version:** 31
- **Database Class:** `GlobalSmsDatabase`
- **Location:** Local device storage (`/data/data/com.global.sms/databases/global_sms.db`)
- **Query Optimization:** Memory-mapped I/O, composite indices, and FTS Full-Text Search.

---

## 2. Entity Schema & Table Catalog

The database schema encompasses 40+ specialized entities categorized by operational domain:

### 2.1 Core Messaging & Threads
1. **`messages` (`MessageEntity`):** Stores received and sent SMS/MMS messages. Includes `id`, `threadId`, `address`, `body`, `timestamp`, `type`, `read`, `status`, `subId`, `category`, `isHidden`, `locked`, and `systemSmsId`.
2. **`messages_fts` (`MessageFtsEntity`):** FTS4/FTS5 virtual table for lightning-fast full-text search across message content.
3. **`conversations` (`ConversationEntity`):** Thread-level metadata, unread message count, latest snippet, pin status, archive state, and contact mapping.
4. **`scheduled_messages` (`ScheduledMessageEntity`):** Future-dated SMS messages managed by WorkManager with execution status and retry counts.

### 2.2 Financial Intelligence & Transactions
5. **`financial_transactions` (`FinancialTransactionEntity`):** Automatically parsed banking messages. Includes `messageId`, `bankName`, `transactionType` (EXPENSE, INCOME, TRANSFER), `amount`, `cardOrAccount`, `balanceAfter`, `category`, and `timestamp`.

### 2.3 CRM, Contacts & Enterprise
6. **`contacts` (`ContactEntity`):** Normalized contacts with E.164 phone numbers, Persian display names, avatar URIs, and favorite state.
7. **`contact_profiles` (`ContactProfileEntity`):** Enterprise CRM metadata, company name, job title, and VIP level.
8. **`contact_groups` & `contact_group_members`:** Multi-tiered contact segmentation for targeted messaging.
9. **`crm_customers` (`CrmCustomerEntity`):** Customer lifecycle stage, lifetime value, and interaction history.
10. **`campaigns` & `campaign_recipients`:** Enterprise bulk campaign templates, recipient queue, dynamic token placeholders, and delivery confirmation logs.
11. **`enterprise_profiles` & `bulk_sms_jobs`:** Corporate messaging rules and batch throttling configurations.

### 2.4 On-Device Artificial Intelligence & Insights
12. **`ai_message_analysis`:** Intent, urgency level, and summary metadata.
13. **`otps` (`OtpEntity`):** Extracted verification codes, sender verification, and auto-expiration timestamp.
14. **`ai_feedback` & `ai_insights`:** User correction telemetry stored locally to fine-tune offline classification without cloud transmission.
15. **`smart_replies` & `quick_replies`:** Frequently used and AI-generated quick responses.
16. **`emotion_analysis` & `semantic_index`:** Sentiment scores and semantic vectors for smart search.

### 2.5 Productivity, Reminders & Security
17. **`reminders`, `tasks`, `task_reminders`, `calendar_suggestions`:** Action items extracted from incoming messages.
18. **`conversation_tags` & `bookmarks`:** User-defined labels and starred messages.
19. **`spam_rules` & `classification_rules`:** User-defined and system blacklist/whitelist filters.
20. **`backups` (`BackupEntity`):** History of local and cloud backup archives, container hashes, and restore checkpoints.

---

## 3. Indexing & Query Optimization Strategy

To guarantee sub-10ms query execution across datasets exceeding 500,000 messages:
- **Index on `messages(threadId, isHidden, timestamp DESC)`:** Enables instantaneous thread conversation loading.
- **Index on `messages(category, timestamp DESC)`:** Accelerates category tab filtering (*Banking*, *Spam*, *Personal*).
- **Index on `contacts(normalizedNumber)`:** O(1) resolution of sender phone numbers to contact names.
- **Index on `financial_transactions(timestamp, transactionType)`:** Powers instant chart rendering in the financial dashboard.

---

## 4. Database Migrations History

Global SMS implements non-destructive migrations from Version 1 through Version 31:
- `MIGRATION_1_2` to `MIGRATION_10_11`: Baseline tables, FTS virtual table addition, and contact indexing.
- `MIGRATION_11_12` to `MIGRATION_20_21`: Enterprise CRM, bulk campaign tables, and financial transaction entities.
- `MIGRATION_21_22` to `MIGRATION_28_29`: AI analysis metadata, semantic indexing, and backup tracking.
- `MIGRATION_29_30` & `MIGRATION_30_31`: Schema stabilization, `balanceAfter` financial entity updates, and Recycle Bin retention support.
