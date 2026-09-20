package com.aavishkar.pace1.data.model

/**
 * Domain model representing a single GPS location sample.
 */
data class LocationPoint(
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long,
    val altitude: Double,
    val speed: Float,
    val bearing: Float,
    val accuracy: Float
)
