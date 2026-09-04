package com.global.sms.core.rule

import com.global.sms.core.classifier.SmsClassifierEngine
import com.global.sms.data.entity.CategoryEntity
import com.global.sms.data.entity.MessageCategory

object CategoryRuleEngine {

    data class RuleResult(
        val categoryEnum: MessageCategory,
        val customCategoryName: String,
        val customCategoryId: Long?
    )

    /**
     * Evaluates incoming message parameters against custom user category rules,
     * spam/phishing detectors, and the intelligent SmsClassifierEngine.
     */
    fun classifyMessage(
        sender: String,
        body: String,
        customCategories: List<CategoryEntity>,
        isSpamOrPhishing: Boolean = false,
        isBank: Boolean = false,
        isOtp: Boolean = false
    ): RuleResult {
        // 1. Phishing / Spam Detection
        if (isSpamOrPhishing) {
            val spamCat = customCategories.firstOrNull { 
                it.name.contains("اسپم") || it.name.contains("تبلیغ") 
            }
            return RuleResult(
                categoryEnum = MessageCategory.SPAM,
                customCategoryName = spamCat?.name ?: "تبلیغات و اسپم",
                customCategoryId = spamCat?.id
            )
        }

        // 2. Custom User-Defined Category Rules
        val cleanBody = body.lowercase()
        val cleanSender = sender.lowercase()
        val sortedCategories = customCategories.sortedByDescending { it.priority }
        for (category in sortedCategories) {
            if (category.autoRule.isBlank()) continue
            val keywords = category.autoRule.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }
            val isMatched = keywords.any { rule ->
                cleanBody.contains(rule) || cleanSender.contains(rule)
            }
            if (isMatched) {
                return RuleResult(
                    categoryEnum = mapNameToEnum(category.name),
                    customCategoryName = category.name,
                    customCategoryId = category.id
                )
            }
        }

        // 3. Intelligent Classification Engine
        val classifierResult = SmsClassifierEngine.classifyMessage(sender, body)
        val matchedCategory = customCategories.firstOrNull { cat ->
            mapNameToEnum(cat.name) == classifierResult.category
        }

        val categoryDisplayName = matchedCategory?.name ?: when (classifierResult.category) {
            MessageCategory.OTP -> "کد تایید (OTP)"
            MessageCategory.BANK -> "بانک"
            MessageCategory.TRANSACTIONS -> "تراکنش مالی"
            MessageCategory.SPAM -> "اسپم"
            MessageCategory.ADVERTISEMENT -> "تبلیغات"
            MessageCategory.BUSINESS -> "اداری و تجاری"
            MessageCategory.SHOPPING -> "خرید آنلاین"
            MessageCategory.DELIVERY -> "مرسوله و پست"
            MessageCategory.GOVERNMENT -> "سامانه دولتی"
            MessageCategory.PERSONAL -> "شخصی"
            MessageCategory.UNKNOWN -> "نامشخص"
            else -> classifierResult.category.name
        }

        return RuleResult(
            categoryEnum = classifierResult.category,
            customCategoryName = categoryDisplayName,
            customCategoryId = matchedCategory?.id
        )
    }

    private fun mapNameToEnum(name: String): MessageCategory {
        val lower = name.lowercase()
        return when {
            lower.contains("رمز") || lower.contains("otp") -> MessageCategory.OTP
            lower.contains("تراکنش") || lower.contains("مالی") -> MessageCategory.TRANSACTIONS
            lower.contains("بانک") || lower.contains("bank") -> MessageCategory.BANK
            lower.contains("اسپم") || lower.contains("spam") -> MessageCategory.SPAM
            lower.contains("تبلیغ") || lower.contains("ad") -> MessageCategory.ADVERTISEMENT
            lower.contains("خرید") || lower.contains("فروشگاه") -> MessageCategory.SHOPPING
            lower.contains("پست") || lower.contains("مرسوله") || lower.contains("ارسال") -> MessageCategory.DELIVERY
            lower.contains("دولت") || lower.contains("سامانه") || lower.contains("ثنا") -> MessageCategory.GOVERNMENT
            lower.contains("تجاری") || lower.contains("کار") || lower.contains("اداری") -> MessageCategory.BUSINESS
            lower.contains("مهم") || lower.contains("ضروری") -> MessageCategory.IMPORTANT
            lower.contains("خصوصی") -> MessageCategory.PRIVATE
            else -> MessageCategory.PERSONAL
        }
    }
}

