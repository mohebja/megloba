# Global SMS — Enterprise Security & Privacy White Paper

**Project Name:** Global SMS (`com.global.sms`)  
**Version:** 1.5.0  
**Date:** August 2, 2026  

---

## 1. Zero-Trust Local Architecture

Global SMS implements a **Zero-Trust Local Mobile Security Architecture**. In an era of widespread SMS interception and remote privacy leakage, Global SMS guarantees that **no message body, contact entry, or metadata is ever transmitted to remote cloud servers**.

---

## 2. Cryptographic Specifications

1. **Storage Encryption:** Databases and backup archives are encrypted using **AES-256-GCM** with 128-bit authentication tags.
2. **Key Protection:** Cipher keys are generated inside the hardware-backed **Android KeyStore** using 256-bit AES master keys protected by device lock credentials.
3. **Password Derivation:** User backup keys are derived using **PBKDF2WithHmacSHA256** with 10,000 salt iterations.

---

## 3. Defense Against System Vulnerabilities

- **Screen Capture Protection:** `WindowManager.LayoutParams.FLAG_SECURE` blocks screenshot capture and screen recording during Private Vault viewing.
- **Biometric Gatekeeping:** Android `BiometricPrompt` enforces hardware-authenticated Fingerprint/Face ID verification before unlocking hidden vaults.
- **Clipboard Wiping:** Copied OTP values are auto-expunged from memory after 45 seconds.
