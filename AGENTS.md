# Instructions for AI Coding Agents Working on Pace1

This document contains universal instructions for any AI agent (Gemini in Android Studio, Cursor, Kiro, Claude, ChatGPT, etc.) working on the Pace1 repository.

---

## 1. Permanent Project Context & Rules

- **Project Name:** Pace1 (PERMANENT. DO NOT rename, replace, migrate, or suggest changing).
- **Project Path:** `C:\Users\unkno\AndroidStudioProjects\Pace1`
- **Package Name:** `com.aavishkar.pace1`
- **GitHub Repository:** `https://github.com/Aavishkar04/Pace.git`
- **Branch:** `main`

---

## 2. Core Philosophy & Rules of Engagement

1. **Read Before Editing:** Always inspect existing code and documentation (`docs/HANDOFF.md`, `docs/CURRENT_STATE.md`, `docs/ARCHITECTURE.md`, `docs/DECISIONS.md`) before changing code.
2. **Conservative Changes:** Make the smallest reasonable change. Do not rewrite working code unnecessarily.
3. **No Hidden Knowledge:** Do NOT keep critical project knowledge solely in chat. Update the `docs/` repository files.
4. **Substantial Change Counter:** Track substantial coding prompts in `docs/HANDOFF.md`. Every 5 substantial changes, perform a FULL HANDOFF CHECKPOINT update across all `docs/` files.
5. **No Cloud / Heavy Dependencies:** Do NOT add Firebase, Supabase, AWS, custom backends, or Google Maps without explicit user approval.
6. **Package/Project Integrity:** Never rename the package `com.aavishkar.pace1`, project `Pace1`, or project directory.

---

## 3. Technology Stack Guidelines

- **Language:** Kotlin (idiomatic, simple, readable).
- **UI Framework:** Jetpack Compose (Material 3).
- **Architecture:** MVVM + Repository pattern.
- **Location API:** Fused Location Provider / Android Location APIs.
- **Background Tracking:** Foreground Service with notification (V1.2).
- **Persistence:** Room / SQLite (V1.3).
- **Mapping:** MapLibre (V1.4).
- **Audio Coaching:** Android Text-to-Speech (V2.0).

---

## 4. Real Device Testing & Verification

- **Primary Device:** Samsung Galaxy S23 (physical device).
- **Verification Levels:**
  1. Code compiles (`:app:assembleDebug`).
  2. Unit tests pass.
  3. App launches on S23.
  4. Permission flow works.
  5. GPS works on physical device outdoors.
- **Rule:** Never claim a feature is "working" or "perfect" based only on code compilation or emulator runs.

---

## 5. Documentation Maintenance Checklist

When completing major milestones or every 5 substantial coding prompts:
- [ ] `docs/HANDOFF.md` (Counter, milestone, current task, tests, commit hash)
- [ ] `docs/CURRENT_STATE.md` (Implemented vs. pending features)
- [ ] `docs/ARCHITECTURE.md` (Updated structural models/data flow)
- [ ] `docs/TESTING.md` (Test logs and S23 physical device verification)
- [ ] `docs/DECISIONS.md` (Any architectural or product decisions made)
