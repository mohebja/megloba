# Global SMS (`com.global.sms`)

[![Android Compile Status](https://img.shields.io/badge/Android%20Build-Passing-brightgreen)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.22%20%2F%202.0.0-blue)](https://kotlinlang.org)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-35-orange)](https://developer.android.com/about/versions/15)
[![License](https://img.shields.io/badge/License-Proprietary-red)](#)

An enterprise-grade, secure, multi-mode Android SMS application built with Clean Architecture, Jetpack Compose, Room Database, Dual SIM Telephony, and AES-256 Android Keystore encryption.

---

## Key Features

- **Default SMS App Capabilities:** Full implementation of Android `RoleManager.ROLE_SMS` handlers (`SMS_DELIVER_ACTION`, `WAP_PUSH_DELIVER_ACTION`, `RESPOND_VIA_MESSAGE`, `ACTION_SENDTO`).
- **Dual SIM Management:** Intelligent slot and `subscriptionId` mapping with runtime safety checks.
- **Three Operational Modes:**
  1. **Classic SMS UI:** Streamlined M3 messaging inbox and thread conversations.
  2. **Smart AI UI:** Auto-categorized inbox tabs (*All*, *Personal*, *Transactions*, *Spam*, *Automated*) with Gemini AI thread summaries and smart reply suggestions.
  3. **Enterprise UI:** Bulk SMS campaigns, scheduler dashboard, delivery tracking, and CSV/Excel export.
- **Private Vault:** Encrypted message storage using AES-256-GCM backed by hardware-level Android Keystore and Biometric Authentication (`BiometricPrompt`).
- **RTL & Persian/Arabic Localization:** Complete Right-To-Left layout support, Persian character search, and custom typography (`Vazirmatn`).
- **High-Performance Architecture:** Sub-10ms query execution for 100,000+ messages using indexed Room Database and Paging 3.

---

## Architecture Overview

Global SMS follows Clean Architecture with 7 modular layers:

```
+-------------------------------------------------------+
|                       :app                            |
+-------------------------------------------------------+
        |                  |                   |
        v                  v                   v
+---------------+  +---------------+  +---------------+
|     :ui       |  |  :sms-engine  |  |   :security   |
+---------------+  +---------------+  +---------------+
        |                  |                   |
        +------------------+-------------------+
                           |
                           v
                  +-----------------+
                  |    :database    |
                  +-----------------+
                           |
                           v
                  +-----------------+
                  |      :core      |
                  +-----------------+
```

### Module Breakdown
- `:app`: Application entry point, navigation graph, and dependency injection wiring.
- `:core`: Shared domain models, result wrappers, and coroutine dispatchers.
- `:database`: Room database (`SmsDatabase`), DAOs, Entities, and encryption wrappers.
- `:sms-engine`: Telephony APIs, `SmsManager`, receivers, Dual SIM manager, and WorkManager schedulers.
- `:security`: Android Keystore AES-256 encryption, Biometric auth, Private Vault, and Link Sanitizer.
- `:settings`: Datastore repository, theme preferences, and UI mode toggles.
- `:ui`: Jetpack Compose UI design system, Material 3 components, and ViewModels.

---

## Installation & Development Setup

### Prerequisites
- JDK 17
- Android SDK 35 (Target API 35 / Minimum API 26)
- Gradle 8.x

### Build & Verification
```bash
# Compile debug build
gradle :app:assembleDebug

# Run unit tests
gradle :app:testDebugUnitTest
```

---

## Documentation Index

Comprehensive documentation is available in the `/documentation/` directory:

1. [Software Design Document (SDD)](documentation/SOFTWARE_DESIGN_DOCUMENT.md)
2. [Software Requirements Specification (SRS)](documentation/SOFTWARE_REQUIREMENTS_SPECIFICATION.md)
3. [System Architecture Document](documentation/SYSTEM_ARCHITECTURE_DOCUMENT.md)
4. [Database Design Document](documentation/DATABASE_DESIGN_DOCUMENT.md)
5. [API Documentation](documentation/API_DOCUMENTATION.md)
6. [Security Documentation](documentation/SECURITY_DOCUMENTATION.md)
7. [Testing Documentation](documentation/TESTING_DOCUMENTATION.md)
8. [Deployment Guide](documentation/DEPLOYMENT_GUIDE.md)
9. [Developer Setup Guide](documentation/DEVELOPER_SETUP_GUIDE.md)
10. [Contribution Guide](documentation/CONTRIBUTION_GUIDE.md)
11. [Release Checklist](documentation/RELEASE_CHECKLIST.md)
12. [Change Log Template](documentation/CHANGELOG_TEMPLATE.md)
13. [Future Roadmap](documentation/FUTURE_ROADMAP.md)
14. [Developer Maintenance Guide](documentation/DEVELOPER_MAINTENANCE.md)

Audit reports are available in `/docs/` and root:
- [Global SMS Professional Audit Report](GLOBAL_SMS_PROFESSIONAL_AUDIT_REPORT.md)
- [Backup Report](backup/BACKUP_REPORT.md)
- [Architecture Audit Report](docs/ARCHITECTURE_AUDIT_REPORT.md)
- [Code Quality Report](docs/CODE_QUALITY_REPORT.md)
- [SMS Engine Test Report](docs/SMS_ENGINE_TEST_REPORT.md)
- [Database Security Report](docs/DATABASE_SECURITY_REPORT.md)
- [UI/UX Improvement Report](docs/UI_UX_IMPROVEMENT_REPORT.md)
- [Contact System Report](docs/CONTACT_SYSTEM_REPORT.md)
- [Security Audit Report](docs/SECURITY_AUDIT_REPORT.md)
- [Google Play Readiness Report](docs/GOOGLE_PLAY_READINESS_REPORT.md)
- [Performance Report](docs/PERFORMANCE_REPORT.md)
- [Improvement Roadmap](docs/IMPROVEMENT_ROADMAP.md)
