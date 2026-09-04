# Global SMS — Security Documentation

## Cryptographic Implementation
- Algorithm: AES/GCM/NoPadding (256-bit key length)
- Key Storage: Android Keystore System (`AndroidKeyStore` provider)
- Authentication: `BiometricPrompt` with CryptoObject binding
- Anti-Phishing: On-device URL analysis and domain sanitizer
