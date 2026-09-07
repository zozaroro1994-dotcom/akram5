package com.example.util

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator

class SoundManager(context: Context) {

    private var toneGenerator: ToneGenerator? = null
    var isSoundEnabled: Boolean = true

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 70)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playButtonClick() {
        if (!isSoundEnabled) return
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 50)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playNotificationTone() {
        if (!isSoundEnabled) return
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 150)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun release() {
        toneGenerator?.release()
        toneGenerator = null
    }
}
