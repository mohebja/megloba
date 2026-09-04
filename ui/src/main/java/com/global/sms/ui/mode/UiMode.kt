package com.global.sms.ui.mode

enum class UiMode(val displayName: String, val description: String) {
    CLASSIC(
        displayName = "کلاسیک (Classic)",
        description = "رابط کاربری ساده، سریع و سبک مشابه پیام‌رسان‌های سنتی"
    ),
    SMART(
        displayName = "هوشمند (Smart AI)",
        description = "دسته‌بندی هوشمند، خلاصه ساز AI، کپی سریع OTP و پاسخ‌های پیشنهادی"
    ),
    ENTERPRISE(
        displayName = "سازمانی (Enterprise CRM)",
        description = "داشبورد سازمانی، مدیریت مشتریان CRM، قالب‌های کسب‌وکار و اتوماسیون"
    )
}
