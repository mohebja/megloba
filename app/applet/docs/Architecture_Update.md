# Global SMS — Architecture Update Document (Sprint 2.5)

**Project:** Global SMS (`com.global.sms`)  
**Date:** August 3, 2026  
**Architecture Version:** 2.5.0  

---

## 1. System Overview & Extended Architecture

Sprint 2.5 introduces major architectural expansions across multi-device ecosystems, adaptive UI responsiveness, Wear OS smartwatch integration, web synchronization, and enterprise campaign management.

```
+-----------------------------------------------------------------------------------+
|                                  GLOBAL SMS APP                                   |
+------------------------------------+----------------------------------------------+
|             UI Layer               |             Enterprise & Ecosystem           |
|  - AdaptiveConversationLayout      |  - EnterpriseCampaignEngine                  |
|  - Single-Pane / Two-Pane Split    |  - SmartAssistantV2 (Actionable Entities)    |
|  - High-Contrast & Voice Nav       |  - DeviceMigrationManager (AES-256 P2P)      |
+------------------------------------+----------------------------------------------+
|           Security & Core          |               Companion Layer                |
|  - CryptoManager (AES-256-GCM)     |  - WearCompanionManager (Wear OS)            |
|  - PrivateVaultSecurityManager     |  - WebCompanionSyncManager (QR Sync)         |
|  - ExportEngine (PDF/CSV/HTML)     |  - AutoBackupManager & Google Drive          |
+------------------------------------+----------------------------------------------+
```

---

## 2. Core Architectural Modules Introduced

### 2.1 DeviceMigrationManager (`core/migration/`)
- Handles Phone-to-Phone transfer via Wi-Fi Direct and local encrypted files.
- Generates 6-digit dynamic pairing codes and QR payloads (`GLOBALSMS_MIGRATE_V1`).
- Bundles SMS, contacts, settings, categories, and private vault content in AES-256 encrypted payload bundles.

### 2.2 WearCompanionManager (`core/wear/`)
- Extends Android notifications with `WearableExtender` actions.
- Supports voice reply and quick message responses on Wear OS smartwatches via `RemoteInput`.
- Constructs BLE-optimized JSON packets for low-bandwidth sync.

### 2.3 WebCompanionSyncManager (`core/web/`)
- Establishes secure pairing via 256-bit QR session key exchange.
- Generates AES-256 encrypted data frames for web browser companion app (`web.globalsms.app`).

### 2.4 EnterpriseCampaignEngine (`core/enterprise/`)
- Provides bulk SMS template merge engines (tags like `{Name}`, `{Company}`, `{Value}`).
- Parses CSV recipient lists with robust input sanitization.
- Tracks campaign delivery metrics and response rates.

### 2.5 SmartAssistantV2 (`core/ai/assistant/`)
- Extends AI processing with entity detection: bank card numbers, IBANs (Sheba), meeting appointments, addresses, and task items.
- Returns instant 1-tap contextual intents (copy, add calendar, open map, create task).

### 2.6 AdaptiveConversationLayout (`ui/adaptive/`)
- Implements Jetpack Compose responsive window size classification (`Compact`, `Medium`, `Expanded`).
- Automatically toggles between mobile single-pane and tablet/foldable two-pane split list-detail views.
