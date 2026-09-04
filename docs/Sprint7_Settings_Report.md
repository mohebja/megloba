# Sprint 7 — Advanced Settings Redesign Report

**Project:** Global SMS (`com.global.sms`)  
**Component:** `ProfessionalSettingsDashboard.kt`, `SettingsRepository.kt`  
**Date:** 2026-08-06  

---

## 1. Overview
The settings hierarchy has been reorganized into clear categorical domains with search capability, backup/restore features, and reset controls.

---

## 2. Categorical Settings Matrix
1. **۱. ظاهر و پوسته (Appearance):** Custom color themes, font scaling, bubble background choices.
2. **۲. تنظیمات پیام‌رسانی (Messaging):** SMSC configuration, dual SIM defaults, delivery report toggles, character encoding.
3. **۳. هوش مصنوعی و خودکارسازی (AI & Automation):** Smart Reply V3 rules, offline task extraction, speech-to-text.
4. **۴. مدیریت مخاطبین و دسته‌بندی (Contacts & Categories):** Group managers, auto-categorization rules.
5. **۵. امنیت و گاوصندوق (Security & Private Vault):** Biometric PIN authentication, AES-256 chat vault.
6. **۶. پشتیبان‌گیری و داده‌ها (Backup & Data):** Encrypted local JSON/SQLite export and import.
7. **۷. زبان و تقویم (Localization):** Persian calendar toggles, Persian/English digits choice.

---

## 3. Testability
- Each category card and row contains explicit `testTag` attributes (e.g. `settings_cat_appearance`, `settings_cat_messaging`).
