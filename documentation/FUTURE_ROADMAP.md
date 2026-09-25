# Global SMS — Future Roadmap (v2.0 & Beyond)

**Project Name:** Global SMS (`com.global.sms`)  
**Target Horizon:** 2026 – 2027  
**Current Production Baseline:** Version 8.0.0 (Zero-Defect, 227 Tests Passing)

---

## 1. Vision & Strategy

Having achieved zero-defect production readiness with 227 passing automated tests and full Google Play Store compliance, the next development cycles expand into edge AI hardware acceleration, companion device ecosystems, and enterprise automation.

---

## 2. Key Roadmap Pillars

### 2.1 Pillar I: Next-Generation On-Device Artificial Intelligence
- **NPU-Accelerated Generative Reply Synthesis:**
  - Fine-tuned local micro-LLM execution via Android Neural Networks API (NNAPI) and Qualcomm/Tensor NPU accelerators.
  - Zero cloud calls, maintaining 100% on-device privacy.
- **Smart Attachment OCR:**
  - Automated text and invoice extraction from images attached to MMS or received via chat.
- **Voice-to-Text Dictation:**
  - Offline Persian & English speech recognition for hands-free message composition.

### 2.2 Pillar II: Multi-Device Companion Ecosystem
- **Wear OS Companion App & Tiles:**
  - Smartwatch quick replies, voice dictation, and one-tap OTP notification copying.
- **Android Auto Integration:**
  - Hands-free voice reading and driver-safe verbal responses.
- **Dedicated Tablet & Foldable Canonical Layouts:**
  - Two-pane List-Detail navigation and multi-window drag-and-drop attachment support.

### 2.3 Pillar III: Cross-Device Local Sync (Zero Cloud)
- **Local Wi-Fi & WebRTC Desktop Companion:**
  - Peer-to-peer encrypted sync between Android phone and desktop web browser without intermediary servers.
  - End-to-end authenticated QR-code handshake.

### 2.4 Pillar IV: Enterprise & Commercial Automation
- **Modular Automation Script Engine:**
  - Custom Kotlin/JavaScript rule plugins for enterprise SMS workflows and webhook alerts.
- **Out-of-Office Business Auto-Responder:**
  - Rule-based vacation auto-replies filtered by VIP contacts and working hours.
- **Hardware Security Token Integration (FIDO2 / YubiKey):**
  - Physical NFC/USB key unlocking for Private Vault enterprise authentication.
