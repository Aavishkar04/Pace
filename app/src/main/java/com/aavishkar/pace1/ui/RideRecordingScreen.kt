package com.aavishkar.pace1.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aavishkar.pace1.data.local.entity.RideEntity
import com.aavishkar.pace1.data.model.RideMetrics
import com.aavishkar.pace1.data.model.RideState
import com.aavishkar.pace1.location.LocationUpdateState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RideRecordingScreen(
    rideState: RideState,
    rideMetrics: RideMetrics,
    locationUpdateState: LocationUpdateState?,
    completedRides: List<RideEntity>,
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    onStartRide: () -> Unit,
    onStopRide: () -> Unit,
    onResetRide: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Pace1 Cycling Tracker",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("RECORDING") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("HISTORY (${completedRides.size})") }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (selectedTab == 0) {
            RecordingTabContent(
                rideState = rideState,
                rideMetrics = rideMetrics,
                locationUpdateState = locationUpdateState,
                hasPermission = hasPermission,
                onRequestPermission = onRequestPermission,
                onStartRide = onStartRide,
                onStopRide = onStopRide,
                onResetRide = onResetRide
            )
        } else {
            HistoryTabContent(completedRides = completedRides)
        }
    }
}

@Composable
fun RecordingTabContent(
    rideState: RideState,
    rideMetrics: RideMetrics,
    locationUpdateState: LocationUpdateState?,
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    onStartRide: () -> Unit,
    onStopRide: () -> Unit,
    onResetRide: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        GpsStatusHeader(
            hasPermission = hasPermission,
            locationUpdateState = locationUpdateState,
            onRequestPermission = onRequestPermission
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "CURRENT SPEED (FILTERED)",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = formatSpeedKmh(rideMetrics.currentSpeedMps),
                fontSize = 44.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Raw Speed: ${formatSpeedKmh(rideMetrics.rawSpeedMps)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )

            Spacer(modifier = Modifier.height(16.dp))

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

            // Debug & Diagnostics Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = "Diagnostics & Sensor Health",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "GPS Acc: ${if (rideMetrics.currentAccuracyMeters > 0) "±%.1fm".format(rideMetrics.currentAccuracyMeters) else "N/A"} | Raw Points: ${rideMetrics.totalRawPoints} (Acc: ${rideMetrics.acceptedPoints}, Rej: ${rideMetrics.rejectedPoints})",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Sensors: Accel [${if (rideMetrics.hasAccelerometer) "OK" else "N/A"}] | Gyro [${if (rideMetrics.hasGyroscope) "OK" else "N/A"}] | Baro [${if (rideMetrics.hasBarometer) "OK" else "N/A"}]",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // Action Controls (START / STOP / NEW)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
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
fun HistoryTabContent(completedRides: List<RideEntity>) {
    if (completedRides.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("No completed rides recorded yet.")
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(completedRides) { ride ->
                RideHistoryCard(ride = ride)
            }
        }
    }
}

@Composable
fun RideHistoryCard(ride: RideEntity) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    val dateStr = dateFormat.format(Date(ride.startTimeMillis))

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = dateStr,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Dist: ${formatDistance(ride.distanceMeters)}")
                Text("Time: ${formatElapsedTime(ride.elapsedTimeSeconds)}")
                Text("Avg: ${formatSpeedKmh(ride.averageSpeedMps)}")
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Points: ${ride.totalRawPoints} raw (${ride.acceptedPoints} acc, ${ride.rejectedPoints} rej) | Max: ${formatSpeedKmh(ride.maxSpeedMps)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
