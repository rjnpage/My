# Smart QR & Data Scanner

Android application package: `com.smartqr.scanner`

## Features delivered
- Real-time camera QR/barcode scanning with torch toggle.
- Auto data type detection: URL, phone, email, UPI, plain text.
- Smart action routing (browser, dialer, mail, UPI app, share text).
- Local scan history with Room (date/time, search, delete).
- Export latest scan as PDF/TXT and share to any app.
- QR generator from any input (text/url/phone/upi) with PNG download.
- Dark/light mode + animated screen transitions.
- Offline-first workflow (no network required).
- Auto copy scan result to clipboard.
- English + Hindi localization resources.
- App icon + Android splash screen setup.

## Tech stack
- Kotlin + Jetpack Compose UI
- ZXing (`zxing-android-embedded`) for scanner engine
- Room database for local persistence

## Build prerequisites
- **JDK 17** (not Java 21/25)
- Android SDK with platform/build-tools for `compileSdk 35`
- Network access to Gradle repositories (`google()`, `mavenCentral()`)

## Generate APK quickly
Use the helper script:

```bash
./scripts/build_apk.sh debug
```

Output:
`app/build/outputs/apk/debug/app-debug.apk`

## Signed APK (Release)
Set environment variables before build:

```bash
export SMARTQR_KEYSTORE_PATH=/absolute/path/your-release.jks
export SMARTQR_KEYSTORE_PASSWORD=******
export SMARTQR_KEY_ALIAS=******
export SMARTQR_KEY_PASSWORD=******
./scripts/build_apk.sh release
```

Output:
`app/build/outputs/apk/release/app-release.apk`

## If `gradlew` is missing
Create wrapper once in an online environment:

```bash
JAVA_HOME=<path-to-jdk17> gradle wrapper --gradle-version 8.14.3
```

Then run `./scripts/build_apk.sh debug`.

## Play Store readiness checklist
- Replace placeholder launcher icon with final branded artwork.
- Add Play Store feature graphic and screenshots.
- Complete Data Safety and Privacy Policy declarations.
- Verify all required permissions and target SDK policy.
