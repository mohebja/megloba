# Sprint 14.1 — Runtime Permission Audit

## 1. Declared Permissions Audit

| Permission | Category | Necessity | Protection Rationale |
|---|---|---|---|
| `android.permission.RECEIVE_SMS` | Dangerous | Core Requirement | Real-time incoming SMS processing via BroadcastReceiver |
| `android.permission.SEND_SMS` | Dangerous | Core Requirement | Direct message dispatching via `SmsManager` |
| `android.permission.READ_SMS` | Dangerous | Core Requirement | Historical sync and inbox reading |
| `android.permission.READ_CONTACTS` | Dangerous | Core Feature | Contact name matching, avatars, and fast search |
| `android.permission.POST_NOTIFICATIONS` | Dangerous (API 33+) | UX Requirement | Incoming SMS alerts, OTP quick copy, lockscreen masking |
| `android.permission.RECEIVE_MMS` | Signature / System | Standard SMS App | MMS multimedia message reception |
| `android.permission.RECEIVE_WAP_PUSH` | Signature / System | Standard SMS App | WAP Push alerts and MMS notification processing |
| `android.permission.USE_BIOMETRIC` | Normal | Security | Biometric unlocking for Private Vault |
| `android.permission.VIBRATE` | Normal | UX | Haptic feedback for message alerts and typing |
| `android.permission.INTERNET` | Normal | Optional Connectors | Cloud sync / Enterprise API connectors (disabled by default) |
| `android.permission.ACCESS_NETWORK_STATE` | Normal | Connectivity | Network state monitoring for optional sync |
| `android.permission.READ_PHONE_STATE` | Dangerous | Dual SIM | SIM slot and subscription detection for multi-carrier phones |

## 2. Unnecessary Permissions Check
* **Location (`ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`):** NOT DECLARED (Zero tracking)
* **Camera / Microphone (`CAMERA`, `RECORD_AUDIO`):** NOT DECLARED
* **External Storage (`READ_EXTERNAL_STORAGE`, `WRITE_EXTERNAL_STORAGE`):** NOT DECLARED (scoped app-private storage used exclusively)
* **Call Logs (`READ_CALL_LOG`, `WRITE_CALL_LOG`):** NOT DECLARED

## 3. Dynamic Permission Handling & Flow
* Implemented graceful rationales prior to requesting runtime permissions.
* Implemented `shouldShowRequestPermissionRationale` recovery flow.
* Provided direct deep-link to App Settings (`Settings.ACTION_APPLICATION_DETAILS_SETTINGS`) when permissions are permanently denied.
