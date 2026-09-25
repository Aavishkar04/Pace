package com.aavishkar.pace1.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aavishkar.pace1.coach.PaceCoachManager
import com.aavishkar.pace1.data.model.RideMetrics
import com.aavishkar.pace1.data.model.RideState
import com.aavishkar.pace1.location.LocationUpdateState
import com.aavishkar.pace1.ui.map.LiveMapView

@Composable
fun RideRecordingScreen(
    rideState: RideState,
    rideMetrics: RideMetrics,
    locationUpdateState: LocationUpdateState?,
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    onStartRide: () -> Unit,
    onStopRide: () -> Unit,
    onResetRide: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coachManager = remember { PaceCoachManager(context) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        GpsStatusHeader(
            hasPermission = hasPermission,
            locationUpdateState = locationUpdateState,
            onRequestPermission = onRequestPermission
        )

        // Live Route Map View Area
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            LiveMapView(
                lastLocationPoint = rideMetrics.lastLocationPoint,
                routePoints = listOfNotNull(rideMetrics.lastLocationPoint)
            )
        }

        // Live Cycling Metrics Panel
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "CURRENT SPEED",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = formatSpeedKmh(rideMetrics.currentSpeedMps),
                fontSize = 40.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MetricCard(
                    title = "DISTANCE",
                    value = formatDistance(rideMetrics.distanceMeters),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.padding(4.dp))
                MetricCard(
                    title = "ELAPSED TIME",
                    value = formatElapsedTime(rideMetrics.elapsedTimeSeconds),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MetricCard(
                    title = "AVG SPEED",
                    value = formatSpeedKmh(rideMetrics.averageSpeedMps),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.padding(4.dp))
                MetricCard(
                    title = "MAX SPEED",
                    value = formatSpeedKmh(rideMetrics.maxSpeedMps),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tell Me My Stats Button (During Active Recording)
            if (rideState == RideState.RECORDING) {
                Button(
                    onClick = { coachManager.speakRideStats(rideMetrics) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Text("TELL ME MY STATS (VOICE)", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Controls
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (rideState) {
                RideState.IDLE -> {
                    Button(
                        onClick = onStartRide,
                        enabled = hasPermission,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text("START RIDE", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
                RideState.RECORDING -> {
                    Button(
                        onClick = onStopRide,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text("STOP RIDE", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
                RideState.STOPPED -> {
                    Button(
                        onClick = onResetRide,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text("NEW RIDE", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun GpsStatusHeader(
    hasPermission: Boolean,
    locationUpdateState: LocationUpdateState?,
    onRequestPermission: () -> Unit
) {
    if (!hasPermission) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Permissions Required",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Button(onClick = onRequestPermission) {
                    Text("Grant")
                }
            }
        }
    } else {
        val statusText = when (locationUpdateState) {
            null -> "GPS Status: Initializing..."
            is LocationUpdateState.LocationDisabled -> "GPS Status: Disabled on Device"
            is LocationUpdateState.PermissionDenied -> "GPS Status: Permission Denied"
            is LocationUpdateState.Error -> "GPS Status: Error (${locationUpdateState.message})"
            is LocationUpdateState.Success -> "GPS Status: Connected"
        }

        Text(
            text = statusText,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

fun formatElapsedTime(seconds: Long): String {
    val hrs = seconds / 3600
    val mins = (seconds % 3600) / 60
    val secs = seconds % 60
    return if (hrs > 0) {
        "%02d:%02d:%02d".format(hrs, mins, secs)
    } else {
        "%02d:%02d".format(mins, secs)
    }
}

fun formatSpeedKmh(speedMps: Float): String {
    val kmh = speedMps * 3.6f
    return "%.1f km/h".format(kmh)
}

fun formatDistance(meters: Float): String {
    return if (meters >= 1000f) {
        "%.2f km".format(meters / 1000f)
    } else {
        "%.0f m".format(meters)
    }
}
