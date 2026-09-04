package com.global.sms.core.ai.banking

import com.global.sms.core.parser.BankTransactionParser
import com.global.sms.core.parser.TransactionType

object BankMessageParser {

    fun parse(
        sender: String,
        body: String,
        messageId: Long = 0L,
        timestamp: Long = System.currentTimeMillis()
    ): TransactionEntity? {
        val analysis = BankTransactionParser.analyzeMessage(
            sender = sender,
            body = body,
            messageId = messageId,
            timestamp = timestamp
        )

        if (!analysis.isBankMessage || analysis.amountTomans == null) {
            return null
        }

        val txTypeStr = when (analysis.transactionType) {
            TransactionType.CREDIT -> "DEPOSIT"
            TransactionType.DEBIT -> "WITHDRAWAL"
            TransactionType.BALANCE_INQUIRY -> "BALANCE"
            else -> "TRANSFER"
        }

        val tomans = analysis.amountTomans ?: 0L
        val rials = analysis.amountRials ?: (tomans * 10)

        return TransactionEntity(
            messageId = messageId,
            bankName = analysis.bankName,
            transactionType = txTypeStr,
            amountRials = rials,
            amountTomans = tomans,
            formattedAmount = analysis.formattedAmount ?: "$tomans تومان",
            balanceTomans = analysis.balanceTomans,
            cardOrAccount = analysis.cardNumber,
            timestamp = timestamp
        )
    }
}
