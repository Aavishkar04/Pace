# Current Project State: Pace1

**Date:** March 2026
**Current Milestone:** V1.0 Stable Outdoor Test Build (Stationary Noise Filter + Hardware Sensors + Foreground Service + Room DB + Ride History)

## Implemented & Verified
- Base Android project structure created (`com.aavishkar.pace1`).
- Jetpack Compose Material 3 UI template initialized.
- Build system configured with AGP 8.8.0 / Kotlin 2.0.21 / KSP 2.0.21-1.0.28.
- **Location Permissions & Dependency:** `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`, `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_LOCATION`, and `POST_NOTIFICATIONS` integrated. `play-services-location:21.3.0` integrated.
- **Stationary GPS Noise Filter:**
  - Rejects poor accuracy fixes (> 25.0m).
  - Rejects unrealistic GPS speed/distance jumps (> 35 m/s / 126 km/h).
  - Suppresses micro-jitter (< 2.5m delta or < 0.8 m/s speed) so stationary table placement displays `0.0 km/h` and `0 m` distance increase.
- **Raw Data Preservation:**
  - Every raw location sample (including provider, vertical accuracy, speed accuracy, timestamp) is stored to Room database regardless of filter status, with `isAccepted` flag marked.
- **Hardware Sensor Collection (`SensorCollector`):**
  - Monitors Accelerometer, Gyroscope, Magnetometer, and Barometer (pressure) via Android `SensorManager`.
  - Records sensor health metadata in ride summary.
- **Foreground Service & Recovery:** `RideRecordingService` runs as a Foreground Service with persistent status bar notification (`Pace1 Ride Recording`). Survives backgrounding, lock screen, and Activity recreation. Reopening app recovers active recording session from Room DB.
- **Room Persistence & DAO:** `PaceDatabase`, `RideDao`, `RideEntity`, `LocationPointEntity`.
- **Ride History UI:** "HISTORY" tab in `RideRecordingScreen.kt` listing all previously saved completed rides from Room DB with date, distance, duration, average speed, max speed, and raw/accepted point counts.
- **Diagnostics & Data Quality Panel:** Live UI displays raw point counts (total, accepted, rejected), raw vs filtered speed comparison, GPS accuracy, and sensor health status.

## Not Yet Implemented
- Map & route visualization (MapLibre) (V1.4).
- Audio speed coach (V2.0).

## Build & Test Status
- **Build Status:** PASS (`:app:assembleDebug` succeeds).
- **Unit Tests:** PASS (`:app:testDebugUnitTest` - 9 tests passed, 0 failed).
- **Stationary Table Test Verification:** PASS (Filtered speed stays `0.0 km/h`, distance stays at `0 m` without false accumulation, timer continues, foreground service stays active).
