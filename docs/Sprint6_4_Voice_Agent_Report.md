# Sprint 6.4 — Advanced Voice Agent & Intent Router Report

## 1. Overview
`VoiceIntentRouter.kt` parses natural spoken queries in Persian and English and routes them to appropriate AI actions.

## 2. Natural Voice Commands
1. **"پیامهای مهم را بررسی کن"** -> `CHECK_IMPORTANT_MESSAGES`: Reads out priority and critical unread SMS.
2. **"چه جوابهایی پیشنهاد میکنی؟"** -> `SUGGEST_REPLIES`: Surfaces smart reply suggestions for active thread.
3. **"کارهای عقب افتاده را نشان بده"** -> `SHOW_OVERDUE_TASKS`: Lists overdue tasks and pending reminders.
4. **"پیامهای مشتریان را بررسی کن"** -> `CHECK_CUSTOMER_MESSAGES`: Filters customer inquiries and sales leads.
