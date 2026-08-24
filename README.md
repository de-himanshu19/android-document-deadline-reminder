# DocAlert

DocAlert is a privacy-first native Android app for tracking document expiry dates and important deadlines. Version **1.0.0** stores user-entered metadata locally and can request approximate reminders without accounts, cloud services, or internet access.

Repository: [github.com/de-himanshu19/android-document-deadline-reminder](https://github.com/de-himanshu19/android-document-deadline-reminder)

## Features

- Add, view, edit, and delete documents or deadlines.
- Track passport, residence permit, driving licence, insurance, vehicle/TÜV, warranty, medicine, school, job/application, contract, and custom categories.
- Deterministic Active, Expiring soon, Urgent, Due today, and Expired status labels.
- Case-insensitive title/owner/category search combined with category and status filters.
- Select 90-, 30-, 7-, 1-day, and due-date reminders.
- System, light, and dark themes with persisted defaults.
- Private notification text enabled by default.
- Fully offline Room storage with no sample records, login, analytics, advertising, or `INTERNET` permission.

Typical uses include passport and residence-permit renewal, vehicle TÜV, warranty and insurance expiry, medicine dates, school submissions, applications, and contract renewal.

## Screenshots

Device-verified screenshots are planned; placeholders are documented in [`docs/screenshots`](docs/screenshots/README.md). Recommended names are `01-home-empty-light.png`, `02-add-item.png`, `03-home-records.png`, `04-item-details-dark.png`, and `05-settings.png`.

## Privacy

Records and settings remain in Android app-private local storage. Version 1 has no backend or network capability and disables Android backup/device-transfer extraction for its database and preferences. See [`docs/PRIVACY.md`](docs/PRIVACY.md) for the precise implemented behavior.

## Technology and architecture

- Kotlin and Java 17 source compatibility
- Jetpack Compose with Material 3 and Navigation Compose
- Single activity, single `app` module
- MVVM with immutable screen state and lifecycle-aware Flow collection
- Repository pattern and manual `AppContainer` dependency injection
- Room schema v1 with KSP and tracked schemas
- Preferences DataStore
- WorkManager and Android notification APIs
- JUnit, coroutine tests, in-memory Room tests, and Compose UI tests

UI code never accesses DAOs directly. Domain validation, status, wording, filtering, sorting, scheduling, and notification-content rules remain outside composables and are independently testable.

## Project structure

```text
app/src/main/java/de/himanshu19/docalert/
├── data/local/          Room database, DAO, entity, converters
├── data/repository/     Repository interface and Room implementation
├── data/settings/       DataStore settings
├── domain/model/        Types, validation, date/status/query rules
├── navigation/          Compose navigation and permission routes
├── notifications/       WorkManager scheduling and notifications
└── ui/                  Home, editor, details, settings, components, theme
app/src/test/            JVM unit and ViewModel tests
app/src/androidTest/     Room and Compose instrumentation tests
app/schemas/             Exported Room schema
docs/                    Privacy, manual test plan, screenshot guidance
.github/                 CI and Dependabot
```

## Prerequisites and setup

- Latest stable Android Studio compatible with AGP 9.3
- Android SDK Platform 37.0 and Build Tools 36.0.0
- JDK 17 or newer for Gradle (CI uses Temurin 17)

Clone the repository or open this project root directly in Android Studio with **File → Open**. Let Android Studio use the Gradle wrapper, confirm its Gradle JDK is 17 or newer, install requested SDK components, and wait for sync/indexing to complete. Do not create a nested project.

## Build and tests

On macOS/Linux:

```bash
./gradlew lintDebug
./gradlew testDebugUnitTest
./gradlew assembleDebug
./gradlew assembleDebugAndroidTest
```

On Windows PowerShell, replace `./gradlew` with `.\gradlew.bat`. Run device tests only with an emulator or phone connected:

```bash
./gradlew connectedDebugAndroidTest
```

The debug APK is produced at `app/build/outputs/apk/debug/app-debug.apk`. Install it from Android Studio or with `adb install -r app/build/outputs/apk/debug/app-debug.apk`.

The full manual checklist is in [`docs/TEST_PLAN.md`](docs/TEST_PLAN.md).

## Notification behavior

DocAlert explains notification use and requests Android 13+ permission only when a user saves selected reminders or asks for a test notification. Each reminder is unique to the record and interval, scheduled with WorkManager for approximately 09:00 in the current device timezone. WorkManager and Android may defer execution, so delivery is not exact. Past reminder dates are skipped; a due-today reminder created after 09:00 is queued without a negative delay. Editing reschedules and deleting cancels all work tagged to that record.

Tapping an item notification opens its details. Missing/deleted records show a safe unavailable state. With private content enabled, notification text does not reveal the item title.

## Known Version 1 limitations

- Reminder timing is approximate and depends on Android background execution and notification permission.
- No recurring renewal history, import/export, sharing, attachments, scanning/OCR, cloud backup, account sync, biometric/PIN lock, or additional languages.
- Release signing and Play Store publication are not configured.
- Device/emulator execution remains necessary to verify real notification delivery, reboot behavior, TalkBack, and manufacturer-specific background limits.

## Roadmap

Possible future work—subject to privacy and product review—includes optional encrypted export/backup, renewal history, localization, and app locking. Version 1 intentionally excludes these features.

## Contributing and licence

Open an issue or propose a focused pull request with tests and updated documentation. Run lint, JVM tests, and instrumentation-source compilation before requesting review; include device results when behavior depends on Android services.

No legal licence has been selected by the repository owner. Until one is explicitly added, the source is not offered under an open-source licence.
