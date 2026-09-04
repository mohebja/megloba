# Global SMS — Software Design Document (Updated Sprint 1.5)

**Project Name:** Global SMS (`com.global.sms`)  
**Version:** 1.5.0-RELEASE  
**Date:** August 2, 2026  
**Status:** Production Ready  

---

## 1. System Vision & Purpose

Global SMS is an enterprise-grade, privacy-first Android SMS/MMS application designed for high performance, secure communication, localized Persian/English/Arabic processing, and offline-first AI intelligence. Serving as the default SMS handler, it features local AES-256 database encryption, biometric authentication for private conversations, dual SIM support, and real-time transaction parsing.

---

## 2. Structural Module Breakdown

```
+-------------------------------------------------------------------+
|                            :app                                   |
|   (Application, MainActivity, Navigation Graph, Dependency Root)  |
+---------------------------------+---------------------------------+
                                  |
            +---------------------+---------------------+
            |                                           |
            v                                           v
    +---------------+                           +---------------+
    |      :ui      |                           |   :settings   |
    | (Compose UI)  |                           |  (DataStore)  |
    +-------+-------+                           +-------+-------+
            |                                           |
            +---------------------+---------------------+
                                  |
                                  v
                        +-------------------+
                        |    :sms-engine    |
                        | (Telephony & MMS) |
                        +---------+---------+
                                  |
            +---------------------+---------------------+
            |                                           |
            v                                           v
    +---------------+                           +---------------+
    |     :core     |                           |   :security   |
    | (AI, NLP, FTS)|                           | (KeyStore/AES)|
    +-------+-------+                           +-------+-------+
            |                                           |
            +---------------------+---------------------+
                                  |
                                  v
                        +-------------------+
                        |     :database     |
                        |  (Room, Entities) |
                        +-------------------+
```

---

## 3. Design Patterns & Principles

1. **Clean MVVM Architecture:** Explicit separation of UI, ViewModel, Repository, and Data Sources.
2. **Unidirectional Data Flow (UDF):** StateFlow emitting immutable `UiState` objects; UI emitting user event sealed classes.
3. **Offline-First AI:** All natural language processing, regex scoring, and rule matching execute locally on device.
4. **Defense in Depth:** Hardware-backed KeyStore encryption, `FLAG_SECURE` window protection, and biometric gatekeeping for private messages.
