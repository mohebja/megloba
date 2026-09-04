# Sprint 6.4 — AI Automation Workflow System Report

## 1. Engine Overview
`SmartWorkflowEngine.kt` enables users to define rules that automatically process messages based on triggers and create pending actions.

## 2. Trigger & Action Specifications
- **Supported Triggers**:
  - `NEW_SMS`: Fires on every new incoming message.
  - `SENDER`: Matches specific sender addresses/phone numbers.
  - `CATEGORY`: Matches message category (`FINANCE`, `OTP`, `PERSONAL`, etc.).
  - `TIME`: Matches time windows or work hours.
  - `KEYWORD`: Matches specific keyword substrings in body text.
  - `AI_INTENT`: Matches AI-classified intent (`FINANCIAL`, `OTP`, `CUSTOMER_INQUIRY`).

- **Supported Actions**:
  - `CREATE_REMINDER`: Adds a scheduled notification or reminder task.
  - `CATEGORIZE`: Assigns a smart category label.
  - `SUGGEST_REPLY`: Pre-fills smart reply templates.
  - `ARCHIVE`: Moves conversation to archive upon approval.
  - `MARK_IMPORTANT`: Highlights message with high priority status.
  - `NOTIFY_USER`: Triggers high-priority local notification.

## 3. Approval Mandate
All actions evaluated by `SmartWorkflowEngine` require explicit user permission prior to state mutation.
