# AutoLink — Android Automotive connected-vehicle POC

A learning/portfolio project: a dashboard of **simulated** vehicle telemetry for Android Automotive OS (AAOS),
built to demonstrate signal freshness, event ordering and clean architecture.

> **Portfolio integrity:** every value on screen is **SIMULATED**. This is not a certified automotive or
> safety-critical system and does not read from or control a real vehicle.

| Live | Paused → stale | Dropped readings | Resumed |
|---|---|---|---|
| ![](docs/screenshots/01-live.png) | ![](docs/screenshots/02-paused-stale.png) | ![](docs/screenshots/03-dropped-counters.png) | ![](docs/screenshots/04-resumed-live.png) |

## What works (milestones 0–2)

- Simulated vehicle emitting speed, battery and cabin temperature every 500 ms.
- Freshness: **LIVE** → **STALE** when no sample arrives for 2 s (monotonic clock), **UNAVAILABLE** before the first.
- Unknown values render as `--`, never `0`.
- Ordering: duplicate and out-of-order samples are dropped by sequence number and counted on screen.
- Developer panel: pause/resume signals, send a duplicate, send an out-of-order sample.

## Architecture

Clean Architecture + MVVM/UDF, multi-module, Koin DI, Gradle convention plugins in `build-logic`.

```
androidApp ──► shared (Koin root, App())
                 └─► feature:vehicle   domain/ data/ presentation/ packages
                       └─► core:domain  core:common  core:ui  core:designsystem
```

- `VehicleRepository` returns domain objects only; no UI code sees VHAL property IDs.
- `ReconcilingVehicleRepository` wraps any `VehicleSignalSource` (simulator today, Car API adapter later).
- Modules are Kotlin Multiplatform with Android + JVM targets; JVM exists only for fast tests. No iOS (AAOS-only).

## Build and run

Tested with: macOS (Apple Silicon), Android Studio JBR 25, Gradle 9.9.1, AGP 9.5.1, Kotlin 2.4.21,
compileSdk 37 / targetSdk 36 / minSdk 29, emulator image
`system-images;android-35-ext15;android-automotive;arm64-v8a` (API 35, AAOS).

```bash
./gradlew desktopTest                # 13 unit tests (virtual time, no emulator needed)
./gradlew :androidApp:assembleDebug
emulator -avd AutoLink_AAOS_API35 -gpu host
adb install -r androidApp/build/outputs/apk/debug/androidApp-debug.apk
```

The AAOS emulator has a second (instrument cluster) display; pass `-d <display-id>` to `screencap`.

## Limitations / next

Not yet built: Room telemetry queue (M3), Ktor backend + WorkManager sync (M4), Car API adapter (M5), CI (M6).
Freshness is snapshot-level, not per-signal. The app is not distraction-optimised (parked-use developer tool).
