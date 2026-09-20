# Technical Architecture: Pace1

## Architecture Pattern
Pace1 follows modern Android development principles with standard **MVVM (Model-View-ViewModel)** and the **Repository Pattern**, combined with an Android **Foreground Service** for background recording, **SensorManager** for hardware telemetry, and **Room** for local database persistence.

```
[ UI Layer (Compose / RideRecordingScreen) ]
        │
        ▼
[ ViewModels (RideViewModel) ]
        │
        ▼
[ Repository Layer (RideRepository - Singleton) ]
    ┌───┴───────────────────────┬───────────────────────────┬───────────────────────────┐
    ▼                           ▼                           ▼                           ▼
[ Foreground Service ]  [ Location Client ]     [ Sensor Collector ]    [ Room Database ]
(RideRecordingService)  (FusedLocationClient)     (SensorManager)         (PaceDatabase)
```

## Packages (`com.aavishkar.pace1`)
- `ui/`
  - `theme/` - Color scheme, typography, and Material 3 theme.
  - `RideRecordingScreen.kt` - Dashboard with RECORDING tab and HISTORY tab.
  - `RideViewModel.kt` - ViewModel exposing StateFlows for metrics, completed rides, and location states.
- `data/`
  - `model/` - Domain models (`LocationPoint`, `RideMetrics`, `RideState`).
  - `local/` - Room DB (`PaceDatabase`), DAOs (`RideDao`), and entities (`RideEntity`, `LocationPointEntity`).
  - `repository/` - Data repository (`RideRepository`).
- `location/` - Fused Location Provider client wrapper (`LocationClient`, `DefaultLocationClient`, `HaversineDistanceCalculator`).
- `sensor/` - Hardware sensors collector (`SensorCollector`, `SensorHealth`, `LatestSensorData`).
- `service/` - Android Foreground Service (`RideRecordingService`) managing background location tracking and persistent status bar notification.

## Build Configuration
- AGP: 8.8.0
- Kotlin: 2.0.21
- KSP: 2.0.21-1.0.28
- Room: 2.6.1
- Play Services Location: 21.3.0
