# Phase 3 — Google Play SMS Application Compliance Report

**Project Name:** Global SMS (`com.global.sms`)  
**Audit Date:** August 2, 2026  
**Auditor:** Google Play Policy & Compliance Specialist  

---

## 1. Default SMS Handler Role Declarations

Global SMS strictly complies with Google Play Policy regarding Default SMS Handlers. `AndroidManifest.xml` implements all 4 mandatory intent filter actions:

1. **`SMS_DELIVER_ACTION` (`android.provider.Telephony.SMS_DELIVER`):** Handled by `com.global.sms.receiver.SmsReceiver` with `BROADCAST_SMS` permission protection.
2. **`WAP_PUSH_DELIVER_ACTION` (`android.provider.Telephony.WAP_PUSH_DELIVER`):** Handled by `com.global.sms.receiver.MmsReceiver` with `BROADCAST_WAP_PUSH` permission protection.
3. **`RESPOND_VIA_MESSAGE` (`android.intent.action.RESPOND_VIA_MESSAGE`):** Handled by `com.global.sms.service.HeadlessSmsSendService` with `SEND_RESPOND_VIA_MESSAGE` permission.
4. **`ACTION_SENDTO` (`android.intent.action.SENDTO` / `smsto:`):** Handled by `com.global.sms.ui.ComposeSmsActivity`.

---

## 2. Runtime Permission Request Strategy

| Permission | Justification | Request Pattern |
|---|---|---|
| `android.permission.RECEIVE_SMS` | Intercept incoming messages | Dynamic runtime prompt with clear rationale dialog |
| `android.permission.SEND_SMS` | Transmit user messages | Granted upon Default SMS role approval |
| `android.permission.READ_SMS` | Load conversation thread history | Granted upon Default SMS role approval |
| `android.permission.READ_CONTACTS` | Display contact names and avatars | Runtime request with graceful fallback to phone numbers |
| `android.permission.POST_NOTIFICATIONS` | Deliver incoming SMS alerts on Android 13+ | Dynamic runtime request on home screen setup |

---

## 3. Privacy & Compliance Guarantees

- **No Unnecessary Permissions:** Zero background location, camera, audio, or hardware permissions requested.
- **Data Minimization:** SMS data stays 100% local on-device unless user explicitly exports AES-256 encrypted backup.
- **Privacy Policy Link:** Configured in settings and app store manifest.
