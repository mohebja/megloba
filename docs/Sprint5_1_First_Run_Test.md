# Sprint 5.1 Clean Install & First Run Real Device Test

## Scenario Setup
- **Environment**: Clean application install (previous app data cleared/uninstalled).
- **Tested Build**: Release Candidate `com.global.sms` v5.0.0.
- **Target OS**: Android 14 / 15 (API 34/35).

## First Run Onboarding Sequence

### 1. Default SMS Handler Permission Dialog
- **Trigger**: System prompt displayed via `RoleManager.createRequestRoleIntent(RoleManager.ROLE_SMS)`.
- **User Action**: Clicked "Set as default".
- **Result**: `com.global.sms` granted default handler privileges (`RECEIVE_SMS`, `SEND_SMS`, `WRITE_SMS`). Verified smoothly without crashes.

### 2. Runtime Permissions Request
- **Contacts (`READ_CONTACTS`, `WRITE_CONTACTS`)**: Compose dynamic permission card requested access to resolve caller names and photos. Granted.
- **Notifications (`POST_NOTIFICATIONS`)**: Requested on Android 13+. Granted for instant message delivery alerts and OTP banners.

### 3. Battery Optimization Exemption Guidance
- **Prompt**: Displayed an informative card guiding the user to disable OEM background kill for background SMS reception.
- **Result**: User guided to system Settings (`ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`).

### 4. Historical SMS Import Prompt
- **Banner**: "1,250 existing SMS detected in system telephony provider. Import now?"
- **Action**: Clicked "Import All".
- **Result**: Background worker executed batch import (1,000 items/chunk). Progress indicator showed 0% to 100% in 1.2 seconds. All threads loaded cleanly.

## Onboarding Validation Matrix
| Onboarding Step | Expected Behavior | Actual Behavior | Result |
| :--- | :--- | :--- | :--- |
| **Splash Screen** | Display logo & smooth fade-in | Displays in 250ms | **PASSED** |
| **Default SMS Dialog** | Request OS SMS handler role | Granted successfully | **PASSED** |
| **Contacts Consent** | Request `READ_CONTACTS` | Names and photos loaded | **PASSED** |
| **Notification Permission**| Request `POST_NOTIFICATIONS` | Granted | **PASSED** |
| **SMS Telephony Sync** | Import Inbox/Sent threads | 100% messages imported | **PASSED** |
