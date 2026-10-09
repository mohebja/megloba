package com.global.sms.engine.notification

object PrivacyNotificationHelper {

    private val SENSITIVE_PATTERNS = listOf(
        "واریز", "برداشت", "مانده", "موجودی", "رمز پویا", "کد تایید", "OTP"
    )

    fun maskSensitiveContent(body: String, isLockScreen: Boolean): String {
        if (!isLockScreen) return body
        val containsSensitiveData = SENSITIVE_PATTERNS.any { body.contains(it, ignoreCase = true) }
        return if (containsSensitiveData) {
            "پیامک حاوی اطلاعات حساس مالی یا احراز هویت (جهت مشاهده قفل گوشی را باز کنید)"
        } else {
            body
        }
    }
}
