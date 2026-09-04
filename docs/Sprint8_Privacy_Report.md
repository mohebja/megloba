# Sprint 8 — Data Safety & Privacy Center Report

**Project:** Global SMS (`com.global.sms`)  
**Component:** `PrivacyCenterScreen.kt`  

---

## 1. Privacy Center Overview
The `PrivacyCenterScreen.kt` provides complete user transparency regarding local data handling, encryption protocols, and zero-cloud processing.

---

## 2. Privacy Center Feature Matrix

1. **Local Storage Guarantee:** Display indicator confirming all SMS messages remain strictly inside local Room SQLite databases.
2. **Offline AI Verification:** Status badge confirming `LocalAIBrain` processes natural language and OTP extraction completely offline.
3. **AES-256-GCM Status:** Indicator verifying KeyStore-backed encryption for database assets and Private Vault items.
4. **Vault Protection Status:** Biometric lock status and exclusion from system search indices.
5. **Export Privacy Report:** Button (`export_privacy_report_button`) allowing users to export a localized JSON summary of data safety metrics.

---

## 3. Play Store Data Safety Declaration
- **Data Collected:** None.
- **Data Shared:** None.
- **Data Encrypted in Transit:** N/A (Offline application).
- **Data Encrypted at Rest:** Yes (AES-256-GCM via Android KeyStore).
