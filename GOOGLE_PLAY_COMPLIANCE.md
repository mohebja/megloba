# 📋 Global SMS - Google Play Compliance Review Report
**بررسی جامع انطباق با قوانین و سیاست‌های انتشار کنسول گوگل پلی (Google Play Developer Program Policies)**

---

### 1. Default SMS App Policy Declaration (سیاست برنامه‌های پیش‌فرض SMS)
- **Status:** **FULLY COMPLIANT**
- **Details:** The app is configured with appropriate Intent Filters (`SMS_DELIVER_ACTION`, `WAP_PUSH_DELIVER_ACTION`, `SENDTO`, `ACTION_SEND`) and prompts the standard system `RoleManager.ROLE_SMS` dialog for user consent.

### 2. User Data & Privacy Policy (حریم خصوصی و داده‌های کاربر)
- **Status:** **FULLY COMPLIANT**
- **Details:** Zero external tracking, zero background telemetry, zero third-party ads. Privacy policy URL provided and Data Safety form answers verified.

### 3. Financial & Transactional Data Safety (امنیت داده‌های مالی)
- **Status:** **FULLY COMPLIANT**
- **Details:** Bank transaction parser functions strictly on-device without network communication. Bank cards and account numbers are masked when rendered or logged.

### 4. Target SDK & API Requirements
- **Target SDK:** API 36 (Android 16 Ready)
- **Min SDK:** API 24 (Android 7.0+)
- **64-bit Architecture Support:** Fully supported via Kotlin / Jetpack Compose and native libraries.

### 5. Malware, Phishing & Unwanted Software Policy
- **Status:** **PASSED**
- **Details:** Integrated on-device phishing detector scans incoming SMS URLs against suspicious keywords and alerts the user locally.

### 6. Encryption & Data Protection Architecture
- **Status:** **FULLY COMPLIANT**
- **Details:** Local database entities utilize hardware-backed (Android Keystore) AES-256-GCM field-level encryption for sensitive fields. Backup archives utilize password-derived (PBKDF2-HMAC-SHA256, 210,000 iterations) AES-256-GCM encryption with 16-byte random salt and 12-byte random IV — which by design is intentionally portable (not bound to device hardware) so backups remain restorable across devices with the user's password. Private Vault hidden messages use dedicated password-derived AES-256-GCM encryption keyed directly to the user's vault passcode.
