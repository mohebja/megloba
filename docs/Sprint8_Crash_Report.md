# Sprint 8 — Production Crash Monitoring & ANR System Report

**Project:** Global SMS (`com.global.sms`)  
**Components:** `ProductionCrashReporter.kt`, `CrashManager.kt`  

---

## 1. System Architecture & Features

1. **Uncaught Exception Handling:**
   - Intercepts all uncaught JVM exceptions via `Thread.UncaughtExceptionHandler`.
   - Writes sanitized crash logs to internal application storage (`files/production_crash_logs/`).

2. **ANR (Application Not Responding) Detection:**
   - Monitors main thread heartbeat and records ANR state entries when UI thread stalls exceed 5,000ms.

3. **PII (Personally Identifiable Information) Sanitization:**
   - Automatic redaction of Persian and international phone numbers (`[REDACTED_PHONE]`).
   - Redaction of email addresses (`[REDACTED_EMAIL]`).
   - Redaction of OTP verification codes (`[REDACTED_OTP]`).

4. **Zero Unconsented Network Uploads:**
   - Crash logs remain 100% local on device.
   - Users must explicitly opt-in via Settings before any diagnostic log export occurs.
