# Phase 8 — Crash & Error Management Report

**Project Name:** Global SMS (`com.global.sms`)  
**Audit Date:** August 2, 2026  
**Auditor:** Quality Assurance & Reliability Lead  

---

## 1. Global Crash Handler Architecture

- **Uncaught Exception Interception:** `GlobalCrashHandler` implements `Thread.UncaughtExceptionHandler`.
- **Safe State Recovery:** When an unexpected runtime exception occurs, the crash handler logs sanitised diagnostic data locally to encrypted log storage (`crash_logs.txt`) and safely restarts the main launcher activity rather than displaying a raw system crash dialog.
- **ANR (Application Not Responding) Prevention:** All Room database I/O, backup exports, and SMS network dispatch operations are enforced on `Dispatchers.IO` / `Dispatchers.Default` background coroutines. Zero blocking I/O calls executed on Main UI Thread.

---

## 2. Sensitive Data Filtering in Logs

- **PII / Message Body Protection:** `GlobalCrashHandler` and `Log` wrappers automatically strip telephone numbers, security OTP codes, and message body strings from crash stack traces.
- **Production Build Log Elimination:** ProGuard rules remove verbose debug log calls (`android.util.Log.v`, `android.util.Log.d`) from release binaries.
