package com.global.sms.core.ai.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import com.global.sms.core.util.PersianUtils
import java.util.Locale

class VoiceMessageAssistant(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var isInitialized = false

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val faLocale = Locale.forLanguageTag("fa-IR")
            val langResult = tts?.setLanguage(faLocale)
            if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale.US)
            }
            isInitialized = true
        }
    }

    /**
     * Reads sender name with TTS.
     */
    fun readSenderName(sender: String, contactName: String? = null) {
        if (!isInitialized) return
        val displayName = contactName ?: sender
        val text = "پیامک از طرف $displayName"
        speakText(text)
    }

    /**
     * Reads latest message body with privacy checks.
     */
    fun readLatestMessage(
        sender: String,
        body: String,
        isHidden: Boolean = false,
        isAuthenticated: Boolean = false
    ) {
        if (!isInitialized) return
        if (isHidden && !isAuthenticated) {
            speakText("پیام شخصی دریافت شد. جهت شنیدن، ابتدا احراز هویت کنید.")
            return
        }

        val speech = "پیام جدید از $sender. متن پیام: $body"
        speakText(speech)
    }

    /**
     * Reads selected message body with privacy checks.
     */
    fun readSelectedMessage(
        sender: String,
        body: String,
        isHidden: Boolean = false,
        isAuthenticated: Boolean = false
    ) {
        if (!isInitialized) return
        if (isHidden && !isAuthenticated) {
            speakText("دسترسی به پیام‌های صندوق شخصی نیازمند ورود رمزمانی است.")
            return
        }
        speakText(body)
    }

    /**
     * Reads OTP code digit by digit with confirmation request.
     */
    fun readOtpWithConfirmation(
        otpCode: String,
        isAuthenticated: Boolean = true
    ) {
        if (!isInitialized) return
        val spacedDigits = otpCode.toCharArray().joinToString(" ")
        val text = "کد تایید شما: $spacedDigits است."
        speakText(text)
    }

    fun speakText(text: String) {
        val targetLocale = if (PersianUtils.containsPersian(text)) {
            Locale.forLanguageTag("fa-IR")
        } else {
            Locale.US
        }
        tts?.language = targetLocale
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "VOICE_ASSISTANT_UTTERANCE")
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
