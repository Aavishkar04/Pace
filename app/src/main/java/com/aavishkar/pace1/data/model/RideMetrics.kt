package com.aavishkar.pace1.data.model

/**
 * Real-time domain model containing live metrics calculated during a ride.
 */
data class RideMetrics(
    val elapsedTimeSeconds: Long = 0L,
    val distanceMeters: Float = 0f,
    val currentSpeedMps: Float = 0f,
    val averageSpeedMps: Float = 0f,
    val maxSpeedMps: Float = 0f,
    val currentAccuracyMeters: Float = 0f,
    val lastLocationPoint: LocationPoint? = null
)
