package com.aavishkar.pace1

import com.aavishkar.pace1.location.HaversineDistanceCalculator
import org.junit.Assert.assertEquals
import org.junit.Test

class HaversineDistanceCalculatorTest {

    @Test
    fun calculateDistanceMeters_sameCoordinates_returnsZero() {
        val distance = HaversineDistanceCalculator.calculateDistanceMeters(
            lat1 = 19.0390978,
            lon1 = 73.0697035,
            lat2 = 19.0390978,
            lon2 = 73.0697035
        )
        assertEquals(0f, distance, 0.001f)
    }

    @Test
    fun calculateDistanceMeters_knownCoordinates_returnsAccurateDistance() {
        // Known geographic distance between two points approx 157 meters apart
        val lat1 = 19.0390978
        val lon1 = 73.0697035
        val lat2 = 19.0405000
        val lon2 = 73.0697035

        val distance = HaversineDistanceCalculator.calculateDistanceMeters(
            lat1 = lat1,
            lon1 = lon1,
            lat2 = lat2,
            lon2 = lon2
        )

        // Expected distance is approx 155.9 meters
        assertEquals(155.9f, distance, 2.0f)
    }
}
