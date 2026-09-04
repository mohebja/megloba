package com.global.sms.core.ai.intelligence

enum class ReplyTone {
    FRIENDLY,
    FORMAL,
    BUSINESS,
    SHORT
}

data class ContextualSmartReply(
    val replyText: String,
    val tone: ReplyTone,
    val language: String = "fa",
    val quickActionType: String? = null
)

class SmartReplyV2Engine {

    fun generateReplies(
        incomingMessage: String,
        category: MessageCategory = MessageCategory.PERSONAL
    ): List<ContextualSmartReply> {
        val text = incomingMessage.lowercase()
        val replies = mutableListOf<ContextualSmartReply>()

        when {
            text.contains("قیمت") || text.contains("چنده") || text.contains("هزینه") -> {
                replies.add(ContextualSmartReply("سلام، قیمت خدمت/محصول ... تومان می‌باشد.", ReplyTone.BUSINESS))
                replies.add(ContextualSmartReply("در صورت تمایل اطلاعات و پیش‌فاکتور ارسال می‌شود.", ReplyTone.FORMAL))
                replies.add(ContextualSmartReply("سلام عزیز، قیمت رو برات فرستادم.", ReplyTone.FRIENDLY))
                replies.add(ContextualSmartReply("مبلغ ارسال شد.", ReplyTone.SHORT))
            }
            text.contains("سلام") || text.contains("درود") -> {
                replies.add(ContextualSmartReply("سلام و درود، در خدمت شما هستم.", ReplyTone.BUSINESS))
                replies.add(ContextualSmartReply("سلام روز بخیر، چطور می‌تونم کمکتون کنم؟", ReplyTone.FORMAL))
                replies.add(ContextualSmartReply("سلام رفیق! چطوری؟", ReplyTone.FRIENDLY))
            }
            text.contains("شماره کارت") || text.contains("کارت") || text.contains("واریز") -> {
                replies.add(ContextualSmartReply("شماره کارت: ۶۰۳۷۹۹۷۹۰۰۰۰۰۰۰۰ به نام حساب", ReplyTone.BUSINESS, quickActionType = "SEND_CARD"))
                replies.add(ContextualSmartReply("مبلغ واریز شد، عکس فیش ارسال می‌شود.", ReplyTone.FORMAL))
                replies.add(ContextualSmartReply("ممنون، الان واریز می‌کنم.", ReplyTone.FRIENDLY))
            }
            text.contains("کجایی") || text.contains("رسیدی") -> {
                replies.add(ContextualSmartReply("در مسیر هستم، تا چند دقیقه دیگه می‌رسم.", ReplyTone.FRIENDLY))
                replies.add(ContextualSmartReply("رسیدم محل جلسه.", ReplyTone.FORMAL))
            }
            category == MessageCategory.BANKING -> {
                replies.add(ContextualSmartReply("تراکنش در دفترچه ثبت شد.", ReplyTone.SHORT))
                replies.add(ContextualSmartReply("بررسی و تایید شد.", ReplyTone.FORMAL))
            }
            category == MessageCategory.OTP -> {
                replies.add(ContextualSmartReply("کد تایید کپی شد.", ReplyTone.SHORT, quickActionType = "COPY_OTP"))
            }
            else -> {
                replies.add(ContextualSmartReply("ممنون، بررسی می‌کنم و اطلاع میدم.", ReplyTone.FORMAL))
                replies.add(ContextualSmartReply("سلام، متوجه شدم. تشکر.", ReplyTone.BUSINESS))
                replies.add(ContextualSmartReply("باشه حتماً رفیق!", ReplyTone.FRIENDLY))
                replies.add(ContextualSmartReply("تایید شد.", ReplyTone.SHORT))
            }
        }

        return replies
    }
}
