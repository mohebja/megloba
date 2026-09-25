# Global SMS — System Architecture Document

---

## 1. Architectural Philosophy & Patterns

Global SMS is designed around **Clean Architecture**, **Modularization**, and **Unidirectional Data Flow (MVI / MVVM)**. The system guarantees high responsiveness, zero cloud leakage, fault tolerance in background telecommunications, and battery efficiency.

```
+-------------------------------------------------------------------+
|                            :app                                   |
|   (Application, NavHost, Dependency Wiring, App Maintenance)     |
+-------------------------------------------------------------------+
         |                        |                        |
         v                        v                        v
+-----------------+      +-----------------+      +-----------------+
|      :ui        |      |   :sms-engine   |      |    :security    |
|  (Compose, M3,  |      | (Telephony APIs,|      | (KeyStore, GCM, |
|   ViewModels)   |      |  Dual SIM, WAPs)|      |  Vault, GSMS)   |
+-----------------+      +-----------------+      +-----------------+
         |                        |                        |
         +------------------------+------------------------+
                                  |
                                  v
                         +-----------------+
                         |    :settings    |
                         | (DataStore,     |
                         |  Preferences)   |
                         +-----------------+
                                  |
                                  v
                         +-----------------+
                         |    :database    |
                         | (Room v31, DAOs,|
                         |  FTS4/5, WAL)   |
                         +-----------------+
                                  |
                                  v
                         +-----------------+
                         |      :core      |
                         | (AI Classifier, |
                         |  Models, Util)  |
                         +-----------------+
```

---

## 2. Layer & Module Responsibilities

### 2.1 `:app` (Application & Entry Point)
- Houses `GlobalSmsApp`, the top-level Android Application class.
- Configures global uncaught exception handling (`GlobalCrashHandler`), image loaders (Coil memory/disk cache), and navigation routing.
- Implements test environment isolation (`isTestEnvironment()`) to safeguard background maintenance workers during JVM tests.

### 2.2 `:core` (Domain Logic & Offline Intelligence)
- Contains business rules, pure domain models, and utility engines.
- **On-Device AI Engine:** `AIMessageClassifier`, `BankTransactionAnalyzer`, `EntityExtractionEngine`, `SmartReplyV2Engine`.
- **Financial & Export Processing:** `FinancialExportEngine` with UTF-8 BOM CSV generation.
- **Search & Indexing:** `SearchRankingEngine`, `SearchQueryParser`, Persian text normalizer.
- **Recycle Bin:** `RecycleBinManager` managing 30-day soft retention and restore mechanisms.

### 2.3 `:database` (Persistence & Cache)
- Room Database Schema Version 31 (`GlobalSmsDatabase`).
- Includes DAOs for high-throughput messaging (`MessageDao`, `ConversationDao`), CRM (`EnterpriseDaos`), and AI analysis (`AiDaos`).
- Full-Text Search integration via `messages_fts` virtual table.

### 2.4 `:sms-engine` (Telephony & Broadcasts)
- Default SMS App role implementations (`SmsReceiver`, `MmsReceiver`, `HeadlessSmsSendService`).
- `DualSimManager` handling active SIM subscriptions, slot assignment, and hardware telephony events.
- Exponential backoff message dispatching backed by Android WorkManager.

### 2.5 `:security` (Zero-Trust Cryptography & Protection)
- Hardware-backed master key generation in `AndroidKeyStore` (`AES/GCM/NoPadding`).
- Private Vault isolation with BiometricPrompt and PBKDF2 password derivation (21,000 iterations).
- Enterprise backup encryption pipeline (`EncryptedBackupManager`, `AutoBackupManager`) utilizing authenticated `GSMS` container format.

### 2.6 `:settings` (Configuration & Modes)
- `PreferencesManager` and `SecurePreferencesManager`.
- Controls switching between 3 UI Modes (*Classic Clean*, *Smart AI OS*, *Enterprise Workforce*).

### 2.7 `:ui` (Presentation & Jetpack Compose)
- Pure Jetpack Compose UI adhering to Material Design 3 guidelines.
- Dynamic color theming (Light, Dark, OLED Deep Black) and RTL layout mirroring.
- Production screens: `InboxScreen`, `ConversationScreen`, `FinancialDashboardScreen`, `EnterpriseDashboardScreen`, `PrivateVaultScreen`, `RecycleBinScreen`, `SettingsScreen`.

---

## 3. Governance via Multi-Agent Role Framework (AGENTS.md)

System development is governed by multi-disciplinary sub-agent roles:
1. **Product Management (PM/PO):** Prioritization of enterprise and offline AI capabilities.
2. **UI/UX Design:** Material 3, 48dp touch targets, RTL readability, and smooth micro-interactions.
3. **Software Architecture:** Module decoupling, dependency inversion, and thread safety.
4. **Database Administration (DBA):** Schema indexing, SQLite WAL optimization, and safe Room migrations.
5. **Security Engineering (AppSec):** Hardware KeyStore enforcement, PII redaction, and Data Safety compliance.
6. **Quality Assurance (QA):** 100% automated test coverage with 227 passing test cases.
