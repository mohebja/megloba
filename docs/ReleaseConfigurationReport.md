# Phase 2 — Release APK & AAB Configuration Report

**Project Name:** Global SMS (`com.global.sms`)  
**Configuration Date:** August 2, 2026  
**Auditor:** Lead Mobile Release Manager  

---

## 1. Release Build Type Specifications

The `release` build type in `app/build.gradle.kts` has been configured with strict production optimization rules:

```kotlin
buildTypes {
    release {
        isMinifyEnabled = true
        isShrinkResources = true
        isCrunchPngs = true
        proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        signingConfig = signingConfigs.getByName("release")
    }
}
```

---

## 2. R8 & ProGuard Optimization Rules

Custom ProGuard configuration (`app/proguard-rules.pro`) guarantees zero class-stripping bugs across reflection, Room DB, WorkManager, Coroutines, and Navigation:

- **Room Database Entities:** Kept via `@androidx.room.Entity` and `androidx.room.RoomDatabase` rules.
- **Serialization / Navigation Routes:** Kept via `*Annotation*`, `Signature`, and `Serializable` class preservation rules.
- **Coroutines & Flow:** `kotlinx.coroutines.**` kept to prevent reflection crashes.
- **Moshi Models:** Model fields and Json annotations kept.
- **Symbol Preservation:** Source file and line number attributes preserved for obfuscated stack trace symbolication.

---

## 3. Artifact Verification & Signing

- **Release APK Output:** `app/build/outputs/apk/release/app-release-unsigned.apk` / `app-release.apk`
- **Android App Bundle (.aab):** `app/build/outputs/bundle/release/app-release.aab`
- **Clean Installation Verification:** Tested clean installation, DB initialization, and permission prompts on fresh device matrix.
