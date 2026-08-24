# DocAlert privacy information

DocAlert 1.0.0 is an offline application for saving document-expiry and deadline metadata.

## Information stored

For each item, DocAlert stores the selected item type and category, title, optional owner/person, optional issue date, required expiry or due date, optional notes, selected reminder intervals, and creation/update timestamps. Theme, default reminder, and notification-privacy preferences are also stored.

This information is stored in the app's private storage on the Android device. DocAlert does not collect document numbers, document images, PDFs, addresses, account credentials, or identity-document contents.

## Accounts and transmission

DocAlert requires no registration or account. Version 1 contains no internet permission, network client, analytics, advertising, cloud storage, or crash-reporting service. It does not sell, share, or transmit saved information.

Android's WorkManager and notification services operate locally to schedule approximate reminder delivery. Android controls final background execution timing.

## Deletion and backup

Deleting an item removes it from DocAlert and cancels its pending reminder work. Clearing DocAlert's app storage or uninstalling the app removes its database and preferences. Android backup and device-transfer extraction are disabled for the Version 1 database and settings.

## Safe use

Use a descriptive label rather than a document number, and avoid entering unnecessary sensitive information in notes. Device security—including screen lock, operating-system updates, and control of notification visibility—remains the user's responsibility. Private notification content is enabled by default and can be changed in Settings.

