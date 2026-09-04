package com.global.sms.core.automation

import java.util.regex.Pattern

/**
 * Intelligent keyword and intent parser for converting natural language automation requests
 * into structured AutomationRule models.
 */
object AutomationIntentParser {

    fun parsePromptToRule(prompt: String): AutomationRule {
        val cleanPrompt = prompt.trim()

        // 1. Extract Trigger Type and Value
        val phonePattern = Pattern.compile("(\\+?\\d{7,14})")
        val phoneMatcher = phonePattern.matcher(cleanPrompt)
        val hasPhone = phoneMatcher.find()
        val extractedPhone = if (hasPhone) phoneMatcher.group(1) else null

        val (triggerType, triggerValue) = when {
            cleanPrompt.contains("کد تایید", ignoreCase = true) ||
            cleanPrompt.contains("رمز پویا", ignoreCase = true) ||
            cleanPrompt.contains("OTP", ignoreCase = true) -> {
                AutomationTriggerType.OTP_RECEIVED to "OTP"
            }

            cleanPrompt.contains("اسپم", ignoreCase = true) ||
            cleanPrompt.contains("هرزنامه", ignoreCase = true) ||
            cleanPrompt.contains("فیشینگ", ignoreCase = true) -> {
                AutomationTriggerType.SPAM_DETECTED to "SPAM"
            }

            cleanPrompt.contains("بانک", ignoreCase = true) ||
            cleanPrompt.contains("تراکنش", ignoreCase = true) ||
            cleanPrompt.contains("واریز", ignoreCase = true) ||
            cleanPrompt.contains("برداشت", ignoreCase = true) -> {
                AutomationTriggerType.CATEGORY_IS to "BANKING"
            }

            cleanPrompt.contains("فاکتور", ignoreCase = true) ||
            cleanPrompt.contains("بدهی", ignoreCase = true) ||
            cleanPrompt.contains("قبض", ignoreCase = true) -> {
                AutomationTriggerType.PAYMENT_REMINDER to "فاکتور"
            }

            cleanPrompt.contains("شکایت", ignoreCase = true) ||
            cleanPrompt.contains("نارضایتی", ignoreCase = true) ||
            cleanPrompt.contains("پشتیبانی", ignoreCase = true) -> {
                AutomationTriggerType.COMPLAINT_DETECTED to "شکایت"
            }

            cleanPrompt.contains("VIP", ignoreCase = true) ||
            cleanPrompt.contains("مشتری ویژه", ignoreCase = true) ||
            cleanPrompt.contains("ویژه", ignoreCase = true) -> {
                AutomationTriggerType.VIP_MESSAGE to "VIP"
            }

            cleanPrompt.contains("مشتری جدید", ignoreCase = true) -> {
                AutomationTriggerType.NEW_CUSTOMER_MESSAGE to "سلام"
            }

            extractedPhone != null && (cleanPrompt.contains("از شماره") || cleanPrompt.contains("از فرستنده") || cleanPrompt.contains("از")) -> {
                AutomationTriggerType.SENDER_CONTAINS to extractedPhone
            }

            else -> {
                val extractedKeyword = extractKeyword(cleanPrompt)
                AutomationTriggerType.BODY_CONTAINS to extractedKeyword
            }
        }

        // 2. Extract Action Type and Value
        val (actionType, actionValue) = when {
            cleanPrompt.contains("مسدود", ignoreCase = true) ||
            cleanPrompt.contains("بلاک", ignoreCase = true) ||
            cleanPrompt.contains("رد کن", ignoreCase = true) -> {
                AutomationActionType.BLOCK_SENDER to null
            }

            cleanPrompt.contains("کپی", ignoreCase = true) ||
            cleanPrompt.contains("استخراج", ignoreCase = true) -> {
                AutomationActionType.COPY_OTP to null
            }

            cleanPrompt.contains("فوروارد", ignoreCase = true) ||
            cleanPrompt.contains("ارسال کن", ignoreCase = true) ||
            cleanPrompt.contains("بفرست", ignoreCase = true) ||
            cleanPrompt.contains("هدایت", ignoreCase = true) -> {
                AutomationActionType.FORWARD_SMS to (extractedPhone ?: "شماره مقصد")
            }

            cleanPrompt.contains("مالی", ignoreCase = true) ||
            cleanPrompt.contains("حسابداری", ignoreCase = true) ||
            cleanPrompt.contains("هزینه", ignoreCase = true) -> {
                AutomationActionType.CREATE_FINANCE_RECORD to "ثبت در دفاتر مالی"
            }

            cleanPrompt.contains("تسک", ignoreCase = true) ||
            cleanPrompt.contains("کار", ignoreCase = true) ||
            cleanPrompt.contains("وظیفه", ignoreCase = true) ||
            cleanPrompt.contains("یادآوری", ignoreCase = true) -> {
                AutomationActionType.CREATE_TASK to "پیگیری خودکار: $triggerValue"
            }

            cleanPrompt.contains("مدیر", ignoreCase = true) ||
            cleanPrompt.contains("اطلاع بده", ignoreCase = true) ||
            cleanPrompt.contains("هشدار", ignoreCase = true) -> {
                AutomationActionType.NOTIFY_MANAGER to "اطلاع‌رسانی فوری به سرپرست"
            }

            cleanPrompt.contains("دسته‌بندی", ignoreCase = true) ||
            cleanPrompt.contains("پوشه", ignoreCase = true) -> {
                AutomationActionType.AUTO_CATEGORIZE to null
            }

            cleanPrompt.contains("پاسخ", ignoreCase = true) ||
            cleanPrompt.contains("جواب", ignoreCase = true) ||
            cleanPrompt.contains("پیام بده", ignoreCase = true) -> {
                val replyText = extractReplyText(cleanPrompt) ?: "درخواست شما دریافت و بررسی خواهد شد."
                AutomationActionType.AUTO_REPLY to replyText
            }

            else -> {
                AutomationActionType.SHOW_NOTIFY to "اعلان فوری"
            }
        }

        // 3. Construct Rule Name
        val ruleName = generateRuleName(triggerType, triggerValue, actionType)

        return AutomationRule(
            id = "rule_${System.currentTimeMillis()}",
            name = ruleName,
            triggerType = triggerType,
            triggerValue = triggerValue,
            actionType = actionType,
            actionValue = actionValue,
            isEnabled = true
        )
    }

    private fun extractKeyword(text: String): String {
        // Look for quoted words: '...', "...", «...»
        val quoteRegex = "['\"«](.*?)['\"»]".toRegex()
        val quoteMatch = quoteRegex.find(text)
        if (quoteMatch != null && quoteMatch.groupValues[1].isNotBlank()) {
            return quoteMatch.groupValues[1].trim()
        }

        // Look for keyword after specific trigger words
        val keywordTriggers = listOf("حاوی", "شامل", "کلمه", "عبارت", "با متن")
        for (kt in keywordTriggers) {
            val idx = text.indexOf(kt)
            if (idx != -1) {
                val after = text.substring(idx + kt.length).trim()
                val candidate = after.split(Regex("[\\s,،.]+")).firstOrNull { it.isNotBlank() }
                if (!candidate.isNullOrBlank()) {
                    return candidate
                }
            }
        }

        val words = text.split(Regex("[\\s,،.]+")).filter { it.length > 2 }
        return words.firstOrNull() ?: text.take(15)
    }

    private fun extractReplyText(text: String): String? {
        val replyIndicators = listOf("پاسخ بده:", "بنویس:", "متن:", "جواب بده:")
        for (indicator in replyIndicators) {
            val idx = text.indexOf(indicator)
            if (idx != -1) {
                val reply = text.substring(idx + indicator.length).trim()
                if (reply.isNotBlank()) return reply
            }
        }
        return null
    }

    private fun generateRuleName(
        triggerType: AutomationTriggerType,
        triggerValue: String,
        actionType: AutomationActionType
    ): String {
        val actionName = when (actionType) {
            AutomationActionType.BLOCK_SENDER -> "مسدودسازی"
            AutomationActionType.COPY_OTP -> "کپی کد تایید"
            AutomationActionType.FORWARD_SMS -> "هدایت پیامک"
            AutomationActionType.CREATE_FINANCE_RECORD -> "ثبت در امور مالی"
            AutomationActionType.CREATE_TASK -> "ایجاد وظیفه خودکار"
            AutomationActionType.NOTIFY_MANAGER -> "اعلام به مدیر"
            AutomationActionType.AUTO_CATEGORIZE -> "دسته‌بندی خودکار"
            AutomationActionType.AUTO_REPLY -> "پاسخ خودکار"
            AutomationActionType.SHOW_NOTIFY -> "نمایش اعلان"
            AutomationActionType.CATEGORIZE_CUSTOMER -> "دسته‌بندی مخاطب"
            AutomationActionType.GENERATE_REPLY_SUGGESTION -> "پیشنهاد پاسخ"
            AutomationActionType.ARCHIVE -> "بایگانی خودکار"
        }

        val triggerName = when (triggerType) {
            AutomationTriggerType.BODY_CONTAINS -> "پیام‌های حاوی «$triggerValue»"
            AutomationTriggerType.SENDER_CONTAINS -> "پیام‌های فرستنده $triggerValue"
            AutomationTriggerType.CATEGORY_IS -> "پیام‌های دسته $triggerValue"
            AutomationTriggerType.OTP_RECEIVED -> "کدهای یکبار مصرف"
            AutomationTriggerType.SPAM_DETECTED -> "پیام‌های اسپم و فیشینگ"
            AutomationTriggerType.PAYMENT_REMINDER -> "قبوض و فاکتورها"
            AutomationTriggerType.COMPLAINT_DETECTED -> "شکایات و انتقادات"
            AutomationTriggerType.VIP_MESSAGE -> "پیام‌های مخاطبان VIP"
            AutomationTriggerType.NEW_CUSTOMER_MESSAGE -> "پیام‌های مشتری جدید"
            AutomationTriggerType.KEYWORD_DETECTED -> "کلیدواژه $triggerValue"
        }

        return "$actionName برای $triggerName"
    }
}
