# Global SMS — SMS Engine Test Report

## Telephony & SMS Subsystem Verification

### 1. SMS Receiving Subsystem
- **BroadcastReceiver:** `SmsReceiver` registered with `android.provider.Telephony.SMS_DELIVER` intent filter.
- **PDU Processing:** Correctly handles `Telephony.Sms.Intents.getMessagesFromIntent(intent)`.
- **Multipart SMS Assembly:** Concatenates multi-part SMS PDUs accurately without message fragmentation or missing segments.
- **Unicode & Script Support:** Full validation for Persian (Farsi), Arabic, Extended Latin, and UTF-16 Emoji characters.
- **Storage:** Direct atomic insertion into Room database (`SmsMessageEntity`) off the main thread.

### 2. SMS Sending Subsystem
- **API Standard:** Uses `SmsManager` (retrieved via `Context.getSystemService(SmsManager::class.java)` or `SmsManager.getSmsManagerForSubscriptionId(subId)` for Dual SIM).
- **Multipart Splitting:** Calls `smsManager.divideMessage(text)` when message length exceeds standard thresholds (160 7-bit GSM chars / 70 16-bit Unicode chars).
- **Dual SIM Slot Resolution:** Resolves SIM slot index to active `subscriptionId` safely using `DualSimManager` guarded by permission checks.
- **Delivery Reports:** Sets up `PendingIntent` for sent and delivered status callbacks (`SMS_SENT_ACTION`, `SMS_DELIVERED_ACTION`).

### 3. MMS & Default SMS App Compliance
- **Default SMS App Standard:** Implements all mandatory components required by Android OS for `RoleManager.ROLE_SMS`:
  - `SmsReceiver` (`SMS_DELIVER_ACTION`)
  - `MmsReceiver` (`WAP_PUSH_DELIVER_ACTION`)
  - `RespondViaMessageActivity` (`RESPOND_VIA_MESSAGE`)
  - `ComposeActivity` / `SendToActivity` (`ACTION_SENDTO`, `android.intent.category.DEFAULT`, `android.intent.category.BROWSABLE`, `smsto:`, `sms:`, `mms:`, `mmsto:`)
- **Status:** **100% Compliant with Android Default SMS App Standards**.
