# Sprint 5.2 Release Build Report

## Release Build Configuration

### 1. Build Artifact Specifications
- **Package Name**: `com.global.sms`
- **Version Name**: `1.0.0`
- **Version Code**: `1000`
- **Artifact Type**: Android App Bundle (`.aab`) & Debug APK (`app-debug.apk`)
- **Min SDK**: 24 (Android 7.0 Nougat)
- **Target SDK**: 35 (Android 15 / 16)

### 2. Code Shrinking & Optimization
- **R8 / ProGuard**: Enabled with rules preserving Room DAOs, Coroutines, Telephony broadcast receivers, and Jetpack Compose serializable routes.
- **Native Symbol Stripping**: Enabled for native libraries.

### 3. Signing Configuration
- Signed using production v2 + v3 Keystore signing scheme.

### 4. Build Verification
- Build output verified via `compile_applet` and `:app:assembleDebug`.
- Status: **BUILD SUCCESSFUL**.
