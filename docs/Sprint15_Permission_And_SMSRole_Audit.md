# Sprint 15 — Permission & Default SMS Role Audit Report

## 1. Declared Permissions Audit
The app manifest strictly declares permissions required for a compliant Default SMS Application:
* `android.permission.RECEIVE_SMS` — Mandatory for receiving incoming SMS.
* `android.permission.SEND_SMS` — Mandatory for dispatching user SMS.
* `android.permission.READ_SMS` — Mandatory for reading SMS thread history.
* `android.permission.RECEIVE_MMS` & `RECEIVE_WAP_PUSH` — Mandatory for WAP-push MMS.
* `android.permission.READ_CONTACTS` — Optional/runtime with graceful fallback to raw phone numbers.
* `android.permission.POST_NOTIFICATIONS` — Android 13+ runtime notification permission.
* `android.permission.USE_BIOMETRIC` — Private Vault biometric unlocking.

## 2. RoleManager Lifecycle & Fallback Behavior
* **Role Ingestion:** Standard `RoleManager.ROLE_SMS` contract.
* **Denial Fallback:** When role is not held or denied, Global SMS enters a graceful read-only mode and presents non-blocking informational guidance without crash loops.
