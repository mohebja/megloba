# Global SMS — Settings Center Finalization Report

**Project Name:** Global SMS (`com.global.sms`)  
**Review Date:** August 2, 2026  
**Lead:** Product Manager & Settings Architecture Lead  

---

## 1. Enterprise Settings Center Structure

The `:settings` module provides persistent configuration options managed via Android Jetpack **DataStore Preferences**.

### 1.1 Categories & Configuration Controls

| Category | Options & Switches | Reset & Defaults |
| :--- | :--- | :---: |
| **Appearance** | Theme Mode (System/Light/Dark/AMOLED), Custom Accent Colors, Font Family (Vazirmatn/System), Text Size Scale (80%-150%) | Reset Theme Defaults |
| **Messaging** | SMS Center (SMSC) address, Auto-delete old messages, Delivery Reports toggle, Character Counter, MMS APN settings | Reset Telephony Defaults |
| **Contacts** | Auto-sync contacts, Display order (First/Last), Persian number normalization toggle | Reset Contact Rules |
| **AI Features** | Auto-classification enable/disable, OTP quick copy popup, Smart replies toggle, Category filter chips, Voice Assistant TTS | Reset AI Engine Rules |
| **Security** | App Lock PIN/Pattern/Biometric, Private Vault auto-lock timeout (15s/30s/1m), Screenshot Protection (`FLAG_SECURE`) | Clear PIN / Security Reset |
| **Privacy** | Incognito mode, Hide message previews in notifications, Wiping clipboard timeout (15s/30s/45s) | Reset Privacy Defaults |
| **Backup** | Local AES-256 backup creation, Automated daily backups, Restore from file, Backup password management | Clear Local Backups |
| **Notifications** | Sound, Vibration patterns, Category-specific notification channels (OTP, Banking, General, Spam) | Reset Channel Configs |
| **Advanced** | FTS Search Index rebuild, Database vacuum/compact tool, Export debug logs, Factory Reset | Factory System Reset |

---

## 2. Architecture & DataStore Persistence

All setting changes update `DataStore<Preferences>` asynchronously via Kotlin Flow. Changes apply instantly across the UI without requiring an application restart.

---

## 3. Settings Center Verdict

**Status:** ✅ **APPROVED FOR PRODUCTION RELEASE.**
