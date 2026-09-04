package com.global.sms.core.ai.summarizer

import com.global.sms.core.ai.nlp.LocalNlpEngine
import com.global.sms.data.entity.MessageEntity
import java.util.Locale

object ConversationSummarizerEngine {

    /**
     * Generates a concise local summary of a message thread or single message.
     */
    fun summarizeThread(messages: List<MessageEntity>): String {
        if (messages.isEmpty()) return "هیچ پیامکی در این گفتگو یافت نشد."

        val sorted = messages.sortedBy { it.timestamp }
        val totalCount = sorted.size
        val latest = sorted.last()

        val textBuffer = StringBuilder()
        var hasBankTx = false
        var hasOtp = false
        var hasDelivery = false
        var hasAppointment = false

        for (msg in sorted) {
            val body = LocalNlpEngine.normalizeDigits(msg.body.lowercase(Locale.ROOT))
            if (body.contains("واریز") || body.contains("برداشت") || body.contains("مانده")) hasBankTx = true
            if (body.contains("کد ورود") || body.contains("کد تایید") || body.contains("رمز پویا")) hasOtp = true
            if (body.contains("پست") || body.contains("مرسوله") || body.contains("تحویل") || body.contains("سفارش")) hasDelivery = true
            if (body.contains("جلسه") || body.contains("قرار") || body.contains("ساعت")) hasAppointment = true
        }

        when {
            hasBankTx -> textBuffer.append("خلاصه گفتگو ($totalCount پیامک): اطلاعیه‌ها و تراکنش‌های مالی بانک. ")
            hasOtp -> textBuffer.append("خلاصه گفتگو ($totalCount پیامک): کدهای تایید و رمزهای یکبارمصرف ورود. ")
            hasDelivery -> textBuffer.append("خلاصه گفتگو ($totalCount پیامک): هماهنگی ارسال، مرسوله و زمان تحویل سفارش. ")
            hasAppointment -> textBuffer.append("خلاصه گفتگو ($totalCount پیامک): هماهنگی زمان و مکان جلسه کاری یا ملاقات. ")
            else -> textBuffer.append("خلاصه گفتگو ($totalCount پیامک): گفتگوی شخصی یا کاری. ")
        }

        val snippet = latest.body.take(80).replace("\n", " ")
        textBuffer.append("آخرین پیام: «$snippet...»")

        return textBuffer.toString()
    }
}
