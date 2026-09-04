# Sprint 14.1 — SMS Sending, Receiving & Dual SIM Functional Report

## 1. SMS Dispatch & Ingestion Architecture
* **Sending Engine (`SmsSendDispatcher`):**
  * Uses `android.telephony.SmsManager.sendTextMessage` / `sendMultipartTextMessage`.
  * Passes `PendingIntent` for `ACTION_SMS_SENT` and `ACTION_SMS_DELIVERED`.
  * Optimistic UI update with delivery ticks (Clock -> Single Checkmark -> Double Green Checkmark).
* **Receiving Engine (`SmsReceiver`):**
  * `BROADCAST_SMS` intent filter with `Telephony.Sms.Intents.SMS_DELIVER_ACTION`.
  * Reassembles PDU parts for multipart messages.
  * Uses `goAsync()` with `SupervisorJob` to safely complete database write before releasing lock.

## 2. Multi-Encoding & Character Set Verification
| SMS Payload Type | Tested Content | Length (Chars / Parts) | GSM 7-bit / UCS-2 Encoding | Result |
|---|---|---|---|---|
| English Standard | "Hello, this is a test SMS." | 28 chars (1 part) | GSM 7-bit (160 limit) | PASS |
| Persian RTL UTF-8 | "سلام، پیامک فارسی تست با موفقیت ارسال شد." | 41 chars (1 part) | UCS-2 16-bit (70 limit) | PASS |
| Long Multipart Persian | 250 characters Persian text | 250 chars (4 parts) | UCS-2 Multipart Reassembled | PASS |
| Complex Emojis | "تست ایموجی 🚀✨🔥🇮🇷🎉" | 24 chars (1 part) | UCS-2 UTF-16 surrogate pairs | PASS |
| Mixed BiDi | "Order #9824 با موفقیت ثبت شد. پیگیری: https://track.ir" | 54 chars (1 part) | UCS-2 BiDi | PASS |

## 3. Dual SIM Hardware Behavior (POCO X3 NFC)
* **SIM 1 (MCI / Hamrah-e Aval) & SIM 2 (Irancell / MTN):**
* `SubscriptionManager.getActiveSubscriptionInfoList()` correctly reads both slot IDs (`slot 0`, `slot 1`).
* Composer provides a SIM switch button allowing instant toggling prior to sending.
* Sent and received messages clearly display SIM badge (`SIM 1` / `SIM 2`) in conversation list.
