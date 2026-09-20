package com.aavishkar.pace1.data.repository

import com.aavishkar.pace1.data.local.PaceDatabase
import android.content.Context
import com.aavishkar.pace1.data.local.dao.RideDao
import com.aavishkar.pace1.data.local.entity.LocationPointEntity
import com.aavishkar.pace1.data.local.entity.RideEntity
import com.aavishkar.pace1.data.model.LocationPoint
import com.aavishkar.pace1.data.model.RideMetrics
import com.aavishkar.pace1.data.model.RideState
import com.aavishkar.pace1.location.HaversineDistanceCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * Repository serving as the single source of truth for ride tracking states and database persistence.
 */
class RideRepository(
    private val rideDao: RideDao
) {
    private val _activeRideState = MutableStateFlow(RideState.IDLE)
    val activeRideState: StateFlow<RideState> = _activeRideState.asStateFlow()

    private val _activeRideMetrics = MutableStateFlow(RideMetrics())
    val activeRideMetrics: StateFlow<RideMetrics> = _activeRideMetrics.asStateFlow()

    private val _activeRideId = MutableStateFlow<String?>(null)
    val activeRideId: StateFlow<String?> = _activeRideId.asStateFlow()

    suspend fun startNewRide(): String {
        val existingId = _activeRideId.value
        if (_activeRideState.value == RideState.RECORDING && existingId != null) {
            return existingId
        }

        val newRideId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        val newRideEntity = RideEntity(
            rideId = newRideId,
            startTimeMillis = now,
            status = "RECORDING"
        )

        rideDao.insertRide(newRideEntity)

        _activeRideId.value = newRideId
        _activeRideMetrics.value = RideMetrics()
        _activeRideState.value = RideState.RECORDING

        return newRideId
    }

    suspend fun addLocationPoint(point: LocationPoint) {
        val rideId = _activeRideId.value ?: return
        if (_activeRideState.value != RideState.RECORDING) return

        val currentMetrics = _activeRideMetrics.value
        val previousPoint = currentMetrics.lastLocationPoint

        val addedDistance = if (previousPoint != null) {
            HaversineDistanceCalculator.calculateDistanceMeters(
                lat1 = previousPoint.latitude,
                lon1 = previousPoint.longitude,
                lat2 = point.latitude,
                lon2 = point.longitude
            )
        } else {
            0f
        }

        val newDistance = currentMetrics.distanceMeters + addedDistance
        val newMaxSpeed = maxOf(currentMetrics.maxSpeedMps, point.speed)
        val elapsed = currentMetrics.elapsedTimeSeconds
        val newAvgSpeed = if (elapsed > 0) newDistance / elapsed else 0f

        val updatedMetrics = currentMetrics.copy(
            distanceMeters = newDistance,
            currentSpeedMps = point.speed,
            averageSpeedMps = newAvgSpeed,
            maxSpeedMps = newMaxSpeed,
            currentAccuracyMeters = point.accuracy,
            lastLocationPoint = point
        )

        _activeRideMetrics.value = updatedMetrics

        rideDao.insertLocationPoint(
            LocationPointEntity(
                rideId = rideId,
                latitude = point.latitude,
                longitude = point.longitude,
                timestamp = point.timestamp,
                altitude = point.altitude,
                speed = point.speed,
                bearing = point.bearing,
                accuracy = point.accuracy
            )
        )

        val existingEntity = rideDao.getRideById(rideId)
        if (existingEntity != null) {
            rideDao.updateRide(
                existingEntity.copy(
                    elapsedTimeSeconds = elapsed,
                    distanceMeters = newDistance,
                    maxSpeedMps = newMaxSpeed,
                    averageSpeedMps = newAvgSpeed
                )
            )
        }
    }

    suspend fun incrementTimerSecond() {
        val rideId = _activeRideId.value ?: return
        if (_activeRideState.value != RideState.RECORDING) return

        val currentMetrics = _activeRideMetrics.value
        val newElapsed = currentMetrics.elapsedTimeSeconds + 1
        val newAvgSpeed = if (newElapsed > 0) currentMetrics.distanceMeters / newElapsed else 0f

        val updatedMetrics = currentMetrics.copy(
            elapsedTimeSeconds = newElapsed,
            averageSpeedMps = newAvgSpeed
        )

        _activeRideMetrics.value = updatedMetrics

        val existingEntity = rideDao.getRideById(rideId)
        if (existingEntity != null) {
            rideDao.updateRide(
                existingEntity.copy(
                    elapsedTimeSeconds = newElapsed,
                    averageSpeedMps = newAvgSpeed
                )
            )
        }
    }

    suspend fun stopRide() {
        val rideId = _activeRideId.value ?: return
        if (_activeRideState.value != RideState.RECORDING) return

        _activeRideState.value = RideState.STOPPED

        val currentMetrics = _activeRideMetrics.value
        val existingEntity = rideDao.getRideById(rideId)

        if (existingEntity != null) {
            rideDao.updateRide(
                existingEntity.copy(
                    endTimeMillis = System.currentTimeMillis(),
                    elapsedTimeSeconds = currentMetrics.elapsedTimeSeconds,
                    distanceMeters = currentMetrics.distanceMeters,
                    maxSpeedMps = currentMetrics.maxSpeedMps,
                    averageSpeedMps = currentMetrics.averageSpeedMps,
                    status = "STOPPED"
                )
            )
        }
    }

    fun resetRide() {
        _activeRideState.value = RideState.IDLE
        _activeRideMetrics.value = RideMetrics()
        _activeRideId.value = null
    }

    suspend fun recoverActiveRideFromDb() {
        val activeRide = rideDao.getActiveRecordingRide() ?: return
        val points = rideDao.getLocationPointsForRide(activeRide.rideId)

        _activeRideId.value = activeRide.rideId
        _activeRideState.value = RideState.RECORDING

        val lastEntityPoint = points.lastOrNull()
        val lastLocationPoint = lastEntityPoint?.let {
            LocationPoint(
                latitude = it.latitude,
                longitude = it.longitude,
                timestamp = it.timestamp,
                altitude = it.altitude,
                speed = it.speed,
                bearing = it.bearing,
                accuracy = it.accuracy
            )
        }

        _activeRideMetrics.value = RideMetrics(
            elapsedTimeSeconds = activeRide.elapsedTimeSeconds,
            distanceMeters = activeRide.distanceMeters,
            currentSpeedMps = lastLocationPoint?.speed ?: 0f,
            averageSpeedMps = activeRide.averageSpeedMps,
            maxSpeedMps = activeRide.maxSpeedMps,
            currentAccuracyMeters = lastLocationPoint?.accuracy ?: 0f,
            lastLocationPoint = lastLocationPoint
        )
    }

    companion object {
        @Volatile
        private var INSTANCE: RideRepository? = null

        fun getInstance(context: Context): RideRepository {
            return INSTANCE ?: synchronized(this) {
                val db = PaceDatabase.getInstance(context)
                val instance = RideRepository(db.rideDao())
                INSTANCE = instance
                instance
            }
        }
    }
}
