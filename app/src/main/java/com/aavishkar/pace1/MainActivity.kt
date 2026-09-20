package com.aavishkar.pace1

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.aavishkar.pace1.data.local.PaceDatabase
import com.aavishkar.pace1.data.repository.RideRepository
import com.aavishkar.pace1.location.DefaultLocationClient
import com.aavishkar.pace1.location.LocationClient
import com.aavishkar.pace1.ui.RideRecordingScreen
import com.aavishkar.pace1.ui.RideViewModel
import com.aavishkar.pace1.ui.theme.Pace1Theme
import com.google.android.gms.location.LocationServices

class MainActivity : ComponentActivity() {

    private lateinit var locationClient: LocationClient
    private lateinit var repository: RideRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        repository = RideRepository.getInstance(applicationContext)
        locationClient = DefaultLocationClient(
            context = applicationContext,
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(applicationContext)
        )

        setContent {
            Pace1Theme {
                val context = LocalContext.current
                var hasPermission by remember {
                    mutableStateOf(checkRequiredPermissions(context))
                }

                val viewModel = remember { RideViewModel(repository, locationClient) }

                val rideState by viewModel.rideState.collectAsState()
                val rideMetrics by viewModel.rideMetrics.collectAsState()
                val completedRides by viewModel.completedRides.collectAsState()
                val locationUpdateState by viewModel.locationUpdateState.collectAsState()

                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) {
                    hasPermission = checkRequiredPermissions(context)
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    RideRecordingScreen(
                        rideState = rideState,
                        rideMetrics = rideMetrics,
                        locationUpdateState = locationUpdateState,
                        completedRides = completedRides,
                        hasPermission = hasPermission,
                        onRequestPermission = {
                            val perms = mutableListOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                perms.add(Manifest.permission.POST_NOTIFICATIONS)
                            }
                            permissionLauncher.launch(perms.toTypedArray())
                        },
                        onStartRide = { viewModel.startRide(context) },
                        onStopRide = { viewModel.stopRide(context) },
                        onResetRide = { viewModel.resetRide() },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

private fun checkRequiredPermissions(context: Context): Boolean {
    val fine = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    val coarse = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    val notifications = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }

    return (fine || coarse) && notifications
}
