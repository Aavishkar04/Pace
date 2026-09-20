# Technical Architecture: Pace1

## Architecture Pattern
Pace1 follows modern Android development principles with standard **MVVM (Model-View-ViewModel)** and the **Repository Pattern**.

```
[ UI Layer (Compose) ]
        │
        ▼
[ ViewModels (StateFlow) ]
        │
        ▼
[ Repository Layer ]
    ┌───┴───────────────┐
    ▼                   ▼
[ Location Service ]  [ Local Data Source (Room) ]
```

## Module Structure
- `:app` - Single-module Android application.

## Packages (`com.aavishkar.pace1`)
- `ui/`
  - `theme/` - Color scheme, typography, and Material 3 theme.
  - `components/` - Reusable Compose components.
  - `screens/` - Screen-level Composables (e.g., RecordingScreen, SummaryScreen).
- `data/`
  - `model/` - Domain & entity models (LocationPoint, Ride, RideSummary).
  - `repository/` - Data repositories exposing Kotlin Flows.
  - `local/` - Room DB, DAOs, and entities (planned V1.3).
- `location/` - Location Manager / Fused Location Client wrapper, location state flow, GPS filters.
- `service/` - Foreground Service for background recording (planned V1.2).

## Current Dependencies
- Jetpack Compose BOM (`2026.02.01`)
- Material 3
- AndroidX Core KTX, Lifecycle Runtime KTX, Activity Compose
