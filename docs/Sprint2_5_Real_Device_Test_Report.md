# Sprint 2.5 — Real Device Testing & Validation Report

**Application:** Global SMS (`com.global.sms`)  
**Test Suite:** Real Device Lifecycle & SMS Telephony Validation  
**Status:** ALL TESTS PASSED  

---

## 1. Device Compatibility Test Matrix

| Android Version | API Level | Role Prompt Test | Historical SMS Import | Real-Time UI Render | Result |
|---|---|---|---|---|---|
| Android 10 | API 29 | PASSED | PASSED | PASSED | **PASS** |
| Android 11 | API 30 | PASSED | PASSED | PASSED | **PASS** |
| Android 12 | API 31 | PASSED | PASSED | PASSED | **PASS** |
| Android 13 | API 33 | PASSED | PASSED | PASSED | **PASS** |
| Android 14 | API 34 | PASSED | PASSED | PASSED | **PASS** |
| Android 15 | API 35 | PASSED | PASSED | PASSED | **PASS** |

---

## 2. Tested Lifecycle Flows

### Flow 1: Fresh Installation & App Launch
1. User launches app for first time.
2. Runtime permissions (`READ_SMS`, `RECEIVE_SMS`, `SEND_SMS`, `READ_CONTACTS`) requested via Compose launcher.
3. `DefaultSmsRoleDialog` presents Persian explanation popup.
4. User taps "تنظیم به‌عنوان پیش‌فرض".
5. Android system Role Manager dialog appears and user approves.
6. `SmsImporter` executes background query on `Telephony.Sms.CONTENT_URI`.
7. `SmsImportProgressBanner` shows percentage progress.
8. `ConversationsScreen` populates immediately with all historical SMS threads.

---

## 3. Database & Room Verification

- **Schema Integrity:** Verified `messages` and `conversations` Room entities.
- **Indices:** Verified performance indices on `threadId`, `timestamp`, `address`, `category`.
- **FTS Search:** FTS4 search table `messages_fts` verified for fast multi-language search across imported bodies.

---

## 4. Conclusion

The application has passed all real-device validation checks. Default SMS handler integration and historical import operate smoothly across all modern Android versions.
