# Global SMS — Software Requirements Specification (SRS)

## 1. Functional Requirements
- **FR-1:** Receive and send single and multi-part SMS messages.
- **FR-2:** Support Dual SIM devices with active subscription ID and SIM slot selection.
- **FR-3:** Act as Android Default SMS Application (`RoleManager.ROLE_SMS`).
- **FR-4:** Provide three UI modes: Classic SMS UI, Smart AI UI, Enterprise UI.
- **FR-5:** Private Vault with AES-256 encryption and biometric lock.
- **FR-6:** Contact synchronization with RTL (Persian/Arabic) character search support.

## 2. Non-Functional Requirements
- **NFR-1 (Performance):** Support 100,000+ messages and 10,000+ contacts without UI frame drops (< 16ms per frame).
- **NFR-2 (Security):** Zero unencrypted plaintext storage for vault items. Hardware-backed Android Keystore system keys.
- **NFR-3 (Usability):** Adaptive layouts for Compact (phones), Medium (foldables), and Expanded (tablets) displays.
