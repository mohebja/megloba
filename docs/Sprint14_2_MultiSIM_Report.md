# Sprint 14.2 — Multi-SIM & SMS Engine Functional Report

## 1. Dual SIM Engine Architecture
* **Hardware Detection:** Uses `SubscriptionManager.getActiveSubscriptionInfoList()` to query active SIM slots (`slotIndex` 0 and 1).
* **SIM Selection:** UI composer allows explicit carrier selection prior to message dispatch.
* **Dispatch Routing:** `SmsManager.getSmsManagerForSubscriptionId(subId)` directs the message through the designated cellular radio.

## 2. SMS Transmission & Encoding
* **GSM 7-bit Encoding:** Standard Latin SMS up to 160 characters per part.
* **UCS-2 (UTF-16) Encoding:** Persian, Arabic, and Emoji payloads up to 70 characters per part.
* **Multipart SMS:** Seamless concatenation for long payloads with delivery receipts.
* **Retry Queue:** Automatic exponential backoff retry for transient cellular failures.
