# Global SMS — Internal API & Engine Documentation

---

## 1. Core Module APIs (`:core`)

### 1.1 Artificial Intelligence & Classification
- `AIMessageClassifier.classifyMessage(sender: String, body: String): ClassificationResult`
  - Runs zero-cloud offline heuristic analysis on sender format and text contents.
  - Returns classification category (`BANK`, `OTP`, `SPAM`, `PERSONAL`, `PROMO`) and localized Persian display strings.
- `BankTransactionAnalyzer.parseTransaction(body: String): ParsedTransaction?`
  - Parses bank name, transaction type (debit/credit), amount in Tomans/Rials, account/card suffix, and remaining balance.
- `EntityExtractionEngine.extractEntities(text: String): ExtractedEntities`
  - Extracts tracking codes, verification PINs, monetary amounts, and calendar timestamps.

### 1.2 Financial Analytics & Data Export
- `FinancialExportEngine.generateCsvExport(transactions: List<FinancialTransactionEntity>, targetFile: File): File`
  - Generates Microsoft Excel and LibreOffice compatible CSV with UTF-8 BOM encoding (`\uFEFF`) and proper Persian currency formatting.
- `FinancialExportEngine.generateTextSummary(transactions: List<FinancialTransactionEntity>): String`
  - Generates formatted clipboard-ready summaries of income, expenses, and net balance.

### 1.3 Recycle Bin & Message Lifecycle
- `RecycleBinManager.moveToTrash(context: Context, message: MessageEntity): Unit`
  - Safely soft-deletes a message into local preferences storage with a 30-day retention window.
- `RecycleBinManager.restoreFromTrash(context: Context, item: TrashedMessageItem): Unit`
  - Restores a trashed message back into the active `messages` Room database table.
- `RecycleBinManager.purgeExpiredTrash(context: Context): Unit`
  - Permanently purges items older than 30 days to free storage space.

### 1.4 Search & Ranking
- `SearchRankingEngine.rankResults(query: String, messages: List<MessageEntity>): List<RankedSearchResult>`
  - Performs BM25-style frequency scoring, token matching, and Persian character normalization.

---

## 2. Telephony & SMS Engine APIs (`:sms-engine`)

- `DualSimManager.getActiveSimCards(context: Context): List<SimInfo>`
  - Queries `SubscriptionManager` to return active SIM cards, carrier names, slot indices, and subscription IDs.
- `SmsSender.sendMessage(context: Context, address: String, body: String, subscriptionId: Int?): Flow<SendResult>`
  - Handles single and multi-part SMS dispatching with carrier delivery tracking.
- `SimPermissionManager.hasTelephonyPermissions(context: Context): Boolean`
  - Verifies telephony, SMS, and phone state permissions before triggering hardware actions.

---

## 3. Security & Cryptography APIs (`:security`)

- `CryptoManager.encryptWithPassword(rawText: String, password: String): String`
  - Encrypts payload with PBKDF2WithHmacSHA256 (21,000 rounds) and AES-256-GCM.
- `CryptoManager.decryptWithPassword(ciphertext: String, password: String): String`
  - Decrypts and authenticates password-protected payloads.
- `EncryptedBackupManager.createEncryptedBackupWithMasterKey(context: Context, model: EnterpriseBackupModel, masterKey: SecretKey): File`
  - Packages and serializes message database into authenticated `GSMS` v1 container.
- `AutoBackupManager.setAutoBackupEnabled(context: Context, enabled: Boolean, intervalHours: Long): Unit`
  - Enqueues or cancels periodic background WorkManager backups with test environment safety checks.

---

## 4. Database Access APIs (`:database`)

- `GlobalSmsDatabase.getInstance(context: Context): GlobalSmsDatabase`
  - Singleton entry point to SQLite WAL-enabled Room database (Schema v31).
- `MessageDao`: High-performance paged access (`getMessagesForThreadPagingSource`), search (`searchMessagesFts`), and deletion (`deleteMessageById`).
- `ConversationDao`: Thread summaries, unread counters, pin/archive operations.
- `EnterpriseDaos`: Bulk campaign scheduling, recipient queues, and customer CRM notes.
