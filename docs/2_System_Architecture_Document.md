# 2. System Architecture Document — Global SMS

**Project Name:** Global SMS (`com.global.sms`)  
**Version:** 1.0.0 (Release Candidate)  
**Date:** August 2, 2026  

---

## 1. High-Level Architecture Diagram

```
+-------------------------------------------------------------------+
|                        JETPACK COMPOSE UI                         |
|  [Classic UI]      [Smart AI UI]      [Enterprise UI]    [Vault]  |
+-------------------------------------------------------------------+
                                  |
                                  v
+-------------------------------------------------------------------+
|                      VIEWMODEL & STATEFLOW                        |
|                       GlobalSmsViewModel                          |
+-------------------------------------------------------------------+
        |                         |                         |
        v                         v                         v
+---------------+       +------------------+       +----------------+
|  SMS ENGINE   |       |  SECURITY MODULE |       | ROOM DATABASE  |
| DualSimManager|       |  AES-256 Keystore|       | SQLite WAL DB  |
|  SmsReceiver  |       | Biometric Vault  |       | Indexing & FTS |
+---------------+       +------------------+       +----------------+
        |                         |                         |
        v                         v                         v
+-------------------------------------------------------------------+
|                     ANDROID TELEPHONY & OS                        |
| Carrier Radio / SmsManager / Android Keystore / SQLite / Storage  |
+-------------------------------------------------------------------+
```

---

## 2. Core Architectural Pillars

1. **Modularization:** Strict boundaries across 7 Gradle modules prevent monolithic bloat.
2. **Offline-First:** Room SQLite database serves as the local database source of truth.
3. **Hardware Keystore Security:** Hardware-backed cryptographic keys isolate sensitive user content.
4. **Adaptive UI Engine:** Dynamic theme palettes, font scaling, and 3 distinct UI paradigms.
