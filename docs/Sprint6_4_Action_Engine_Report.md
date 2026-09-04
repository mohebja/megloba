# Sprint 6.4 — Intelligent Action Recommendation Engine Report

## 1. Executive Summary
`ActionRecommendationEngine.kt` automatically analyzes incoming SMS text to extract actionable intents and recommend relevant system actions.

## 2. Recommendation Patterns

| Category / Message Sample | Detected Intent | Recommended Action | Default Urgency |
|---|---|---|---|
| **Bank SMS:** "قسط وام شما فردا سررسید میشود" | `FINANCIAL_DUE_DATE` | `CREATE_REMINDER` ("افزودن یادآوری سررسید قسط") | 85 |
| **Customer SMS:** "قیمت محصول را ارسال کنید" | `CUSTOMER_INQUIRY` | `REPLY_TEMPLATE` ("ارسال لیست قیمت و کاتالوگ") | 75 |
| **Delivery SMS:** "مرسوله آماده تحویل است" | `DELIVERY_TRACKING` | `TRACK_PACKAGE` ("پیگیری خودکار وضعیت مرسوله پستی") | 65 |
| **Calendar Event:** "جلسه کاری فردا ساعت ۱۰" | `CALENDAR_EVENT` | `CALENDAR_EVENT` ("ثبت رویداد جدید در تقویم") | 70 |

## 3. Human Confirmation Policy
Every recommended action is emitted in `SUGGESTED` or `PENDING_APPROVAL` status and never executed automatically without user confirmation.
