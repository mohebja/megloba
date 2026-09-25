package com.global.sms.core.smart

import com.global.sms.core.util.PersianUtils
import java.util.regex.Pattern

sealed class SmartAction(
    val title: String,
    val actionType: SmartActionType
) {
    data class CopyOtp(val code: String) : SmartAction("کپی کد تایید ($code)", SmartActionType.OTP)
    data class PayBill(val billId: String, val paymentId: String, val amount: Long = 0L) : 
        SmartAction("پرداخت قبض", SmartActionType.BILL)
    data class CalendarReminder(val titleText: String, val dateOrTime: String) : 
        SmartAction("افزودن به تقویم", SmartActionType.CALENDAR)
    data class TrackDelivery(val trackingNumber: String) : 
        SmartAction("پیگیری مرسوله ($trackingNumber)", SmartActionType.TRACKING)
}

enum class SmartActionType {
    OTP,
    BILL,
    CALENDAR,
    TRACKING
}

object SmartActionExtractor {

    // Regex for OTP Extraction (4 to 8 digits with context keywords)
    private val OTP_PATTERN = Pattern.compile("(?i)(?:کد|رمز|تایید|کلمه عبور|code|otp|passcode)[:\\s]*([0-9]{4,8})")
    private val STANDALONE_OTP_PATTERN = Pattern.compile("(?<!\\d)(\\d{4,8})(?!\\d)")

    // Bill extraction regex (شناسه قبض و شناسه پرداخت)
    private val BILL_ID_PATTERN = Pattern.compile("(?i)(?:شناسه قبض|قبض)[:\\s]*(\\d{6,15})")
    private val PAYMENT_ID_PATTERN = Pattern.compile("(?i)(?:شناسه پرداخت|پرداخت)[:\\s]*(\\d{6,15})")

    // Post tracking number pattern (24 digits or custom tracking code)
    private val POST_TRACKING_PATTERN = Pattern.compile("(?i)(?:کد رهگیری|مرسوله|پستی)[:\\s]*(\\d{10,24})")

    fun extractActions(body: String, sender: String = ""): List<SmartAction> {
        val actions = mutableListOf<SmartAction>()
        val normalizedBody = PersianUtils.toEnglishDigits(body)

        // 1. Bill Payment Detection
        val billMatcher = BILL_ID_PATTERN.matcher(normalizedBody)
        val payMatcher = PAYMENT_ID_PATTERN.matcher(normalizedBody)
        if (billMatcher.find() && payMatcher.find()) {
            val billId = billMatcher.group(1) ?: ""
            val payId = payMatcher.group(1) ?: ""
            if (billId.isNotEmpty() && payId.isNotEmpty()) {
                actions.add(SmartAction.PayBill(billId, payId))
            }
        }

        // 2. OTP Detection
        val otpMatcher = OTP_PATTERN.matcher(normalizedBody)
        if (otpMatcher.find()) {
            val code = otpMatcher.group(1) ?: ""
            if (code.isNotEmpty()) {
                actions.add(SmartAction.CopyOtp(code))
            }
        } else {
            // Check if sender is banking/service and message contains short digits
            val isServiceSender = sender.contains("bank", ignoreCase = true) ||
                    sender.matches(Regex("^[0-9+]{4,7}$")) ||
                    sender.contains("OTP", ignoreCase = true)
            if (isServiceSender) {
                val standalone = STANDALONE_OTP_PATTERN.matcher(normalizedBody)
                if (standalone.find()) {
                    val code = standalone.group(1) ?: ""
                    actions.add(SmartAction.CopyOtp(code))
                }
            }
        }

        // 3. Post Tracking Detection
        val postMatcher = POST_TRACKING_PATTERN.matcher(normalizedBody)
        if (postMatcher.find()) {
            val trackingNum = postMatcher.group(1) ?: ""
            if (trackingNum.isNotEmpty()) {
                actions.add(SmartAction.TrackDelivery(trackingNum))
            }
        }

        // 4. Meeting / Cheque / Calendar Reminder Detection
        if (normalizedBody.contains("نوبت") || normalizedBody.contains("جلسه") || normalizedBody.contains("صیاد") || normalizedBody.contains("سررسید")) {
            val dateSnippet = if (normalizedBody.contains("تاریخ")) {
                normalizedBody.substringAfter("تاریخ").take(15).trim()
            } else {
                "یادآور پیام"
            }
            actions.add(SmartAction.CalendarReminder("یادآوری نوبت/رویداد", dateSnippet))
        }

        return actions
    }
}
