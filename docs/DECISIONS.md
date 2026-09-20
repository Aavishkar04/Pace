# Architectural & Product Decisions Log: Pace1

## Decision 001: Project Name & Repository Architecture
- **Date:** 2026-03-03
- **Status:** Approved
- **Context:** Initial project setup and repository identification.
- **Decision:** The project is permanently named `Pace1`, with package `com.aavishkar.pace1` and repository `https://github.com/Aavishkar04/Pace.git`. The project name and folder path shall not be altered or migrated.
- **Consequences:** Ensures consistency across AI agent sessions and development environments.

## Decision 002: Technology Stack Selection
- **Date:** 2026-03-03
- **Status:** Approved
- **Context:** Selecting baseline libraries and architecture for Pace1 cycling app.
- **Decision:**
  - Standard Kotlin + Jetpack Compose (Material 3).
  - MVVM Architecture + Repository pattern.
  - Native Fused Location Provider API for location tracking.
  - Room SQLite for local database storage (V1.3).
  - MapLibre for mapping (V1.4).
  - Android Text-to-Speech for audio coaching (V2.0).
  - Zero external cloud/backend dependencies (no Firebase, Supabase, Google Maps API key requirements).
- **Consequences:** Keeps the app lightweight, privacy-focused, offline-first, and easy to build/test without API keys or costs.

## Decision 003: Incremental Milestone Roadmap
- **Date:** 2026-03-03
- **Status:** Approved
- **Context:** Planning feature releases from basic tracking to advanced analytics.
- **Decision:** Features will strictly progress through V1.0 (GPS Recording) -> V1.1 (Filtering) -> V1.2 (Foreground Service) -> V1.3 (Room History) -> V1.4 (Map) -> V2.0 (Audio Coach) -> V3.0 (Comparisons) -> V4.0 (Flyover).
- **Consequences:** Avoids over-engineering early versions or jumping to complex features before baseline tracking is rock solid.

## Decision 004: Persistent Documentation & Handoff System
- **Date:** 2026-03-03
- **Status:** Approved
- **Context:** Ensuring continuity between different AI agents (Gemini, Cursor, Kiro, Claude, etc.) and developer sessions.
- **Decision:** Maintain `AGENTS.md`, `README.md`, `docs/PROJECT.md`, `docs/ARCHITECTURE.md`, `docs/CURRENT_STATE.md`, `docs/HANDOFF.md`, `docs/TESTING.md`, and `docs/DECISIONS.md`. Enforce a 5-substantial-change handoff checkpoint update.
- **Consequences:** Eliminates memory loss between sessions; repo documentation serves as single source of truth.
