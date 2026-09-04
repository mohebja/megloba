# 👨‍💻 Global SMS - Developer & Maintenance Guide

## 1. Prerequisites
- Android Studio Ladybug (or newer)
- JDK 17
- Android SDK 36 (Build Tools 36.0.0)

## 2. Building the Project
To compile and generate Debug/Release builds from terminal:
```bash
# Debug Build
./gradlew assembleDebug

# Run Unit Tests
./gradlew testDebugUnitTest

# Build Release Bundle
./gradlew bundleRelease
```

## 3. Code Style & Conventions
- **Kotlin Standard Coding Style:** Enforced via `detekt.yml`.
- **Compose Rules:** Keep Composables stateless where possible, hoist state to ViewModels.
- **Resource Naming:** Lowercase snake_case (`ic_*, bg_*, str_*`).
