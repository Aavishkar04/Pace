package com.aavishkar.pace1.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aavishkar.pace1.data.model.LocationPoint
import com.aavishkar.pace1.data.model.RideMetrics
import com.aavishkar.pace1.data.model.RideState
import com.aavishkar.pace1.location.HaversineDistanceCalculator
import com.aavishkar.pace1.location.LocationClient
import com.aavishkar.pace1.location.LocationUpdateState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel managing the state machine and real-time metric calculations for a ride.
 */
class RideViewModel(
    private val locationClient: LocationClient
) : ViewModel() {

    private val _rideState = MutableStateFlow(RideState.IDLE)
    val rideState: StateFlow<RideState> = _rideState.asStateFlow()

    private val _rideMetrics = MutableStateFlow(RideMetrics())
    val rideMetrics: StateFlow<RideMetrics> = _rideMetrics.asStateFlow()

    private val _locationUpdateState = MutableStateFlow<LocationUpdateState?>(null)
    val locationUpdateState: StateFlow<LocationUpdateState?> = _locationUpdateState.asStateFlow()

    private var timerJob: Job? = null
    private var locationJob: Job? = null

    fun startRide() {
        if (_rideState.value == RideState.RECORDING) return

        _rideState.value = RideState.RECORDING
        _rideMetrics.value = RideMetrics()

        startTimer()
        startLocationTracking()
    }

    fun stopRide() {
        if (_rideState.value != RideState.RECORDING) return

        _rideState.value = RideState.STOPPED
        timerJob?.cancel()
        locationJob?.cancel()
        timerJob = null
        locationJob = null
    }

    fun resetRide() {
        stopRide()
        _rideState.value = RideState.IDLE
        _rideMetrics.value = RideMetrics()
        _locationUpdateState.value = null
    }

    fun processLocationPoint(point: LocationPoint) {
        if (_rideState.value != RideState.RECORDING) return

        val currentMetrics = _rideMetrics.value
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

        _rideMetrics.value = currentMetrics.copy(
            distanceMeters = newDistance,
            currentSpeedMps = point.speed,
            averageSpeedMps = newAvgSpeed,
            maxSpeedMps = newMaxSpeed,
            currentAccuracyMeters = point.accuracy,
            lastLocationPoint = point
        )
    }

    fun incrementTimerSecond() {
        if (_rideState.value != RideState.RECORDING) return

        val currentMetrics = _rideMetrics.value
        val newElapsed = currentMetrics.elapsedTimeSeconds + 1
        val newAvgSpeed = if (newElapsed > 0) currentMetrics.distanceMeters / newElapsed else 0f

        _rideMetrics.value = currentMetrics.copy(
            elapsedTimeSeconds = newElapsed,
            averageSpeedMps = newAvgSpeed
        )
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_rideState.value == RideState.RECORDING) {
                delay(1000L)
                incrementTimerSecond()
            }
        }
    }

    private fun startLocationTracking() {
        locationJob?.cancel()
        locationJob = viewModelScope.launch {
            locationClient.getLocationUpdates(1000L).collect { updateState ->
                _locationUpdateState.value = updateState
                if (updateState is LocationUpdateState.Success) {
                    processLocationPoint(updateState.locationPoint)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        locationJob?.cancel()
    }
}
