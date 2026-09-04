# Sprint 16 — Rollback & Recovery Plan

## 1. Scope & Objective
Establish standardized rollback and incident recovery procedures for production store deployments and runtime edge cases.

## 2. Recovery Playbooks
1. **Google Play Bad Build Incident:**
   * Action: Roll back staged rollout in Google Play Console to previous stable release (v7.9.x) or increment versionCode to `801` with hotfix branch patch.
2. **Database Migration Failure:**
   * Action: SQLite transactions automatically abort. If unrecoverable corruption occurs, trigger user prompt to restore from the latest verified `.gsmsbak` file.
3. **SMS Role Revocation:**
   * Action: App detects loss of `RoleManager.ROLE_SMS` via `RoleManager.isRoleHeld()`, displays a non-blocking prompt explaining role benefits with direct intent to system default apps settings.
4. **Biometric Sensor Lockout:**
   * Action: Seamless fallback to Private Vault master passcode / PIN entry.
