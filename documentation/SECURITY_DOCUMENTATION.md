# Global SMS — Comprehensive Security & Cryptographic Architecture

## 1. Overview
The Global SMS application is engineered according to Zero-Trust and Defense-in-Depth principles. All sensitive communications, authentication secrets, message payloads, and database tables are protected both at rest and in transit.

---

## 2. Cryptographic Architecture
### 2.1 Hardware-Backed Key Storage (Android KeyStore)
- **Provider:** `AndroidKeyStore`
- **Master Key Alias:** `GlobalSmsMasterKey_AES256`
- **Backup Key Alias:** `AutoBackupMasterKey_AES256`
- **Specification:** AES-256 in Galois/Counter Mode (GCM) with 128-bit authentication tag (`AES/GCM/NoPadding`).
- **Hardware Isolation:** Protected by StrongBox Keymaster / Trusted Execution Environment (TEE) on supported devices.

### 2.2 Database Encryption (SQLCipher)
- All Room database tables (`messages`, `conversations`, `crm_customers`, `bulk_jobs`, `audit_logs`) are transparently encrypted with SQLCipher using 256-bit AES encryption.
- Passphrase keys are derived at startup via hardware-backed encryption keys and never saved in cleartext or memory dumps.

### 2.3 Secure Preferences & Vault
- Uses `EncryptedSharedPreferences` backed by `MasterKey` (`AES256_SIV` for keys, `AES256_GCM` for values).
- Passcodes and PIN codes are hashed using PBKDF2WithHmacSHA256 with salted multi-round iterations.

### 2.4 Encrypted Backup Architecture
- **Format:** Custom `GSMS` authenticated encrypted format.
- **Key Derivation:** PBKDF2WithHmacSHA256 (21,000 iterations, 16-byte random salt).
- **Cipher:** AES-256-GCM with random 12-byte IV per backup archive.
- **Auto-Backup:** Periodic hardware-encrypted backups without plaintext leakage.

---

## 3. Application Protection & Runtime Security
1. **Screen Capture & Task Hijacking:** `FLAG_SECURE` enforced to prevent screenshots and task snapshot exposure.
2. **Secure Clipboard:** Sensitive tokens and OTPs cleared automatically after 30 seconds.
3. **Phishing & Malicious Link Detection:** Local offline heuristic domain parser preventing USSD injection and homograph domain spoofing.
4. **Tamper & Root Detection:** Runtime integrity validation via `AdvancedAppProtection`.
