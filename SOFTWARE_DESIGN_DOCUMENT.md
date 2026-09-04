# 📐 Global SMS - Software Design Document (SDD)
**Version:** 1.0.0  
**Package:** `com.global.sms`  
**Target Platform:** Android 7.0 (API 24) to Android 16 (API 36)

---

## 1. Executive Overview
Global SMS is an enterprise-grade, privacy-first Android SMS/MMS client and financial intelligence manager. The application is built entirely with modern Android technologies including Kotlin, Jetpack Compose, Coroutines, Flow, Room, Paging 3, and WorkManager.

## 2. Core Architectural Principles
- **Clean Architecture & Multi-Module Isolation:**
  - `:app` - Main application entry point, Navigation host, Crash Handler.
  - `:core` - Domain logic, SMS segmentation, Persian utilities, Bank transaction parser, Phishing detector.
  - `:database` - Room DB, DAOs, Maintenance Manager, Performance Paging.
  - `:security` - Encrypted Vault, AES-256 GCM Crypto, Keystore, Backup Engine.
  - `:settings` - Configuration management, Classification rule editor, Theme settings.
  - `:sms-engine` - Telephony receivers, SmsSender, Queue Manager, Dual SIM Manager, WorkManager schedulers.
  - `:ui` - M3 Compose screens, ViewModels, Themes.

- **Unidirectional Data Flow (UDF):**
  ViewModels expose immutable `StateFlow` and `PagingData` streams to Compose screens, listening for UI events.

- **100% On-Device Privacy:**
  Zero external network transmission for user messages, bank transactions, or personal contacts.

---

## 3. High-Volume Data Management (500,000+ SMS)
- **SQLite Write-Ahead Logging (WAL):** High-throughput concurrent reads and writes.
- **Paging 3 Integration:** Direct DAO pagination streams (`Pager` + `collectAsLazyPagingItems`).
- **Memory Optimization:** Automatic bitmap memory caching limits, fast LazyColumn key recycling.
