# Global SMS — Architecture Audit Report

## Architecture Overview
Global SMS follows modern Android architectural principles based on Clean Architecture, MVVM, Jetpack Compose, and Multi-Module Separation.

### Module Topology
- **`:app`**: Application entry point, Application class initialization, Navigation graphs, Dependency wiring, and Hilt/Service Locator setup.
- **`:core`**: Common utilities, Domain models, Result wrappers (`Resource<T>`), Base ViewModels, Extension functions, Coroutine Dispatcher providers.
- **`:database`**: Room Database instance (`SmsDatabase`), DAOs (`SmsDao`, `ConversationDao`, `ContactDao`, `VaultDao`), Entities, Converters, Type Converters, and Encrypted SQLite driver wrappers.
- **`:sms-engine`**: Low-level Telephony API handling, `SmsManager`, `SmsReceiver`, `MmsReceiver`, `HeadlessSmsSendService`, Dual SIM manager (`DualSimManager`), WorkManager background schedulers (`SmsSchedulerWorker`).
- **`:security`**: Biometric auth (`BiometricPromptHelper`), Android Keystore AES-256 GCM encryption engine (`KeyStoreManager`), Message Vault encryption repository, Phishing & Spam link analyzer.
- **`:settings`**: App preferences (`DataStoreRepository`), Theme selection state (Classic, Smart AI, Enterprise), RTL layout toggles, Notification channel configurations.
- **`:ui`**: Jetpack Compose Design System, Material 3 Components, Custom Themes, Adaptive Screen layouts (Compact, Medium, Expanded for Foldables & Tablets), ViewModel implementations for Classic, Smart AI, and Enterprise views.

### Architectural Evaluation
- **Circular Dependencies:** 0 detected. Dependency graph flows unidirectionally: `:app` -> `:ui` / `:sms-engine` / `:security` / `:settings` -> `:database` -> `:core`.
- **State Management:** Reactive flow via Kotlin `StateFlow` and Compose `collectAsStateWithLifecycle()`.
- **Maintainability & Scalability:** High. Modular structure allows independent compilation and clear isolation of telephony, database, and UI logic.
- **Risk Level:** **LOW**.
