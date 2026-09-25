# Global SMS — Developer Setup Guide

---

## 1. Prerequisites & Environment Setup

Before building and developing on Global SMS, ensure your local or cloud environment meets the following specifications:

- **Operating System:** Linux (Ubuntu 22.04+ recommended), macOS (Ventura+), or Windows 11 with WSL2.
- **JDK:** OpenJDK 17 or Eclipse Temurin 17 (set via `JAVA_HOME`).
- **Android SDK:** 
  - Platforms: `android-36` (Target), `android-35`, `android-34`
  - Build Tools: `36.0.0` or `35.0.0`
  - Platform Tools: `35.0.0+`
- **IDE:** Android Studio Ladybug (2024.2.1+) or newer.
- **Gradle:** Gradle 8.x with Kotlin DSL enabled.

---

## 2. Initial Configuration

1. **Clone the Repository:**
   ```bash
   git clone https://github.com/global-sms/global-sms-android.git
   cd global-sms-android
   ```

2. **Environment Variables & Secrets:**
   - Copy `.env.example` to `.env` in the root project directory:
     ```bash
     cp .env.example .env
     ```
   - In AI Studio, configure necessary keys via the Secrets panel. Do **NOT** commit `.env` containing sensitive tokens.

3. **Verify Keystore Setup:**
   - Debug builds use the pre-generated `debug.keystore`.
   - Release signing is configured via environment variables (`KEYSTORE_PATH`, `STORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`).

---

## 3. Building & Running

### 3.1 Command-Line Build
```bash
# Compile and build debug APK
gradle :app:assembleDebug

# Verify full compilation in AI Studio environment
compile_applet
```

### 3.2 Running the Full Automated Test Suite
```bash
# Execute all 227 JVM unit and Robolectric tests
gradle :app:testDebugUnitTest
```

---

## 4. Multi-Module Project Navigation

- `:app`: Android application manifest, navigation graph, DI wiring.
- `:core`: Domain entities, AI classifiers, financial parsers, export engines.
- `:database`: Room database v31, entities, DAOs, and migrations.
- `:sms-engine`: Telephony broadcast receivers, Dual SIM manager, and WorkManager retry dispatchers.
- `:security`: KeyStore AES-256-GCM, Private Vault, and GSMS encrypted backup archives.
- `:settings`: User preferences, DataStore, and UI mode toggles.
- `:ui`: Jetpack Compose UI, Material Design 3 screens, and ViewModels.
