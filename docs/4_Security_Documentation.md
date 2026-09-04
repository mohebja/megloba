# 4. Security Documentation — Global SMS

**Project:** Global SMS (`com.global.sms`)  

---

## 1. Cryptographic Security Standards

- **AES-256-GCM:** Used for all encrypted operations (Private Vault content, database keys, backup ZIP exports).
- **Android Keystore System:** Master cryptographic keys (`GlobalSmsMasterKey`) are generated inside the Android Keystore hardware security module (HSM).
- **PBKDF2 Key Derivation:** User PINs are hashed using PBKDF2 with HMAC-SHA256 and a 16-byte random salt (10,000 iterations).

---

## 2. Vault & System Safeguards

1. **BiometricPrompt Integration:** Hardware-backed fingerprint and facial recognition.
2. **FLAG_SECURE Enforcement:** `ScreenshotProtectionManager` applies `FLAG_SECURE` to prevent screen capture or recents preview thumbnails.
3. **Notification Privacy:** Vault contact notifications suppress sender and text body.
4. **Root & Tamper Detection:** Checks for binary anomalies and test key signatures on app launch.
