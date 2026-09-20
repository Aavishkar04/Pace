package com.aavishkar.pace1.data.model

/**
 * Domain model representing a raw GPS location sample with metadata.
 */
data class LocationPoint(
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long,
    val altitude: Double,
    val speed: Float,
    val bearing: Float,
    val accuracy: Float,
    val verticalAccuracy: Float = 0f,
    val speedAccuracy: Float = 0f,
    val provider: String = "fused",
    val hasSpeed: Boolean = false
)
