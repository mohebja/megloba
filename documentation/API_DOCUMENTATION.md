# Global SMS — Internal API Documentation

## 1. SmsEngine Module APIs
- `SmsSender.sendMessage(context, address, body, simSlot, subId)`: Sends single or multi-part SMS via specified SIM subscription.
- `DualSimManager.getActiveSimCards(context)`: Returns list of active SIM card info with slot index and subscription IDs safely.

## 2. Security Module APIs
- `KeyStoreManager.encryptData(plaintext)`: Encrypts data using AES-256 GCM key stored in Android Keystore.
- `BiometricPromptHelper.showBiometricPrompt(activity, onSuccess, onError)`: Prompts user for biometric verification.
