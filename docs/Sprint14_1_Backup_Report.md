# Sprint 14.1 — Backup Report

## 1. Backup Metadata
* **Archive Path:** `/backup/Sprint14_1_before_release_gate.zip`
* **Timestamp:** 2026-08-13T15:14:47Z
* **Archive Size:** 34,848,651 bytes (~34.8 MB)
* **Format:** ZIP (DEFLATE compression, standard Python 3 `zipfile` engine)
* **Verification Status:** Verified readable, checksum validated.

## 2. Inclusions & Directory Coverage
The backup archive includes all source modules, configurations, manifests, database schemas, assets, tests, and documentation:
1. `:app` — Application root, MainActivity, navigation controllers, dependency injection, application class, and ProGuard / R8 rules.
2. `:core` — Domain models, AI copilot engines, sync protocols, onboarding flows, accessibility managers, localization engines, licensing systems, and benchmarking suites.
3. `:database` — Room v29 entities, DAOs, converters, migration definitions (`MIGRATION_1_2` through `MIGRATION_28_29`), and room export schemas.
4. `:security` — Keystore integration, AES-256-GCM encryption wrappers, biometric auth gates, zero-trust vault engines.
5. `:settings` — User preferences, theme managers, dual SIM configurations, and granular privacy controls.
6. `:sms-engine` — Default SMS handlers, Telephony receivers, delivery report processors, headless services, and WAP-PUSH MMS decoders.
7. `:ui` — Jetpack Compose design system (M3), Three UI systems (Classic, Smart AI, Enterprise Professional), dynamic typography, and animations.
8. `gradle/` & `*.gradle.kts` — Multi-module build configurations, Version Catalog (`libs.versions.toml`), and plugin setups.
9. `schemas/` — Room schema JSON files for compile-time schema verification.
10. `docs/` — Architectural specifications, audit logs, and sprint documentation.

## 3. Exclusion Rules
Transient build outputs and caches were strictly excluded to keep the backup clean and reproducible:
* `*/build/*`
* `*/.gradle/*`
* `*/kspCaches/*`
* `*/.transforms/*`

## 4. Integrity Check
* MD5 / SHA-256 integrity check performed upon creation.
* Archive can be restored with standard unpack commands.
