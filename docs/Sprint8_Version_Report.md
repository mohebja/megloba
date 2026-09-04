# Sprint 8 — Version Management & Manifest Audit Report

**Project:** Global SMS (`com.global.sms`)  
**Target Version:** 8.0.0  
**Build Code:** 800  

---

## 1. Release Version Configuration

```kotlin
// app/build.gradle.kts
defaultConfig {
    applicationId = "com.global.sms"
    minSdk = 24
    targetSdk = 36
    versionCode = 800
    versionName = "8.0.0"
}
```

---

## 2. SDK Target Matrix

| Parameter | Configured Value | Compliance Status |
| :--- | :--- | :--- |
| **`minSdk`** | **24** (Android 7.0 Nougat) | Broad device reach across 98.2% of active Android devices |
| **`targetSdk`** | **36** (Android 16 / Baklava Preview Ready) | Complies with Google Play target API level requirements |
| **`compileSdk`** | **36 extension 1** | Compiles against latest Android 16 SDK APIs |
| **`versionCode`** | **800** | Sequential build increment for production track |
| **`versionName`** | **8.0.0** | Major public milestone version |
