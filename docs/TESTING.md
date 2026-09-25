# Testing & Real-World Validation Log: Pace1

## Primary Testing Device
- **Device Name:** Samsung Galaxy S23 (Model: RZCXB208S6L)
- **OS Version:** Android 14+
- **Type:** Physical Device

## Verification Levels
1. **Compilation Check:** Gradle build (`:app:assembleDebug`) compiles without errors.
2. **Unit / Integration Testing:** JUnit test suite execution (`:app:testDebugUnitTest`).
3. **App Launch & Permission Check:** Fine location and Notification permission dialogs behave correctly on physical device.
4. **Stationary Table Test:** Filtered speed remains at `0.0 km/h` and distance does not false-accumulate when phone is stationary on table.
5. **Foreground Service & Lock Screen Test:** Ride tracking remains active while app is backgrounded or screen locked.
6. **Real-World Outdoor Cycling Validation:** Outdoor cycling ride data compared against reference trackers (Strava & Samsung Health).
7. **Vehicle / Car Pipeline Validation:** High-speed vehicle tracking dataset verification.

---

## Real-World Outdoor Validation Datasets

### 1. September 24, 2026 — Outdoor Cycling Ride Test

| Platform | Distance | Duration / Moving Time | Avg Speed | Max Speed | Points / Data Quality |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Pace1** | **20.48 km** | **56:16** | **21.8 km/h** | **38.0 km/h** | **3,456 raw / 3,456 accepted** |
| **Strava** | 20.47 km | 54:58 (moving) | 22.3 km/h | N/A | Reference tracker |
| **Samsung Health** | 20.35 km | 54:04 (workout) / 56:46 (total) | 22.5 km/h | 33.8 km/h | Reference tracker |

#### Analysis & Key Observations:
- **Distance Accuracy:** Pace1 registered `20.48 km`, remarkably close to Strava (`20.47 km`, +10m delta) and Samsung Health (`20.35 km`).
- **Average Speed:** Pace1 calculated `21.8 km/h`, aligning closely with Strava (`22.3 km/h`) and Samsung Health (`22.5 km/h`).
- **Maximum Speed:** Pace1 recorded `38.0 km/h` vs Samsung Health `33.8 km/h`. Max speed spikes require continued refinement during V1.1 filtering.
- **Hardware Agreement:** All three applications ran on the same Samsung Galaxy S23 hardware. Close agreement demonstrates pipeline stability, though true absolute accuracy requires continued field analysis.

---

### 2. September 21, 2026 — Vehicle / Car Pipeline Tests

#### Ride A (Car Test)
- **Pace1:** `16.31 km` | `33:20` elapsed | `29.3 km/h` avg | `78.7 km/h` max
- **Strava:** `16.32 km` | `32:07` moving | `30.5 km/h` avg

#### Ride B (Car Test)
- **Pace1:** `16.62 km` | `25:32` elapsed | `39.1 km/h` avg | `85.0 km/h` max
- **Strava:** `16.79 km` | `26:26` moving | `38.1 km/h` avg

#### Analysis & Key Observations:
- Demonstrates that Pace1's raw GPS tracking pipeline handles higher velocity vehicle movement smoothly without losing updates or truncating routes.
- Vehicle tracking confirms high-speed robustness but does not substitute for dedicated low-speed cycling movement validation.

---

## Unit Test Suite Results

| Test Class | Tests Passed | Status |
| :--- | :--- | :--- |
| `HaversineDistanceCalculatorTest` | 2 / 2 | PASS |
| `RideRepositoryTest` | 4 / 4 | PASS |
| `RideViewModelTest` | 2 / 2 | PASS |
| `PaceCoachTest` | 3 / 3 | PASS |
| **TOTAL** | **11 / 11** | **PASS** |
