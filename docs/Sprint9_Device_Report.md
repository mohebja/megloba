# Sprint 9 — OEM Device Compatibility & Optimization Report

**Project:** Global SMS (`com.global.sms`)  
**Components:** `DeviceCompatibilityManager.kt`, `DeviceOptimizationGuideScreen.kt`  

---

## 1. Supported OEM Fleet & Diagnostics

| Manufacturer | Hardware / UI Detected | Special Handling | Persian Optimization Guide |
| :--- | :--- | :--- | :--- |
| **Xiaomi / Poco / Redmi** | MIUI / HyperOS | Autostart & No Restrictions battery saver guide | **Included** |
| **Samsung** | One UI | Never Sleeping Apps exception guide | **Included** |
| **OnePlus** | OxygenOS | Battery Optimization exemption guide | **Included** |
| **Google Pixel** | Stock Android 12-16 | Standard Android Doze optimization | **Included** |

---

## 2. In-App Device Optimization Guide
`DeviceOptimizationGuideScreen.kt` provides clear step-by-step Persian instructions tailored to the user's specific detected hardware brand.
