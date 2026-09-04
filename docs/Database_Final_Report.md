# Global SMS — Database Production & Migration Report

**Project Name:** Global SMS (`com.global.sms`)  
**Review Date:** August 2, 2026  
**Lead:** Database Administrator & Room Architecture Lead  

---

## 1. Database Architecture & Schema Overview

Global SMS uses **Room Database** with SQLite storage. High-performance composite indexes and encrypted backup serialization are enabled.

### Primary Database Entities
1. **`MessageEntity`** (`messages` table): Primary SMS/MMS log holding address, body, timestamp, threadId, status, category, otpCode, spamScore, and isHidden fields.
2. **`ThreadEntity`** (`threads` table): Conversation summaries holding snippet, lastTimestamp, unreadCount, and draft.
3. **`ContactEntity`** (`contacts` table): Local contact cache with Iranian number normalization keys (`normalizedAddress`).
4. **`RuleEntity`** (`classification_rules` table): User-defined category rules.
5. **`ScheduledSmsEntity`** (`scheduled_sms` table): Outgoing queue with execution timestamps and retry counts.

---

## 2. Room Migration Paths

Database schema evolution is strictly tested from Schema Version 1 through Current Version:

```
Version 1 -> Version 2: Added `category` and `otpCode` columns to `messages`.
Version 2 -> Version 3: Added `classification_rules` table and `spamScore` column.
Version 3 -> Version 4: Added `isHidden` column and Private Vault index.
```

### Migration Code Verification
- All migrations use explicit SQL `ALTER TABLE` statements without destructive fallback (`fallbackToDestructiveMigration()` is **DISABLED** for production builds to preserve user data).

---

## 3. Database Backup & Integrity Validation

- **Encryption:** Backup archives produced by `EncryptedBackupManager` serialize Room database tables into JSON encrypted via **AES-256-GCM** using a user password derived via **PBKDF2WithHmacSHA256** (10,000 iterations).
- **Integrity Check:** Backup payloads include SHA-256 integrity checksums to prevent corrupted restores.

---

## 4. Production Readiness

**Status:** ✅ **APPROVED FOR PRODUCTION (Zero Data Loss Migration Verified).**
