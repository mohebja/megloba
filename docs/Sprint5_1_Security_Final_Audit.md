# Sprint 5.1 Security Final Audit & Penetration Review

## Security Controls Audit

### 1. Data Encryption & Key Management
- **Zero-Knowledge Architecture**: `ZeroKnowledgePrivacyEngine.kt` uses hardware-backed Android KeyStore (`AndroidKeyStore`).
- **Encryption Scheme**: AES-256-GCM for Private Vault messages and local backups.
- **Key Derivation**: PBKDF2WithHmacSHA256 with 10,000 iterations for password-derived backup keys.

### 2. Private Vault Privacy
- Hidden vault threads are completely segregated in local Room database (`isVault = true`).
- Query results for normal conversation list explicitly filter out vault items (`WHERE isVault = 0`).
- Vault entry protected by Biometric Prompt (`BiometricPrompt`) or 4-digit PIN.

### 3. Notification Privacy (Lock Screen Safeguard)
- Option `isPrivateNotificationMode` masks incoming SMS content on lock screen.
- Notification body replaced with *"پیامک جدید دریافت شد"* (New SMS Received) when device is locked.

### 4. Screenshot & Recorder Protection
- `ScreenshotProtectionManager.setProtection(activity, true)` applies `WindowManager.LayoutParams.FLAG_SECURE`.
- Prevents screen recording, system screen capture, and task switcher preview leaks.

### 5. Clipboard & OTP Privacy
- Auto-copied OTP codes cleared from system clipboard after 60 seconds.
- OTP notifications offer one-tap "Copy Code" with instant notification dismissal.

### 6. Logcat & Log Leakage Audit
- Zero sensitive data (SMS text, contact numbers, encryption keys) emitted in `android.util.Log` calls in production release builds.
