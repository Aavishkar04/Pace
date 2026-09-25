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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.aavishkar.pace1.coach.CoachSettingsRepository
import com.aavishkar.pace1.data.repository.RideRepository
import com.aavishkar.pace1.location.DefaultLocationClient
import com.aavishkar.pace1.location.LocationClient
import com.aavishkar.pace1.ui.RideViewModel
import com.aavishkar.pace1.ui.navigation.Pace1MainApp
import com.aavishkar.pace1.ui.theme.Pace1Theme
import com.google.android.gms.location.LocationServices

class MainActivity : ComponentActivity() {

    private lateinit var locationClient: LocationClient
    private lateinit var repository: RideRepository
    private lateinit var coachSettingsRepository: CoachSettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        repository = RideRepository.getInstance(applicationContext)
        coachSettingsRepository = CoachSettingsRepository(applicationContext)
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

                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) {
                    hasPermission = checkRequiredPermissions(context)
                }

                Pace1MainApp(
                    viewModel = viewModel,
                    coachSettingsRepository = coachSettingsRepository,
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
                    }
                )
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
