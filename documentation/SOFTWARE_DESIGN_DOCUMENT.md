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
