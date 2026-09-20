package com.aavishkar.pace1.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.aavishkar.pace1.MainActivity
import com.aavishkar.pace1.R
import com.aavishkar.pace1.data.local.PaceDatabase
import com.aavishkar.pace1.data.model.RideMetrics
import com.aavishkar.pace1.data.repository.RideRepository
import com.aavishkar.pace1.location.DefaultLocationClient
import com.aavishkar.pace1.location.LocationClient
import com.aavishkar.pace1.location.LocationUpdateState
import com.aavishkar.pace1.ui.formatDistance
import com.aavishkar.pace1.ui.formatElapsedTime
import com.aavishkar.pace1.ui.formatSpeedKmh
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Foreground Service maintaining ride recording, location tracking, and Room persistence
 * across background, lock screen, and app recreation events.
 */
class RideRecordingService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private lateinit var repository: RideRepository
    private lateinit var locationClient: LocationClient

    private var locationJob: Job? = null
    private var timerJob: Job? = null
    private var isRecording = false

    override fun onCreate() {
        super.onCreate()
        repository = RideRepository.getInstance(applicationContext)
        locationClient = DefaultLocationClient(
            context = applicationContext,
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(applicationContext)
        )
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_RIDE -> startRecordingRide()
            ACTION_STOP_RIDE -> stopRecordingRide()
        }
        return START_STICKY
    }

    private fun startRecordingRide() {
        if (isRecording) return
        isRecording = true

        val notification = buildNotification(RideMetrics())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        serviceScope.launch {
            repository.recoverActiveRideFromDb()
            if (repository.activeRideId.value == null) {
                repository.startNewRide()
            }
            startLocationTracking()
            startTimer()
        }
    }

    private fun startLocationTracking() {
        locationJob?.cancel()
        locationJob = serviceScope.launch {
            locationClient.getLocationUpdates(1000L).collect { updateState ->
                if (updateState is LocationUpdateState.Success) {
                    repository.addLocationPoint(updateState.locationPoint)
                    updateNotification(repository.activeRideMetrics.value)
                }
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (isRecording) {
                delay(1000L)
                repository.incrementTimerSecond()
                updateNotification(repository.activeRideMetrics.value)
            }
        }
    }

    private fun stopRecordingRide() {
        isRecording = false

        serviceScope.launch {
            repository.stopRide()
            locationJob?.cancel()
            timerJob?.cancel()
            locationJob = null
            timerJob = null

            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun updateNotification(metrics: RideMetrics) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, buildNotification(metrics))
    }

    private fun buildNotification(metrics: RideMetrics): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val textContent = "Dist: ${formatDistance(metrics.distanceMeters)} | " +
                "Time: ${formatElapsedTime(metrics.elapsedTimeSeconds)} | " +
                "Speed: ${formatSpeedKmh(metrics.currentSpeedMps)}"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Pace1 Ride Recording")
            .setContentText(textContent)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_WORKOUT)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Ride Recording Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows live metrics during an active ride recording session."
            }
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object {
        const val CHANNEL_ID = "pace1_ride_recording_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START_RIDE = "com.aavishkar.pace1.action.START_RIDE"
        const val ACTION_STOP_RIDE = "com.aavishkar.pace1.action.STOP_RIDE"
    }
}
