# Sprint 5.1 UI/UX & Settings Architecture Review

## UI Systems Audit
Global SMS provides three fully independent UI modes switchable on-the-fly via **Settings -> Appearance**:

1. **Classic UI (`UiMode.CLASSIC`)**:
   - Clean, lightweight Google Messages inspired layout.
   - High contrast light canvas with Material 3 top bar and bottom navigation.
   - Ideal for traditional SMS users desiring speed and familiar navigation.

2. **Smart AI UI (`UiMode.SMART`)**:
   - Visual category chips (Banking, OTP, Delivery, Business, Spam).
   - Real-time AI conversation summaries and smart reply suggestion pills.
   - Interactive voice assistant button and smart search bar.

3. **Enterprise CRM UI (`UiMode.ENTERPRISE`)**:
   - Full enterprise management dashboard (`EnterpriseDashboardScreen.kt`).
   - Campaign analytics, customer response rates, bulk SMS safety controls, and group management.
   - Adaptive Three-Pane layout support for foldables, tablets, and desktop displays.

## Comprehensive Settings Structure (7-Category System)
Organized in `ProfessionalSettingsDashboard.kt`:

1. **۱. ظاهر و پوسته (Appearance)**: UI mode selection (Classic / Smart / Enterprise), Persian fonts (Vazirmatn, Samim, Yekan), dynamic theme colors, AMOLED dark mode.
2. **۲. تنظیمات پیام‌رسانی (Messaging)**: SMSC center config, Default SIM selector, Delivery reports, MMS settings, categories & classification rules.
3. **۳. حریم خصوصی و امنیت (Privacy & Security)**: Private Vault (Pin/Biometric), Screenshot protection (`FLAG_SECURE`), Root/Emulator security assessment.
4. **۴. اعلان‌ها و هشدارهای هوشمند (Notifications)**: Private notification lockscreen mode, custom sound/vibration, unknown sender alerts.
5. **۵. مخاطبین و گروه‌ها (Contacts)**: Group creation & broadcast lists, contact cache refresh, contact sync.
6. **۶. پشتیبان‌گیری و بازیابی (Backup & Restore)**: AES-256 local encrypted backup with password PBKDF2 derivation, restore inspection preview.
7. **۷. هوش مصنوعی و ارتباطات (AI Intelligence)**: Smart classification toggle, Smart Replies, AI Summaries, Fraud & Phishing detection, Voice Assistant, Local-Only processing.
8. **۸. پیشرفته و عیب‌یابی (Advanced)**: Security audit log generator, Room DB diagnostic benchmark.

## RTL & Localization
- **Primary Language**: Persian (فارسی).
- **Secondary Languages**: English, Arabic.
- **RTL Support**: Native Compose layout directions (`LayoutDirection.Rtl`) enforced across drawers, back arrows, list items, and form fields.
