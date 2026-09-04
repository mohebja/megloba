package com.global.sms.engine.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import com.global.sms.core.util.PersianUtils
import java.util.Locale

class TtsManager(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var isInitialized = false

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            tts?.setSpeechRate(0.9f)
            tts?.setPitch(1.0f)
        }
    }

    fun speakMessage(body: String, privacyMode: Boolean = false) {
        if (!isInitialized) return

        val isPersian = PersianUtils.containsPersian(body)
        val locale = if (isPersian) Locale.forLanguageTag("fa-IR") else Locale.ENGLISH

        val result = tts?.setLanguage(locale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            tts?.language = Locale.ENGLISH
        }

        val textToSpeak = if (privacyMode) "پیام جدید دریافت شد" else body
        tts?.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, "SMS_READER_ID")
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}
