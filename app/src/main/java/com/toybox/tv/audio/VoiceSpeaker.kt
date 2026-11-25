package com.toybox.tv.audio

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import java.util.Locale

/**
 * Offline Android Text-to-Speech wrapper with a warm, cheerful speaking pace for toddlers.
 * Deferred past the first frame so cold start stays instant on Android TV.
 */
class VoiceSpeaker(context: Context) {
    private val appContext = context.applicationContext
    @Volatile
    private var ready = false
    @Volatile
    private var released = false
    private var tts: TextToSpeech? = null
    private var lastSpeakMs = 0L

    init {
        Handler(Looper.getMainLooper()).postDelayed({
            if (released) return@postDelayed
            tts = TextToSpeech(appContext) { status ->
                if (!released && status == TextToSpeech.SUCCESS) {
                    tts?.language = Locale.US
                    tts?.setPitch(1.18f)
                    tts?.setSpeechRate(0.95f)
                    ready = true
                }
            }
        }, 300L)
    }

    fun speak(phrase: String) {
        if (!ready || released) return
        val now = System.currentTimeMillis()
        if (now - lastSpeakMs < 250L) return
        lastSpeakMs = now
        tts?.speak(phrase, TextToSpeech.QUEUE_FLUSH, null, "toybox_$now")
    }

    fun release() {
        released = true
        ready = false
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
