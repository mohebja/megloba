package com.global.sms.core.ai.finance

import com.global.sms.core.ai.copilot.EntityExtractionEngine
import com.global.sms.data.entity.FinancialTransactionEntity
import com.global.sms.data.entity.MessageEntity

object BankTransactionAnalyzer {

    private val BANK_KEYWORDS = listOf(
        "ملی", "ملت", "پارسیان", "سامان", "پاسارگاد", "بلوبانک", "سپه",
        "صادرات", "کشاورزی", "مسکن", "توسعه تعاون", "رفاه", "تجارت", "قوامین", "شهر"
    )

    fun analyzeMessage(message: MessageEntity): FinancialTransactionEntity? {
        val body = message.body
        val normalizedBody = body.lowercase()

        // Check if message looks financial
        val isFinancial = normalizedBody.contains("واریز") ||
                normalizedBody.contains("برداشت") ||
                normalizedBody.contains("خرید") ||
                normalizedBody.contains("موجودی") ||
                normalizedBody.contains("انتقال") ||
                normalizedBody.contains("کارت به کارت")

        if (!isFinancial) return null

        // Detect Bank Name
        val detectedBank = BANK_KEYWORDS.firstOrNull { body.contains(it) }
            ?: if (message.address.lowercase().contains("bank")) "بانک" else "بانک غیرمشخص"

        // Detect Transaction Type
        val type = when {
            normalizedBody.contains("واریز") || normalizedBody.contains("افزایش") -> "INCOME"
            normalizedBody.contains("موجودی") && !normalizedBody.contains("برداشت") -> "BALANCE"
            else -> "EXPENSE"
        }

        // Extract Amount
        val extractedAmounts = EntityExtractionEngine.extractEntities(body).amounts
        val amount = if (extractedAmounts.isNotEmpty()) {
            val cleanStr = extractedAmounts.first().replace(",", "").replace("تومان", "").replace("ریال", "").trim()
            cleanStr.toDoubleOrNull() ?: 0.0
        } else {
            0.0
        }

        return FinancialTransactionEntity(
            messageId = message.id,
            bankName = detectedBank,
            transactionType = type,
            amount = amount,
            cardOrAccount = message.address,
            balanceAfter = null,
            timestamp = message.timestamp,
            category = if (type == "EXPENSE") "PURCHASE" else "BANK"
        )
    }
}
