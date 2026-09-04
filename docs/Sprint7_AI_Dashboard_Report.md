# Sprint 7 — AI Home Dashboard Redesign Report

**Project:** Global SMS (`com.global.sms`)  
**Component:** `AiHomeDashboardScreen.kt`, `DashboardRepository.kt`, `DashboardViewModel.kt`  
**Date:** 2026-08-06  

---

## 1. Executive Overview
The AI Home Dashboard has been redesigned to replace static cards with live intelligence derived directly from the Room encrypted database (`MessageEntity`, `TaskEntity`, `FinancialTransactionEntity`, `SpamRuleEntity`). All cards and statistics reactively reflect actual user communication metrics without synthetic data.

---

## 2. Dynamic Communication Intelligence

### Today's Communication Summary
The header card (`TodaySummaryHeaderCard`) computes live messaging statistics over the last 24 hours:
- **Bank Messages Received:** e.g., *"امروز ۱۲ پیام بانکی دریافت کردید"*
- **Pending Replies:** Active count of unread incoming messages needing user response.
- **Upcoming Payments:** Detected financial transactions scheduled or flagged with future due dates.

---

## 3. The 6 Core Intelligence Cards

1. **Important Messages Card (`important_messages_card`):**
   - Live query of pinned, bank, and unread priority messages with direct tap-to-thread navigation.

2. **Pending Replies Card (`pending_replies_card`):**
   - Isolates messages awaiting answer and provides a quick reply button.

3. **Financial Alerts Card (`financial_alerts_card`):**
   - Displays parsed bank transactions, credit/debit alerts, and expenses color-coded in green/red.

4. **Tasks Card (`tasks_card`):**
   - Pulls active tasks from `TaskDao` extracted automatically from incoming SMS context.

5. **Security Status Card (`security_status_card`):**
   - Summarizes blocked spam rules and anti-phishing security logs.

6. **AI Suggestions Card (`ai_suggestions_card`):**
   - Generates dynamic contextual action tips based on message processing.

---

## 4. Verification & Testing
- **Data Integrity:** All stats bind dynamically to Room Flows via `DashboardRepository.getAiDashboardDataFlow()`.
- **UI Architecture:** Built using Jetpack Compose Material Design 3, Persian RTL layout support, and unique `testTag` attributes for deterministic automated UI tests.
