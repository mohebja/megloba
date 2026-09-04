# Sprint 2.3 — AI Messaging Intelligence Architecture

**Package:** `com.global.sms.ai` / `com.global.sms.core.ai`  
**Architecture Style:** Clean Architecture + Offline AI Pipeline  

---

## 1. System Overview
Sprint 2.3 introduces an end-to-end, privacy-preserving, on-device AI system for Global SMS. All natural language processing, message categorization, fraud analysis, smart reply generation, and text summarization run entirely on the local device without sending any data over the internet.

---

## 2. Architectural Subsystems

```
                                  +-----------------------+
                                  | Incoming SMS Receiver |
                                  +-----------+-----------+
                                              |
                                              v
                                  +-----------+-----------+
                                  |    LocalNlpEngine     |
                                  +-----------+-----------+
                                              |
       +-----------------------+--------------+--------------+-----------------------+
       |                       |                             |                       |
       v                       v                             v                       v
+------+------+     +----------+----------+     +------------+------------+   +------+------+
| SmartMessage|     |   SmartFraudDetector    |     |  ConversationSummarizer |   | SmartReply  |
| Classifier  |     | & FraudDetectionEngine  |     | & SummaryEngine         |   | Engine      |
+------+------+     +----------+----------+     +------------+------------+   +------+------+
       |                       |                             |                       |
       v                       v                             v                       v
+------+------+     +----------+----------+     +------------+------------+   +------+------+
| OtpManager  |     |  AIMessageAnalysis      |     |  Thread Summary Output  |   | Contextual  |
| & OtpDao    |     |  & Risk Score           |     |                         |   | Suggestions |
+-------------+     +---------------------+     +-------------------------+   +-------------+
```

### 2.1 Component Responsibilities
1. **`LocalNlpEngine`**:
   - Persian digit normalization (Converting `۱۲۳` to `123`).
   - Keyword matching, tokenization, regex extraction, and URL discovery.

2. **`SmartMessageClassifier`**:
   - Categorizes incoming SMS into `OTP`, `BANK`, `PERSONAL`, `BUSINESS`, `SPAM`, and `IMPORTANT`.

3. **`OtpManager` & `OtpExtractor`**:
   - Extracts 4 to 8 digit OTP codes.
   - Detects origin service name (e.g. Bank Melli, Digikala, Snapp).
   - Manages expiration timers and stores active codes in `OtpEntity`.

4. **`FraudDetectionEngine`**:
   - Analyzes links, personal mobile senders impersonating banks, and phishing keywords.
   - Generates risk level (`SAFE`, `WARNING`, `DANGEROUS`) and risk score (0-100%).

5. **`SmartReplyEngine`**:
   - Offers contextual replies in Persian and English.
   - Learns frequently selected user replies and saves them in `SmartReplyEntity`.

6. **`ConversationSummaryEngine`**:
   - Generates concise offline thread summaries for long message histories.

7. **`SmartVoiceAssistant`**:
   - Drives Persian TTS for OTP reading, sender announcement, and Driving Mode filtering.

---

## 3. Privacy & Compliance
- **100% Offline Local Inference**: Zero remote API dependency.
- **Google Play SMS Compliance**: Adheres to strict SMS permissions and default SMS handler rules.
