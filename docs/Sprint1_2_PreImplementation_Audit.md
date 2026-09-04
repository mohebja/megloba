# Sprint 1.2 — Pre-Implementation Audit Report

**Project Name:** Global SMS (`com.global.sms`)  
**Sprint:** 1.2 — Advanced Contact Management & Message Composer  
**Date:** August 2, 2026  
**Status:** **AUDIT COMPLETE — PREPARED FOR SPRINT 1.2 EXECUTION**  

---

## 1. Existing System Inspection

### Contact Subsystem
- **Current Helpers:** `ContactPermissionHelper.kt`, `PersianContactUtils.kt`, `PhoneNumberNormalizer.kt`, `ContactManager.kt`, `ContactCacheManager.kt`, `ContactRepository.kt`.
- **Gaps Identified:** Needs `ContactPermissionManager.kt` with explicit state model (`NOT_REQUESTED`, `GRANTED`, `DENIED`, `PERMANENTLY_DENIED`), settings redirect helper, and Persian rationale dialog state.
- **Search Engine:** `PhoneNumberNormalizer` handles Persian/Arabic digits (۰-۹ -> 0-9). `PersianContactUtils` normalizes Persian characters (ك -> ک, ي -> ی). Needs enhanced LRU caching for 10,000+ contacts query optimization.

### Database & Entities
- **Current Entities:** `ContactEntity`, `ContactGroupEntity`, `ContactGroupMemberEntity` exist in `:database`.
- **Gaps Identified:** Ensure room DAOs (`ContactDao`, `ContactGroupDao`) expose reactive `Flow` methods for group management, group member insertions, and search queries.

### Message Composer & SMS Engine
- **Current Composer:** `MultiContactComposeScreen.kt` and `MessageThreadScreen.kt`.
- **Gaps Identified:** Add precise character & SMS segment counter (160 GSM / 70 Unicode threshold), multipart calculation preview ("Message will be sent as X SMS"), recipient chips with deletion, group selection sheet, and Dual SIM selector integration.

### Three UI Systems
- **Classic UI:** `com.global.sms.ui.classic` — Needs recipient chips, contact picker bottom sheet, and group selector.
- **Smart AI UI:** `com.global.sms.ui.smart` — Needs AI-recommended frequent contact suggestions.
- **Enterprise UI:** `com.global.sms.ui.enterprise` — Needs customer group selection, bulk dispatch integration, and recipient analytics.

---

## 2. Sprint 1.2 Implementation Action Plan

1. **Create `ContactPermissionManager.kt`**: State-driven permission helper with settings redirect.
2. **Upgrade `GroupManagementRepository.kt`**: Full CRUD for native and custom contact groups.
3. **Enhance Search Engine & Cache**: In-memory LRU cache + Coroutine Flow for 10,000+ contact search.
4. **Upgrade Message Composer**: Recipient chips, group selector, character/segment counter, Unicode detection.
5. **Connect Group SMS Engine**: Dispatch to `SmsSender` and `SmsQueueManager` across selected SIM slots.
6. **Update Three UI Modes**: Classic, Smart AI, Enterprise views.
7. **Create Unit & Performance Tests**: `ContactSearchTest`, `ContactNormalizationTest`, `ContactGroupTest`, `PermissionFlowTest`.
8. **Generate Sprint 1.2 Architecture Documentation**.
