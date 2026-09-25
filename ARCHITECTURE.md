# 🏗️ Global SMS - Architecture Specification (v8.0.0)

## 1. Module Dependency Graph
```
        ┌──────────┐
        │   :app   │
        └────┬─────┘
             │
   ┌─────────┼──────────┬───────────┐
   ▼         ▼          ▼           ▼
 ┌────┐  ┌────────┐ ┌────────┐ ┌─────────┐
 │:ui │  │:settings│ │:security│ │:sms-engine│
 └─┬──┘  └───┬────┘ └───┬────┘ └────┬────┘
   │         │          │           │
   └─────────┼──────────┴───────────┘
             ▼
        ┌─────────┐
        │:database│  (Room Schema v31, FTS4/5, SQLite WAL)
        └────┬────┘
             ▼
         ┌─────┐
         │:core│     (On-Device AI, Financial Parser, Domain Engines)
         └─────┘
```

## 2. Layer Specifications
1. **Presentation Layer (`:ui`, `:settings`):**
   - 100% Jetpack Compose adhering to Material Design 3 and WCAG 2.2 AA accessibility.
   - Dynamic M3 color theming, RTL typography scaling, and high-contrast OLED Dark mode.
   - State management via `ViewModel` and `MutableStateFlow` with unidirectional data flow.
   - 3 switchable UI modes: Classic SMS, Smart AI OS, and Enterprise Workforce.

2. **Domain & Business Logic Layer (`:core`, `:sms-engine`):**
   - **Offline AI Classifier:** Local heuristic & rule-based categorization (*Banking*, *OTP*, *Spam*, *Personal*, *Promotions*).
   - **Financial Analytics & Export:** Bank transaction detection, balance tracking, and UTF-8 BOM CSV/Excel export.
   - **Search & Ranking:** Sub-10ms BM25-ranked full-text search with Persian token normalization.
   - **Recycle Bin:** Soft delete with 30-day retention and one-tap restore (`RecycleBinManager`).
   - **Telephony & Dispatching:** Default SMS handler services, Dual SIM subscription routing, and WorkManager exponential backoff.

3. **Security & Cryptography Layer (`:security`):**
   - Hardware-backed master key generation in `AndroidKeyStore` (`AES/GCM/NoPadding`).
   - Private Vault isolation with BiometricPrompt and PBKDF2 (21,000 iterations).
   - Authenticated `GSMS` v1 container format for periodic and manual encrypted backups.
   - Test environment isolation protecting background workers from headless crashes.

4. **Data Persistence Layer (`:database`):**
   - Android Room Database Schema Version 31 with non-destructive migrations (`MIGRATION_1_2` ... `MIGRATION_30_31`).
   - SQLite Write-Ahead Logging (WAL) and memory mapping for 500,000+ message datasets.
   - Android Paging 3 integration for zero-jank 60-120fps scrolling.

## 3. Engineering & Role Governance
Governed by the Multi-Agent Framework in `AGENTS.md` covering Product, Architecture, DevSecOps, Database Administration, and Automated QA (227 tests passing at 100%).
