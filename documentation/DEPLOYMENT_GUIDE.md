# Global SMS — Enterprise Deployment & Release Engineering Guide

---

## 1. Environment & Prerequisites
- **Java Development Kit (JDK):** Version 17 (Eclipse Temurin or OpenJDK recommended)
- **Android Target SDK:** API 36 (Android 16)
- **Compile SDK:** API 36
- **Minimum Supported SDK:** API 24 (Android 7.0 Nougat)
- **Gradle:** Version 8.x with Kotlin DSL (`build.gradle.kts`)
- **Version Code:** 800
- **Version Name:** 8.0.0
- **Environment Variables:** `.env` / `.env.example` for secure API keys (injected via AI Studio Secrets Panel).

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
Execute all 227 unit tests, Robolectric suites, and regression matrices:
```bash
gradle :app:testDebugUnitTest
```

### 2.4 AI Studio Applet Verification
```bash
compile_applet
```

---

## 3. Google Play Store Compliance & Permissions

Global SMS strictly qualifies as a **Default SMS and MMS Handler** pursuant to Google Play Permissions Policies:
1. **Declared Permissions:**
   - `android.permission.RECEIVE_SMS`
   - `android.permission.SEND_SMS`
   - `android.permission.READ_SMS`
   - `android.permission.RECEIVE_MMS`
   - `android.permission.RECEIVE_WAP_PUSH`
   - `android.permission.READ_CONTACTS` & `android.permission.WRITE_CONTACTS`
   - `android.permission.READ_PHONE_STATE` (Dual SIM slot resolution)
2. **Data Safety Declaration:**
   - Zero remote tracking or external telemetry without explicit user master switch.
   - All messages, contacts, and logs are stored locally on-device with 256-bit encryption.
   - Zero broad storage permissions (`READ_EXTERNAL_STORAGE` is prohibited; zero-permission Android Photo Picker is utilized).
3. **Application Title & Metadata:**
   - Complies with the 30-character limit, free of promotional buzzwords, and fully localized.
