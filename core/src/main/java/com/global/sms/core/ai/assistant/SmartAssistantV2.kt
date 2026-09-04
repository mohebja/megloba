package com.global.sms.core.ai.assistant

import java.util.regex.Pattern

enum class DetectedEntityType {
    APPOINTMENT,
    ADDRESS,
    PAYMENT_IBAN,
    CARD_NUMBER,
    TASK_ACTION_ITEM
}

data class ExtractedActionableEntity(
    val type: DetectedEntityType,
    val value: String,
    val label: String,
    val actionText: String,
    val actionIntentType: String
)

/**
 * Enterprise AI Personal Assistant V2 for Global SMS.
 * Scans SMS content for Appointments, Addresses, Financial Payment Card/IBAN numbers,
 * and Actionable Tasks, generating instant 1-tap contextual actions.
 */
object SmartAssistantV2 {

    // Regex Patterns for Persian/English entities
    private val CARD_PATTERN = Pattern.compile("\\b(?:6037|5892|6219|6273|6063|5022|6393|6280|5041|6104|5894|6274|\\d{4})\\d{12}\\b")
    private val IBAN_PATTERN = Pattern.compile("IR\\d{24}", Pattern.CASE_INSENSITIVE)
    private val APPOINTMENT_PATTERN = Pattern.compile("(?:قرار|جلسه|ساعت|فردا|امروز|یکشنبه|دوشنبه|سه شنبه|چهارشنبه|پنجشنبه|جمعه|شنبه)\\s*(\\d{1,2}(?::\\d{2})?)", Pattern.UNICODE_CASE)
    private val ADDRESS_PATTERN = Pattern.compile("(?:خیابان|بلوار|میدان|کوچه|پلاک|طبقه|واحد|بزرراه|خیابان اصلی)\\s+[\\u0600-\\u06FF\\s0-9]+", Pattern.UNICODE_CASE)
    private val TASK_PATTERN = Pattern.compile("(?:لطفا|لطفاً|پیگیری کن|یادت نره|ارسال کن|انجام بده|واریز کن|تماس بگیر)\\s+.+", Pattern.UNICODE_CASE)

    /**
     * Analyze message text and extract actionable entities.
     */
    fun analyzeAndExtractEntities(messageText: String): List<ExtractedActionableEntity> {
        val normalizedText = messageText
            .replace('۰', '0').replace('۱', '1').replace('۲', '2').replace('۳', '3').replace('۴', '4')
            .replace('۵', '5').replace('۶', '6').replace('۷', '7').replace('۸', '8').replace('۹', '9')
            .replace("،", "")

        val entities = mutableListOf<ExtractedActionableEntity>()

        // 1. Bank Card Number Detection
        val cardMatcher = CARD_PATTERN.matcher(normalizedText)
        if (cardMatcher.find()) {
            val cardNum = cardMatcher.group()
            entities.add(
                ExtractedActionableEntity(
                    type = DetectedEntityType.CARD_NUMBER,
                    value = cardNum,
                    label = "شماره کارت بانکی",
                    actionText = "کپی شماره کارت $cardNum",
                    actionIntentType = "COPY_TEXT"
                )
            )
        }

        // 2. IBAN (Sheba) Detection
        val ibanMatcher = IBAN_PATTERN.matcher(messageText)
        if (ibanMatcher.find()) {
            val ibanNum = ibanMatcher.group()
            entities.add(
                ExtractedActionableEntity(
                    type = DetectedEntityType.PAYMENT_IBAN,
                    value = ibanNum,
                    label = "شماره شبا بانکی",
                    actionText = "کپی شماره شبا $ibanNum",
                    actionIntentType = "COPY_TEXT"
                )
            )
        }

        // 3. Appointment / Meeting Detection
        val apptMatcher = APPOINTMENT_PATTERN.matcher(messageText)
        if (apptMatcher.find()) {
            val timeMatch = apptMatcher.group()
            entities.add(
                ExtractedActionableEntity(
                    type = DetectedEntityType.APPOINTMENT,
                    value = timeMatch,
                    label = "قرار ملاقات یا جلسه",
                    actionText = "افزودن جلسه به تقویم ($timeMatch)",
                    actionIntentType = "ADD_CALENDAR"
                )
            )
        }

        // 4. Address Detection
        val addrMatcher = ADDRESS_PATTERN.matcher(messageText)
        if (addrMatcher.find()) {
            val addrText = addrMatcher.group()
            entities.add(
                ExtractedActionableEntity(
                    type = DetectedEntityType.ADDRESS,
                    value = addrText,
                    label = "آدرس و موقعیت مکانی",
                    actionText = "مسیریابی با نقشه",
                    actionIntentType = "OPEN_MAP"
                )
            )
        }

        // 5. Task / Action Item Detection
        val taskMatcher = TASK_PATTERN.matcher(messageText)
        if (taskMatcher.find()) {
            val taskText = taskMatcher.group()
            entities.add(
                ExtractedActionableEntity(
                    type = DetectedEntityType.TASK_ACTION_ITEM,
                    value = taskText,
                    label = "وظیفه یا یادآوری",
                    actionText = "افزودن به لیست کارها",
                    actionIntentType = "CREATE_TASK"
                )
            )
        }

        return entities
    }
}
