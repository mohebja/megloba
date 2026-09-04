# Sprint 7 — Message Experience Upgrade Report

**Project:** Global SMS (`com.global.sms`)  
**Component:** `MessageThreadScreen.kt`, `MessageActionBottomSheet.kt`  
**Date:** 2026-08-06  

---

## 1. Overview
The messaging thread experience has been upgraded with AI-powered quick action chips, enhanced long-press action sheets, threat indicators, and segment counter feedback for Persian text.

---

## 2. Key Features

1. **AI Action Shortcuts Bar:**
   - **خلاصه گفتگو:** Live local conversation summarization using `LocalAIBrain.summarizeConversation`.
   - **پیشنهاد پاسخ:** Smart Reply V3 one-tap message composition.
   - **استخراج کار:** Automatic task extraction from message context into `TaskEntity`.
   - **ترجمه پیام:** On-device Persian translation for foreign sender texts.

2. **Long-Press Actions Sheet (`MessageActionBottomSheet.kt`):**
   - Options for Copy, Forward, Reply, Delete, Pin message, and AI Analyze (`action_ai_analyze`).

3. **Contact & SIM Metadata Header:**
   - Shows contact display name/avatar, current SIM slot indicator (سیم ۱ / سیم ۲), and call shortcuts.

4. **Persian GSM & Character Counter:**
   - Displays real-time segment calculation (`SmsSegmenter`) supporting standard Persian Unicode limits (70 chars/segment).
