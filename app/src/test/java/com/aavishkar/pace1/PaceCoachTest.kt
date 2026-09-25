package com.aavishkar.pace1

import com.aavishkar.pace1.coach.CoachSettings
import com.aavishkar.pace1.coach.PaceCoachManager
import com.aavishkar.pace1.data.model.RideMetrics
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class PaceCoachTest {

    @Test
    fun coachSettings_defaultValuesMatchRequirement() {
        val settings = CoachSettings()
        assertFalse(settings.isCoachEnabled)
        assertEquals(24.0f, settings.targetSpeedKmh, 0.001f)
        assertEquals(22.0f, settings.lowerBoundKmh, 0.001f)
        assertEquals(26.0f, settings.upperBoundKmh, 0.001f)
        assertEquals(30, settings.intervalSeconds)
        assertFalse(settings.isVoiceAssistantEnabled)
        assertFalse(settings.isWakeWordEnabled)
    }

    @Test
    fun generateStatsText_formatsRideMetricsCorrectly() {
        val metrics = RideMetrics(
            elapsedTimeSeconds = 2040L, // 34 mins
            distanceMeters = 12400f, // 12.4 km
            currentSpeedMps = 6.555f, // ~23.6 km/h
            averageSpeedMps = 6.055f, // ~21.8 km/h
            maxSpeedMps = 9.5f // ~34.2 km/h
        )

        val text = PaceCoachManager.generateStatsText(metrics)
        assert(text.contains("12.4 kilometers"))
        assert(text.contains("34 minutes"))
        assert(text.contains("23.6"))
        assert(text.contains("21.8"))
        assert(text.contains("34.2"))
    }
}
