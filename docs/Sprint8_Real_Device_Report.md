# Sprint 8 — Real Device Validation Report

**Project:** Global SMS (`com.global.sms`)  

---

## 1. Verified Device Fleet

1. **Poco X3 NFC (Android 12 / MIUI 13):**
   - Verified default SMS handler registration, autostart background receipt, and dual SIM dispatch.
2. **Samsung Galaxy (Android 14 / One UI 6):**
   - Verified edge-to-edge window insets, predictive back navigation, and biometric vault unlock.
3. **Google Pixel (Android 15 / Stock):**
   - Verified targetSdk 36 compatibility, material dynamic color support, and notification permission cards.

---

## 2. Functional Test Lifecycle
- **Fresh Installation:** PASSED
- **Default SMS Role Prompt:** PASSED
- **Telephony SMS Import:** PASSED
- **Sending & Receiving Messages:** PASSED
- **Offline AI Copilot Processing:** PASSED
- **Private Vault Biometric Isolation:** PASSED
- **Encrypted Local Backup & Restore:** PASSED
