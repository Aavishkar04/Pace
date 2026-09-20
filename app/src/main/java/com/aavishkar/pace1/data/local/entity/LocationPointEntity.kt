package com.aavishkar.pace1.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity representing an individual GPS location coordinate for a ride.
 */
@Entity(
    tableName = "location_points",
    foreignKeys = [
        ForeignKey(
            entity = RideEntity::class,
            parentColumns = ["rideId"],
            childColumns = ["rideId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("rideId")]
)
data class LocationPointEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val rideId: String,
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
    val isAccepted: Boolean = true
)
