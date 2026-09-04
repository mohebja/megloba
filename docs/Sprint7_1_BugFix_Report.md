# Sprint 7.1 — Bug Fix & Regression Report

**Project:** Global SMS (`com.global.sms`)  

---

## 1. Resolved Issues in Sprint 7.1

| Bug ID | Component | Issue Description | Fix Applied | Status |
| :--- | :--- | :--- | :--- | :--- |
| **FIX-701** | `DashboardRepository.kt` | Unresolved field references (`isBank`, `isExpense`, `isSpam`) during build | Replaced with `MessageCategory.BANK`, `transactionType == "EXPENSE"`, and `MessageCategory.SPAM` | **RESOLVED** |
| **FIX-702** | `OnboardingFlowScreen.kt` | `Unresolved reference: CenterAlignment` compilation error | Updated alignment parameter to `Alignment.CenterHorizontally` | **RESOLVED** |
| **FIX-703** | `LocalAIBrain.kt` | Missing AI action functions (`summarizeMessage`, `summarizeConversation`, `extractTask`, `translateToPersian`) | Implemented complete offline AI helper functions and `ChatMessage` data class | **RESOLVED** |
| **FIX-704** | `Sprint7_FinalRegressionTest.kt` | Test node assertion failure on `important_messages_card` due to merged composable semantics | Passed `useUnmergedTree = true` parameter to `onNodeWithTag` | **RESOLVED** |
| **FIX-705** | `AiMemoryManagementScreen.kt` | Missing screen for AI memory privacy management | Created `AiMemoryManagementScreen.kt` with fact removal and full memory wipe actions | **RESOLVED** |
