# Sprint 14.1 — Private Vault Security & Cryptography Audit

## 1. Cryptographic Specifications
* **Encryption Algorithm:** AES-256-GCM (Galois/Counter Mode) with 128-bit authentication tag.
* **Key Derivation & Storage:** Android Keystore Hardware Security Module (StrongBox / TEE backed where available).
* **Key Alias:** `com.global.sms.vault.master_key`
* **Non-Exportable:** Master key cannot be extracted from device memory or filesystem.

## 2. Real-Device Security Defenses (POCO X3 NFC / MIUI 13)
* **FLAG_SECURE:** Active on all Vault composables (`window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)`).
  * Screenshots blocked: System returns black screen / "Can't take screenshot due to security policy".
  * Recent Apps thumbnail masked: Displayed as blank privacy preview in MIUI multitasking task switcher.
* **Biometric Authentication:** Integrates `androidx.biometric.BiometricPrompt` with `BIOMETRIC_STRONG` and device credential PIN/Passcode fallback.
* **Auto-Lock Timeout:** Vault locks automatically after 30 seconds of inactivity or upon application backgrounding.

## 3. Penetration Testing & Tampering Simulation
1. Direct SQLite Inspection: Vault payload columns store ciphertext and initialization vectors (`iv`); plain text is completely inaccessible.
2. Keystore tampering: Corrupting encrypted blobs triggers `AEADBadTagException` cleanly without crashing, displaying an authentication error.
3. Cold-boot memory dumping: Plaintext decrypted buffers are cleared from memory immediately following UI disposal.
