package com.global.sms.core.parser

import com.global.sms.core.util.PersianUtils
import java.text.DecimalFormat
import java.util.regex.Pattern

data class BankSmsAnalysis(
    val messageId: Long = 0L,
    val sender: String = "",
    val rawBody: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isBankMessage: Boolean = false,
    val bankName: String = "سایر بانک‌ها",
    val transactionType: TransactionType = TransactionType.OTHER,
    val amountTomans: Long? = null,
    val amountRials: Long? = null,
    val formattedAmount: String? = null,
    val balanceTomans: Long? = null,
    val balanceRials: Long? = null,
    val formattedBalance: String? = null,
    val cardNumber: String? = null,
    val otpCode: String? = null,
    val paymentLink: String? = null,
    val trackingNumber: String? = null
)

enum class TransactionType {
    DEBIT, CREDIT, OTP, BALANCE_INQUIRY, OTHER
}

object BankTransactionParser {

    private val BANK_SENDERS = listOf(
        "MELLI", "MELLAT", "TEJARAT", "SAMAN", "PARSIAN", "PASARGAD", "SADERAT",
        "KESHAVARZI", "MASKAN", "REFAH", "SHAHR", "SINA", "SEPAH", "DEY",
        "GARDESHGARI", "KARAFRIN", "BLU", "MEHR", "RESALAT", "CHARTER", "BANK"
    )

    private val BANK_KEYWORDS = listOf(
        "بانک", "حساب", "واریز", "برداشت", "مانده", "کارت", "رمز پویا", "کد تایید",
        "تراکنش", "مبلغ", "انتقال", "موجودی", "شاپرک", "OTP", "Bank", "Transfer",
        "Withdrawal", "Deposit", "Balance", "پایا", "ساتنا", "پل"
    )

    private val OTP_PATTERNS = listOf(
        Pattern.compile("(?:کد تایید|رمز پویا|رمز یکبار مصرف|OTP|code)[:\\s]*([0-9]{4,8})", Pattern.CASE_INSENSITIVE),
        Pattern.compile("رمز پویا:?\\s*([0-9]{4,8})"),
        Pattern.compile("کد[:\\s]+([0-9]{4,8})")
    )

    private val AMOUNT_PATTERN = Pattern.compile("(?:مبلغ|مبلغ:|واریز|برداشت|خرید|انتقال|بدهکار|بستانکار)?[:\\s]*([0-9,٬]+)\\s*(ریال|تومان|Rials|IRR|Toman)", Pattern.CASE_INSENSITIVE)

    private val BALANCE_PATTERN = Pattern.compile("(?:مانده|موجودی|مانده فعلی|موجودی جدید)[:\\s]*([0-9,٬]+)\\s*(ریال|تومان|Rials|IRR|Toman)", Pattern.CASE_INSENSITIVE)

    private val CARD_PATTERN = Pattern.compile("(?:کارت|حساب|به|از)?[:\\s]*\\b(6[0-9]{3}[-\\s\\*\\.]?[0-9]{2,4}[-\\s\\*\\.]?[0-9]{2,4}[-\\s\\*\\.]?[0-9]{4})\\b")

    private val TRACKING_PATTERN = Pattern.compile("(?:پیگیری|ش پیگیری|شماره پیگیری|مرجع|ارجاع)[:\\s]*([0-9]{5,16})")

    private val URL_PATTERN = Pattern.compile("(https?://[^\\s]+|(?:www\\.)?[a-zA-Z0-9\\.\\-]+\\.(?:ir|com|net|org)/[^\\s]*)")

    private val numberFormat = DecimalFormat("#,###")

    fun analyzeMessage(
        sender: String,
        body: String,
        messageId: Long = 0L,
        timestamp: Long = System.currentTimeMillis()
    ): BankSmsAnalysis {
        val upperSender = sender.uppercase()
        val normalizedBody = PersianUtils.toEnglishDigits(body)
        val isBankSender = BANK_SENDERS.any { upperSender.contains(it) }
        val containsBankKeyword = BANK_KEYWORDS.any { body.contains(it, ignoreCase = true) }

        if (!isBankSender && !containsBankKeyword) {
            return BankSmsAnalysis(
                messageId = messageId,
                sender = sender,
                rawBody = body,
                timestamp = timestamp,
                isBankMessage = false
            )
        }

        val bankName = extractBankName(upperSender, body)
        val otpCode = extractOtpCode(normalizedBody)
        val paymentLink = extractUrl(body)
        val cardNumber = extractCardNumber(normalizedBody)
        val trackingNumber = extractTrackingNumber(normalizedBody)

        val (amountTomans, amountRials, formattedAmount) = extractAmountDetails(body, normalizedBody)
        val (balanceTomans, balanceRials, formattedBalance) = extractBalanceDetails(body, normalizedBody)

        val type = when {
            otpCode != null || body.contains("رمز پویا") || body.contains("کد تایید") -> TransactionType.OTP
            body.contains("برداشت") || body.contains("بدهکار") || body.contains("خرید") || body.contains("انتقال") || body.contains("Withdrawal") -> TransactionType.DEBIT
            body.contains("واریز") || body.contains("بستانکار") || body.contains("Deposit") -> TransactionType.CREDIT
            body.contains("مانده") || body.contains("موجودی") || body.contains("Balance") -> TransactionType.BALANCE_INQUIRY
            else -> TransactionType.OTHER
        }

        return BankSmsAnalysis(
            messageId = messageId,
            sender = sender,
            rawBody = body,
            timestamp = timestamp,
            isBankMessage = true,
            bankName = bankName,
            transactionType = type,
            amountTomans = amountTomans,
            amountRials = amountRials,
            formattedAmount = formattedAmount,
            balanceTomans = balanceTomans,
            balanceRials = balanceRials,
            formattedBalance = formattedBalance,
            cardNumber = cardNumber,
            otpCode = otpCode,
            paymentLink = paymentLink,
            trackingNumber = trackingNumber
        )
    }

    private fun extractBankName(sender: String, body: String): String {
        return when {
            sender.contains("MELLI") || body.contains("ملی") -> "بانک ملی"
            sender.contains("MELLAT") || body.contains("ملت") -> "بانک ملت"
            sender.contains("TEJARAT") || body.contains("تجارت") -> "بانک تجارت"
            sender.contains("SAMAN") || body.contains("سامان") -> "بانک سامان"
            sender.contains("PARSIAN") || body.contains("پارسیان") -> "بانک پارسیان"
            sender.contains("PASARGAD") || body.contains("پاسارگاد") -> "بانک پاسارگاد"
            sender.contains("SEPAH") || body.contains("سپه") -> "بانک سپه"
            sender.contains("SADERAT") || body.contains("صادرات") -> "بانک صادرات"
            sender.contains("KESHAVARZI") || body.contains("کشاورزی") -> "بانک کشاورزی"
            sender.contains("MASKAN") || body.contains("مسکن") -> "بانک مسکن"
            sender.contains("REFAH") || body.contains("رفاه") -> "بانک رفاه"
            sender.contains("SHAHR") || body.contains("شهر") -> "بانک شهر"
            sender.contains("BLU") || body.contains("بلوبانک") || body.contains("بلو") -> "بلوبانک (Blu)"
            sender.contains("MEHR") || body.contains("مهر ایران") -> "بانک مهر ایران"
            sender.contains("RESALAT") || body.contains("رسالت") -> "بانک رسالت"
            sender.contains("SINA") || body.contains("سینا") -> "بانک سینا"
            sender.contains("DEY") || body.contains("دی") -> "بانک دی"
            sender.contains("GARDESHGARI") || body.contains("گردشگری") -> "بانک گردشگری"
            sender.contains("KARAFRIN") || body.contains("کارآفرین") -> "بانک کارآفرین"
            else -> "پیامک بانکی"
        }
    }

    private fun extractOtpCode(normalizedBody: String): String? {
        for (pattern in OTP_PATTERNS) {
            val matcher = pattern.matcher(normalizedBody)
            if (matcher.find()) {
                val code = matcher.group(1)
                if (code != null && code.length in 4..8) return code
            }
        }
        return null
    }

    private fun extractUrl(body: String): String? {
        val matcher = URL_PATTERN.matcher(body)
        if (matcher.find()) {
            return matcher.group(1)
        }
        return null
    }

    private fun extractCardNumber(normalizedBody: String): String? {
        val matcher = CARD_PATTERN.matcher(normalizedBody)
        if (matcher.find()) {
            val rawCard = matcher.group(1)
            if (!rawCard.isNullOrBlank()) {
                val digitsOnly = rawCard.replace(Regex("[^0-9\\*]"), "")
                if (digitsOnly.length >= 12) {
                    return digitsOnly
                }
            }
        }
        val simpleCardMatcher = Pattern.compile("کارت[:\\s]*([0-9\\*]{4,16})").matcher(normalizedBody)
        if (simpleCardMatcher.find()) {
            return simpleCardMatcher.group(1)
        }
        return null
    }

    private fun extractTrackingNumber(normalizedBody: String): String? {
        val matcher = TRACKING_PATTERN.matcher(normalizedBody)
        if (matcher.find()) {
            return matcher.group(1)
        }
        return null
    }

    private fun extractAmountDetails(rawBody: String, normalizedBody: String): Triple<Long?, Long?, String?> {
        val matcher = AMOUNT_PATTERN.matcher(normalizedBody)
        if (matcher.find()) {
            val rawNum = matcher.group(1)?.replace(",", "")?.replace("٬", "") ?: return Triple(null, null, null)
            val unit = matcher.group(2) ?: "تومان"
            val valNum = rawNum.toLongOrNull() ?: return Triple(null, null, null)

            val (tomans, rials) = if (unit.contains("ریال", ignoreCase = true) || unit.contains("Rials", ignoreCase = true) || unit.contains("IRR", ignoreCase = true)) {
                Pair(valNum / 10, valNum)
            } else {
                Pair(valNum, valNum * 10)
            }

            val formatted = "${numberFormat.format(tomans)} تومان"
            return Triple(tomans, rials, formatted)
        }
        return Triple(null, null, null)
    }

    private fun extractBalanceDetails(rawBody: String, normalizedBody: String): Triple<Long?, Long?, String?> {
        val matcher = BALANCE_PATTERN.matcher(normalizedBody)
        if (matcher.find()) {
            val rawNum = matcher.group(1)?.replace(",", "")?.replace("٬", "") ?: return Triple(null, null, null)
            val unit = matcher.group(2) ?: "تومان"
            val valNum = rawNum.toLongOrNull() ?: return Triple(null, null, null)

            val (tomans, rials) = if (unit.contains("ریال", ignoreCase = true) || unit.contains("Rials", ignoreCase = true) || unit.contains("IRR", ignoreCase = true)) {
                Pair(valNum / 10, valNum)
            } else {
                Pair(valNum, valNum * 10)
            }

            val formatted = "${numberFormat.format(tomans)} تومان"
            return Triple(tomans, rials, formatted)
        }
        return Triple(null, null, null)
    }
}

