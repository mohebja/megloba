# 🏗️ Global SMS - Architecture Specification

## Module Dependency Graph
```
        ┌──────────┐
        │   :app   │
        └────┬─────┘
             │
   ┌─────────┼──────────┬───────────┐
   ▼         ▼          ▼           ▼
 ┌────┐  ┌────────┐ ┌────────┐ ┌─────────┐
 │:ui │  │:settings│ │:security│ │:sms-engine│
 └─┬──┘  └───┬────┘ └───┬────┘ └────┬────┘
   │         │          │           │
   └─────────┼──────────┴───────────┘
             ▼
        ┌─────────┐
        │:database│
        └────┬────┘
             ▼
         ┌─────┐
         │:core│
         └─────┘
```

## Layers
1. **Presentation Layer (`:ui`, `:settings`):**
   - Jetpack Compose M3 Views
   - ViewModels with `StateFlow` and `collectAsStateWithLifecycle`
   - Type-safe Navigation Compose routes

2. **Domain / Business Layer (`:core`, `:sms-engine`):**
   - Bank Transaction Parsing Engine
   - Phishing & Spam Shield
   - SMS Segmentation Logic
   - Telephony Receivers & Dual SIM Dispatcher

3. **Security & Cryptography Layer (`:security`):**
   - Android Keystore Integration
   - AES-256 GCM File/Backup Encryption
   - Biometric Authentication Lock

4. **Data Layer (`:database`):**
   - Room Persistence Engine
   - Paging 3 Sources
   - SQLite Maintenance Worker
