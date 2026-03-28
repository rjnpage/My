# Smart QR & Data Scanner

Android app project in Kotlin + Gradle.

## Features

- QR / barcode scanner (CameraX + ML Kit)
- Detection type:
  - URL
  - Phone
  - Text
- Flashlight toggle
- History using Room database

## Tech

- Kotlin
- Android Gradle Plugin
- Java 17
- compileSdk 34

## Build locally

```bash
./gradlew :app:assembleDebug
```

> APK generation is intended for CI/CD workflows (GitHub Actions) and local Gradle runs only.

## GitHub Actions

Workflow file: `.github/workflows/android-build.yml`

- Uses GitHub Actions `v4` actions only.
- Builds debug APK via Gradle.
- Uploads generated APK as workflow artifact.
