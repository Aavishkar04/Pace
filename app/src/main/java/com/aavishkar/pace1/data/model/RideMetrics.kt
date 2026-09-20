package com.aavishkar.pace1.data.model

/**
 * Domain model containing live filtered metrics and raw data diagnostics for a ride.
 */
data class RideMetrics(
    val elapsedTimeSeconds: Long = 0L,
    val distanceMeters: Float = 0f,
    val currentSpeedMps: Float = 0f,
    val rawSpeedMps: Float = 0f,
    val averageSpeedMps: Float = 0f,
    val maxSpeedMps: Float = 0f,
    val currentAccuracyMeters: Float = 0f,
    val totalRawPoints: Int = 0,
    val acceptedPoints: Int = 0,
    val rejectedPoints: Int = 0,
    val lastLocationPoint: LocationPoint? = null,
    val hasAccelerometer: Boolean = false,
    val hasGyroscope: Boolean = false,
    val hasBarometer: Boolean = false
)
