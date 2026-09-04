# Sprint 7 — Smart Onboarding System Report

**Project:** Global SMS (`com.global.sms`)  
**Component:** `OnboardingFlowScreen.kt`  
**Date:** 2026-08-06  

---

## 1. Overview
Sprint 7 introduces a user-centric, Material Design 3 onboarding system (`OnboardingFlowScreen.kt`) built specifically with native Persian (Farsi) Right-to-Left (RTL) layout support. The onboarding system communicates the app's key architectural and security benefits before requesting system permissions or Default SMS role assignment.

---

## 2. Key Features Implemented

1. **Default SMS Handler Context:**
   - Clearly explains to first-time users **why** Default SMS handler status (`RoleManager.ROLE_SMS`) is required for reliable message routing and MMS support.
   - Provides an inline trigger ("ارتقا و ادامه") to invoke system default SMS prompts directly.

2. **Zero-Cloud Privacy Architecture:**
   - Highlights local AES-256-GCM message encryption and zero network transmission of personal user data.

3. **Local Offline AI Processing:**
   - Informs users that Smart Reply V3, fraud detection, and expense extraction run entirely on-device via the `LocalAIBrain` without cloud API costs or privacy risks.

4. **Biometric Private Vault:**
   - Educates users on shielding sensitive financial SMS and personal chats behind fingerprint/PIN authentication.

5. **AI Personal Assistant & Copilot:**
   - Demonstrates natural language conversation search, automated reminder creation, and smart transaction reporting.

---

## 3. UI/UX & Technical Specifications
- **Layout Direction:** RTL via `CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl)`.
- **Navigation Controls:** Top bar skip option (`onboarding_skip_button`), bottom progress dots indicator, and previous/next buttons.
- **Animations:** Smooth slide & fade transitions between pages using Jetpack Compose `AnimatedContent`.
- **Testability:** Unique `testTag` attributes added (`onboarding_screen`, `onboarding_skip_button`, `onboarding_next_button`, `onboarding_back_button`).

---

## 4. Verification
- **Compilation:** Clean build verified via `compile_applet`.
- **Unit & UI Tests:** Included in `Sprint7_FinalRegressionTest.kt`.
