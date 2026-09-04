# 📝 Changelog

All notable changes to the Global SMS Android application across development sprints are documented in this file.

## [1.0.0-PROD] - 2026-08-20

### Security & Cryptographic Integrity
- **Hardware-Backed Field Encryption:** Replaced full-database encryption claims with hardware-backed AES-256 GCM field-level encryption for sensitive entity fields (`body`, `contactName`, `snippet`).
- **Zero-Trust Master Key Isolation:** Integrated Android KeyStore with hardware TEE/StrongBox backing where available; hardened against software fallback outside JVM testing harnesses.
- **Robust Encrypted Backups:** Hardened `BackupProvider` and `ProfessionalBackupEngine` to require explicit constructor key injection, non-deterministic `SecureRandom` 16-byte salt, and 12-byte IV per backup creation under container format `GSMS` (v1). Retained backward compatibility decode path for legacy archives.
- **Private Vault Isolation:** Password-protected local storage utilizing PBKDF2 key derivation and AES-256 GCM encryption for vaulted messages.

### SMS Engine & Architecture
- **Dual SIM Management:** Centralized telephony detection in `:core` module (`DualSimManager`, `SimPermissionManager`) with backward-compatible delegate mappings in `:sms-engine`.
- **Message Dispatcher & Reliability:** Integrated WorkManager-backed exponential backoff retry policies and safe headless SMS/MMS services compliant with Google Play Default SMS Handler requirements.
- **Log Data Sanitization:** Replaced ad-hoc string slicing with unified `FieldEncryptionManager.redactedForLog` across background workers, schedulers, and receivers.
- **Exception Handling Hardening:** Eliminated raw `printStackTrace()` calls across `core`, `database`, `security`, `sms-engine`, and `ui` modules in favor of structured Android `Log` logging.

### Documentation & Repository Hygiene
- Pruned redundant sprint report and certification artifacts from the repository root.
- Consolidated architectural and compliance documentation (`ARCHITECTURE.md`, `SECURITY_DOCUMENTATION.md`, `GOOGLE_PLAY_COMPLIANCE.md`, `DATA_SAFETY.md`).
