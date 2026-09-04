# Global SMS — Sprint 3 Google Play Store Compliance Report

**Application Package:** `com.global.sms`  
**Target Policy:** Default SMS Handler Exception & Data Safety Compliance  
**Compliance Status:** 100% READY FOR PRODUCTION RELEASE  

---

## 1. SMS & Call Log Policy Justification

Global SMS is designed as a core SMS and messaging application. Under Google Play's SMS and Call Log policy guidelines, Global SMS qualifies for the **Default SMS Handler** exception:

- **Primary Purpose**: Serving as the user's default SMS client for sending, receiving, organizing, and managing text messages, OTP verification codes, and multi-SIM communications.
- **Declared SMS Permissions**:
  - `android.permission.READ_SMS`: Required to display inbox and thread history.
  - `android.permission.SEND_SMS`: Required to send user messages and campaigns.
  - `android.permission.RECEIVE_SMS`: Required for real-time incoming message toasts and OTP notifications.
  - `android.permission.READ_CONTACTS`: Required for contact name resolution in CRM and thread headers.

---

## 2. Google Play Data Safety Declarations

| Data Category | Collected / Transmitted | On-Device Processing Only | Purpose |
|---|---|---|---|
| **SMS / MMS Messages** | **No** (0% Remote Collection) | **Yes** | Local inbox management, local AI categorization, OTP detection. |
| **Contacts & CRM Data** | **No** (0% Remote Collection) | **Yes** | Name lookup, contact group campaign management. |
| **Location / Sensor Data** | **No** | N/A | Not accessed or requested. |
| **Financial / Banking Text**| **No** | **Yes** | On-device transaction parsing in Bank Dashboard. |

---

## 3. Privacy Policy & Runtime Permission Requirements

- **In-App Disclosure**: Prominent upfront disclosure screen during first launch explaining why Default SMS permissions are necessary.
- **Dynamic Runtime Request**: Standard Android Compose runtime permission dialogs implemented before accessing SMS or Contact providers.
- **Data Erasure**: Complete local wipe options provided in Settings screen to remove all database tables and encrypted vault data.
