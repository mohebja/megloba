# 1. Software Design Document (SDD) — Global SMS

**Project Name:** Global SMS (`com.global.sms`)  
**Version:** 1.0.0 (Release Candidate)  
**Date:** August 2, 2026  

---

## 1. Executive Summary & Purpose
Global SMS is a production-grade, secure, multi-mode Android SMS client engineered for high reliability, enterprise capabilities, and privacy. It features three distinct UI systems (Classic SMS, Smart AI Dashboard, Enterprise CRM), full Persian/RTL localization, AES-256 encrypted Private Vault, dual SIM management, and offline-first Room persistence.

---

## 2. Key Modules & Design Patterns

### 2.1 MVVM + UDF (Unidirectional Data Flow)
- **ViewModels:** `GlobalSmsViewModel` acts as the single source of truth, exposing state via `StateFlow` / `collectAsStateWithLifecycle`.
- **UI Views:** Built entirely with Jetpack Compose (Material Design 3). Composables observe immutable UI states and emit user intent events to ViewModels.

### 2.2 Modular Architecture
1. **`:app`:** Application entry point, `MainActivity`, Compose Navigation routing, dependency setup.
2. **`:core`:** Domain entities, utilities (Persian typography, `PhoneNumberNormalizer`, date formatters).
3. **`:database`:** Room DB, DAOs, entities, migrations, and Room encryption wrappers.
4. **`:sms-engine`:** Telephony framework, `SmsManager` wrapper, `SmsReceiver`, `MmsReceiver`, `DualSimManager`.
5. **`:security`:** Hardware Keystore AES-256 cryptographic managers, `BiometricPrompt` controller, `FLAG_SECURE` manager.
6. **`:settings`:** Theme customization, font loader, classification rules engine, backup/restore managers.
7. **`:ui`:** Compose UI screens (Classic, Smart AI, Enterprise, Conversation, Vault, Contact Picker).
