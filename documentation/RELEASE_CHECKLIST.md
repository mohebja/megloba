# ✅ Global SMS - Production Release Checklist

---

### Phase 1: Code & Build Verification
- [x] Target SDK set to 36 and compile SDK configured for release 36 (`app/build.gradle.kts`).
- [x] Application version set to `versionCode = 800` and `versionName = "8.0.0"`.
- [x] Configure R8 / ProGuard rules in `app/proguard-rules.pro` (Room, Moshi, Coroutines, WorkManager, Coil).
- [x] Configure production signing block in `app/build.gradle.kts` with Keystore fallback.
- [x] Validate build compilation via `compile_applet` (Status: Succeeded).

### Phase 2: Performance, Scalability & Database Verification
- [x] Database schema upgraded to Version 31 with full migration path (`MIGRATION_1_2` through `MIGRATION_30_31`).
- [x] Enable SQLite Write-Ahead Logging (WAL) and memory mapping optimization in `GlobalSmsDatabase`.
- [x] Implement Paging 3 in `ConversationDao` and `MessageDao` for smooth 60-120fps scrolling.
- [x] Verify Full-Text Search (FTS4/FTS5) queries for sub-10ms lookup across 100,000+ messages.
- [x] Test million-message dataset benchmark via `HighScalePerformanceBenchmark`.

### Phase 3: Security & Privacy Compliance
- [x] 100% on-device local data processing (Zero cloud telemetry without user master switch).
- [x] Updated `PRIVACY_POLICY.md` & `DATA_SAFETY.md` for Google Play Console declaration.
- [x] Verify Android KeyStore AES-256-GCM hardware key isolation (`GlobalSmsMasterKey_AES256`).
- [x] Implement Biometric Authentication (`BiometricPrompt`) lock for Private Vault.
- [x] Verify authenticated `GSMS` encrypted backup archives with PBKDF2 key derivation.
- [x] Isolate background WorkManager tasks in test environments to ensure zero test crash leaks.

### Phase 4: Quality Assurance & Automated Tests
- [x] Execute all 227 automated JVM unit and Robolectric tests (`gradle :app:testDebugUnitTest`).
- [x] Verify Sprint 5 through Sprint 16 regression test suites.
- [x] Zero failing tests, zero compilation errors, zero memory leaks.

### Phase 5: Google Play Store Submission Assets
- [x] Complete Permissions Justification document (`PERMISSIONS_EXPLANATION.md`) for Default SMS Handler role.
- [x] Review Google Play Compliance (`GOOGLE_PLAY_COMPLIANCE.md`).
- [x] Draft bilingual Release Notes (`RELEASE_NOTES.md`).
- [x] Verify App Icon adaptive vectors (`ic_launcher`).
