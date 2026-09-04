# Global SMS - Security & Privacy Specification

## 1. Local Database & Private Vault Encryption
- **Crypto Engine:** AES-256 (CBC mode with PKCS5Padding) with PBKDF2 key derivation.
- **Private Vault:** Hidden messages are removed from standard list queries (`isHidden = 0`) and require PIN or Biometric verification (`BiometricPrompt`) to view.
- **Notification Masking:** Private Notification mode hides contact names and message bodies in system notifications (`"New message received"`).

## 2. Phishing & Link Security
- Every URL contained in incoming SMS messages is scanned prior to rendering.
- Suspicious domains (`bit.ly`, `.xyz`, `.top`) and fraudulent keywords flag the message with a prominent warning banner.

## 3. Anti-Fraud & USSD Protection
- Global SMS never executes incoming SMS payload links or USSD commands automatically without explicit user confirmation.
