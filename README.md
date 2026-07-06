# Galaxy FM Radio & V4L2 Hardware SDK

Production-quality Android application and standalone SDK specifically designed and optimized for **Samsung Galaxy Tab A9+ 5G (SM-X216B, Qualcomm Snapdragon 695)** running Android 16.

## Architecture & Modules

1. **`:galaxyfmsdk` (Galaxy FM SDK)**:
   - Modular Android Library generating `GalaxyFM-SDK-release.aar`.
   - Implemented in Kotlin and C++ (JNI).
   - Communicates directly with physical Linux V4L2 kernel device nodes (`/dev/radio0`, `/dev/v4l2-radio0`, `/dev/wcn3990_fm`) using standard IOCTLs (`VIDIOC_S_TUNER`, `VIDIOC_S_FREQUENCY`, `VIDIOC_S_HW_FREQ_SEEK`).
   - Pure production code: zero simulation, zero mock data, zero internet streaming, zero external dongles required.

2. **`:app` (Android FM Radio Application)**:
   - Uses **ONLY** the `galaxyfmsdk` library for all FM operations.
   - Material 3 reactive interface optimized for Galaxy Tab A9+ 11.0" display (canonical tablet side-by-side layout + handheld mobile layout).
   - Features digital tuning dial, live RSSI signal meter (-120 dBm to -30 dBm), Stereo/Mono status indicator, RDS Program Service (PS) Name & Radio Text (RT) decoder, and Room persistence for favorite presets.

## Build Instructions

### Prerequisites
- JDK 21
- Android SDK (API 36)
- Android NDK & CMake (3.22.1+)

### Build via Gradle Command Line
To compile the Release AAR and APK with default debug signing:
```bash
./gradlew :galaxyfmsdk:assembleRelease :app:assembleRelease
```

Output artifacts:
- SDK Release AAR: `galaxyfmsdk/build/outputs/aar/galaxyfmsdk-release.aar`
- Radio App Release APK: `app/build/outputs/apk/release/app-release.apk`

## Automated CI/CD (GitHub Actions)
This repository includes a complete automated workflow (`.github/workflows/android_build.yml`) that builds both the SDK and App upon push or pull request, uploading `GalaxyFM-SDK-release.aar` and `GalaxyFM-Radio-release.apk` as downloadable GitHub artifacts.
