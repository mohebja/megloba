# Sprint 15 — Battery & Background Execution Audit

## 1. Background Architecture
* **WorkManager Jobs:** Expedited background sync and backup tasks scheduled with battery-not-low constraints.
* **SMS Receivers:** High-priority `Telephony.Sms.Intents.SMS_DELIVER_ACTION` broadcast receivers with `goAsync()` lifecycle handling.
* **Doze Mode Compatibility:** Complies with Android 12+ Doze and App Standby buckets; zero unneeded wake locks or persistent foreground services.
