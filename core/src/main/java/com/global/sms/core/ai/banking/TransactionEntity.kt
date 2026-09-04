package com.global.sms.core.ai.banking

data class TransactionEntity(
    val id: Long = 0L,
    val messageId: Long = 0L,
    val bankName: String,
    val transactionType: String, // "DEPOSIT", "WITHDRAWAL", "TRANSFER", "BALANCE"
    val amountRials: Long,
    val amountTomans: Long,
    val formattedAmount: String,
    val balanceTomans: Long? = null,
    val cardOrAccount: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
