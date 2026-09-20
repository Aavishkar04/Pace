# Technical Architecture: Pace1

## Architecture Pattern
Pace1 follows modern Android development principles with standard **MVVM (Model-View-ViewModel)** and the **Repository Pattern**, combined with a **Foreground Service** for background lifecycle management and **Room** for local database persistence.

```
[ UI Layer (Compose) ]
        │
        ▼
[ ViewModels (StateFlow) ]
        │
        ▼
[ Repository Layer (RideRepository - Singleton) ]
    ┌───┴───────────────────────┬───────────────────────────┐
    ▼                           ▼                           ▼
[ Foreground Service ]  [ Location Client ]     [ Room Database ]
(RideRecordingService)  (FusedLocationClient)     (PaceDatabase)
```

## Module Structure
- `:app` - Single-module Android application.

## Packages (`com.aavishkar.pace1`)
- `ui/`
  - `theme/` - Color scheme, typography, and Material 3 theme.
  - `RideRecordingScreen.kt` - Ride recording dashboard Composable.
  - `RideViewModel.kt` - ViewModel observing repository state and managing service intents.
- `data/`
  - `model/` - Domain models (`LocationPoint`, `RideMetrics`, `RideState`).
  - `local/` - Room DB (`PaceDatabase`), DAOs (`RideDao`), and entities (`RideEntity`, `LocationPointEntity`).
  - `repository/` - Data repository (`RideRepository`).
- `location/` - Fused Location Provider client wrapper (`LocationClient`, `DefaultLocationClient`, `HaversineDistanceCalculator`).
- `service/` - Android Foreground Service (`RideRecordingService`) managing background location tracking and persistent status bar notification.

## Build Configuration
- AGP: 8.8.0
- Kotlin: 2.0.21
- KSP: 2.0.21-1.0.28
- Room: 2.6.1
- Play Services Location: 21.3.0
