package com.aavishkar.pace1.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aavishkar.pace1.data.local.entity.RideEntity
import com.aavishkar.pace1.ui.formatDistance
import com.aavishkar.pace1.ui.formatElapsedTime
import com.aavishkar.pace1.ui.formatSpeedKmh
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    completedRides: List<RideEntity>,
    onNavigateToRecord: () -> Unit,
    onNavigateToRideDetail: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val latestRide = completedRides.firstOrNull()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Pace1 Cycling",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        // Quick Start Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Ready to ride?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onNavigateToRecord,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text("START RECORDING", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Recent Ride Overview
        if (latestRide != null) {
            Text(
                text = "Latest Ride",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val dateStr = dateFormat.format(Date(latestRide.startTimeMillis))

            Card(
                onClick = { onNavigateToRideDetail(latestRide.rideId) },
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Dist: ${formatDistance(latestRide.distanceMeters)}")
                        Text("Time: ${formatElapsedTime(latestRide.elapsedTimeSeconds)}")
                        Text("Avg: ${formatSpeedKmh(latestRide.averageSpeedMps)}")
                    }
                }
            }
        }

        // Overall Stats Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Lifetime Stats",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Total Rides: ${completedRides.size}")
                val totalMeters = completedRides.sumOf { it.distanceMeters.toDouble() }.toFloat()
                Text("Total Distance: ${formatDistance(totalMeters)}")
            }
        }
    }
}
