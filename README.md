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

## Build
```bash
./gradlew assembleDebug
```

## Signed APK (Release)
Set environment variables before build:

```bash
export SMARTQR_KEYSTORE_PATH=/absolute/path/your-release.jks
export SMARTQR_KEYSTORE_PASSWORD=******
export SMARTQR_KEY_ALIAS=******
export SMARTQR_KEY_PASSWORD=******
./gradlew assembleRelease
```

Then locate signed APK at:
`app/build/outputs/apk/release/app-release.apk`

## Play Store readiness checklist
- Replace placeholder launcher icon with final branded artwork.
- Add Play Store feature graphic and screenshots.
- Complete Data Safety and Privacy Policy declarations.
- Verify all required permissions and target SDK policy.
