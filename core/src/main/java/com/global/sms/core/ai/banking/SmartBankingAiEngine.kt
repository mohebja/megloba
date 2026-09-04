package com.global.sms.core.ai.banking

import com.global.sms.core.ai.nlp.LocalNlpEngine
import com.global.sms.data.entity.FinancialTransactionEntity
import java.util.Locale

data class BankSmsDetails(
    val bankName: String,
    val transactionType: String, // "EXPENSE", "INCOME", "TRANSFER"
    val amount: Double,
    val cardOrAccount: String?,
    val balanceAfter: Double?,
    val timestamp: Long
)

object SmartBankingAiEngine {

    private val BANK_PATTERNS = mapOf(
        "ملی" to "بانک ملی ایران",
        "ملت" to "بانک ملت",
        "صادرات" to "بانک صادرات",
        "تجارت" to "بانک تجارت",
        "سامان" to "بانک سامان",
        "پاسارگاد" to "بانک پاسارگاد",
        "پارسیان" to "بانک پارسیان",
        "بلوبانک" to "بلوبانک",
        "blu" to "بلوبانک",
        "سپه" to "بانک سپه",
        "کشاورزی" to "بانک کشاورزی",
        "رفاه" to "بانک رفاه کارگران",
        "مسکن" to "بانک مسکن",
        "مهر" to "بانک قرض‌الحسنه مهر ایران"
    )

    fun parseBankingSms(sender: String, body: String, messageId: Long, timestamp: Long): FinancialTransactionEntity? {
        val cleanSender = sender.trim().lowercase(Locale.ROOT)
        val cleanBody = LocalNlpEngine.normalizeDigits(body.lowercase(Locale.ROOT))

        var detectedBank = "بانک نامشخص"
        for ((kw, fullName) in BANK_PATTERNS) {
            if (cleanSender.contains(kw) || cleanBody.contains(kw)) {
                detectedBank = fullName
                break
            }
        }

        val type = when {
            cleanBody.contains("برداشت") || cleanBody.contains("خرید") || cleanBody.contains("بدهکار") || cleanBody.contains("پرداخت") -> "EXPENSE"
            cleanBody.contains("واریز") || cleanBody.contains("بستانکار") || cleanBody.contains("دریافت") -> "INCOME"
            cleanBody.contains("انتقال") || cleanBody.contains("کارت به کارت") -> "TRANSFER"
            else -> return null
        }

        val amounts = LocalNlpEngine.extractAmounts(cleanBody)
        if (amounts.isEmpty()) return null

        val primaryAmount = amounts.first()
        val balance = if (amounts.size > 1) amounts.last() else null

        // Extract card/account number
        val cardRegex = Regex("(کارت|حساب)\\s*[:\\-]?\\s*(\\d{4}\\*{2,8}\\d{4}|\\d{4,16})")
        val cardMatch = cardRegex.find(cleanBody)
        val cardOrAccount = cardMatch?.groupValues?.getOrNull(2)

        return FinancialTransactionEntity(
            messageId = messageId,
            bankName = detectedBank,
            transactionType = type,
            amount = primaryAmount,
            cardOrAccount = cardOrAccount,
            balanceAfter = balance,
            timestamp = timestamp,
            category = if (type == "EXPENSE") "خرید و خدمات" else "درآمد"
        )
    }
}
