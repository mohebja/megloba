# Sprint 6.4 — Autonomous AI Agent Core Architecture

## 1. Overview
The Autonomous Communication Agent (`CommunicationAgent.kt`) forms the cognitive centerpiece of Global SMS Version 6.4.0. It coordinates message observation, reasoning, suggestion generation, user confirmation, and execution.

## 2. Five-Stage Agent Lifecycle
1. **Observation**: Monitored context extraction from incoming or selected SMS messages.
2. **Reasoning**: Local intent classification, urgency scoring, and contextual evaluation via `ActionRecommendationEngine`.
3. **Suggestion**: Generating recommended `AiAgentActionEntity` candidates.
4. **User Confirmation**: **Strict Human-in-the-Loop policy**. Sensitive actions (e.g., calendar insertion, auto-reply, archiving) require explicit user approval.
5. **Execution**: Execution of confirmed actions through local repository handlers.

## 3. Privacy & Security Principles
- **100% On-Device Execution**: Zero external cloud or server dependencies.
- **Kill Switch Protection**: Emergency hardware/software toggle (`setKillSwitch`) instantly halts all background observation and recommendation tasks.
- **Local Storage**: All agent states, approvals, and actions stored in encrypted Room Database v23 tables.
