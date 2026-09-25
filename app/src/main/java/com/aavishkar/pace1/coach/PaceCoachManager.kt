package com.aavishkar.pace1.coach

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.speech.tts.TextToSpeech
import com.aavishkar.pace1.data.model.RideMetrics
import java.util.Locale

/**
 * Audio manager handling Android Text-To-Speech coaching and stats announcements.
 */
class PaceCoachManager(
    context: Context
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var isTtsReady = false

    private val audioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    private var lastAnnouncementTime = 0L
    private var lastCoachingState: CoachState = CoachState.NONE

    enum class CoachState {
        NONE, BELOW, TARGET, ABOVE, WEAK_GPS
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                isTtsReady = true
            }
        }
    }

    fun evaluateAndCoach(metrics: RideMetrics, settings: CoachSettings) {
        if (!settings.isCoachEnabled || !isTtsReady) return

        val now = System.currentTimeMillis()
        val intervalMs = settings.intervalSeconds * 1000L

        // Stationary Check: Do NOT coach or tell rider to increase effort if stationary!
        if (metrics.currentSpeedMps < 0.5f) {
            return
        }

        // GPS Accuracy Check
        if (metrics.currentAccuracyMeters > 35.0f) {
            if (now - lastAnnouncementTime >= 60_000L && lastCoachingState != CoachState.WEAK_GPS) {
                speak("GPS signal weak.")
                lastCoachingState = CoachState.WEAK_GPS
                lastAnnouncementTime = now
            }
            return
        }

        val currentKmh = metrics.currentSpeedMps * 3.6f
        val currentState = when {
            currentKmh < settings.lowerBoundKmh -> CoachState.BELOW
            currentKmh > settings.upperBoundKmh -> CoachState.ABOVE
            else -> CoachState.TARGET
        }

        val isIntervalPassed = (now - lastAnnouncementTime) >= intervalMs
        val isStateChanged = currentState != lastCoachingState && lastCoachingState != CoachState.NONE

        if (isIntervalPassed || isStateChanged) {
            val message = when (currentState) {
                CoachState.BELOW -> "Increase effort. You're at %.1f.".format(currentKmh)
                CoachState.TARGET -> "Hold pace. %.1f.".format(currentKmh)
                CoachState.ABOVE -> "Ease off. You're at %.1f.".format(currentKmh)
                else -> return
            }

            speak(message)
            lastCoachingState = currentState
            lastAnnouncementTime = now
        }
    }

    fun speakRideStats(metrics: RideMetrics) {
        val text = generateStatsText(metrics)
        speak(text)
    }

    fun speak(text: String) {
        if (!isTtsReady) return
        requestAudioFocus()
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "pace1_tts_${System.currentTimeMillis()}")
    }

    private fun requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                .build()
            audioManager?.requestAudioFocus(focusRequest)
        } else {
            @Suppress("DEPRECATION")
            audioManager?.requestAudioFocus(
                null,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
            )
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isTtsReady = false
    }

    companion object {
        fun generateStatsText(metrics: RideMetrics): String {
            val distanceKm = metrics.distanceMeters / 1000f
            val minutes = metrics.elapsedTimeSeconds / 60
            val currentKmh = metrics.currentSpeedMps * 3.6f
            val avgKmh = metrics.averageSpeedMps * 3.6f
            val maxKmh = metrics.maxSpeedMps * 3.6f

            return "You've ridden %.1f kilometers in %d minutes. Current speed %.1f. Average speed %.1f. Maximum speed %.1f."
                .format(distanceKm, minutes, currentKmh, avgKmh, maxKmh)
        }
    }
}
