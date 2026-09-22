# Database encryption

The Room database (`global_sms_encrypted_db`) is encrypted **as a whole file** with SQLCipher
(AES-256, per-page HMAC). Nothing about individual rows changes for the rest of the code.

## How it works

| Piece | Where | Role |
|---|---|---|
| `DatabaseKeyStore` | `:database` `data/db/crypto` | Creates a random 256-bit key, wraps it with a non-exportable Android Keystore key (AES-256-GCM) and stores the wrapped blob in `noBackupFilesDir`. |
| `DatabaseEncryption.prepare()` | `:database` | Called by `GlobalSmsDatabase.getInstance()`. Returns the `SupportOpenHelperFactory` Room must use and migrates legacy databases. |
| `LegacyFieldDecryptionMigration` + `Worker` | `:core` `core/security` | One-time background conversion of old `enc:v1:` fields back to plain text. |
| `tools/verify_sqlcipher_export.py` | repo root | Replays the export SQL against a real SQLCipher build. |

The Keystore key is **not** tied to biometrics or to the device being unlocked: incoming SMS must be stored
while the phone is locked. The key never leaves the device, and the database is excluded from cloud backup
and device-to-device transfer (a copy could not be opened anyway).

## Upgrade path for existing installs

1. `prepare()` finds a plaintext SQLite file (`SQLite format 3` header).
2. It checkpoints the WAL, snapshots the schema and row counts, and exports into `<db>.enc.tmp`
   (`ATTACH ... KEY ''` + `sqlcipher_export`).
3. It re-opens the copy with the key and checks: not plaintext, `user_version`, `PRAGMA quick_check`,
   identical schema objects (tables, FTS shadow tables, indexes, triggers) and identical row counts.
4. Only then: `db -> db.plain.bak`, `db.enc.tmp -> db` (two renames), and the backup is overwritten and deleted.
   If the process dies between the renames, the next start puts the plaintext file back and retries.
5. If anything fails the app keeps using the plaintext file (no data loss) and retries on the next start,
   at most 3 times. `DatabaseEncryption.status` and `ZeroTrustSecurityLayer.auditEncryptionState()` report
   `MIGRATION_FAILED_UNENCRYPTED` in that case.

The first call to `getInstance()` after the upgrade does this work synchronously. `GlobalSmsApp` triggers it
on a background thread at startup; a very large history (100k+ messages) can take several seconds.

## Field-level encryption is gone

Earlier versions stored message bodies, contact names and snippets as `enc:v1:` ciphertext in a plaintext file,
and no screen decrypted them. That was replaced by whole-file encryption. `FieldEncryptionManager.encryptXxx`
are now pass-through shims; `decryptXxx` still understand `enc:v1:`. `LegacyFieldDecryptionMigration` converts the
old rows (conversations first, then messages newest-first, in 400-row transactions) and re-indexes full-text
search through Room's content-sync triggers. Rows whose ciphertext can no longer be decrypted are left untouched.

## Key loss

If the database is encrypted but its key can never be unwrapped again (Keystore alias gone, authentication tag
mismatch), the unreadable database is deleted and a fresh one is created; the SMS import repopulates from the
system provider when the database is empty. `DatabaseEncryption.consumeKeyLostNotice()` returns `true` once so
the UI can tell the user. Transient Keystore errors are *not* treated as key loss: they surface as exceptions.

## Testing

* Host JVM (unit tests): SQLCipher's native library is unavailable, so `prepare()` returns `null` and
  `status == UNAVAILABLE_ON_HOST_JVM`; Room uses the normal SQLite.
* Device/emulator: `./gradlew :app:connectedDebugAndroidTest --tests "*DatabaseEncryptionMigrationTest"`
  (legacy migration, crash recovery, fresh install).
* SQL sequence against real SQLCipher: `pip install sqlcipher3-binary && python3 tools/verify_sqlcipher_export.py`.
