# DocAlert engineering guide

## Project identity

- Product/version: DocAlert 1.0.0 (version code 1)
- Application ID and namespace: `de.himanshu19.docalert`
- Android: minSdk 26, compile/target SDK 37
- Stack: Kotlin, Compose Material 3, MVVM, repository pattern, Room, Flow, DataStore, WorkManager
- Repository: `https://github.com/de-himanshu19/android-document-deadline-reminder.git`, default branch `main`

## Architecture rules

- Use the single `app` module and manual dependency injection through `AppContainer`.
- Composables render immutable UI state and emit events; they never access DAOs or perform date, filter, persistence, or scheduling logic.
- ViewModels own screen state and call repository/settings/reminder abstractions.
- Room is the only record store. Date-only values persist as ISO local-date strings through explicit converters.
- Use `Clock`/`LocalDate` injection for deterministic time calculations.
- User data stays on-device. Never add `INTERNET`, analytics, advertising, cloud, or user-content logging.
- Reminder jobs use stable unique names and are cancelled before record rescheduling/deletion.

## Package structure

- `data/local`: Room entity, DAO, converters, database
- `data/repository`: repository interface and Room implementation
- `domain/model`: domain types, status, validation and filtering
- `notifications`: WorkManager scheduling and notification delivery
- `navigation`: routes and navigation host
- `ui/home`, `ui/editor`, `ui/details`, `ui/settings`: screens and ViewModels
- `ui/components`, `ui/theme`: reusable UI and visual identity
- `util`: narrowly scoped utilities

## Build and test commands

On Windows use `gradlew.bat`; on macOS/Linux use `./gradlew`.

- `./gradlew lintDebug`
- `./gradlew testDebugUnitTest`
- `./gradlew assembleDebug`
- `./gradlew assembleDebugAndroidTest`
- `./gradlew connectedDebugAndroidTest` (requires a connected emulator/device)

Use Java 17 or newer to run Gradle; source/bytecode compatibility remains Java 17.

## Git safety

- Work on `main` unless explicitly directed otherwise.
- Preserve user changes and never use destructive resets, force pushes, or broad deletes.
- Do not change global Git identity or commit secrets, `local.properties`, signing files, generated APKs, or build output.
- Local commits are allowed for completed milestones. Never push, publish, or change GitHub settings without explicit owner approval.

## Definition of done

- Required offline CRUD, search/filter/status, settings, reminders, notification permission, privacy behavior, and navigation are functional.
- Validation and deterministic business logic are tested.
- Room schema is exported and instrumentation sources compile.
- `lintDebug`, `testDebugUnitTest`, `assembleDebug`, and `assembleDebugAndroidTest` pass locally.
- Documentation matches the implementation; the final manifest has no `INTERNET` permission and no secrets are tracked.
- Emulator/physical-device checks are reported separately and never claimed unless executed.

## Version 1 scope and exclusions

Track document/deadline metadata, date status, search/filtering, themes, default reminders, and approximate WorkManager notifications. Do not add document numbers, photos/PDFs, OCR, accounts, network/cloud services, Firebase, analytics, ads, AI, localization, locks, sharing, import/export, recurring subscriptions, exact alarms, release signing, or Play publication.

