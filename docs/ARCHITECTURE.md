# Technical Architecture: Pace1

## Architecture Pattern
Pace1 follows modern Android development principles with standard **MVVM (Model-View-ViewModel)** and the **Repository Pattern**, combined with an Android **Foreground Service** for background lifecycle management, **MapLibre Native SDK** for vector maps, **Text-To-Speech** for headphone coaching, **SensorManager** for hardware telemetry, and **Room** for local database persistence.

```
[ UI Layer (Compose Navigation) ]
    ├── HomeScreen
    ├── RideRecordingScreen (LiveMapView)
    ├── HistoryScreen
    ├── RideDetailScreen (PostRideMapView)
    └── SettingsScreen (CoachSettings)
        │
        ▼
[ ViewModels (RideViewModel) ]
        │
        ▼
[ Repository Layer (RideRepository - Singleton) ]
    ┌───┴───────────────────────┬───────────────────────────┬───────────────────────────┬───────────────────────────┐
    ▼                           ▼                           ▼                           ▼                           ▼
[ Foreground Service ]  [ Location Client ]     [ Sensor Collector ]    [ Pace Coach Manager ]  [ Room Database ]
(RideRecordingService)  (FusedLocationClient)     (SensorManager)        (Android TTS + Focus)    (PaceDatabase)
```

## Module & Package Structure (`com.aavishkar.pace1`)
- `ui/`
  - `theme/` - Material 3 theme definitions.
  - `navigation/` - `Pace1Navigation.kt` (`Pace1MainApp` Compose navigation host).
  - `map/` - `MapViewComponents.kt` (`LiveMapView` and `PostRideMapView` wrapping MapLibre SDK).
  - `screens/` - `HomeScreen.kt`, `RideDetailScreen.kt`, `HistoryScreen.kt`, `SettingsScreen.kt`.
  - `RideRecordingScreen.kt` - Dashboard with live route map, hero speed, metrics, and voice stats button.
  - `RideViewModel.kt` - ViewModel observing repository state and managing service intents.
- `coach/`
  - `CoachSettings.kt` - `CoachSettings` data model and `CoachSettingsRepository` (SharedPreferences).
  - `PaceCoachManager.kt` - Android Text-To-Speech engine, audio focus request, coaching logic, and voice stats generator.
- `data/`
  - `model/` - Domain models (`LocationPoint`, `RideMetrics`, `RideState`).
  - `local/` - Room DB (`PaceDatabase`), DAOs (`RideDao`), and entities (`RideEntity`, `LocationPointEntity`).
  - `repository/` - Data repository (`RideRepository`).
- `location/` - Fused Location Provider client wrapper (`LocationClient`, `DefaultLocationClient`, `HaversineDistanceCalculator`).
- `sensor/` - Hardware sensors collector (`SensorCollector`, `SensorHealth`, `LatestSensorData`).
- `service/` - Android Foreground Service (`RideRecordingService`) managing background location tracking, periodic TTS coaching evaluations, and persistent status bar notification.

## Map Technology & Tile Provider
- **SDK:** MapLibre Native Android SDK (`org.maplibre.gl:android-sdk:11.5.1`).
- **Style / Tile Source:** MapLibre Demotiles open style (`https://demotiles.maplibre.org/style.json`). Open-source free vector style hosted by MapLibre Organization requiring no private API keys or paid accounts.

## Build Configuration
- AGP: 8.8.0
- Kotlin: 2.0.21
- KSP: 2.0.21-1.0.28
- Room: 2.6.1
- MapLibre: 11.5.1
- Play Services Location: 21.3.0
