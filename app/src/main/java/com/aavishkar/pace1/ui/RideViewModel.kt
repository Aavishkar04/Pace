package com.aavishkar.pace1.ui

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aavishkar.pace1.data.local.entity.RideEntity
import com.aavishkar.pace1.data.model.RideMetrics
import com.aavishkar.pace1.data.model.RideState
import com.aavishkar.pace1.data.repository.RideRepository
import com.aavishkar.pace1.location.LocationClient
import com.aavishkar.pace1.location.LocationUpdateState
import com.aavishkar.pace1.service.RideRecordingService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel connecting UI to RideRepository and managing start/stop service requests.
 */
class RideViewModel(
    private val repository: RideRepository,
    locationClient: LocationClient
) : ViewModel() {

    val rideState: StateFlow<RideState> = repository.activeRideState
    val rideMetrics: StateFlow<RideMetrics> = repository.activeRideMetrics

    val completedRides: StateFlow<List<RideEntity>> =
        repository.completedRides.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    val locationUpdateState: StateFlow<LocationUpdateState?> =
        locationClient.getLocationUpdates(1000L).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = null
        )

    init {
        viewModelScope.launch {
            repository.recoverActiveRideFromDb()
        }
    }

    fun startRide(context: Context) {
        val intent = Intent(context, RideRecordingService::class.java).apply {
            action = RideRecordingService.ACTION_START_RIDE
        }
        ContextCompat.startForegroundService(context, intent)
    }

    fun stopRide(context: Context) {
        val intent = Intent(context, RideRecordingService::class.java).apply {
            action = RideRecordingService.ACTION_STOP_RIDE
        }
        context.startService(intent)
    }

    fun resetRide() {
        repository.resetRide()
    }
}
