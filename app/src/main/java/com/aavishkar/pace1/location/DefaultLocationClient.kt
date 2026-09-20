package com.aavishkar.pace1.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Looper
import androidx.core.content.ContextCompat
import com.aavishkar.pace1.data.model.LocationPoint
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Default implementation of [LocationClient] using Google's FusedLocationProviderClient.
 */
class DefaultLocationClient(
    private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient
) : LocationClient {

    override fun getLocationUpdates(intervalMs: Long): Flow<LocationUpdateState> = callbackFlow {
        if (!hasLocationPermission()) {
            trySend(LocationUpdateState.PermissionDenied)
            close()
            return@callbackFlow
        }

        if (!isLocationEnabled()) {
            trySend(LocationUpdateState.LocationDisabled)
            close()
            return@callbackFlow
        }

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMs)
            .setMinUpdateIntervalMillis(intervalMs / 2)
            .build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)
                result.locations.lastOrNull()?.let { location ->
                    val verticalAcc = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && location.hasVerticalAccuracy()) {
                        location.verticalAccuracyMeters
                    } else 0f

                    val speedAcc = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && location.hasSpeedAccuracy()) {
                        location.speedAccuracyMetersPerSecond
                    } else 0f

                    val point = LocationPoint(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        timestamp = location.time,
                        altitude = location.altitude,
                        speed = location.speed,
                        bearing = location.bearing,
                        accuracy = location.accuracy,
                        verticalAccuracy = verticalAcc,
                        speedAccuracy = speedAcc,
                        provider = location.provider ?: "fused"
                    )
                    trySend(LocationUpdateState.Success(point))
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                request,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (_: SecurityException) {
            trySend(LocationUpdateState.PermissionDenied)
            close()
            return@callbackFlow
        } catch (e: Exception) {
            trySend(LocationUpdateState.Error(e.message ?: "Failed to request location updates"))
            close()
            return@callbackFlow
        }

        awaitClose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    private fun hasLocationPermission(): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fineGranted || coarseGranted
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        return isGpsEnabled || isNetworkEnabled
    }
}
