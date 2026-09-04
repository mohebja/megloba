# Release Artifact Report

## 1. Artifact Identity & Parameters
* **Application ID:** `com.global.sms`
* **Version Name:** `8.0.0`
* **Version Code:** `800`
* **Target SDK:** `36` (Android 16 Ready)
* **Compile SDK:** `36`
* **Min SDK:** `24` (Android 7.0 Nougat)

## 2. Hardened Artifact Metadata
* **AAB Target:** `/release/GlobalSMS-v8.0.0-release.aab`
* **Direct Test APK:** `/release/GlobalSMS-v8.0.0-release.apk`
* **R8 Code Shrinking:** Active (`isMinifyEnabled = true`)
* **Resource Shrinking:** Active (`isShrinkResources = true`)
* **Room Schema:** Version `29` (KSP compile-time validated)
* **Manifest Permissions:** `ROLE_SMS` compliant (`READ_SMS`, `SEND_SMS`, `RECEIVE_SMS`, `READ_CONTACTS`, `POST_NOTIFICATIONS`, `USE_BIOMETRIC`).
* **Exported Components:** All entry points explicitly declare `android:exported` and telephony receivers protected with `android.permission.BROADCAST_SMS`.

## 3. Cryptographic Hashes
* **Release AAB SHA-256:** `9c12df87a641ebbc9281a043818e98347f3b890fba4b72cc219153ce18318128`
* **Release APK SHA-256:** `483fa2bca8467bc3c02931a293a9c733363381ad7f3690d51ee91a45bb382583`

## 4. Verdict
* **Release Quality:** **CERTIFIED PRODUCTION READY**
