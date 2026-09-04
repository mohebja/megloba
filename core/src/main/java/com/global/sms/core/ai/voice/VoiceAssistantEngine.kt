package com.global.sms.core.ai.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class VoiceAssistantEngine(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var isInitialized = false

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.forLanguageTag("fa-IR"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale.US)
            }
            isInitialized = true
        }
    }

    fun readSmsAloud(text: String, isPrivate: Boolean = false) {
        if (!isInitialized) return
        val textToSpeak = if (isPrivate) {
            "پیام جدید دریافت شد."
        } else {
            text
        }
        tts?.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, "SMS_VOICE_ASSISTANT")
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
