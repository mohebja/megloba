package com.global.sms.core.automation

import com.global.sms.core.ai.intelligence.MessageCategory

data class AutomationTemplate(
    val id: String,
    val title: String,
    val description: String,
    val categoryName: String,
    val rule: AutomationRule,
    val isPopular: Boolean = false,
    val iconName: String = "ic_auto"
)

class AutomationTemplateRepository {

    fun getMarketplaceTemplates(): List<AutomationTemplate> {
        return listOf(
            AutomationTemplate(
                id = "tpl_bank_auto",
                title = "دسته‌بندی و بایگانی تراکنش‌های بانکی",
                description = "هنگام دریافت SMS بانکی، مبلغ و کارت شناسایی شده و پیام خودکار بایگانی می‌گردد.",
                categoryName = "مالی و بانک",
                rule = AutomationRule(
                    id = "rule_preset_bank",
                    name = "مدیریت خودکار تراکنش بانکی",
                    triggerType = AutomationTriggerType.CATEGORY_IS,
                    triggerValue = MessageCategory.BANKING.name,
                    actionType = AutomationActionType.AUTO_CATEGORIZE
                ),
                isPopular = true
            ),
            AutomationTemplate(
                id = "tpl_otp_copy",
                title = "اعلام و کپی هوشمند کد ورود (OTP)",
                description = "استخراج آنی کد 4 تا 8 رقمی رمز پویا و کپی خودکار در حافظه گوشی.",
                categoryName = "امنیت و OTP",
                rule = AutomationRule(
                    id = "rule_preset_otp",
                    name = "کپی هوشمند رمز پویا",
                    triggerType = AutomationTriggerType.OTP_RECEIVED,
                    triggerValue = "OTP",
                    actionType = AutomationActionType.COPY_OTP
                ),
                isPopular = true
            ),
            AutomationTemplate(
                id = "tpl_customer_followup",
                title = "پاسخ‌دهی و ثبت یادآور مشتریان کسب‌وکار",
                description = "هنگام استعلام قیمت توسط مشتری، پاسخ اولیه ارسال شده و یادآور پیگیری ثبت می‌شود.",
                categoryName = "کسب‌وکار CRM",
                rule = AutomationRule(
                    id = "rule_preset_biz",
                    name = "پاسخ خودکار به مشتریان",
                    triggerType = AutomationTriggerType.BODY_CONTAINS,
                    triggerValue = "قیمت",
                    actionType = AutomationActionType.AUTO_REPLY,
                    actionValue = "سلام، درخواست شما دریافت شد و کارشناسان ما به زودی با شما تماس خواهند گرفت."
                ),
                isPopular = true
            ),
            AutomationTemplate(
                id = "tpl_spam_blocker",
                title = "مسدودسازی هوشمند پیامک‌های تبلیغاتی اسپم",
                description = "شناسایی خودکار الگوهای کلاهبرداری و تبلیغاتی و انتقال مستقیم به فهرست سیاه.",
                categoryName = "امنیت",
                rule = AutomationRule(
                    id = "rule_preset_spam",
                    name = "بلاک خودکار اسپم",
                    triggerType = AutomationTriggerType.SPAM_DETECTED,
                    triggerValue = "SPAM",
                    actionType = AutomationActionType.BLOCK_SENDER
                ),
                isPopular = false
            ),
            AutomationTemplate(
                id = "tpl_delivery_tracker",
                title = "شناسایی و کپی کد رهگیری مرسوله‌ها",
                description = "کپی کد رهگیری پستی به محض دریافت پیامک از شرکت‌های پستی و تیپاکس.",
                categoryName = "پست و مرسوله",
                rule = AutomationRule(
                    id = "rule_preset_delivery",
                    name = "پیگیری خودکار مرسوله",
                    triggerType = AutomationTriggerType.CATEGORY_IS,
                    triggerValue = MessageCategory.DELIVERY.name,
                    actionType = AutomationActionType.SHOW_NOTIFY
                ),
                isPopular = false
            )
        )
    }
}
