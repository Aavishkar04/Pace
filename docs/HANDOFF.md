# Handoff & Context Checkpoint: Pace1

## Context Checkpoint Counter
- **Substantial Change Checkpoint Counter:** 2 / 5
- **Current Milestone:** V1.4 - Live Route Map, Headphone Voice Coach, Voice Stats, and Navigation Structure
- **Current Task:** Added MapLibre vector route map (`LiveMapView`, `PostRideMapView`), headphone Text-To-Speech Pace Coach (`PaceCoachManager`), voice stats announcement ("TELL ME MY STATS"), Settings screen, Ride Detail view, and Compose navigation (`Pace1MainApp`).

## Repository Details
- **Project Name:** Pace1 (PERMANENT)
- **Package Name:** `com.aavishkar.pace1`
- **GitHub Repository:** `https://github.com/Aavishkar04/Pace.git`
- **Branch:** `main`
- **Latest Commit Hash:** Pending Commit

## Completed Work & Capabilities
1. **Live Route Map (`LiveMapView`):** Integrated MapLibre Native SDK (`org.maplibre.gl:android-sdk:11.5.1`) rendering live accepted route polylines and current position markers on Demotiles open vector style (`https://demotiles.maplibre.org/style.json`).
2. **Post-Ride Route Map (`PostRideMapView`):** Renders full recorded ride route, start marker, finish marker, and camera bounds on `RideDetailScreen`.
3. **Headphone Pace Coach (`PaceCoachManager`):** Text-To-Speech audio feedback engine evaluating filtered cycling speed against target range (e.g. 22–26 km/h) at configurable intervals (15s, 30s, 60s) or state transitions ("Increase effort", "Hold pace", "Ease off").
4. **Voice Stats Announcement:** Functionality allowing the rider to tap "TELL ME MY STATS" during active recording to hear spoken distance, elapsed time, current speed, average speed, and maximum speed.
5. **Coach Settings (`CoachSettingsRepository`):** Local SharedPreferences persistence for Coach ON/OFF, target speed, lower/upper boundaries, announcement interval, and a "TEST VOICE / TTS" button.
6. **Compose Navigation & Screens:** Full navigation host (`Pace1MainApp`) with bottom navigation bar for `Home`, `Ride`, `History`, and `Settings`, plus `RideDetail` routing.
7. **Background & Lock Screen Recording:** `RideRecordingService` maintains location updates, Room DB persistence, and periodic TTS coaching evaluations across screen locks and app backgrounding.
8. **Test Suite:** 11 / 11 unit tests passing (`:app:testDebugUnitTest`).

## Real-World Outdoor Validation Summary (Sept 24, 2026)
- **Pace1:** 20.48 km | 56:16 duration | 21.8 km/h avg | 38.0 km/h max | 3,456 raw/accepted points
- **Strava Reference:** 20.47 km | 54:58 moving | 22.3 km/h avg
- **Samsung Health Reference:** 20.35 km | 54:04 workout / 56:46 total | 22.5 km/h avg | 33.8 km/h max

## Known Limitations & Deferred Items
- **Wake Word ("Hey Pace"):** Button & Audio Action fallback used. On-device openWakeWord model integration deferred to keep implementation lightweight and zero-latency without heavy ONNX dependencies.
- **Offline Map Datasets:** MapLibre offline region download architecture defined; packaging huge offline map tiles deferred to avoid unnecessary storage/bandwidth consumption.

## Files Modified / Created
- `app/src/main/java/com/aavishkar/pace1/coach/CoachSettings.kt`
- `app/src/main/java/com/aavishkar/pace1/coach/PaceCoachManager.kt`
- `app/src/main/java/com/aavishkar/pace1/ui/map/MapViewComponents.kt`
- `app/src/main/java/com/aavishkar/pace1/ui/screens/HomeScreen.kt`
- `app/src/main/java/com/aavishkar/pace1/ui/screens/RideDetailScreen.kt`
- `app/src/main/java/com/aavishkar/pace1/ui/screens/HistoryScreen.kt`
- `app/src/main/java/com/aavishkar/pace1/ui/screens/SettingsScreen.kt`
- `app/src/main/java/com/aavishkar/pace1/ui/navigation/Pace1Navigation.kt`
- `app/src/main/java/com/aavishkar/pace1/ui/RideRecordingScreen.kt`
- `app/src/main/java/com/aavishkar/pace1/MainActivity.kt`
- `app/src/test/java/com/aavishkar/pace1/PaceCoachTest.kt`
- `app/src/test/java/com/aavishkar/pace1/RideViewModelTest.kt`
- `docs/CURRENT_STATE.md`
- `docs/ARCHITECTURE.md`
- `docs/TESTING.md`
- `docs/HANDOFF.md`
- `docs/DECISIONS.md`

## Next Recommended Development Step
1. Field-test the live route map rendering and headphone TTS voice coach on an outdoor cycling ride with the Samsung S23.
2. Conduct post-ride analysis of route polyline rendering smoothness and TTS audio focus behavior with Bluetooth headphones connected.
