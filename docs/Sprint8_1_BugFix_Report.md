# Sprint 8.1 — Final Bug Fix Round Report

**Project:** Global SMS (`com.global.sms`)  

---

## 1. Resolved Issues in Sprint 8.1

| Issue ID | Area | Root Cause / Description | Resolution Applied | Status |
| :--- | :--- | :--- | :--- | :--- |
| **BUG-801** | `ProductionCrashReporter` | PII regex replacement omitted international Persian number prefix formats | Updated regex pattern to support `+98`, `09xx`, and formatted phone strings | **RESOLVED** |
| **BUG-802** | `PrivacyCenterScreen` | Unmerged composable semantics tag missing for automated UI test runner | Added explicit `testTag` properties to privacy action buttons | **RESOLVED** |
| **BUG-803** | `LocalFeatureConfigEngine` | Default flag state reset on process termination | Added local preference backing store to retain toggle selections across reboot | **RESOLVED** |
