# ✅ Global SMS - Production Release Checklist (چک‌لیست نهایی انتشار)

---

### Phase 1: Code & Build Verification
- [x] Set `versionCode = 100` and `versionName = "1.0.0"` in `app/build.gradle.kts`.
- [x] Configure R8 / ProGuard rules in `app/proguard-rules.pro` (Room, Moshi, Coroutines, WorkManager, Coil).
- [x] Configure production signing block in `app/build.gradle.kts`.
- [x] Validate build compilation via `compile_applet`.

### Phase 2: Performance & Scalability Benchmark
- [x] Enable SQLite Write-Ahead Logging (WAL) and 256MB memory mapping.
- [x] Implement Paging 3 in `ConversationDao` and `MessageDao`.
- [x] Verify LazyColumn keying (`itemKey`) for fast Recomposition.
- [x] Test 500,000 SMS dataset scalability via `PerformanceReportManager`.

### Phase 3: Security & Privacy Compliance
- [x] Verify 100% on-device local data processing (Zero cloud transmission).
- [x] Generate `PRIVACY_POLICY.md` & `DATA_SAFETY.md`.
- [x] Implement `GlobalCrashHandler` with automatic PII redaction.
- [x] Verify Biometric Authentication lock for Private Vault.

### Phase 4: Google Play Store Submission Assets
- [x] Complete Permissions Justification document (`PERMISSIONS_EXPLANATION.md`).
- [x] Review Google Play Compliance (`GOOGLE_PLAY_COMPLIANCE.md`).
- [x] Draft Release Notes (`RELEASE_NOTES.md`).
- [x] Verify App Icon adaptive vectors (`ic_launcher`).
