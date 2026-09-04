# Sprint 15 — Release Version & Build Identity Report

## 1. Build Identity Specifications (`app/build.gradle.kts`)
* **Application ID:** `com.global.sms`
* **Namespace:** `com.global.sms`
* **Compile SDK:** `36` (Android 16 Ready, minorApiLevel 1)
* **Target SDK:** `36` (Android 16 Target Ready; exceeds Google Play minimum requirement)
* **Min SDK:** `24` (Android 7.0 Nougat — covering 98.7% of active global Android devices)
* **Version Code:** `800`
* **Version Name:** `"8.0.0"`

## 2. Version Consistency & Deterministic Derivation
* **Version Code Calculation:** Major (8) * 100 + Minor (0) * 10 + Patch (0) = `800`.
* **Consistency Check:** Aligned across root `build.gradle.kts`, `app/build.gradle.kts`, `metadata.json`, and runtime `BuildConfig.VERSION_NAME`.
* **Reproducibility Status:** 100% deterministic build identity across all target modules.
