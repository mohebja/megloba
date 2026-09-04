# Sprint 5.2 Google Play Policy Audit Report

## Compliance Overview
A strict policy audit was conducted against Google Play Developer Program Policies (specifically the **SMS and Call Log Permissions Policy** and **User Data Policy**).

## Detailed Compliance Findings

### 1. SMS Permission Policy (`SMS and CALL_LOG`)
- **Policy**: `READ_SMS`, `SEND_SMS`, `RECEIVE_SMS`, `RECEIVE_MMS`, `RECEIVE_WAP_PUSH` are strictly restricted to core Default SMS Handler applications.
- **Status**: **100% COMPLIANT**. Global SMS functions as a full-featured default SMS/MMS application.

### 2. Contacts Permission Policy
- **Usage**: Used exclusively to display contact names, phone number labels, and avatar photos in conversation lists and compose views.
- **Data Safeguard**: Contacts directory is read locally via `ContactsContract` and cached in Room database. No contacts are uploaded to any external server.

### 3. Notification Policy
- **Implementation**: Uses `POST_NOTIFICATIONS` runtime permission on Android 13+ (API 33).
- **User Privacy**: Provides a setting for lockscreen notification content masking.

### 4. Data Safety Form & Privacy Policy
- **Data Shared**: **0%**. No data is shared with third parties or advertising networks.
- **Data Collected**: **0%**. All processing (AI classification, OTP detection, message translation, backups) occurs 100% offline on-device.
- **Encryption**: Data in local database is secured with AES-256 Zero-Knowledge encryption.

### 5. Prominent Disclosure & Consent
- Runtime permission cards clearly explain why permissions are needed before triggering system consent dialogs.
