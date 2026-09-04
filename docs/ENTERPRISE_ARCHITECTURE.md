# Global SMS — Enterprise Architecture Specification (v9.0 Business Edition)

## Core Architectural Principles
1. **Offline-First & Zero Cloud Dependency**:
   - All AI analysis, RBAC permission resolution, and database processing execute 100% locally on-device.
2. **Clean MVVM + UDF (Unidirectional Data Flow)**:
   - Clear separation between UI Composables, ViewModels, Core Engines, and Data Access Objects.
3. **AES-256-GCM Security Model**:
   - Encrypted Room Database (`global_sms_encrypted_db`) with SQLCipher/Android Keystore protection.
   - Encrypted backup archives with PBKDF2 key derivation.

## Database Schema (Room Version 25)
- `organizations`: Enterprise organization profile and subscription state.
- `departments`: Organizational sub-units and managers.
- `employees`: Departmental members with assigned roles.
- `permissions`: Role-to-permission mapping rules.
- `sync_logs`: Multi-device E2EE sync history.
- `audit_trail`: Immutable enterprise security audit log.

## Sync Architecture
- **E2EE Packet Protocol**: End-to-end encrypted JSON payloads (`EnterpriseSyncPacket`).
- **Supported Form Factors**: Phone, Tablet, Foldable, Desktop Companion, Wear OS.
