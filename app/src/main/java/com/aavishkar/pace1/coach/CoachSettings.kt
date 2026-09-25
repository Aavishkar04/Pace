package com.aavishkar.pace1.coach

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Settings configuration for the Pace Coach audio system.
 */
data class CoachSettings(
    val isCoachEnabled: Boolean = false,
    val targetSpeedKmh: Float = 24.0f,
    val lowerBoundKmh: Float = 22.0f,
    val upperBoundKmh: Float = 26.0f,
    val intervalSeconds: Int = 30,
    val isVoiceAssistantEnabled: Boolean = false,
    val isWakeWordEnabled: Boolean = false
)

/**
 * SharedPreferences-backed repository for persisting Pace Coach settings.
 */
class CoachSettingsRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("pace1_coach_settings", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<CoachSettings> = _settings.asStateFlow()

    fun updateSettings(newSettings: CoachSettings) {
        prefs.edit().apply {
            putBoolean(KEY_COACH_ENABLED, newSettings.isCoachEnabled)
            putFloat(KEY_TARGET_SPEED, newSettings.targetSpeedKmh)
            putFloat(KEY_LOWER_BOUND, newSettings.lowerBoundKmh)
            putFloat(KEY_UPPER_BOUND, newSettings.upperBoundKmh)
            putInt(KEY_INTERVAL, newSettings.intervalSeconds)
            putBoolean(KEY_VOICE_ASSISTANT, newSettings.isVoiceAssistantEnabled)
            putBoolean(KEY_WAKE_WORD, newSettings.isWakeWordEnabled)
            apply()
        }
        _settings.value = newSettings
    }

    private fun loadSettings(): CoachSettings {
        return CoachSettings(
            isCoachEnabled = prefs.getBoolean(KEY_COACH_ENABLED, false),
            targetSpeedKmh = prefs.getFloat(KEY_TARGET_SPEED, 24.0f),
            lowerBoundKmh = prefs.getFloat(KEY_LOWER_BOUND, 22.0f),
            upperBoundKmh = prefs.getFloat(KEY_UPPER_BOUND, 26.0f),
            intervalSeconds = prefs.getInt(KEY_INTERVAL, 30),
            isVoiceAssistantEnabled = prefs.getBoolean(KEY_VOICE_ASSISTANT, false),
            isWakeWordEnabled = prefs.getBoolean(KEY_WAKE_WORD, false)
        )
    }

    companion object {
        private const val KEY_COACH_ENABLED = "coach_enabled"
        private const val KEY_TARGET_SPEED = "target_speed"
        private const val KEY_LOWER_BOUND = "lower_bound"
        private const val KEY_UPPER_BOUND = "upper_bound"
        private const val KEY_INTERVAL = "interval"
        private const val KEY_VOICE_ASSISTANT = "voice_assistant"
        private const val KEY_WAKE_WORD = "wake_word"
    }
}
