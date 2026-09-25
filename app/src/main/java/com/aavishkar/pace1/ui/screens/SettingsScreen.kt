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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aavishkar.pace1.coach.CoachSettings
import com.aavishkar.pace1.coach.CoachSettingsRepository
import com.aavishkar.pace1.coach.PaceCoachManager

@Composable
fun SettingsScreen(
    coachSettingsRepository: CoachSettingsRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentSettings by coachSettingsRepository.settings.collectAsState()
    val coachManager = remember { PaceCoachManager(context) }

    var isCoachEnabled by remember(currentSettings) { mutableStateOf(currentSettings.isCoachEnabled) }
    var targetSpeed by remember(currentSettings) { mutableFloatStateOf(currentSettings.targetSpeedKmh) }
    var lowerBound by remember(currentSettings) { mutableFloatStateOf(currentSettings.lowerBoundKmh) }
    var upperBound by remember(currentSettings) { mutableFloatStateOf(currentSettings.upperBoundKmh) }
    var intervalSec by remember(currentSettings) { mutableIntStateOf(currentSettings.intervalSeconds) }
    var isVoiceAssistantEnabled by remember(currentSettings) { mutableStateOf(currentSettings.isVoiceAssistantEnabled) }

    val saveSettings = {
        val updated = CoachSettings(
            isCoachEnabled = isCoachEnabled,
            targetSpeedKmh = targetSpeed,
            lowerBoundKmh = lowerBound,
            upperBoundKmh = upperBound,
            intervalSeconds = intervalSec,
            isVoiceAssistantEnabled = isVoiceAssistantEnabled,
            isWakeWordEnabled = false
        )
        coachSettingsRepository.updateSettings(updated)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Pace1 Settings",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        // Pace Coach Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Headphone Pace Coach",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Enable Voice Coach")
                    Switch(
                        checked = isCoachEnabled,
                        onCheckedChange = {
                            isCoachEnabled = it
                            saveSettings()
                        }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text("Target Speed: %.1f km/h".format(targetSpeed))
                Slider(
                    value = targetSpeed,
                    onValueChange = {
                        targetSpeed = it
                        if (lowerBound > targetSpeed) lowerBound = targetSpeed - 2f
                        if (upperBound < targetSpeed) upperBound = targetSpeed + 2f
                        saveSettings()
                    },
                    valueRange = 10f..45f
                )

                Text("Lower Boundary: %.1f km/h".format(lowerBound))
                Slider(
                    value = lowerBound,
                    onValueChange = {
                        if (it <= targetSpeed) {
                            lowerBound = it
                            saveSettings()
                        }
                    },
                    valueRange = 5f..targetSpeed
                )

                Text("Upper Boundary: %.1f km/h".format(upperBound))
                Slider(
                    value = upperBound,
                    onValueChange = {
                        if (it >= targetSpeed) {
                            upperBound = it
                            saveSettings()
                        }
                    },
                    valueRange = targetSpeed..50f
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("Announcement Interval: ${intervalSec}s")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf(15, 30, 60).forEach { sec ->
                        Button(
                            onClick = {
                                intervalSec = sec
                                saveSettings()
                            },
                            enabled = intervalSec != sec
                        ) {
                            Text("${sec}s")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        coachManager.speak(
                            "Pace Coach is active. Target speed %.1f kilometers per hour.".format(targetSpeed)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("TEST VOICE / TTS")
                }
            }
        }

        // Voice Assistant & Voice Stats Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Voice Commands & Stats",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Voice Assistant Status")
                    Switch(
                        checked = isVoiceAssistantEnabled,
                        onCheckedChange = {
                            isVoiceAssistantEnabled = it
                            saveSettings()
                        }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Use the 'TELL ME MY STATS' button during a ride for audio stats announcements.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Map Provider Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Route Map Provider",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "MapLibre Native SDK (Demotiles Open Style)",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Open tile style hosted by MapLibre Organization. No private API key required.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
