# Sprint 5 Google Play Store Readiness & Release Declaration

## Package & Signing Configuration
- **Application ID**: `com.global.sms`
- **Target SDK**: 35 (Android 15 / 16 Preview)
- **Min SDK**: 24 (Android 7.0 Nougat)
- **Build Output**: Android App Bundle (AAB) with dynamic feature support.

## Code Shrinking & Security Optimization
- **R8 / ProGuard**: Enabled with rules preserving Room entities, Coroutine reflection, and Telephony Broadcast Receivers.
- **Symbol Stripping**: Native libraries stripped of debug symbols.

## Google Play Data Safety & Privacy Policy Declaration
- **Core App Functionality**: Default SMS application (`SMS`, `MMS`, `READ_CONTACTS`).
- **Data Collection**: 0% external telemetry or data selling.
- **On-Device Processing**: 100% Zero-Knowledge local AI classification, backup encryption, and local SQLite/Room database storage.
- **Sensitive Permissions**: Protected by explicit user consent dialogs and `RoleManager` Android default SMS application dialogs.
