package com.aavishkar.pace1.location

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Utility for computing geographic distance between latitude/longitude pairs
 * using the Haversine formula.
 */
object HaversineDistanceCalculator {

    private const val EARTH_RADIUS_METERS = 6_371_000.0

    /**
     * Calculates great-circle distance between two points in meters.
     */
    fun calculateDistanceMeters(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Float {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return (EARTH_RADIUS_METERS * c).toFloat()
    }
}
