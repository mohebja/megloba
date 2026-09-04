# Sprint 8 — First Run Experience & Onboarding Audit Report

**Project:** Global SMS (`com.global.sms`)  
**Component:** `OnboardingFlowScreen.kt`  

---

## 1. Onboarding Carousel Flow

The onboarding sequence consists of 5 interactive, RTL-compliant screens designed with Material Design 3 and smooth page transitions:

1. **Screen 1: خوش آمدید (Welcome)**
   - Introduces Global SMS as a modern, high-performance messaging app for Persian users.
2. **Screen 2: پیام‌رسان پیش‌فرض (Default SMS Handler)**
   - Explains why system Default SMS status is required and guides the user through the OS permission prompt.
3. **Screen 3: حریم خصوصی ۱۰۰٪ محلی (Privacy Protection)**
   - Highlights offline storage, AES-256-GCM encryption, and zero cloud uploads.
4. **Screen 4: هوش مصنوعی محلی (AI Capabilities)**
   - Demonstrates Smart Reply, OTP detection, conversation summaries, and task extraction.
5. **Screen 5: آماده‌سازی نهایی (Ready Screen)**
   - Completes initialization, sets `onboarding_completed = true`, and launches the main UI.

---

## 2. Accessibility & UX Features
- **Skip Option:** Users can tap "رد شدن" at any point to proceed directly to permission setup.
- **RTL Support:** Full right-to-left layout alignment with Persian typography.
- **Test Tags:** Standardized test tags (`onboarding_next_button`, `onboarding_skip_button`) for automated regression testing.
