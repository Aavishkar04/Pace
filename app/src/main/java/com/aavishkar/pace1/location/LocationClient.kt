package com.aavishkar.pace1.location

import com.aavishkar.pace1.data.model.LocationPoint
import kotlinx.coroutines.flow.Flow

/**
 * State seal for location tracking updates.
 */
sealed interface LocationUpdateState {
    data class Success(val locationPoint: LocationPoint) : LocationUpdateState
    data object PermissionDenied : LocationUpdateState
    data object LocationDisabled : LocationUpdateState
    data class Error(val message: String) : LocationUpdateState
}

/**
 * Interface abstraction for location services.
 */
interface LocationClient {
    /**
     * Emits location update states at the given time interval in milliseconds.
     */
    fun getLocationUpdates(intervalMs: Long): Flow<LocationUpdateState>
}
