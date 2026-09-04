# Sprint 14.2 — Private Vault & Cryptography Security Report

## 1. Cryptographic Specifications
* **Encryption Algorithm:** AES-256-GCM (Galois/Counter Mode) with 128-bit authentication tag.
* **Key Derivation & Storage:** Android Keystore Hardware Security Module (StrongBox / TEE backed where available).
* **Key Alias:** `com.global.sms.vault.master_key`
* **Non-Exportable:** Master key cannot be extracted from device memory or filesystem.

## 2. Defense-in-Depth Mechanisms
* **FLAG_SECURE:** Active on all Private Vault composables; prevents system screenshots and obscures multitasking task previews.
* **Biometric Authentication:** Integrates `androidx.biometric.BiometricPrompt` with `BIOMETRIC_STRONG` and device credential PIN/Passcode fallback.
* **Auto-Lock Timeout:** Vault locks automatically after 30 seconds of inactivity or upon application backgrounding.
* **Log Redaction:** Phone numbers, OTP codes, bank card numbers, and message bodies are sanitized before any logging.
