# Global SMS - Developer Documentation

## Architecture & Project Structure
Global SMS is built using **Clean Architecture + MVVM + Repository Pattern** in modern Kotlin with Jetpack Compose and Room Database.

### Directory Mapping
```
com.example.globalsms/
├── core/
│   ├── PersianUtils.kt          # Persian digits, Jalali calendar, RTL direction helpers
│   ├── SmsSegmenter.kt          # GSM 7-bit vs Unicode 16-bit SMS segment calculator
│   ├── BankTransactionParser.kt # Bank SMS, transaction amount, and OTP extractor
│   └── PhishingDetector.kt      # URL threat scanner and phishing warning analyzer
├── data/
│   ├── Entities.kt              # Room entities: MessageEntity, ConversationEntity, etc.
│   ├── Daos.kt                  # MessageDao, ConversationDao, ScheduledDao, etc.
│   └── GlobalSmsDatabase.kt     # Room database builder
├── security/
│   ├── CryptoManager.kt         # AES-256 encryption/decryption engine
│   ├── AppLockManager.kt        # PIN & Biometric state management
│   └── BackupManager.kt         # Password-protected JSON backup exporter/importer
├── engine/
│   ├── SmsReceiver.kt           # BroadcastReceiver for SMS_DELIVERED & SMS_RECEIVED
│   ├── MmsAndHeadlessServices.kt# MMS receiver and Headless SMS service
│   ├── SmsSchedulerWorker.kt    # WorkManager CoroutineWorker for message scheduling
│   ├── DualSimManager.kt        # Multi-SIM subscription manager
│   └── TtsManager.kt            # TextToSpeech Persian & English voice assistant
└── ui/
    ├── Theme.kt                 # Material 3 Theme with Light, Dark, AMOLED True Dark & RTL
    ├── GlobalSmsViewModel.kt    # ViewModel managing reactive flows
    ├── ConversationsScreen.kt   # Main inbox view with categories
    ├── MessageThreadScreen.kt   # Conversation thread view with composer
    ├── PrivateVaultScreen.kt    # Encrypted biometric private vault
    ├── ScheduledMessagesScreen.kt# Pending scheduled SMS view
    ├── SpamFolderScreen.kt      # Phishing and spam folder
    ├── MessageStatsScreen.kt    # Message analytics and visual statistics
    └── SettingsScreen.kt        # Complete application settings panel
```

## How to Build & Run
- Open project in Android Studio (Ladybug or later).
- JDK 11 or higher required.
- Build via Gradle: `./gradlew assembleDebug` or run from AI Studio.
