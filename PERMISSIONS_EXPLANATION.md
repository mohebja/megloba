# 🔑 Global SMS - Permission Explanation & Justification
**توضیحات و توجیحات فنی مجوزهای سیستم درخواستی جهت ارسال به گوگل پلی**

---

### 1. `android.permission.READ_SMS` & `android.permission.RECEIVE_SMS`
- **Core Functionality Declaration:** App is a **Core SMS Handler / Default SMS Application**.
- **Justification:** Necessary for reading incoming text messages, grouping them into threads, identifying OTP authentication codes, categorizing spam/financial messages, and rendering conversations.

### 2. `android.permission.SEND_SMS`
- **Justification:** Allows the user to compose, reply to, or schedule text messages directly from the application.

### 3. `android.permission.READ_CONTACTS`
- **Justification:** Resolves phone numbers into friendly contact names and avatar photos within thread lists. Contact data never leaves the device.

### 4. `android.permission.USE_BIOMETRIC` / `android.permission.USE_FINGERPRINT`
- **Justification:** Securely authenticates the device owner before unlocking the **Private Vault** screen.

### 5. `android.permission.RECEIVE_BOOT_COMPLETED`
- **Justification:** Re-schedules active user-scheduled SMS jobs after the device reboots.

### 6. `android.permission.POST_NOTIFICATIONS`
- **Justification:** Displays system heads-up notifications for new incoming SMS messages with Quick Reply and OTP Copy actions.
