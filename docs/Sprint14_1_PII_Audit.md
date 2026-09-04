# Sprint 14.1 — Security & PII Logging Audit

## 1. Log Redaction Architecture
Global SMS implements an automated PII sanitizer (`GlobalCrashHandler`, `SecurityLogger`):
* **Phone Numbers:** Masked automatically via regex `\d{4,}` -> `[REDACTED_NUM]`.
* **OTP Codes:** Never logged in `Logcat`.
* **Message Bodies:** Sanitized before crash report serialization.
* **Bank Cards / Account Numbers:** Masked to show only last 4 digits.

## 2. Grep Audit for Sensitive Logging
Audited all `Log.d`, `Log.i`, `Log.e`, `Log.w` calls across all 7 modules.
* **Result:** Zero occurrences of raw message payloads, passwords, or authentication keys logged to Android system logcat.
