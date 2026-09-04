package com.global.sms.core.ai.smartreply

import com.global.sms.core.ai.nlp.LocalNlpEngine
import com.global.sms.data.dao.SmartReplyDao
import com.global.sms.data.entity.SmartReplyEntity
import java.util.Locale

enum class ReplyTone {
    FORMAL,
    FRIENDLY,
    PROFESSIONAL,
    NEGOTIATION,
    BUSINESS,
    SHORT
}

enum class ConversationContextCategory {
    BANK,
    FAMILY,
    CUSTOMER,
    WORK,
    GENERAL
}

data class AdvancedSmartReplyV3Result(
    val persianReplies: Map<ReplyTone, List<String>>,
    val englishReplies: Map<ReplyTone, List<String>>,
    val detectedContext: ConversationContextCategory,
    val requiresUserConfirmation: Boolean = true // MANDATORY RULE: Never auto-send
)

data class ToneBasedReplies(
    val business: List<String>,
    val friendly: List<String>,
    val short: List<String>
)

object SmartReplyEngine {

    fun generateAdvancedRepliesV3(incomingBody: String): AdvancedSmartReplyV3Result {
        val clean = LocalNlpEngine.normalizeDigits(incomingBody.lowercase(Locale.ROOT))

        val context = when {
            clean.contains("بانک") || clean.contains("واریز") || clean.contains("حساب") -> ConversationContextCategory.BANK
            clean.contains("مامان") || clean.contains("بابا") || clean.contains("خونه") || clean.contains("عزیز") -> ConversationContextCategory.FAMILY
            clean.contains("قیمت") || clean.contains("خرید") || clean.contains("مشتری") || clean.contains("فاکتور") -> ConversationContextCategory.CUSTOMER
            clean.contains("جلسه") || clean.contains("پروژه") || clean.contains("گزارش") -> ConversationContextCategory.WORK
            else -> ConversationContextCategory.GENERAL
        }

        val faMap = mutableMapOf<ReplyTone, List<String>>()
        val enMap = mutableMapOf<ReplyTone, List<String>>()

        when (context) {
            ConversationContextCategory.BANK -> {
                faMap[ReplyTone.FORMAL] = listOf("با تشکر، اطلاعات حساب دریافت شد.", "پیامک بانکی جهت بررسی ثبت شد.")
                faMap[ReplyTone.PROFESSIONAL] = listOf("رسید بانکی با موفقیت پردازش شد.", "با سپاس از اطلاع‌رسانی امور مالی.")
                faMap[ReplyTone.NEGOTIATION] = listOf("لطفاً فاکتور نهایی را بفرستید.", "در صورت نیاز به تسویه مجدد تماس بگیرید.")
                faMap[ReplyTone.FRIENDLY] = listOf("ممنون دم دستت درد نکنه!", "دستت طلا، رسید به دستم.")

                enMap[ReplyTone.BUSINESS] = listOf("Bank notification received and noted.", "Payment details acknowledged.")
                enMap[ReplyTone.FORMAL] = listOf("Thank you, transaction record verified.", "Receipt logged successfully.")
                enMap[ReplyTone.FRIENDLY] = listOf("Got it, thanks!", "All good, thanks!")
            }
            ConversationContextCategory.WORK -> {
                faMap[ReplyTone.FORMAL] = listOf("جناب عالی، پیام شما جهت اقدام بررسی خواهد شد.", "با سلام و احترام، هماهنگ گردید.")
                faMap[ReplyTone.PROFESSIONAL] = listOf("با تشکر، صورت‌جلسه هماهنگ گردید.", "گزارش مربوطه ارسال خواهد شد.")
                faMap[ReplyTone.NEGOTIATION] = listOf("امکان تغییر زمان جلسه وجود دارد؟", "پیشنهاد می‌کنم شرایط را مکتوب بررسی کنیم.")
                faMap[ReplyTone.FRIENDLY] = listOf("سلام رفیق، ردیفه حتماً انجامش میدم!", "اوکیه، فردا می‌بینمت.")

                enMap[ReplyTone.BUSINESS] = listOf("Acknowledged. Will process and update shortly.", "Meeting confirmed as scheduled.")
                enMap[ReplyTone.FORMAL] = listOf("Dear colleague, your request is being reviewed.", "Thank you for the operational update.")
                enMap[ReplyTone.FRIENDLY] = listOf("Sounds good! See you tomorrow.", "Sure thing, working on it now.")
            }
            ConversationContextCategory.CUSTOMER -> {
                faMap[ReplyTone.FORMAL] = listOf("مشتری گرامی، درخواست شما ثبت شد.", "با تشکر از همراهی شما با ما.")
                faMap[ReplyTone.PROFESSIONAL] = listOf("پیش‌فاکتور خدمت شما ارسال می‌گردد.", "جهت دریافت مشاوره پاسخگوی شما هستیم.")
                faMap[ReplyTone.NEGOTIATION] = listOf("تخفیف ویژه شامل سفارش‌های عمده می‌باشد.", "مبلغ درخواستی قابل بررسی و توافق است.")
                faMap[ReplyTone.FRIENDLY] = listOf("سلام وقتت بخیر، بله موجود داریم!", "خیلی مخلصیم، در خدمتم.")

                enMap[ReplyTone.BUSINESS] = listOf("Dear Customer, your inquiry is being processed.", "Invoice details have been prepared.")
                enMap[ReplyTone.FORMAL] = listOf("Thank you for choosing our products.", "We appreciate your valuable business.")
                enMap[ReplyTone.FRIENDLY] = listOf("Hi there! We are happy to assist you.", "Thanks for reaching out!")
            }
            else -> {
                faMap[ReplyTone.FORMAL] = listOf("پیام شما دریافت گردید. با تشکر.", "ممنون از ارسال این پیام.")
                faMap[ReplyTone.PROFESSIONAL] = listOf("موضوع بررسی و اطلاع‌رسانی خواهد شد.", "سپاسگزارم، در اسرع وقت اقدام می‌شود.")
                faMap[ReplyTone.NEGOTIATION] = listOf("شرایط را بررسی کرده و پاسخ می‌دهم.", "به زودی در این خصوص صحبت می‌کنیم.")
                faMap[ReplyTone.FRIENDLY] = listOf("سلام، مخلصم! چطوری؟", "دمت گرم، ممنون!")

                enMap[ReplyTone.BUSINESS] = listOf("Message received with thanks.", "Will get back to you shortly.")
                enMap[ReplyTone.FORMAL] = listOf("Thank you for contacting me.", "Your message is acknowledged.")
                enMap[ReplyTone.FRIENDLY] = listOf("Hey! Hope you are doing great.", "Thanks, catch you later!")
            }
        }

        return AdvancedSmartReplyV3Result(
            persianReplies = faMap,
            englishReplies = enMap,
            detectedContext = context,
            requiresUserConfirmation = true
        )
    }

    /**
     * Generates contextual reply suggestions categorized by tone.
     * Note: All replies generated require explicit user action/confirmation before sending.
     */
    fun generateContextualReplies(incomingBody: String): ToneBasedReplies {
        val clean = LocalNlpEngine.normalizeDigits(incomingBody.lowercase(Locale.ROOT))

        val businessReplies = mutableListOf<String>()
        val friendlyReplies = mutableListOf<String>()
        val shortReplies = mutableListOf<String>()

        when {
            clean.contains("جلسه") || clean.contains("قرار") || clean.contains("ساعت") -> {
                businessReplies.addAll(listOf("سلام، وقت جلسه تایید است.", "با تشکر، در زمان مقرر حضور خواهم داشت."))
                friendlyReplies.addAll(listOf("سلام، حتماً می‌بینمت! 😊", "باشه رفیق، عالیه 🙌"))
                shortReplies.addAll(listOf("تایید شد", "باشه"))
            }
            clean.contains("پرداخت") || clean.contains("مبلغ") || clean.contains("تراکنش") -> {
                businessReplies.addAll(listOf("دریافت شد، بررسی و اقدام می‌شود.", "با تشکر از اطلاع‌رسانی شما."))
                friendlyReplies.addAll(listOf("ممنون، الان چک می‌کنم! 👍", "دستت درد نکنه، پرداخت شد"))
                shortReplies.addAll(listOf("ممنون", "انجام شد"))
            }
            else -> {
                businessReplies.addAll(listOf("پیام شما دریافت شد، بررسی می‌کنم.", "با تشکر، پیگیری خواهم کرد."))
                friendlyReplies.addAll(listOf("سلام، خوبی؟ حتماً انجام میشه! 😊", "ممنونم رفیق 🙌"))
                shortReplies.addAll(listOf("باشه", "اوکی"))
            }
        }

        return ToneBasedReplies(
            business = businessReplies,
            friendly = friendlyReplies,
            short = shortReplies
        )
    }

    /**
     * Generates contextual reply suggestions in Persian and English.
     */
    fun generateSmartReplies(incomingBody: String): List<String> {
        val clean = LocalNlpEngine.normalizeDigits(incomingBody.lowercase(Locale.ROOT))

        return when {
            clean.contains("فردا") || clean.contains("وقت داری") || clean.contains("فردا هستی") || clean.contains("available") -> listOf(
                "سلام، تایید شد",
                "ممنون، حضور دارم",
                "لطفاً زمان را تغییر دهید"
            )
            clean.contains("جلسه") || clean.contains("قرار") || clean.contains("ساعت") || clean.contains("زمان") -> listOf(
                "سلام، بله حضور دارم. 👍",
                "لطفاً ساعت دقیق را بفرستید. 🕒",
                "متأسفانه در این زمان امکان پذیر نیست. 🙏"
            )
            clean.contains("سلام") || clean.contains("چطوری") || clean.contains("احوال") -> listOf(
                "سلام، وقت بخیر! ممنون شما چطورید؟ 😊",
                "سلام، خوبم متشکرم. کاری داشتید؟",
                "سلام! بعداً تماس می‌گیرم. 🙏"
            )
            clean.contains("رسید") || clean.contains("تحویل") || clean.contains("انجام شد") -> listOf(
                "بله، دریافت شد. با تشکر! ✅",
                "دست شما درد نکنه، ممنون. 🙌",
                "لطفاً کد پیگیری بفرستید. 📩"
            )
            clean.matches(Regex(".*[a-z]{3,}.*")) -> listOf(
                "Yes, I am available. 👍",
                "No, please suggest another time. 🙏",
                "I will let you know. 📞"
            )
            else -> listOf(
                "سلام، پیام شما دریافت شد. 👍",
                "ممنون از اطلاع‌رسانی شما. 🙏",
                "بررسی می‌کنم و خبر می‌دهم. 📩"
            )
        }
    }

    suspend fun getLearnedReplies(
        smartReplyDao: SmartReplyDao,
        category: String = "GENERAL"
    ): List<String> {
        val dbReplies = smartReplyDao.getTopReplies(5)
        if (dbReplies.isNotEmpty()) {
            return dbReplies.map { it.replyText }
        }
        return emptyList()
    }

    suspend fun recordUserReplyChoice(
        smartReplyDao: SmartReplyDao,
        replyText: String,
        category: String = "GENERAL"
    ) {
        val entity = SmartReplyEntity(
            replyText = replyText,
            category = category,
            usageCount = 1,
            isCustom = true
        )
        smartReplyDao.insertReply(entity)
    }
}
