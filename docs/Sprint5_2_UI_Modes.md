# Sprint 5.2 UI Modes & Enterprise Professional Navigation Architecture

## Architecture Overview
Global SMS features 3 independent UI systems selectable dynamically and saved persistently in user preferences:

1. **Classic UI (`UiMode.CLASSIC`)**:
   - High-speed, lightweight, minimal layout for maximum responsiveness.
   - Dedicated `ClassicConversationsScreen`, `ClassicMessageThreadScreen`, and `ClassicNavGraph`.
2. **Smart AI UI (`UiMode.SMART`)**:
   - Smart message categorization chips, real-time dynamic AI summary header card, Quick OTP actions, and Smart Reply chips.
   - Powered by `AdaptiveConversationLayout`, `SmartConversationsScreen`, and `SmartNavGraph`.
3. **Enterprise Professional UI (`UiMode.ENTERPRISE`)**:
   - Executive CRM Dashboard, active customer management, business templates, workflow automation engine, bulk SMS safety controls, and security audit logs.
   - Powered by `EnterpriseDashboardScreen`, `EnterpriseNavGraph`, and `EnterpriseViewModel`.

## Persistence & Dynamic Switching
- Selection is managed by `SettingsViewModel` and saved to `SettingsRepository` in Room Database.
- Managed in root composable `MainActivity.kt` and `GlobalSmsAppNavHost`.
- Modifiable from Settings -> "Conversation Style" (کلاسیک / مدرن / سازمانی) and `ProfessionalSettingsDashboard.kt`.
