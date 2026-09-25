# Technical & Product Decisions: Pace1

This document logs all major architectural, technical, and product decisions made for Pace1.

---

### 1. Identity & Repository Boundaries
- **Decision:** `Pace1` is the permanent name of the application. The package name is permanently `com.aavishkar.pace1`, and the GitHub repository is `https://github.com/Aavishkar04/Pace.git`.
- **Rationale:** Prevents package drift and maintains clear version history across Git commits and documentation.

---

### 2. Architecture & Data Flow
- **Decision:** MVVM + Singleton `RideRepository` + Android Foreground Service (`RideRecordingService`) + Room DB (`PaceDatabase`).
- **Rationale:** Ensures ride tracking, distance accumulation, and TTS voice coaching run reliably in the background across screen locks, app backgrounding, and Activity recreation without process death.

---

### 3. Mapping Technology & Tile Provider
- **Decision:** MapLibre Native Android SDK (`org.maplibre.gl:android-sdk:11.5.1`) using MapLibre Demotiles open vector style (`https://demotiles.maplibre.org/style.json`).
- **Rationale:** Open-source, high-performance native vector map rendering that requires no private API keys, paid tokens, or proprietary vendor lock-in.

---

### 4. Voice Coaching & Audio System
- **Decision:** Use Android `TextToSpeech` wrapped in `PaceCoachManager` with `USAGE_ASSISTANCE_NAVIGATION_GUIDANCE` AudioFocus request.
- **Rationale:** Guarantees voice coaching ("Increase effort", "Hold pace", "Ease off") ducking background audio (e.g. music/podcasts) and speaking clearly through Bluetooth headphones.

---

### 5. Voice Assistant & Wake Word Approach
- **Decision:** Implement "TELL ME MY STATS" voice stats button & action fallback instead of an always-listening `SpeechRecognizer`.
- **Rationale:** Official Android documentation explicitly advises against using `SpeechRecognizer` as a continuous background listener due to battery, microphone lock, and privacy constraints. The button/audio action fallback provides instant, 100% reliable stats playback.

---

### 6. Raw Data Preservation & Motion Filtering
- **Decision:** Store every raw GPS location sample in Room DB with an `isAccepted` boolean flag.
- **Rationale:** Ensures raw geographic data is never lost or corrupted by filtering heuristics, enabling future post-ride analysis and algorithm tuning.

---

### 7. No Heavy Cloud Dependencies
- **Decision:** Local-first architecture using Room/SQLite. No Firebase, AWS, or external backends required for core tracking.
- **Rationale:** Maximizes offline reliability, rider privacy, and battery efficiency during long outdoor rides.
