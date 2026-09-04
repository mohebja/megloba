# 8. Google Play Submission Checklist — Global SMS

| Requirement | Verification Item | Status |
|---|---|---|
| **Target SDK 36** | `targetSdk = 36` in `build.gradle.kts` | COMPLIANT |
| **Default SMS Role** | Declaration form completed in Play Console | COMPLIANT |
| **Permissions Audit** | Only core SMS/Contacts/Notification permissions declared | COMPLIANT |
| **Privacy Policy URL** | Hosted privacy policy link provided | COMPLIANT |
| **App Bundle (.aab)** | Signed `app-release.aab` compiled with R8/ProGuard | COMPLIANT |
| **Data Safety Section** | Disclosed on-device SMS local storage & optional user encrypted backup | COMPLIANT |
| **64-bit Architecture** | Native libraries include arm64-v8a / x86_64 | COMPLIANT |
