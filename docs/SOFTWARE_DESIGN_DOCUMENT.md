# Software Design Document (SDD)
## Global SMS - Production-Grade Secure Android Messaging Application

**Version:** 1.0.0  
**Status:** Approved Architectural Specification  
**Target Platform:** Android 8.0+ (API Level 26+)  
**Language:** Kotlin 2.2+  
**UI Engine:** Jetpack Compose (Material 3)  
**Architecture:** Clean Architecture + MVVM + Repository Pattern  

---

### 1. Project Overview
**Global SMS** is an enterprise-grade, secure, scalable, and highly customizable Android SMS and MMS application designed to meet international standards while providing rich native localization, specifically full Persian (Farsi) language, Persian calendar support, and right-to-left (RTL) layout capabilities. The application operates as a full **Default SMS Handler** replacement on Android devices, offering encrypted local message storage, private vault privacy modes, real-time security scanning (anti-phishing, fraud, and bank transaction parsing), scheduled messaging via Android WorkManager, dual SIM management, Text-To-Speech (TTS) voice assistant, and comprehensive backup/restore operations.

---

### 2. Goals and Objectives
- **Security & Privacy First:** Protect sensitive personal, financial, and OTP communications using local AES-256 database encryption, PIN/Biometric app lock, and private notification masking.
- **Full Persian & Multilingual Localization:** Seamless Persian text rendering, Jalali/Persian calendar date display, Persian digit conversions, and complete RTL layout symmetry alongside English/LTR.
- **High Performance & Offline Resilience:** Zero external server dependencies for core messaging operations; fast indexed search, smooth 60fps Compose list scrolling, and background sync.
- **Smart SMS Automation:** Automatic categorization into Personal, Bank, Work, Important, Spam, and Private folders, with automated bank OTP extraction and voice reading assistance.
- **Google Play Compliance:** Strict adherence to Google Play policies regarding the Default SMS Handler role, clear permission disclosure dialogs, and zero unauthorized command/USSD execution.

---

### 3. Functional Requirements
#### 3.1 Messaging Core
- **Default SMS Handler Integration:** Process incoming `SMS_RECEIVED`, `MMS_RECEIVED`, and `SMS_DELIVERED` broadcasts; support direct SMS/MMS composition, status updates, and notification replies.
- **Long SMS & Unicode Segmentation:** Handle multi-part concatenated SMS messages (70-character limit for Unicode/Persian, 160-character limit for GSM 7-bit) with accurate segment count preview.
- **Dual SIM Support:** Multi-SIM slot detection, per-message SIM selector prior to transmission, and clear SIM badge indicators on past conversations.
- **Message Scheduling:** Precise future-time scheduling using Android `WorkManager`, background alarm dispatching, and pre-send editing/cancellation.

#### 3.2 Smart Categorization & Security
- **Automated Spam & Phishing Scanner:** Regex and heuristic threat engine to flag spam, suspicious URLs, fraudulent bank impersonations, and unknown promotional senders.
- **Banking Message Engine:** Auto-classify transactions, OTP verification codes, credit/debit alerts, and card balances with one-tap copyable OTP codes.
- **Private Vault & Message Hiding:** Hide individual messages or complete threads behind a secure biometric/PIN wall with private notification masking (`"New message received"`).
- **Phishing Prevention & Link Security:** Plain-text URL previewing and user confirmation popups before launching browser links.

#### 3.3 Backup & Voice Assistant
- **Encrypted Local & Cloud Backup:** Export and import message threads into password-protected AES-256 JSON backup archives on local storage or Google Drive.
- **Voice Reading Assistant:** Native Android `TextToSpeech` engine configured for Persian and English voice playback with speed, pitch, and privacy control toggles.

---

### 4. Non-Functional Requirements
- **Performance:** Cold launch under 1.2 seconds; message thread scrolling at sustained 60 FPS; database search query response under 50ms for 10,000+ messages.
- **Reliability:** 99.9% message delivery dispatch rate with automatic retries on cell network re-connections.
- **Security:** AES-256 encryption for private vault entries and backup exports; zero cleartext key storage in source code or unencrypted shared preferences.
- **Accessibility:** Minimum touch target size of 48x48 dp; full TalkBack screen reader support; scalable typography using `sp`.

---

### 5. System Architecture
Global SMS follows **Clean Architecture** decoupled into distinct architectural layers:

```
+-------------------------------------------------------------------+
|                        UI / Presentation Layer                    |
|  [Compose Screens] <---> [ViewModels] <---> [UI State Flows]      |
+-------------------------------------------------------------------+
                                  |
+-------------------------------------------------------------------+
|                           Domain Layer                            |
|       [Use Cases] <---> [Domain Models] <---> [Repository Interfaces]
+-------------------------------------------------------------------+
                                  |
+-------------------------------------------------------------------+
|                            Data Layer                             |
|  [SMS Engine] | [Room Encrypted DB] | [WorkManager] | [Security] |
+-------------------------------------------------------------------+
```

---

### 6. Module Architecture
1. **Core Module (`core`):** Constants, extensions, date/time tools (Jalali calendar helpers), Persian number converters, result wrappers.
2. **Database Module (`database`):** Room Database (`GlobalSmsDatabase`), DAOs, Entities, Converters, AES-256 cipher helpers.
3. **SMS Engine Module (`sms_engine`):** `SmsReceiver`, `MmsReceiver`, `SmsSender`, `SmsSegmenter`, `DualSimManager`.
4. **Security Module (`security`):** Biometric/PIN Authenticator, Link Phishing Scanner, Private Vault Manager, Data Encryptor.
5. **UI Module (`ui`):** Compose screens (ConversationList, Thread, Vault, Scheduled, Settings, Spam, Stats), M3 Theme, Fonts.
6. **Settings Module (`settings`):** User preference datastore, theme/font configuration, notification toggles, voice settings.

---

### 7. Database Design
#### Schema Overview
- **`messages`**: `id` (PK), `thread_id`, `address`, `body`, `timestamp`, `type` (1=Inbox, 2=Sent, 3=Draft, 4=Outbox), `sim_slot`, `is_read`, `is_hidden`, `category` (PERSONAL, BANK, WORK, IMPORTANT, SPAM, PRIVATE), `is_pinned`, `is_encrypted`.
- **`conversations`**: `thread_id` (PK), `address`, `contact_name`, `last_message`, `last_timestamp`, `unread_count`, `category`, `is_pinned`, `is_hidden`, `avatar_uri`.
- **`scheduled_messages`**: `id` (PK), `address`, `body`, `scheduled_time`, `sim_slot`, `status` (PENDING, SENT, FAILED, CANCELLED).
- **`categories`**: `id` (PK), `name`, `color_hex`, `icon_name`, `is_system`.
- **`spam_rules`**: `id` (PK), `pattern`, `rule_type` (KEYWORD, SENDER, REGEX).
- **`settings`**: `key` (PK), `value`.

---

### 8. Security Architecture
- **AES-256 Encryption:** PBKDF2 key derivation from user salt/PIN combined with Android KeyStore system keys.
- **Biometric Integration:** `BiometricPrompt` framework supporting Fingerprint, Face, and Fallback Device PIN.
- **Phishing Detection:** Regex pattern matcher identifying suspicious IP URLs, shorteners (`bit.ly`, `tinyurl`), and suspicious top-level domains (`.xyz`, `.top`).
- **Notification Masking:** System notification builder replaces message text and sender name with generic string when Private Mode is enabled.

---

### 9. UI/UX Design & Persian Localization
- **Design System:** Material Design 3 (M3) dynamic colors, custom Light, Dark, and AMOLED True Dark themes.
- **RTL Symmetrical Layouts:** Dynamic direction checking using `LocalLayoutDirection` providing natural Persian right-to-left layout symmetry.
- **Persian Typography & Formatting:** Vazirmatn font support, automatic Latin-to-Persian digit conversion (`123` -> `۱۲۳`), Jalali Persian calendar date formatting.

---

### 10. Permission Management & Google Play Compliance
- **Default SMS Handler Declaration:** Manifest declares `android.provider.Telephony.SMS_DELIVERED`, `SMS_RECEIVED`, `SEND_SMS`, `RECEIVE_SMS`, `READ_SMS`, `RECEIVE_MMS`, `WAP_PUSH_DELIVERED`.
- **Runtime Permission Dialog:** Interactive Compose onboarding sheet explaining why SMS permissions are needed prior to system prompt display.
- **Privacy Policy Compliance:** Transparent data handling—messages never leave the local device. Zero background execution of unauthorized USSD or web links.

---

### 11. Testing & Deployment Strategy
- **Unit & Robolectric Testing:** Verification of SMS parsing, bank transaction regex, AES-256 encryption, and Room DAO operations using JVM tests (`ExampleRobolectricTest.kt`).
- **Continuous Integration:** Automated build verification via `compile_applet`.

---

### 12. Future Expansion Roadmap
- **RCS Messaging:** Next-gen Rich Communication Services protocol support.
- **Cloud Sync:** Multi-device encrypted peer-to-peer sync.
- **AI Categorization Expansion:** On-device neural network model for spam classification.
