# 📐 Global SMS — Software Design Document (SDD)

**Version:** 8.0.0  
**Package:** `com.global.sms`  
**Target Platform:** Android 7.0 (API 24) to Android 16 (API 36)  
**Compilation Target:** Android API 36  
**Quality Certification:** 227 Automated Tests Passed (100%)

---

## 1. Executive Overview

Global SMS is an enterprise-grade, privacy-first Android SMS/MMS messaging client, financial intelligence manager, and business workforce platform. The application is built entirely with modern Android technologies:
- **Language:** Kotlin 2.x
- **UI Framework:** Jetpack Compose with Material Design 3 and WCAG 2.2 AA accessibility
- **Architecture:** Clean Architecture + MVI/MVVM with Unidirectional Data Flow (UDF)
- **Data Persistence:** Room Database v31 with SQLite Write-Ahead Logging (WAL) and FTS4/FTS5
- **Security:** Hardware-backed Android KeyStore AES-256-GCM, BiometricPrompt, and GSMS authenticated backups

---

## 2. Multi-Module Decoupling & Structure

The codebase is organized into 7 independent Gradle modules:

```
                          ┌──────────────┐
                          │     :app     │
                          └──────┬───────┘
                                 │
     ┌───────────────────────────┼───────────────────────────┐
     ▼                           ▼                           ▼
┌──────────┐               ┌───────────┐               ┌───────────┐
│   :ui    │               │:sms-engine│               │ :security │
└────┬─────┘               └─────┬─────┘               └─────┬─────┘
     │                           │                           │
     └───────────────────────────┼───────────────────────────┘
                                 ▼
                          ┌─────────────┐
                          │  :settings  │
                          └──────┬──────┘
                                 ▼
                          ┌─────────────┐
                          │  :database  │  (Schema v31, 40+ Entities)
                          └──────┬──────┘
                                 ▼
                          ┌─────────────┐
                          │    :core    │  (AI, Finance, Export, Trash)
                          └─────────────┘
```

### Module Responsibilities:
1. **`:app` (Application & Assembly):**
   - Application lifecycle entry point (`GlobalSmsApp`).
   - Global crash handling (`GlobalCrashHandler`, `CrashManager`).
   - Background maintenance jobs and Coil custom image loading pipeline.
   - Headless test environment isolation (`isTestEnvironment()`).
2. **`:core` (Domain Logic & Offline Intelligence):**
   - Heuristic AI classification (`AIMessageClassifier`).
   - Financial banking parser (`BankTransactionAnalyzer`).
   - UTF-8 BOM CSV/Excel export engine (`FinancialExportEngine`).
   - Soft-delete Recycle Bin manager (`RecycleBinManager`).
   - Sub-10ms search ranking engine (`SearchRankingEngine`).
3. **`:database` (Room Persistence):**
   - Room Database Schema v31 with 40+ specialized entities.
   - Paging 3 integration for 500,000+ messages without UI lag.
   - Full-Text Search via `messages_fts` virtual table.
4. **`:sms-engine` (Telephony & Broadcasts):**
   - Full Default SMS App roles (`SmsReceiver`, `MmsReceiver`, `HeadlessSmsSendService`).
   - Dual SIM telephony management (`DualSimManager`, `SimPermissionManager`).
   - WorkManager exponential backoff dispatching.
5. **`:security` (Zero-Trust Cryptography):**
   - Hardware-backed KeyStore master key (`AES/GCM/NoPadding`).
   - Private Vault with BiometricPrompt and PBKDF2 (21,000 rounds).
   - Authenticated `GSMS` v1 container format for encrypted backups.
6. **`:settings` (Preferences & Modes):**
   - DataStore preferences and UI mode switching (*Classic*, *Smart AI*, *Enterprise*).
7. **`:ui` (Jetpack Compose UI):**
   - Material 3 screens, ViewModels, and RTL typography layouts.

---

## 3. High-Volume Scalability & Performance Strategy

- **SQLite WAL Mode:** Non-blocking concurrent reader and writer threads.
- **Paging 3 Architecture:** Chunked database queries fetching only visible viewports into memory.
- **Memory Caching Bounds:** Coil memory cache restricted to maximum 25% of available heap.
- **FTS Search Optimization:** Sub-10ms queries for Persian and Latin keywords across half a million SMS.
- **Battery Optimization:** Background WorkManager constraints enforce `requiresBatteryNotLow` and idle states.
