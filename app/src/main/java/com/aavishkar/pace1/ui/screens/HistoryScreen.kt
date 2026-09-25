package com.aavishkar.pace1.ui.screens

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aavishkar.pace1.data.local.entity.RideEntity
import com.aavishkar.pace1.ui.formatDistance
import com.aavishkar.pace1.ui.formatElapsedTime
import com.aavishkar.pace1.ui.formatSpeedKmh
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    completedRides: List<RideEntity>,
    onNavigateToRideDetail: (String) -> Unit = {}
) {
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
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(completedRides) { ride ->
                RideHistoryCard(ride = ride, onClick = { onNavigateToRideDetail(ride.rideId) })
            }
        }
    }
}

@Composable
fun RideHistoryCard(
    ride: RideEntity,
    onClick: () -> Unit = {}
) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    val dateStr = dateFormat.format(Date(ride.startTimeMillis))

    Card(
        onClick = onClick,
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
