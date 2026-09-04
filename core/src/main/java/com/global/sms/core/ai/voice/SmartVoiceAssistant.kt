package com.global.sms.core.ai.voice

import android.content.Context
import com.global.sms.data.entity.MessageCategory

enum class VoiceAction {
    SEND_NEW_SMS,
    READ_BANK_MESSAGES,
    SHOW_LATEST_BANK_MESSAGE,
    READ_OTP_MESSAGES,
    CHECK_UNREAD_SMS,
    TOGGLE_DRIVING_MODE,
    REPLY_MESSAGE,
    SHOW_IMPORTANT_MESSAGES,
    SHOW_TASKS,
    SHOW_FINANCIAL_SUMMARY,
    UNKNOWN
}

data class ParsedVoiceCommand(
    val action: VoiceAction,
    val targetRecipient: String? = null,
    val replyBody: String? = null,
    val rawSpokenText: String,
    val isPersian: Boolean = true
)

/**
 * Smart Voice Assistant for Sprint 4.
 * Supports Voice commands, Driving Mode, Accessibility Mode, Persian speech recognition parsing, and Persian TTS.
 */
class SmartVoiceAssistant(val context: Context? = null) {

    private val voiceAssistant = context?.let { VoiceMessageAssistant(it) }
    var isDrivingModeEnabled: Boolean = false
        private set

    var isAccessibilityModeEnabled: Boolean = false
        private set

    fun toggleDrivingMode(enabled: Boolean) {
        isDrivingModeEnabled = enabled
    }

    fun toggleAccessibilityMode(enabled: Boolean) {
        isAccessibilityModeEnabled = enabled
    }

    fun parsePersianVoiceCommand(spokenText: String): ParsedVoiceCommand {
        val text = spokenText.trim().lowercase()
            .replace('۰', '0').replace('۱', '1').replace('۲', '2').replace('۳', '3').replace('۴', '4')
            .replace('۵', '5').replace('۶', '6').replace('۷', '7').replace('۸', '8').replace('۹', '9')

        return when {
            text.contains("پیامهای مهم") || text.contains("پیام های مهم") || text.contains("important messages") -> {
                ParsedVoiceCommand(
                    action = VoiceAction.SHOW_IMPORTANT_MESSAGES,
                    rawSpokenText = spokenText
                )
            }
            text.contains("چه کارهایی") || text.contains("وظایف") || text.contains("read my tasks") || text.contains("show my tasks") -> {
                ParsedVoiceCommand(
                    action = VoiceAction.SHOW_TASKS,
                    rawSpokenText = spokenText
                )
            }
            text.contains("هزینههای این ماه") || text.contains("هزینه های این ماه") || text.contains("مخارج") -> {
                ParsedVoiceCommand(
                    action = VoiceAction.SHOW_FINANCIAL_SUMMARY,
                    rawSpokenText = spokenText
                )
            }
            text.contains("پیام جدید به") || text.contains("ارسال پیام به") || text.contains("اس ام اس به") -> {
                val recipient = text.substringAfter("به").trim().split(" ").firstOrNull() ?: "ناشناس"
                ParsedVoiceCommand(
                    action = VoiceAction.SEND_NEW_SMS,
                    targetRecipient = recipient,
                    rawSpokenText = spokenText
                )
            }
            text.contains("آخرین پیام بانک") -> {
                ParsedVoiceCommand(
                    action = VoiceAction.SHOW_LATEST_BANK_MESSAGE,
                    rawSpokenText = spokenText
                )
            }
            text.contains("پاسخ بده") -> {
                val replyBody = text.substringAfter("پاسخ بده").replace(":", "").trim()
                ParsedVoiceCommand(
                    action = VoiceAction.REPLY_MESSAGE,
                    replyBody = replyBody,
                    rawSpokenText = spokenText
                )
            }
            text.contains("پیامهای بانکی") || text.contains("پیام های بانکی") || text.contains("تراکنش") -> {
                ParsedVoiceCommand(
                    action = VoiceAction.READ_BANK_MESSAGES,
                    rawSpokenText = spokenText
                )
            }
            text.contains("کد تایید") || text.contains("کد ورود") || text.contains("رمز پویا") -> {
                ParsedVoiceCommand(
                    action = VoiceAction.READ_OTP_MESSAGES,
                    rawSpokenText = spokenText
                )
            }
            text.contains("بررسی کن") || text.contains("خوانده نشده") || text.contains("پیامک را بررسی") -> {
                ParsedVoiceCommand(
                    action = VoiceAction.CHECK_UNREAD_SMS,
                    rawSpokenText = spokenText
                )
            }
            text.contains("حالت رانندگی") -> {
                toggleDrivingMode(!isDrivingModeEnabled)
                ParsedVoiceCommand(
                    action = VoiceAction.TOGGLE_DRIVING_MODE,
                    rawSpokenText = spokenText
                )
            }
            else -> ParsedVoiceCommand(
                action = VoiceAction.UNKNOWN,
                rawSpokenText = spokenText
            )
        }
    }

    fun readMessageIfImportant(
        sender: String,
        body: String,
        category: MessageCategory,
        isHidden: Boolean = false
    ) {
        if (isDrivingModeEnabled || isAccessibilityModeEnabled) {
            if (category == MessageCategory.OTP || category == MessageCategory.BANK || category == MessageCategory.IMPORTANT) {
                voiceAssistant?.readLatestMessage(sender, body, isHidden = isHidden)
            } else {
                voiceAssistant?.readSenderName(sender)
            }
        } else {
            voiceAssistant?.readLatestMessage(sender, body, isHidden = isHidden)
        }
    }

    fun readOtpCode(code: String) {
        voiceAssistant?.readOtpWithConfirmation(code)
    }

    fun speakText(text: String) {
        voiceAssistant?.speakText(text)
    }

    fun stop() {
        voiceAssistant?.stop()
    }

    fun shutdown() {
        voiceAssistant?.shutdown()
    }
}
