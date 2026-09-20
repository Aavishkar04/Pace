# Pace1

**Pace1** is a personal cycling application inspired by Strava, built natively for Android.

## Project Overview

- **Project Name:** Pace1
- **Package Name:** `com.aavishkar.pace1`
- **GitHub Repository:** [https://github.com/Aavishkar04/Pace.git](https://github.com/Aavishkar04/Pace.git)
- **Branch:** `main`

## Primary Target Device
- Physical Samsung Galaxy S23

## Tech Stack
- **Language:** Kotlin
- **UI:** Jetpack Compose (Material 3)
- **Architecture:** MVVM + Repository Pattern
- **Location:** Fused Location Provider API / Android Location APIs
- **Target SDK:** 37
- **Min SDK:** 24

## Project Structure
- `app/` - Main Android application module
- `docs/` - Comprehensive project documentation, current state, decisions, architecture, and handoff notes

## Development Roadmap
- **V1.0:** GPS Ride Recording (Distance, Elapsed Time, Speed, GPS Accuracy, Basic Summary)
- **V1.1:** GPS Accuracy & Filtering Improvements
- **V1.2:** Background/Lock-screen Recording (Foreground Service)
- **V1.3:** Local Ride History & Persistent Storage (Room)
- **V1.4:** Map/Route Visualization (MapLibre)
- **V2.0:** Audio Speed Coach & Earbud Feedback
- **V3.0:** Previous Ride & Segment Comparison
- **V4.0:** Flyover-style Animated Ride Replay

## Build Instructions
1. Open the project in Android Studio.
2. Build the project using Gradle:
   ```bash
   ./gradlew :app:assembleDebug
   ```
3. Deploy to a physical device (e.g., Samsung Galaxy S23) for GPS testing.
