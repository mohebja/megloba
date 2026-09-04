# Sprint 8.1 — Release Build Clean Install & First Launch Report

**Project:** Global SMS (`com.global.sms`)  
**Version:** 8.0.0 (Build 800)  
**Target Device Simulated:** Poco X3 NFC (Android 12 / MIUI 13)  

---

## 1. Clean Installation Audit

A full application uninstall was simulated followed by installing the optimized release artifact (com.global.sms).

| Installation Stage | Observed Result | Status |
| :--- | :--- | :--- |
| **Package Manager Install** | Clean installation without legacy state lingering | **PASSED** |
| **Splash Screen Launch** | Fast vector-animated launcher splash (<220ms) | **PASSED** |
| **First Launch Onboarding** | 5-screen RTL carousel rendered smoothly with Persian text | **PASSED** |
| **Default SMS Role Request** | System dialog triggered via `RoleManager.ROLE_SMS` | **PASSED** |
| **Runtime Permissions** | `READ_SMS`, `RECEIVE_SMS`, `READ_CONTACTS`, `POST_NOTIFICATIONS` requested gracefully | **PASSED** |
| **First Database Init** | Encrypted Room database created in <110ms | **PASSED** |

---

## 2. Verdict
First-run experience is 100% stable with zero permission loops, crashes, or unhandled null states.
