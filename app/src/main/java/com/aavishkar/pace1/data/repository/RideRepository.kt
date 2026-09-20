package com.aavishkar.pace1.data.repository

import android.content.Context
import com.aavishkar.pace1.data.local.PaceDatabase
import com.aavishkar.pace1.data.local.dao.RideDao
import com.aavishkar.pace1.data.local.entity.LocationPointEntity
import com.aavishkar.pace1.data.local.entity.RideEntity
import com.aavishkar.pace1.data.model.LocationPoint
import com.aavishkar.pace1.data.model.RideMetrics
import com.aavishkar.pace1.data.model.RideState
import com.aavishkar.pace1.location.HaversineDistanceCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * Repository serving as the single source of truth for ride tracking states,
 * baseline stationary GPS filtering, sensor health, and database persistence.
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

    val completedRides: Flow<List<RideEntity>> = rideDao.getAllCompletedRides()

    fun updateSensorHealth(hasAccel: Boolean, hasGyro: Boolean, hasBaro: Boolean) {
        _activeRideMetrics.value = _activeRideMetrics.value.copy(
            hasAccelerometer = hasAccel,
            hasGyroscope = hasGyro,
            hasBarometer = hasBaro
        )
    }

    suspend fun startNewRide(): String {
        val existingId = _activeRideId.value
        if (_activeRideState.value == RideState.RECORDING && existingId != null) {
            return existingId
        }

        val newRideId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val currentMetrics = _activeRideMetrics.value

        val newRideEntity = RideEntity(
            rideId = newRideId,
            startTimeMillis = now,
            hasAccelerometer = currentMetrics.hasAccelerometer,
            hasGyroscope = currentMetrics.hasGyroscope,
            hasBarometer = currentMetrics.hasBarometer,
            status = "RECORDING"
        )

        rideDao.insertRide(newRideEntity)

        _activeRideId.value = newRideId
        _activeRideMetrics.value = currentMetrics.copy(
            elapsedTimeSeconds = 0L,
            distanceMeters = 0f,
            currentSpeedMps = 0f,
            rawSpeedMps = 0f,
            averageSpeedMps = 0f,
            maxSpeedMps = 0f,
            totalRawPoints = 0,
            acceptedPoints = 0,
            rejectedPoints = 0,
            lastLocationPoint = null
        )
        _activeRideState.value = RideState.RECORDING

        return newRideId
    }

    suspend fun addLocationPoint(point: LocationPoint) {
        val rideId = _activeRideId.value ?: return
        if (_activeRideState.value != RideState.RECORDING) return

        val currentMetrics = _activeRideMetrics.value
        val previousPoint = currentMetrics.lastLocationPoint

        val newTotalRaw = currentMetrics.totalRawPoints + 1
        var isAccepted = true
        var addedDistance = 0f
        var filteredSpeed = 0f

        if (point.accuracy > 25.0f) {
            // Poor accuracy fix -> reject for distance accumulation, keep raw sample
            isAccepted = false
        } else if (previousPoint != null) {
            val deltaDistance = HaversineDistanceCalculator.calculateDistanceMeters(
                lat1 = previousPoint.latitude,
                lon1 = previousPoint.longitude,
                lat2 = point.latitude,
                lon2 = point.longitude
            )
            val deltaTimeSec = maxOf(0.1f, (point.timestamp - previousPoint.timestamp) / 1000f)
            val impliedSpeed = deltaDistance / deltaTimeSec
            
            // FusedLocationProvider often reports speed exactly 0.0 when it detects the phone is perfectly still
            val isReportedStationary = point.hasSpeed && point.speed < 0.5f

            if (impliedSpeed > 35.0f && deltaDistance > 30.0f) {
                // Unrealistic GPS jump rejection
                isAccepted = false
            } else if (isReportedStationary && deltaDistance < 10.0f) {
                // System says we are stationary, and the jump is small. Trust it.
                isAccepted = true
                addedDistance = 0f
                filteredSpeed = 0f
            } else if (deltaDistance < 3.0f || impliedSpeed < 0.8f) {
                // Stationary noise / table jitter -> zero speed, 0 added distance
                isAccepted = true
                addedDistance = 0f
                filteredSpeed = 0f
            } else {
                // Valid cycling movement
                isAccepted = true
                addedDistance = deltaDistance
                filteredSpeed = if (point.hasSpeed && point.speed > 0f) point.speed else impliedSpeed
            }
        } else {
            // First point received
            isAccepted = true
            filteredSpeed = if (point.hasSpeed && point.speed >= 0.8f) point.speed else 0f
        }

        val newAccepted = if (isAccepted) currentMetrics.acceptedPoints + 1 else currentMetrics.acceptedPoints
        val newRejected = if (!isAccepted) currentMetrics.rejectedPoints + 1 else currentMetrics.rejectedPoints
        val newDistance = currentMetrics.distanceMeters + addedDistance
        val newMaxSpeed = maxOf(currentMetrics.maxSpeedMps, filteredSpeed)
        val elapsed = currentMetrics.elapsedTimeSeconds
        val newAvgSpeed = if (elapsed > 0) newDistance / elapsed else 0f

        val updatedMetrics = currentMetrics.copy(
            distanceMeters = newDistance,
            currentSpeedMps = filteredSpeed,
            rawSpeedMps = point.speed,
            averageSpeedMps = newAvgSpeed,
            maxSpeedMps = newMaxSpeed,
            currentAccuracyMeters = point.accuracy,
            totalRawPoints = newTotalRaw,
            acceptedPoints = newAccepted,
            rejectedPoints = newRejected,
            lastLocationPoint = if (isAccepted) point else currentMetrics.lastLocationPoint
        )

        _activeRideMetrics.value = updatedMetrics

        // Always store raw location point to Room database
        rideDao.insertLocationPoint(
            LocationPointEntity(
                rideId = rideId,
                latitude = point.latitude,
                longitude = point.longitude,
                timestamp = point.timestamp,
                altitude = point.altitude,
                speed = point.speed,
                bearing = point.bearing,
                accuracy = point.accuracy,
                verticalAccuracy = point.verticalAccuracy,
                speedAccuracy = point.speedAccuracy,
                provider = point.provider,
                isAccepted = isAccepted
            )
        )

        // Update ride summary in Room
        val existingEntity = rideDao.getRideById(rideId)
        if (existingEntity != null) {
            rideDao.updateRide(
                existingEntity.copy(
                    elapsedTimeSeconds = elapsed,
                    distanceMeters = newDistance,
                    maxSpeedMps = newMaxSpeed,
                    averageSpeedMps = newAvgSpeed,
                    totalRawPoints = newTotalRaw,
                    acceptedPoints = newAccepted,
                    rejectedPoints = newRejected
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
                    totalRawPoints = currentMetrics.totalRawPoints,
                    acceptedPoints = currentMetrics.acceptedPoints,
                    rejectedPoints = currentMetrics.rejectedPoints,
                    status = "STOPPED"
                )
            )
        }
    }

    fun resetRide() {
        _activeRideState.value = RideState.IDLE
        val currentMetrics = _activeRideMetrics.value
        _activeRideMetrics.value = RideMetrics(
            hasAccelerometer = currentMetrics.hasAccelerometer,
            hasGyroscope = currentMetrics.hasGyroscope,
            hasBarometer = currentMetrics.hasBarometer
        )
        _activeRideId.value = null
    }

    suspend fun recoverActiveRideFromDb() {
        val activeRide = rideDao.getActiveRecordingRide() ?: return
        val points = rideDao.getLocationPointsForRide(activeRide.rideId)

        _activeRideId.value = activeRide.rideId
        _activeRideState.value = RideState.RECORDING

        val lastEntityPoint = points.lastOrNull { it.isAccepted } ?: points.lastOrNull()
        val lastLocationPoint = lastEntityPoint?.let {
            LocationPoint(
                latitude = it.latitude,
                longitude = it.longitude,
                timestamp = it.timestamp,
                altitude = it.altitude,
                speed = it.speed,
                bearing = it.bearing,
                accuracy = it.accuracy,
                verticalAccuracy = it.verticalAccuracy,
                speedAccuracy = it.speedAccuracy,
                provider = it.provider
            )
        }

        _activeRideMetrics.value = RideMetrics(
            elapsedTimeSeconds = activeRide.elapsedTimeSeconds,
            distanceMeters = activeRide.distanceMeters,
            currentSpeedMps = 0f,
            rawSpeedMps = lastLocationPoint?.speed ?: 0f,
            averageSpeedMps = activeRide.averageSpeedMps,
            maxSpeedMps = activeRide.maxSpeedMps,
            currentAccuracyMeters = lastLocationPoint?.accuracy ?: 0f,
            totalRawPoints = activeRide.totalRawPoints,
            acceptedPoints = activeRide.acceptedPoints,
            rejectedPoints = activeRide.rejectedPoints,
            lastLocationPoint = lastLocationPoint,
            hasAccelerometer = activeRide.hasAccelerometer,
            hasGyroscope = activeRide.hasGyroscope,
            hasBarometer = activeRide.hasBarometer
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
