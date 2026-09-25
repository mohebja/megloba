# 📝 Changelog

All notable changes to the Global SMS Android application across development sprints are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [8.0.0-PROD] - 2026-09-25

### Software Architecture & Engineering
- **Financial Export Engine Refinement:**
  - Standardized `balanceAfter` calculation and dynamic categorization in `FinancialExportEngine`.
  - Added Microsoft Excel UTF-8 BOM encoding (`\uFEFF`) for flawless Persian character rendering in CSV and spreadsheets.
  - Linked export affordances in `FinancialDashboardScreen` using Material 3 icon components.
- **Recycle Bin (Trash) Architecture:**
  - Implemented explicit `deleteMessageById` and `deleteMessage` in `MessageDao` to prevent foreign-key and transaction lock conflicts.
  - Added 30-day auto-purge expiration and single-tap message restoration in `RecycleBinManager`.
- **Background Task & WorkManager Hardening:**
  - Isolated periodic database maintenance and contact synchronization in `GlobalSmsApp` and `ContactSyncManager` using `isTestEnvironment()` guards.
  - Resolved headless Robolectric `BatteryNotLowTracker` receiver crashes during JVM unit testing.

### Quality Assurance & Verification
- **100% Test Suite Green:** Executed 227 JVM unit and Robolectric tests with 0 failures, 0 errors, and 0 skipped tests (`gradle :app:testDebugUnitTest`).
- **Full Sprint Regression Coverage:** Verified Sprint 5 through Sprint 16 test gates, including AI classification, CRM, backup encryption, and Google Play compliance.

---

## [7.0.0-ENT] - 2026-09-18

### Enterprise & CRM Capabilities
- **Bulk SMS Campaign Engine:** Added batch scheduling, dynamic token substitution (`{name}`, `{company}`), and per-recipient delivery status.
- **CRM Profiles & Tags:** Added customer interaction history, private tags, and conversation bookmarking.
- **Multi-Cloud Connector Framework:** Introduced privacy master switch controlling remote backups to Google Drive or Private Enterprise Server.

---

## [6.0.0-AI] - 2026-09-10

### On-Device Artificial Intelligence
- **Offline Message Classification:** Implemented on-device heuristic & pattern classifier for Banking, OTP, Personal, Spam, and Promotions.
- **Entity Extraction Engine:** Automated extraction of bank card numbers, postal tracking codes, amounts, and meeting timestamps.
- **Smart Reply V2 & Summaries:** Generated context-aware Persian quick replies and conversation thread summaries.

---

## [5.0.0-SEC] - 2026-09-01

### Security & Cryptographic Integrity
- **Authenticated GSMS Container Format:** Implemented `GSMS` v1 binary backup container with PBKDF2 (21,000 iterations, 16-byte salt) and AES-256-GCM.
- **Private Vault Biometric Integration:** Hardware-backed Keystore authentication with BiometricPrompt and StrongBox isolation.
- **Phishing & URL Guard:** Local regex and domain scanner detecting lookalike characters and deceptive links.

---

## [1.0.0-PROD] - 2026-08-20

### Initial Production Architecture
- **Dual SIM Management:** Centralized telephony detection in `:core` module (`DualSimManager`, `SimPermissionManager`).
- **Room Database Architecture:** Established baseline database schema with WAL mode and Paging 3 integration.
- **Default SMS App Compliance:** Registered full Android `RoleManager.ROLE_SMS` intents and services.
