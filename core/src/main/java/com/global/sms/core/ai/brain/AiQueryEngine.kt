package com.global.sms.core.ai.brain

import android.content.Context
import com.global.sms.core.parser.BankSmsAnalysis
import com.global.sms.core.parser.BankTransactionParser
import com.global.sms.core.parser.TransactionType
import com.global.sms.core.util.PersianUtils
import com.global.sms.data.dao.MessageDao
import com.global.sms.data.dao.OtpDao
import com.global.sms.data.db.GlobalSmsDatabase
import com.global.sms.data.entity.MessageCategory
import com.global.sms.data.entity.MessageEntity
import com.global.sms.data.entity.OtpEntity
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Assistant Query Engine that generates factual, data-grounded responses
 * derived directly from the application's local SQLite database entities.
 */
object AiQueryEngine {

    /**
     * Inspects active/latest OTP entities from the database.
     */
    fun answerOtpQuery(otps: List<OtpEntity>): String {
        if (otps.isEmpty()) {
            return "در حال حاضر هیچ کد تایید (OTP) فعالی در سیستم ثبت نشده است."
        }

        val latestOtp = otps.first()
        val now = System.currentTimeMillis()
        val isExpired = latestOtp.expiresTimestamp > 0 && latestOtp.expiresTimestamp < now

        val validityText = if (isExpired) {
            "منقضی شده"
        } else if (latestOtp.expiresTimestamp > 0) {
            val remainingMinutes = ((latestOtp.expiresTimestamp - now) / 60000).coerceAtLeast(1)
            "اعتبار تا حدود $remainingMinutes دقیقه دیگر"
        } else {
            "معتبر"
        }

        val serviceName = if (latestOtp.serviceName.isNotBlank()) "برای سرویس ${latestOtp.serviceName}" else "از فرستنده ${latestOtp.address}"

        return "آخرین کد تایید دریافتی: ${latestOtp.code} ($serviceName)\nوضعیت: $validityText"
    }

    /**
     * Summarizes recent real bank transactions from parsed bank SMS messages.
     */
    fun answerBankQuery(bankAnalyses: List<BankSmsAnalysis>): String {
        if (bankAnalyses.isEmpty()) {
            return "هیچ پیامک یا تراکنش بانکی در سیستم یافت نشد."
        }

        val oneDayAgo = System.currentTimeMillis() - 24 * 3600 * 1000L
        val recent24h = bankAnalyses.filter { it.timestamp >= oneDayAgo }
        val targetList = if (recent24h.isNotEmpty()) recent24h else bankAnalyses.take(3)

        val totalDebit = targetList
            .filter { it.transactionType == TransactionType.DEBIT }
            .mapNotNull { it.amountTomans }
            .sum()

        val totalCredit = targetList
            .filter { it.transactionType == TransactionType.CREDIT }
            .mapNotNull { it.amountTomans }
            .sum()

        val banks = targetList.map { it.bankName }.distinct().joinToString("، ")

        val sb = StringBuilder()
        if (recent24h.isNotEmpty()) {
            sb.append("در ۲۴ ساعت گذشته تعداد ${recent24h.size} تراکنش بانکی پردازش شد (${banks}):\n")
        } else {
            sb.append("گزارش آخرین ${targetList.size} تراکنش بانکی (${banks}):\n")
        }

        if (totalDebit > 0) {
            val formatted = PersianUtils.toPersianDigits(String.format(Locale.US, "%,d", totalDebit))
            sb.append("• مجموع برداشت: $formatted تومان\n")
        }
        if (totalCredit > 0) {
            val formatted = PersianUtils.toPersianDigits(String.format(Locale.US, "%,d", totalCredit))
            sb.append("• مجموع واریز: $formatted تومان\n")
        }
        if (totalDebit == 0L && totalCredit == 0L) {
            sb.append("• شامل استعلام موجودی یا پیام‌های خدماتی بانک.\n")
        }

        val latestBalance = targetList.firstOrNull { it.formattedBalance != null }?.formattedBalance
        if (latestBalance != null) {
            sb.append("• آخرین موجودی ثبت‌شده: $latestBalance")
        }

        return sb.toString().trim()
    }

    /**
     * Evaluates real security status and spam folder contents.
     */
    fun answerSecurityQuery(spamMessages: List<MessageEntity>, totalMessages: Int): String {
        return if (spamMessages.isEmpty()) {
            "وضعیت امنیتی: تمام پیام‌های دریافتی (${totalMessages} پیام) بررسی شدند و هیچ پیامک فیشینگ، مشکوک یا اسپم ثبت نشده است."
        } else {
            val latest = spamMessages.first()
            val dateStr = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date(latest.timestamp))
            "هشدار امنیتی: تعداد ${spamMessages.size} پیام مشکوک یا اسپم در پوشه هرزنامه شناسایی و مسدود شده است.\nآخرین مورد مسدود شده از: ${latest.address} (در $dateStr)"
        }
    }

    /**
     * Searches message history and summarizes actual matched message snippets.
     */
    fun answerGeneralMessageSearch(query: String, allMessages: List<MessageEntity>): String {
        val cleanQuery = query.trim()
        if (cleanQuery.isBlank()) {
            return "لطفاً عبارت مورد نظر خود را برای جستجو در پیام‌ها وارد کنید."
        }

        val stopwords = setOf("آیا", "پیامی", "پیام", "از", "درباره", "دارم", "هست", "بگو", "چیست", "رو", "را", "پیامک", "های", "امروز", "داشتم", "بگرد", "جستجو", "کن")
        val tokens = cleanQuery.split(Regex("[\\s,،؟?]+"))
            .map { it.trim() }
            .filter { it.length > 1 && !stopwords.contains(it) }

        val matches = allMessages.filter { msg ->
            if (msg.body.contains(cleanQuery, ignoreCase = true) || msg.address.contains(cleanQuery, ignoreCase = true)) {
                true
            } else {
                tokens.isNotEmpty() && tokens.any { token ->
                    msg.body.contains(token, ignoreCase = true) || msg.address.contains(token, ignoreCase = true)
                }
            }
        }.sortedByDescending { it.timestamp }

        if (matches.isEmpty()) {
            return "هیچ پیام یا سابقه مرتبطی با «$cleanQuery» در پیامک‌های دستگاه یافت نشد."
        }

        val sb = StringBuilder()
        sb.append("تعداد ${matches.size} پیام مرتبط با «$cleanQuery» یافت شد:\n")
        val sampleMatches = matches.take(3)
        sampleMatches.forEachIndexed { index, msg ->
            val dateStr = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date(msg.timestamp))
            val excerpt = if (msg.body.length > 80) msg.body.take(77) + "..." else msg.body
            sb.append("\n${index + 1}. از ${msg.address} ($dateStr):\n«$excerpt»")
        }
        return sb.toString().trim()
    }

    /**
     * Executes queries asynchronously by pulling real data directly from Room database DAOs.
     */
    suspend fun processUserQuery(
        context: Context,
        userQuery: String
    ): String {
        val db = GlobalSmsDatabase.getInstance(context)
        val otpDao = db.otpDao()
        val messageDao = db.messageDao()

        return when {
            userQuery.contains("کد تایید") || userQuery.contains("OTP") || userQuery.contains("رمز پویا") -> {
                val otps = otpDao.getActiveOtpsFlow().firstOrNull() ?: emptyList()
                val fallbackOtps = if (otps.isEmpty()) {
                    otpDao.getAllOtpsFlow().firstOrNull() ?: emptyList()
                } else otps
                answerOtpQuery(fallbackOtps)
            }

            userQuery.contains("بانک") || userQuery.contains("تراکنش") || userQuery.contains("پرداخت") || userQuery.contains("واریز") || userQuery.contains("برداشت") || userQuery.contains("هزینه") -> {
                val bankMessages = messageDao.getBankMessages().firstOrNull() ?: emptyList()
                val bankAnalyses = bankMessages.map { msg ->
                    BankTransactionParser.analyzeMessage(
                        sender = msg.address,
                        body = msg.body,
                        messageId = msg.id,
                        timestamp = msg.timestamp
                    )
                }
                answerBankQuery(bankAnalyses)
            }

            userQuery.contains("اسپم") || userQuery.contains("مشکوک") || userQuery.contains("امنیت") || userQuery.contains("فیشینگ") || userQuery.contains("هشدار") -> {
                val spamList = messageDao.getSpamMessages().firstOrNull() ?: emptyList()
                val total = messageDao.getTotalMessageCountOnce()
                answerSecurityQuery(spamList, total)
            }

            else -> {
                val allMessages = messageDao.getAllMessagesSync()
                answerGeneralMessageSearch(userQuery, allMessages)
            }
        }
    }
}
