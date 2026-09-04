# Global SMS — Developer & Contributor Guide

**Project Name:** Global SMS (`com.global.sms`)  
**Target Audience:** Android Developers & System Maintainers  

---

## 1. Project Setup & Build Environment

### Prerequisite Environment
- **JDK:** OpenJDK 17
- **Build System:** Gradle (Kotlin DSL `.gradle.kts`)
- **Android SDK:** `compileSdk = 34`, `minSdk = 26`, `targetSdk = 34`

### Building the Project
```bash
# Clean and compile debug APK
./gradlew assembleDebug

# Run unit tests across all modules
./gradlew testDebugUnitTest
```

---

## 2. Module Development Standards

1. **Adding a New Component to `:core`:**
   Place new AI or business domain logic under `com.global.sms.core.<feature>`. Always ensure primitive Persian digit normalization is performed on inputs via `LocalNlpEngine.normalizeDigits()`.

2. **Adding a New Room DAO in `:database`:**
   Define interfaces in `com.global.sms.data.dao`. Write explicit `suspend` functions and return `Flow<List<T>>` for reactive queries. Update `SmsDatabase` version and add migration scripts if schema changes.

3. **Compose UI Guidelines in `:ui`:**
   Strictly adhere to Material Design 3 tokens. Use `MaterialTheme.colorScheme` instead of hardcoded hex values. Wrap text strings in `res/values/strings.xml`. Ensure `testTag` modifiers are present on interactive buttons.
