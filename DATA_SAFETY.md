# 🛡️ Global SMS - Data Safety Document (فرم ایمنی داده‌های گوگل پلی)

---

## Google Play Data Safety Questionnaire Summary

### 1. Data Collection & Sharing (جمع‌آوری و اشتراک‌گذاری داده‌ها)
- **Does your app collect or share any of the required user data types?**
  - **Answer:** **NO**. The app does NOT collect or transmit any personal user data to external servers or third parties. All data remains exclusively on the user's local device.
- **Is all of the user data collected by your app encrypted in transit?**
  - **Answer:** **N/A** (No network transmission of user data takes place).

### 2. Data Types & Local Handling Breakdown

| Data Category | Data Type | Collected? | Shared? | Purpose / Handling |
| :--- | :--- | :--- | :--- | :--- |
| **Personal Info** | Name, Phone Number, Contacts | ❌ No | ❌ No | Used strictly on-device to resolve contact names in SMS threads. |
| **Messages** | SMS / MMS Content | ❌ No | ❌ No | Parsed on-device for categorization, OTP detection, and spam filtering. |
| **Financial Info** | Bank Transaction SMS | ❌ No | ❌ No | Analyzed on-device for financial transaction dashboard analytics. |
| **App Info & Perf** | Crash Logs | ❌ No | ❌ No | Saved locally in app-private folder without PII for diagnostic purposes. |
| **Identifiers** | Device ID, Ad ID | ❌ No | ❌ No | No identifiers collected or requested. |

### 3. Security Practices (شیوه امنیت داده‌ها)
- **Data Encryption at Rest:** Private messages and sensitive vault logs utilize local encryption.
- **Data Deletion:** Users can permanently delete any message or conversation, which instantly purges the records from the local SQLite database.
- **Children's Privacy:** Compliant with COPPA and Google Play Families policy.
