#!/bin/bash

mkdir -p documentation

# 1. SDD
cat << 'EOD' > documentation/SOFTWARE_DESIGN_DOCUMENT.md
# Global SMS — Software Design Document (SDD)

## 1. System Overview
Global SMS (`com.global.sms`) is an enterprise-grade, secure Android messaging platform designed with Clean Architecture, Jetpack Compose, Room Database, and Dual SIM Telephony support.

## 2. System Architecture
The application is structured into 7 distinct modules:
- `:app` - Entry point and dependency injection setup.
- `:core` - Domain models, Result wrappers, Common utils.
- `:database` - Room database, DAOs, Entities, Encryption layer.
- `:sms-engine` - Telephony APIs, SMS/MMS receivers, Dual SIM manager, WorkManager schedulers.
- `:security` - Keystore AES-256 encryption, Biometric authentication, Private Vault, Phishing protection.
- `:settings` - Preferences, Datastore, Custom UI mode configurations.
- `:ui` - Jetpack Compose UI, Material 3 components, Classic, Smart AI, and Enterprise mode screens.

## 3. Core Component Design
- **Default SMS Role:** Fully implements `RoleManager.ROLE_SMS` handlers (`SMS_DELIVER_ACTION`, `WAP_PUSH_DELIVER_ACTION`, `RESPOND_VIA_MESSAGE`, `ACTION_SENDTO`).
- **Data Flow:** Unidirectional data flow (UDF) via Kotlin StateFlow and Jetpack Compose.
EOD

# 2. SRS
cat << 'EOD' > documentation/SOFTWARE_REQUIREMENTS_SPECIFICATION.md
# Global SMS — Software Requirements Specification (SRS)

## 1. Functional Requirements
- **FR-1:** Receive and send single and multi-part SMS messages.
- **FR-2:** Support Dual SIM devices with active subscription ID and SIM slot selection.
- **FR-3:** Act as Android Default SMS Application (`RoleManager.ROLE_SMS`).
- **FR-4:** Provide three UI modes: Classic SMS UI, Smart AI UI, Enterprise UI.
- **FR-5:** Private Vault with AES-256 encryption and biometric lock.
- **FR-6:** Contact synchronization with RTL (Persian/Arabic) character search support.

## 2. Non-Functional Requirements
- **NFR-1 (Performance):** Support 100,000+ messages and 10,000+ contacts without UI frame drops (< 16ms per frame).
- **NFR-2 (Security):** Zero unencrypted plaintext storage for vault items. Hardware-backed Android Keystore system keys.
- **NFR-3 (Usability):** Adaptive layouts for Compact (phones), Medium (foldables), and Expanded (tablets) displays.
EOD

# 3. System Architecture Document
cat << 'EOD' > documentation/SYSTEM_ARCHITECTURE_DOCUMENT.md
# Global SMS — System Architecture Document

## 1. Architectural Style
Global SMS uses Clean Architecture with MVVM and Unidirectional Data Flow (UDF).

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

## 2. Data Persistence Strategy
- Room Database (`SmsDatabase`) with WAL journal mode.
- AES-256-GCM encryption for Private Vault items using Android Keystore.
EOD

# 4. Database Design Document
cat << 'EOD' > documentation/DATABASE_DESIGN_DOCUMENT.md
# Global SMS — Database Design Document

## 1. ER Schema Overview
The database consists of 4 main tables:
- `sms_messages` (Stores SMS & MMS message metadata and contents)
- `conversations` (Stores aggregated thread statistics, unread count, pin/archive state)
- `contacts` (Stores cached contact information, E.164 normalized numbers, Persian display names)
- `vault_messages` (Stores AES-256 encrypted private messages)

## 2. Indexing Strategy
- `sms_messages`: Indexes on `threadId`, `timestamp`, `address`.
- `contacts`: Index on `normalizedNumber` for O(1) caller lookup.
EOD

# 5. API Documentation
cat << 'EOD' > documentation/API_DOCUMENTATION.md
# Global SMS — Internal API Documentation

## 1. SmsEngine Module APIs
- `SmsSender.sendMessage(context, address, body, simSlot, subId)`: Sends single or multi-part SMS via specified SIM subscription.
- `DualSimManager.getActiveSimCards(context)`: Returns list of active SIM card info with slot index and subscription IDs safely.

## 2. Security Module APIs
- `KeyStoreManager.encryptData(plaintext)`: Encrypts data using AES-256 GCM key stored in Android Keystore.
- `BiometricPromptHelper.showBiometricPrompt(activity, onSuccess, onError)`: Prompts user for biometric verification.
EOD

# 6. Security Documentation
cat << 'EOD' > documentation/SECURITY_DOCUMENTATION.md
# Global SMS — Security Documentation

## Cryptographic Implementation
- Algorithm: AES/GCM/NoPadding (256-bit key length)
- Key Storage: Android Keystore System (`AndroidKeyStore` provider)
- Authentication: `BiometricPrompt` with CryptoObject binding
- Anti-Phishing: On-device URL analysis and domain sanitizer
EOD

# 7. Testing Documentation
cat << 'EOD' > documentation/TESTING_DOCUMENTATION.md
# Global SMS — Testing Documentation

## Test Frameworks
- Unit Tests: JUnit 4, Kotlin Coroutines Test framework
- JVM Android Tests: Robolectric
- Visual Regression & Screenshot Tests: Roborazzi

## Execution Commands
- Unit Tests: `gradle :app:testDebugUnitTest`
- Verification: `compile_applet`
EOD

# 8. Deployment Guide
cat << 'EOD' > documentation/DEPLOYMENT_GUIDE.md
# Global SMS — Deployment Guide

## Prerequisites
- JDK 17
- Android SDK 35 (Target API 35 / Min API 26)
- Gradle 8.x

## Build Commands
- Build Debug APK: `gradle :app:assembleDebug`
- Build Release Bundle (AAB): `gradle :app:bundleRelease`
EOD

# 9. Developer Setup Guide
cat << 'EOD' > documentation/DEVELOPER_SETUP_GUIDE.md
# Global SMS — Developer Setup Guide

## Getting Started
1. Clone the repository.
2. Open in Android Studio Ladybug or higher.
3. Sync Gradle dependencies.
4. Execute `gradle :app:compileDebugSources` or run `compile_applet`.
EOD

# 10. Contribution Guide
cat << 'EOD' > documentation/CONTRIBUTION_GUIDE.md
# Global SMS — Contribution Guide

## Guidelines
- Follow Kotlin Coding Conventions and SOLID design principles.
- Ensure all Kotlin files pass formatting and static analysis (`gradle lint`).
- Include unit tests for all new domain logic.
EOD

# 11. Release Checklist
cat << 'EOD' > documentation/RELEASE_CHECKLIST.md
# Global SMS — Release Checklist

- [x] Compilation verified (`compile_applet` succeeds)
- [x] All 7 modules compile without errors
- [x] Default SMS Role handlers declared in AndroidManifest
- [x] Google Play SMS & Call Log policy requirements met
- [x] AES-256 encryption & Keystore security verified
- [x] Persian RTL layout & font support validated
EOD

# 12. Change Log Template
cat << 'EOD' > documentation/CHANGELOG_TEMPLATE.md
# Global SMS — Change Log

## [1.0.0] - 2026-08-02
### Added
- Initial release of Global SMS.
- Multi-module architecture (`app`, `core`, `database`, `sms-engine`, `security`, `settings`, `ui`).
- Dual SIM support with subscription ID resolution.
- Three UI Modes: Classic, Smart AI, Enterprise.
- Private Vault with AES-256 GCM encryption and Biometric Lock.
- RTL / Persian & Arabic localization support.
EOD

# 13. Future Roadmap
cat << 'EOD' > documentation/FUTURE_ROADMAP.md
# Global SMS — Future Roadmap

## Planned Features
- **V1.1:** Advanced local machine learning model for spam message classification.
- **V1.2:** End-to-end encrypted multi-device message backup and synchronization.
- **V2.0:** RCS messaging support and Wear OS companion app.
EOD

# 14. Developer Maintenance Guide
cat << 'EOD' > documentation/DEVELOPER_MAINTENANCE.md
# Global SMS — Developer Maintenance Guide

## Maintainability & Architecture Summary
This document serves as the primary maintenance handbook for core developers and DevOps engineers.

### Build & Maintenance Tasks
- Core Module Updates: Maintain strict separation between `:database` and `:sms-engine`.
- Permission Handling: Always wrap Telephony and SIM SDK calls with runtime permission checks.
- WorkManager Tasks: Manage background SMS workers with `ExistingPeriodicWorkPolicy.KEEP` to ensure zero task duplication.
EOD

