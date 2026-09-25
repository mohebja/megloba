# Global SMS — Software Requirements Specification (SRS)

**Version:** 8.0.0  
**Package:** `com.global.sms`  
**Target Platform:** Android 7.0 (API 24) to Android 16 (API 36)

---

## 1. Functional Requirements (FR)

- **FR-1 (Default SMS & MMS Handler):**
  - Implement all mandatory Android `RoleManager.ROLE_SMS` components: `SMS_DELIVER_ACTION`, `WAP_PUSH_DELIVER_ACTION`, `RESPOND_VIA_MESSAGE`, and `ACTION_SENDTO`.
  - Seamlessly import existing device messages upon first launch without duplication.
- **FR-2 (Dual SIM Management):**
  - Detect active SIM subscriptions, display carrier logos/names, and allow per-message SIM slot selection with fallback logic.
- **FR-3 (Three Operational UI Modes):**
  - Provide instant switching between **Classic Clean**, **Smart AI OS**, and **Enterprise Workforce** modes without restarting the application.
- **FR-4 (Financial Analytics & Data Export):**
  - Automatically parse Iranian and international banking SMS (debit, credit, card number, remaining balance).
  - Provide interactive charts for income/expense tracking and generate UTF-8 BOM CSV/Excel reports.
- **FR-5 (Smart Recycle Bin):**
  - Retain deleted messages safely for 30 days before permanent purging, allowing instant single-tap restoration.
- **FR-6 (On-Device AI Classification & Copilot):**
  - Classify incoming messages into *Banking*, *OTP*, *Spam*, *Personal*, and *Promotions* using 100% offline heuristic analysis.
  - Automatically extract postal tracking codes, monetary amounts, and OTP PINs for one-tap copying.
- **FR-7 (Hardware-Backed Private Vault):**
  - Encrypt sensitive conversations with AES-256-GCM backed by Android KeyStore and StrongBox.
  - Protect access using Biometric Authentication (Fingerprint/Face) and PBKDF2 password derivation (21,000 iterations).
- **FR-8 (Authenticated Encrypted Backup):**
  - Export and import full database backups using custom authenticated `GSMS` v1 container format.
  - Provide automated periodic background backups via WorkManager.
- **FR-9 (Enterprise Bulk SMS & CRM):**
  - Support multi-contact batch campaigns with dynamic token substitution (`{name}`, `{company}`) and scheduling.
  - Provide CRM customer profiles, interaction notes, and conversation tags.
- **FR-10 (Persian Localization & RTL):**
  - Full Right-To-Left UI mirroring, Persian numeral formatting, Solar Hijri (Shamsi) calendar conversion, and Vazirmatn typography.

---

## 2. Non-Functional Requirements (NFR)

- **NFR-1 (High-Scale Performance):**
  - Sub-10ms query execution across 500,000+ messages using SQLite WAL and Paging 3.
  - Maintain consistent 60-120fps UI rendering during rapid message list scrolling.
- **NFR-2 (Zero-Trust Security & Privacy):**
  - 100% local on-device data processing. Zero external network telemetry or message logging.
  - Mask all PII (phone numbers, bodies) in system log outputs.
- **NFR-3 (Quality & Testability):**
  - 100% automated regression test pass rate across 227 unit and Robolectric tests.
  - Headless environment isolation ensuring zero background worker crashes in CI/CD.
- **NFR-4 (Accessibility):**
  - WCAG 2.2 AA compliance: minimum 48dp interactive touch targets, 4.5:1 contrast ratio, and TalkBack descriptions.
- **NFR-5 (Google Play Store Compliance):**
  - Fully compliant with Google Play permissions policies for Target SDK 36. Zero broad storage permissions (`READ_EXTERNAL_STORAGE` prohibited).
