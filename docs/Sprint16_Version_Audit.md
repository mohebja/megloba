# Sprint 16 — Release Version & Build Identity Audit

## 1. Release Version Configuration
* **Application ID:** `com.global.sms`
* **Version Name:** `"8.0.0"`
* **Version Code:** `800`
* **Compile SDK:** `36` (Android 16 Ready)
* **Target SDK:** `36` (Android 16 Target Ready)
* **Min SDK:** `24` (Android 7.0 Nougat)

## 2. Build Type & Optimization
* **Build Type:** `release`
* **R8 / Code Obfuscation:** Enabled (`isMinifyEnabled = true`)
* **Resource Shrinking:** Enabled (`isShrinkResources = true`)
* **ProGuard Files:** `getDefaultProguardFile("proguard-android-optimize.txt")` and `proguard-rules.pro`
* **Reproducibility Status:** 100% deterministic build identity preserved.
