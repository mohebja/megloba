package com.global.sms.core.ai.summary

import com.global.sms.data.entity.MessageCategory
import com.global.sms.data.entity.MessageEntity

data class DailySummaryResult(
    val dateLabel: String,
    val totalReceived: Int,
    val bankCount: Int,
    val otpCount: Int,
    val personalCount: Int,
    val businessCount: Int,
    val spamCount: Int,
    val deliveryCount: Int,
    val summaryPersian: String,
    val summaryEnglish: String,
    val keyActionItems: List<String>
)

object DailyCommunicationSummaryEngine {

    fun generateDailySummary(
        dateLabel: String = "امروز",
        messages: List<MessageEntity>
    ): DailySummaryResult {
        val total = messages.size
        var bank = 0
        var otp = 0
        var personal = 0
        var business = 0
        var spam = 0
        var delivery = 0
        val actionItems = mutableListOf<String>()

        for (msg in messages) {
            val body = msg.body
            when (msg.category) {
                MessageCategory.BANK -> bank++
                MessageCategory.OTP -> otp++
                MessageCategory.PERSONAL -> personal++
                MessageCategory.BUSINESS -> business++
                MessageCategory.SPAM -> spam++
                else -> {
                    if (body.contains("تحویل") || body.contains("مرسوله")) {
                        delivery++
                    }
                }
            }

            if (body.contains("پرداخت") || body.contains("قبض")) {
                actionItems.add("بررسی و پرداخت قبوض")
            }
            if (body.contains("جلسه")) {
                actionItems.add("تایید زمان جلسه")
            }
        }

        val faText = StringBuilder()
        faText.append("$dateLabel: مجموعاً $total پیام دریافت شد. ")
        if (bank > 0) faText.append("$bank پیام بانکی، ")
        if (otp > 0) faText.append("$otp کد تایید (OTP)، ")
        if (business > 0) faText.append("$business پیام کاری مهم، ")
        if (personal > 0) faText.append("$personal پیام شخصی، ")
        if (delivery > 0) faText.append("$delivery سفارش در حال ارسال، ")
        if (spam > 0) faText.append("$spam پیام تبلیغاتی مسدود شده.")

        val enText = StringBuilder()
        enText.append("Today: Received $total total messages. ")
        if (bank > 0) enText.append("$bank bank alerts, ")
        if (otp > 0) enText.append("$otp OTP verification codes, ")
        if (business > 0) enText.append("$business business messages, ")
        if (personal > 0) enText.append("$personal personal chats.")

        return DailySummaryResult(
            dateLabel = dateLabel,
            totalReceived = total,
            bankCount = bank,
            otpCount = otp,
            personalCount = personal,
            businessCount = business,
            spamCount = spam,
            deliveryCount = delivery,
            summaryPersian = faText.toString().removeSuffix(", ") + ".",
            summaryEnglish = enText.toString().removeSuffix(", ") + ".",
            keyActionItems = actionItems.distinct()
        )
    }
}
