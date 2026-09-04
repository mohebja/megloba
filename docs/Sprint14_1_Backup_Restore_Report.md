# Sprint 14.1 — Encrypted Backup, Restore & QR Migration Report

## 1. Backup Engine Architecture
* **Encryption Standard:** AES-256-GCM authenticated encryption with PBKDF2 (100,000 iterations + SHA-256) key derivation for password-protected files.
* **Integrity Validation:** SHA-256 package checksum embedded inside JSON manifest header.
* **Format:** Encrypted `.gsmsbak` file container.

## 2. Test Execution & Resilience
| Test Case | Scenario Description | Expected Outcome | Actual Outcome | Status |
|---|---|---|---|---|
| TC-BAK-01 | Full Encrypted Local Backup (50k msgs) | Exports clean encrypted package to Downloads | Generated in 2.1s, size: ~4.2 MB | PASS |
| TC-BAK-02 | Restore with Correct Password | Decrypts, verifies hash, restores all records | 100% records restored, 0 data loss | PASS |
| TC-BAK-03 | Restore with Wrong Password | Decryption fails gracefully | Throws invalid password dialog, 0 corruption | PASS |
| TC-BAK-04 | Tampered Backup File | Bit flipped in ciphertext blob | GCM auth tag mismatch detected instantly | PASS |
| TC-BAK-05 | QR Code P2P Migration | Generates compact encrypted payload token for camera scan | Scans and pairs instantly via P2P | PASS |
| TC-BAK-06 | Partial Restore (Contacts Only) | Restores only selected entity subset | Restores contacts without overwriting messages | PASS |
