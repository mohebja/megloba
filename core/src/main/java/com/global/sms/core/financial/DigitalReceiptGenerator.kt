package com.global.sms.core.financial

import com.global.sms.core.parser.BankSmsAnalysis
import com.global.sms.core.parser.BankTransactionParser
import com.global.sms.core.parser.TransactionType
import com.global.sms.core.util.PersianUtils
import java.security.MessageDigest
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Structured Digital Receipt for Financial SMS Transactions.
 */
data class DigitalPaymentReceipt(
    val receiptId: String,
    val bankName: String,
    val transactionTypeTitle: String,
    val isCredit: Boolean,
    val amountTomans: Long,
    val amountRials: Long,
    val formattedAmountTomans: String,
    val formattedAmountRials: String,
    val balanceTomans: Long?,
    val formattedBalanceTomans: String?,
    val maskedCardNumber: String?,
    val trackingNumber: String,
    val timestamp: Long,
    val formattedDatePersian: String,
    val verificationHash: String,
    val shareableText: String
)

/**
 * Digital Receipt Generator.
 * Converts raw bank SMS messages or BankSmsAnalysis into verified, printable and shareable
 * digital receipts adhering to Iranian banking standards.
 */
object DigitalReceiptGenerator {

    private val numberFormatter = DecimalFormat("#,###")

    fun generateReceiptFromSms(
        sender: String,
        body: String,
        messageId: Long = 0L,
        timestamp: Long = System.currentTimeMillis()
    ): DigitalPaymentReceipt? {
        val analysis = BankTransactionParser.analyzeMessage(
            sender = sender,
            body = body,
            messageId = messageId,
            timestamp = timestamp
        )

        if (!analysis.isBankMessage || analysis.amountTomans == null) {
            return null
        }

        return generateReceiptFromAnalysis(analysis)
    }

    fun generateReceiptFromAnalysis(analysis: BankSmsAnalysis): DigitalPaymentReceipt {
        val tomans = analysis.amountTomans ?: 0L
        val rials = analysis.amountRials ?: (tomans * 10L)

        val isCredit = analysis.transactionType == TransactionType.CREDIT

        val typeTitle = when (analysis.transactionType) {
            TransactionType.CREDIT -> "واریز به حساب"
            TransactionType.DEBIT -> "برداشت از حساب"
            TransactionType.BALANCE_INQUIRY -> "اعلام موجودی"
            TransactionType.OTP -> "رمز پویای بانکی"
            else -> "تراکنش بانکی"
        }

        val formattedTomans = PersianUtils.toPersianDigits(numberFormatter.format(tomans)) + " تومان"
        val formattedRials = PersianUtils.toPersianDigits(numberFormatter.format(rials)) + " ریال"

        val formattedBalance = analysis.balanceTomans?.let {
            PersianUtils.toPersianDigits(numberFormatter.format(it)) + " تومان"
        }

        val tracking = analysis.trackingNumber ?: (analysis.timestamp % 100000000).toString()
        val formattedTracking = PersianUtils.toPersianDigits(tracking)

        val maskedCard = analysis.cardNumber?.let { maskCard(it) }

        val sdf = SimpleDateFormat("yyyy/MM/dd - HH:mm", Locale.getDefault())
        val formattedDate = PersianUtils.toPersianDigits(sdf.format(Date(analysis.timestamp)))

        val receiptHash = computeReceiptHash(analysis.bankName, tomans, tracking, analysis.timestamp)

        val shareable = buildString {
            appendLine("🧾 رسید دیجیتال تراکنش بانکی")
            appendLine("━━━━━━━━━━━━━━━━━━━")
            appendLine("🏦 بانک عامل: ${analysis.bankName}")
            appendLine("📌 نوع تراکنش: $typeTitle")
            appendLine("💵 مبلغ: $formattedTomans")
            appendLine("💰 معادل ریالی: $formattedRials")
            if (formattedBalance != null) {
                appendLine("💳 مانده حساب: $formattedBalance")
            }
            if (maskedCard != null) {
                appendLine("💳 شماره کارت/حساب: $maskedCard")
            }
            appendLine("🔢 شماره پیگیری: $formattedTracking")
            appendLine("🕒 زمان: $formattedDate")
            appendLine("🔐 شناسه یکتای اعتبارسنجی: ${receiptHash.take(12)}")
            appendLine("━━━━━━━━━━━━━━━━━━━")
            appendLine("صادر شده توسط پیام‌رسان هوشمند Global SMS")
        }

        return DigitalPaymentReceipt(
            receiptId = "RCP-${analysis.timestamp % 1000000}",
            bankName = analysis.bankName,
            transactionTypeTitle = typeTitle,
            isCredit = isCredit,
            amountTomans = tomans,
            amountRials = rials,
            formattedAmountTomans = formattedTomans,
            formattedAmountRials = formattedRials,
            balanceTomans = analysis.balanceTomans,
            formattedBalanceTomans = formattedBalance,
            maskedCardNumber = maskedCard,
            trackingNumber = formattedTracking,
            timestamp = analysis.timestamp,
            formattedDatePersian = formattedDate,
            verificationHash = receiptHash,
            shareableText = shareable
        )
    }

    private fun maskCard(raw: String): String {
        val clean = raw.replace(Regex("[^0-9]"), "")
        return if (clean.length == 16) {
            val p1 = clean.substring(0, 4)
            val p4 = clean.substring(12, 16)
            PersianUtils.toPersianDigits("$p1-****-****-$p4")
        } else {
            PersianUtils.toPersianDigits(raw)
        }
    }

    private fun computeReceiptHash(bank: String, amount: Long, tracking: String, timestamp: Long): String {
        val raw = "$bank|$amount|$tracking|$timestamp"
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(raw.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
