# Global SMS AI OS — Version 8.0.0 Release Notes

**Tag:** `v8.0.0`
**Release Title:** `Global SMS v8.0.0 — Production Release`

---

### Highlights & Major Capabilities

* **On-Device Smart AI Engine:** 100% local, offline message categorization (Banking/OTP/Personal/Promotions), contextual 1-tap smart replies, and entity extraction with zero cloud NLP dependencies and zero tracking.
* **Three Dynamic UI Architectures:** Switch seamlessly between **Classic Clean**, **Smart AI OS**, and **Enterprise Workforce** layouts tailored for everyday users, power users, and enterprise professionals.
* **Zero-Trust Private Vault:** Hardware-backed AES-256-GCM encryption with StrongBox/TEE biometric gates and `FLAG_SECURE` protection to isolate sensitive messages from AI indexing and external capture.
* **Persian & RTL Native Experience:** Full bidirectional layout mirroring, elegant Vazirmatn typography, Persian number formatting, and optimized Persian date conversion.
* **Telephony & Multi-SIM Mastery:** Native Android `RoleManager.ROLE_SMS` integration, multi-SIM carrier routing (Slot 0 / Slot 1), GSM 7-bit & UCS-2 UTF-16 encoding, multipart concatenation, and real-time delivery receipt callbacks.
* **High-Scale Performance:** Sub-150ms cold launch latency, 17ms FTS keyword search, and smooth 120 FPS scrolling across large message datasets (tested up to 1,000,000 messages).
* **Privacy-Preserving Backup & Restore:** Encrypted `.gsmsbak` container format with SHA-256 integrity verification, atomic rollback on corruption, and offline P2P migration capabilities.

---

### Platform Compatibility
* **Minimum Android:** Android 7.0 (API 24 Nougat)
* **Target Android:** Android 16 (API 36 Ready)
* **Supported Architectures:** `arm64-v8a`, `armeabi-v7a`, `x86_64`
* **Room Schema:** Version 29 (with verified migration chain `MIGRATION_1_2` -> `MIGRATION_28_29`)

---

### Checksums & Verification
* **Release AAB (SHA-256):** `9c12df87a641ebbc9281a043818e98347f3b890fba4b72cc219153ce18318128`
* **Direct Test APK (SHA-256):** `483fa2bca8467bc3c02931a293a9c733363381ad7f3690d51ee91a45bb382583`
