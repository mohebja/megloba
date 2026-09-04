# Sprint 15 — AI Production Sanity Audit

## 1. On-Device AI Pipeline
* **Local Processing:** 100% on-device local execution for `AIMessageClassifier`, `SmartSummaryRepository`, `AiCopilotEngine`, `EntityExtractionEngine`, `BankTransactionAnalyzer`.
* **Zero Network Calls:** No external cloud AI APIs required for core operation.
* **Contextual Summarization:** Summaries generated strictly from actual message thread contents. Empty threads display `"اطلاعات کافی برای تهیه خلاصه وجود ندارد"`.
* **Action Safety:** AI cannot send SMS messages without direct user click interaction.
