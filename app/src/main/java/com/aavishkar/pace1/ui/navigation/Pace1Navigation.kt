package com.aavishkar.pace1.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.aavishkar.pace1.coach.CoachSettingsRepository
import com.aavishkar.pace1.ui.RideRecordingScreen
import com.aavishkar.pace1.ui.RideViewModel
import com.aavishkar.pace1.ui.screens.HistoryScreen
import com.aavishkar.pace1.ui.screens.HomeScreen
import com.aavishkar.pace1.ui.screens.RideDetailScreen
import com.aavishkar.pace1.ui.screens.SettingsScreen

sealed class NavScreen(val route: String, val title: String, val icon: ImageVector) {
    data object Home : NavScreen("home", "Home", Icons.Default.Home)
    data object Record : NavScreen("record", "Ride", Icons.Default.PlayArrow)
    data object History : NavScreen("history", "History", Icons.AutoMirrored.Filled.List)
    data object Settings : NavScreen("settings", "Settings", Icons.Default.Settings)
}

@Composable
fun Pace1MainApp(
    viewModel: RideViewModel,
    coachSettingsRepository: CoachSettingsRepository,
    hasPermission: Boolean,
    onRequestPermission: () -> Unit
) {
    val navController = rememberNavController()
    val context = LocalContext.current

    val rideState by viewModel.rideState.collectAsState()
    val rideMetrics by viewModel.rideMetrics.collectAsState()
    val locationUpdateState by viewModel.locationUpdateState.collectAsState()
    val completedRides by viewModel.completedRides.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavItems = listOf(
        NavScreen.Home,
        NavScreen.Record,
        NavScreen.History,
        NavScreen.Settings
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NavScreen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(NavScreen.Home.route) {
                HomeScreen(
                    completedRides = completedRides,
                    onNavigateToRecord = {
                        navController.navigate(NavScreen.Record.route)
                    },
                    onNavigateToRideDetail = { rideId ->
                        navController.navigate("ride_detail/$rideId")
                    }
                )
            }

            composable(NavScreen.Record.route) {
                RideRecordingScreen(
                    rideState = rideState,
                    rideMetrics = rideMetrics,
                    locationUpdateState = locationUpdateState,
                    hasPermission = hasPermission,
                    onRequestPermission = onRequestPermission,
                    onStartRide = { viewModel.startRide(context) },
                    onStopRide = { viewModel.stopRide(context) },
                    onResetRide = { viewModel.resetRide() }
                )
            }

            composable(NavScreen.History.route) {
                HistoryScreen(
                    completedRides = completedRides,
                    onNavigateToRideDetail = { rideId ->
                        navController.navigate("ride_detail/$rideId")
                    }
                )
            }

            composable(NavScreen.Settings.route) {
                SettingsScreen(coachSettingsRepository = coachSettingsRepository)
            }

            composable(
                route = "ride_detail/{rideId}",
                arguments = listOf(navArgument("rideId") { type = NavType.StringType })
            ) { backStackEntry ->
                val rideId = backStackEntry.arguments?.getString("rideId") ?: ""
                RideDetailScreen(rideId = rideId)
            }
        }
    }
}
