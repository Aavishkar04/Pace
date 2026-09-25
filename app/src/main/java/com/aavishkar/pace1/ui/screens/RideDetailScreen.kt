package com.aavishkar.pace1.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aavishkar.pace1.data.local.PaceDatabase
import com.aavishkar.pace1.data.local.entity.LocationPointEntity
import com.aavishkar.pace1.data.local.entity.RideEntity
import com.aavishkar.pace1.ui.formatDistance
import com.aavishkar.pace1.ui.formatElapsedTime
import com.aavishkar.pace1.ui.formatSpeedKmh
import com.aavishkar.pace1.ui.map.PostRideMapView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RideDetailScreen(
    rideId: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val db = remember { PaceDatabase.getInstance(context) }

    var rideEntity by remember { mutableStateOf<RideEntity?>(null) }
    var locationPoints by remember { mutableStateOf<List<LocationPointEntity>>(emptyList()) }

    LaunchedEffect(rideId) {
        rideEntity = db.rideDao().getRideById(rideId)
        locationPoints = db.rideDao().getLocationPointsForRide(rideId)
    }

    val ride = rideEntity

    if (ride == null) {
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Loading ride details...")
        }
    } else {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val dateStr = dateFormat.format(Date(ride.startTimeMillis))

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Ride Summary",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = dateStr,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Post-Ride Route Map
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                PostRideMapView(points = locationPoints)
            }

            // Core Metrics Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("DISTANCE", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = formatDistance(ride.distanceMeters),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column {
                            Text("DURATION", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = formatElapsedTime(ride.elapsedTimeSeconds),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("AVG SPEED", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = formatSpeedKmh(ride.averageSpeedMps),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column {
                            Text("MAX SPEED", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = formatSpeedKmh(ride.maxSpeedMps),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Data Quality & Diagnostics
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Data Quality & Telemetry",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Total Raw GPS Points: ${ride.totalRawPoints}")
                    Text("Accepted Movement Points: ${ride.acceptedPoints}")
                    Text("Rejected/Noise Points: ${ride.rejectedPoints}")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Sensors Recorded: Accel [${if (ride.hasAccelerometer) "YES" else "NO"}], Gyro [${if (ride.hasGyroscope) "YES" else "NO"}], Baro [${if (ride.hasBarometer) "YES" else "NO"}]")
                }
            }
        }
    }
}
