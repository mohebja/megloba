package com.global.sms.core.ai.v2

enum class DetectedEventType {
    NONE,
    DEADLINE,
    APPOINTMENT,
    PAYMENT_REMINDER,
    CUSTOMER_REQUEST,
    ACTION_REQUIRED
}

data class SmartAiInsightV2(
    val conversationId: String,
    val summary: String,
    val detectedEvent: DetectedEventType,
    val eventTitle: String? = null,
    val eventDateOrTime: String? = null,
    val extractedAmount: String? = null,
    val suggestedActions: List<String>,
    val detectedLanguage: String, // "fa", "en", "ar"
    val isUrgent: Boolean,
    val confidenceScore: Float = 0.98f
)

class AiCommunicationAssistantV2 {

    fun analyzeConversationV2(
        conversationId: String,
        messages: List<String>
    ): SmartAiInsightV2 {
        val fullText = messages.joinToString(" ").trim()
        val normalized = normalizeText(fullText)
        val lang = detectLanguage(fullText)

        val hasPayment = containsPaymentKeywords(normalized)
        val hasAppointment = containsAppointmentKeywords(normalized)
        val hasDeadline = containsDeadlineKeywords(normalized)
        val hasCustomerRequest = containsCustomerRequestKeywords(normalized)

        val extractedAmount = extractCurrencyAmount(normalized)
        val dateOrTime = extractDateOrTime(normalized)

        val (eventType, eventTitle) = when {
            hasPayment -> DetectedEventType.PAYMENT_REMINDER to "سررسید / یادآوری پرداخت مالی"
            hasAppointment -> DetectedEventType.APPOINTMENT to "قرارداد / جلسه / نوبت حضوری"
            hasDeadline -> DetectedEventType.DEADLINE to "مهلت زمانی / ددلاین پروژه"
            hasCustomerRequest -> DetectedEventType.CUSTOMER_REQUEST to "درخواست و سفارش مشتری"
            else -> DetectedEventType.NONE to "گفتگوی عمومی"
        }

        val suggestedActions = mutableListOf<String>()
        when (eventType) {
            DetectedEventType.PAYMENT_REMINDER -> {
                suggestedActions.add("واریز سریع و ارسال فیش")
                suggestedActions.add("افزودن به تقویم مالی")
                suggestedActions.add("کپی شماره کارت / شبا")
            }
            DetectedEventType.APPOINTMENT -> {
                suggestedActions.add("ثبت جلسه در تقویم")
                suggestedActions.add("تایید نوبت به فرستنده")
                suggestedActions.add("مسیریابی به محل جلسه")
            }
            DetectedEventType.DEADLINE -> {
                suggestedActions.add("تنظیم یادآور (Reminder)")
                suggestedActions.add("تبدیل پیام به وظیفه (Task)")
            }
            DetectedEventType.CUSTOMER_REQUEST -> {
                suggestedActions.add("ارسال پیش‌فاکتور")
                suggestedActions.add("پاسخ هوشمند هوش مصنوعی")
            }
            else -> {
                suggestedActions.add("پاسخ سریع")
                suggestedActions.add("سنجاق کردن گفتگو")
            }
        }

        val isUrgent = eventType == DetectedEventType.PAYMENT_REMINDER ||
                eventType == DetectedEventType.DEADLINE ||
                normalized.contains("فوری") || normalized.contains("سررسید") ||
                normalized.contains("urgent") || normalized.contains("عاجل") ||
                normalized.contains("اخطار") || normalized.contains("کد ورود") ||
                normalized.contains("واریز")

        val summary = generateSummary(eventType, lang, extractedAmount, dateOrTime)

        return SmartAiInsightV2(
            conversationId = conversationId,
            summary = summary,
            detectedEvent = eventType,
            eventTitle = eventTitle,
            eventDateOrTime = dateOrTime,
            extractedAmount = extractedAmount,
            suggestedActions = suggestedActions,
            detectedLanguage = lang,
            isUrgent = isUrgent
        )
    }

    private fun generateSummary(
        event: DetectedEventType,
        lang: String,
        amount: String?,
        dateOrTime: String?
    ): String {
        return when (lang) {
            "fa" -> when (event) {
                DetectedEventType.PAYMENT_REMINDER -> "یادآوری پرداخت${if (amount != null) " به مبلغ $amount" else ""}"
                DetectedEventType.APPOINTMENT -> "قرارداد و نوبت جلسه${if (dateOrTime != null) " در زمان $dateOrTime" else ""}"
                DetectedEventType.DEADLINE -> "مهلت زمانی و ددلاین مهم"
                DetectedEventType.CUSTOMER_REQUEST -> "استعلام و درخواست مشتری"
                else -> "گفتگوی عمومی"
            }
            "ar" -> when (event) {
                DetectedEventType.PAYMENT_REMINDER -> "تذكير بالدفع المالي"
                DetectedEventType.APPOINTMENT -> "موعد اجتماع أو موعد طبي"
                else -> "محادثة عامة"
            }
            else -> when (event) {
                DetectedEventType.PAYMENT_REMINDER -> "Payment reminder${if (amount != null) " for $amount" else ""}"
                DetectedEventType.APPOINTMENT -> "Scheduled appointment or meeting"
                else -> "General message"
            }
        }
    }

    private fun containsPaymentKeywords(text: String): Boolean {
        return text.contains("پرداخت") || text.contains("بدهی") || text.contains("واریز") ||
                text.contains("تومان") || text.contains("ریال") || text.contains("فاکتور") ||
                text.contains("payment") || text.contains("invoice") || text.contains("due") ||
                text.contains("دفع") || text.contains("فاتورة")
    }

    private fun containsAppointmentKeywords(text: String): Boolean {
        return text.contains("جلسه") || text.contains("نوبت") || text.contains("مطب") ||
                text.contains("قرار") || text.contains("ساعت") || text.contains("meeting") ||
                text.contains("appointment") || text.contains("موعد") || text.contains("اجتماع")
    }

    private fun containsDeadlineKeywords(text: String): Boolean {
        return text.contains("ددلاین") || text.contains("تا تاریخ") || text.contains("مهلت") ||
                text.contains("سررسید") || text.contains("deadline") || text.contains("due date")
    }

    private fun containsCustomerRequestKeywords(text: String): Boolean {
        return text.contains("قیمت") || text.contains("موجود") || text.contains("خرید") ||
                text.contains("سفارش") || text.contains("کاتالوگ") || text.contains("price") ||
                text.contains("order") || text.contains("طلب") || text.contains("سعر")
    }

    private fun extractCurrencyAmount(text: String): String? {
        val regex = "\\b\\d{1,3}(?:[،,.]\\d{3})*(?:\\s*)(?:تومان|ریال|ریال|تومان|مبلغ|USD|\\$)?\\b".toRegex()
        val match = regex.find(text)
        return match?.value
    }

    private fun extractDateOrTime(text: String): String? {
        val timeRegex = "\\b(?:ساعت|فردا|امروز|پنجشنبه|جمعه|شنبه|یکشنبه|دوشنبه|سه شنبه|چهارشنبه|۱۴۰\\d/\\d{1,2}/\\d{1,2}|202\\d-\\d{2}-\\d{2})\\b".toRegex()
        val match = timeRegex.find(text)
        return match?.value
    }

    private fun normalizeText(text: String): String {
        return text.lowercase()
            .replace('۰', '0').replace('۱', '1').replace('۲', '2').replace('۳', '3').replace('۴', '4')
            .replace('۵', '5').replace('۶', '6').replace('۷', '7').replace('۸', '8').replace('۹', '9')
            .replace("أ", "ا").replace("إ", "ا").replace("آ", "ا").replace("ك", "ک").replace("ي", "ی")
    }

    private fun detectLanguage(text: String): String {
        val containsPersian = text.any { it in '\u0600'..'\u06FF' }
        val containsArabic = text.contains("لم") || text.contains("على") || text.contains("هذا") || text.contains("شكرا")
        return when {
            containsArabic -> "ar"
            containsPersian -> "fa"
            else -> "en"
        }
    }
}
