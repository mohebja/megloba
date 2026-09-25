# Global SMS — Comprehensive Security & Cryptographic Architecture

---

## 1. Zero-Trust & Defense-in-Depth Overview

Global SMS enforces strict Zero-Trust and Defense-in-Depth principles across all architectural layers. User messages, contact lists, financial transactions, and configuration tokens are strictly kept on the user's local hardware without unconsented remote transmission.

---

## 2. Cryptographic Architecture

### 2.1 Hardware-Backed Key Storage (Android KeyStore)
- **Key Provider:** `AndroidKeyStore`
- **Master Key Alias:** `GlobalSmsMasterKey_AES256`
- **Backup Key Alias:** `AutoBackupMasterKey_AES256`
- **Algorithm & Mode:** AES-256 in Galois/Counter Mode with 128-bit authentication tag (`AES/GCM/NoPadding`).
- **Hardware Isolation:** Backed by Secure Element (TEE) and StrongBox Keymaster where available on modern hardware.

### 2.2 Secure Private Vault
- **Key Derivation:** PBKDF2WithHmacSHA256 (21,000 iterations, 16-byte cryptographically secure random salt).
- **Authentication Methods:** User Master Passphrase and Android `BiometricPrompt` (Fingerprint, Face Unlock with Class 3 strong biometrics).
- **Storage Protection:** Vaulted items are stored in isolated encrypted rows with non-deterministic IVs per message.

### 2.3 Authenticated Encrypted Backup Architecture (GSMS Container)
- **Container Format:** Custom authenticated `GSMS` v1 binary structure.
- **Header:** Magic bytes `GSMS` (0x47, 0x53, 0x4D, 0x53), format version `1`, 16-byte salt, 12-byte initialization vector (IV).
- **Payload Cipher:** AES-256-GCM with integral authentication tag preventing tampering or partial archive corruption.
- **Auto-Backup Engine:** WorkManager periodic tasks scheduled with battery and idle constraints, isolated from headless test runners.

---

## 3. Runtime Protection & Threat Hardening

1. **Screen Capture & Task Hijacking (`FLAG_SECURE`):**
   - Applied to `PrivateVaultActivity` and sensitive screens to prevent snapshot leakage in the Recent Apps switcher.
2. **Secure Auto-Clearing Clipboard:**
   - One-time passwords (OTP) and extracted sensitive credentials copied to the system clipboard are purged automatically after 30 seconds.
3. **Phishing & Malicious Link Detection:**
   - On-device heuristic scanner detects lookalike characters (homograph spoofing), deceptive IP-based URLs, and rogue USSD execution codes (`*...#`).
4. **Log Data Sanitization:**
   - Raw phone numbers and message contents are scrubbed via `FieldEncryptionManager.redactedForLog()` before emitting system logcat messages.
5. **Headless Environment Isolation:**
   - Background WorkManager trackers check `isTestEnvironment()` to guarantee zero receiver crashes during Robolectric automated verification.
