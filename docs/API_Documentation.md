# Global SMS — Public & Internal API Reference

**Project Name:** Global SMS (`com.global.sms`)  
**Version:** 1.5.0-RELEASE  

---

## 1. Internal Core APIs

### 1.1 `AIMessageClassifier`
```kotlin
object AIMessageClassifier {
    fun classifyMessage(
        sender: String,
        body: String,
        isKnownContact: Boolean = false,
        userReputationScore: Float? = null
    ): ClassificationOutput
}
```
- **Returns:** `ClassificationOutput` containing `MessageCategory`, `confidencePercentage`, `labelPersian`, `labelEnglish`, `matchedKeywords`, and `explanation`.

### 1.2 `OtpDetector`
```kotlin
object OtpDetector {
    fun detect(body: String): OtpDetectionResult
}
```
- **Returns:** `OtpDetectionResult` with `isOtp`, `otpCode`, `serviceName`, `expiryMinutes`, and `confidence`.

### 1.3 `BankMessageParser`
```kotlin
object BankMessageParser {
    fun parse(
        sender: String,
        body: String,
        messageId: Long = 0L,
        timestamp: Long = System.currentTimeMillis()
    ): TransactionEntity?
}
```
- **Returns:** `TransactionEntity?` containing `bankName`, `transactionType`, `amountRials`, `amountTomans`, `formattedAmount`, and `balanceTomans`.

### 1.4 `AdvancedSpamDetector`
```kotlin
object AdvancedSpamDetector {
    fun evaluateSpam(
        sender: String,
        body: String,
        isKnownContact: Boolean = false,
        messageRepeatCount: Int = 1,
        userReportedSpamCount: Int = 0
    ): AdvancedSpamReport
}
```
- **Returns:** `AdvancedSpamReport` with `spamScore` (0-100), `isSpam`, `action`, and `factors`.
