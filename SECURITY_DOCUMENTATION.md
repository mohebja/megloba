# 🔒 Global SMS - Security Architecture & Audit Documentation

## 1. Cryptographic Standards
- **Hardware-Backed Field Encryption:** AES-256 GCM (`AES/GCM/NoPadding`) via Android KeyStore (`AndroidKeyStore`) with Master Key generation for transparent field-level encryption of conversation snippets, message bodies, and contact names in the local Room database (`FieldEncryptionManager`). Keys are non-exportable and bound to hardware-backed TEE/StrongBox where available.
- **Password-Derived Encryption (PBKDF2 + AES-256 GCM):** Utilizes PBKDF2 with HMAC-SHA256, 210,000 iterations, 16-byte random salt, and 12-byte random GCM IV for two distinct, dedicated use cases:
  1. **Private Vault Messages (`PrivateVaultSecurityManager`):** A distinct, dedicated security feature for secret conversations. Hidden message payloads (`isHidden = true`, `isEncrypted = true`) have their body content encrypted using AES-256 GCM with keys derived directly from the user's secret Vault passcode/PIN (210,000 PBKDF2 iterations), gated by PIN/Biometric authentication.
  2. **Standalone Backup Archives (`EncryptedBackupManager` / `ProfessionalBackupEngine`):** Standalone `.gsms` / JSON backup containers encrypted with 210,000 PBKDF2 iterations. By design, backup encryption is password-derived rather than hardware-bound to ensure secure portability across devices.

## 2. On-Device Privacy Guardrails
- **Zero Cloud Leakage:** All message parsing, classification, and analytics run 100% locally.
- **Crash Log Sanitize:** `GlobalCrashHandler` redacts phone numbers, bank account numbers, and card numbers before writing crash files.
- **Biometric Security:** `BiometricPrompt` verification enforces hardware owner authentication for vault access.
- **Clipboard Protection:** OTP copy actions clear or flag sensitive clipboard items.

## 3. Threat Matrix & Mitigations
| Threat Vector | Severity | Mitigation |
| :--- | :--- | :--- |
| Database Extraction | High | Field-level encryption for sensitive entity fields & password-derived AES-GCM encryption for Private Vault items (`isEncrypted = true`, `isHidden = true`). |
| Phishing SMS Links | High | On-device regex scan flags suspicious domains before user clicks. |
| Shoulder Surfing | Medium | Biometric lock for Vault & Screenshot Protection option. |
