package com.aavishkar.pace1.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a recorded ride session in local storage.
 */
@Entity(tableName = "rides")
data class RideEntity(
    @PrimaryKey val rideId: String,
    val startTimeMillis: Long,
    val endTimeMillis: Long? = null,
    val elapsedTimeSeconds: Long = 0L,
    val distanceMeters: Float = 0f,
    val maxSpeedMps: Float = 0f,
    val averageSpeedMps: Float = 0f,
    val status: String // "RECORDING", "STOPPED"
)
