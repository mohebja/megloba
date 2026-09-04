# Global SMS — System Architecture Document

**Project Name:** Global SMS (`com.global.sms`)  
**Version:** 1.5.0  
**Date:** August 2, 2026  

---

## 1. Multi-Module Hierarchy

Global SMS is modularized into 7 distinct Gradle subprojects to enforce build isolation, fast compilation caching, and clear domain separation.

1. **`:app`**: Application entry point, Android Manifest declaration, Hilt/Manual DI wiring, and root Navigation Host.
2. **`:ui`**: Jetpack Compose screens, Material 3 design tokens, customizable themes, and responsive layout adaptors.
3. **`:settings`**: DataStore preferences repository, enterprise settings screen, and theme configuration managers.
4. **`:sms-engine`**: Android Telephony framework integrations, SMS/MMS broadcast receivers, Dual SIM subscription dispatchers, and WorkManager background tasks.
5. **`:core`**: Core business domain logic, local NLP engines, AI classifiers, OTP intelligence tools, banking transaction parsers, and smart search.
6. **`:security`**: KeyStore key generators, AES-256-GCM cipher engines, Private Vault authenticators, and screenshot protection managers.
7. **`:database`**: Room Database schema, DAOs, entities, and migration helpers.

---

## 2. Core Data Flow & Telephony Lifecycle

1. **SMS Arrival:** Android OS broadcasts `SMS_DELIVER_ACTION` to `SmsReceiver` in `:sms-engine`.
2. **On-Device AI Classification:** `MessageDispatcher` passes the body to `AIMessageClassifier` in `:core`.
3. **Persisted Storage:** Message is encrypted if private and inserted into Room Database (`:database`).
4. **Notification:** `SmartNotificationManager` evaluates urgency and posts a notification channel alert with direct "Copy OTP" action if applicable.
5. **UI Update:** Room Flow emits updated dataset; ViewModels in `:ui` re-emit updated state to Compose screens.
