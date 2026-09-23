# Global SMS — Enterprise Deployment & Release Engineering Guide

## 1. Environment & Prerequisites
- **Java Development Kit (JDK):** Version 17 (Eclipse Temurin or OpenJDK recommended)
- **Android Target SDK:** API 35 (Android 15)
- **Minimum Supported SDK:** API 26 (Android 8.0 Oreo)
- **Gradle:** Version 8.x with Kotlin DSL
- **Environment Variables:** `.env` file containing `GEMINI_API_KEY` (or injected via CI secrets / AI Studio Secrets Panel).

---

## 2. Build & Assembly Commands

### 2.1 Debug Build
For internal testing, continuous integration, and local emulator inspection:
```bash
gradle :app:assembleDebug
```
Output artifact: `app/build/outputs/apk/debug/app-debug.apk`

### 2.2 Release Android App Bundle (AAB)
For Google Play Store submission:
```bash
gradle :app:bundleRelease
```
Output artifact: `app/build/outputs/bundle/release/app-release.aab`

### 2.3 Automated Testing
Execute all unit tests, Robolectric suites, and regression matrices:
```bash
gradle testDebugUnitTest
```

---

## 3. Google Play Store Compliance & Permissions
Global SMS qualifies as a **Default SMS and MMS Handler** pursuant to Google Play Permissions Policies:
1. **Declared Permissions:**
   - `android.permission.RECEIVE_SMS`
   - `android.permission.SEND_SMS`
   - `android.permission.READ_SMS`
   - `android.permission.RECEIVE_MMS`
   - `android.permission.RECEIVE_WAP_PUSH`
2. **Data Safety Declaration:**
   - Zero remote tracking or external telemetry without explicit user consent.
   - All messages, contacts, and logs are stored locally on-device with 256-bit encryption.
   - No broad media permissions requested (`READ_EXTERNAL_STORAGE` is strictly forbidden; standard Photo Picker contract is utilized).
