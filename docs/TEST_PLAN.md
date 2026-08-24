# DocAlert 1.0.0 manual test plan

Automated tests cover deterministic business logic and compile the instrumentation suite. Complete these checks on at least one API 26 device/emulator and one Android 13+ device/emulator before a public release.

## Test record

| Tester | Device/model | Android/API | App build/commit | Date | Overall result | Notes |
|---|---|---|---|---|---|---|
| | | | 1.0.0 / | | Pass / Fail | |

For each scenario record Pass, Fail, or Blocked and include useful notes.

| # | Result | Notes |
|---|---|---|
| 1 | | |
| 2 | | |
| 3 | | |
| 4 | | |
| 5 | | |
| 6 | | |
| 7 | | |
| 8 | | |
| 9 | | |
| 10 | | |
| 11 | | |
| 12 | | |
| 13 | | |
| 14 | | |
| 15 | | |
| 16 | | |
| 17 | | |
| 18 | | |
| 19 | | |
| 20 | | |

## Scenarios

1. Fresh install: uninstall any previous build, install the debug APK, launch it, and verify the polished empty dashboard explains DocAlert and offers an Add action. Confirm no notification prompt appears at launch.
2. Add a residence permit: choose Document, title it, select Residence Permit, optionally enter an owner/issue date/notes, select a future expiry date, and save. Confirm it opens in details.
3. Multiple reminders: create or edit an item and select 90, 30, 7, 1, and due-date reminders. Confirm every selection appears in details.
4. Persistence: force-stop/close DocAlert, reopen it, and confirm all saved data remains unchanged.
5. Edit: change the title and expiry date. Save and confirm the list, details, status, and remaining-day label update.
6. Search: search using a different letter case and a title fragment. Also try an owner and category name. Confirm relevant cards remain.
7. Combined filters: apply a category and one or more status filters while search text remains. Confirm only records satisfying all active criteria appear.
8. Status coverage: add items more than 90 days away, 31–90 days away, 1–30 days away, due today, and in the past. Confirm Active, Expiring soon, Urgent, Due today, and Expired text and styling.
9. Date boundaries: verify exactly 30/31 and 90/91 days, yesterday, today, tomorrow, and a leap-day date. Confirm singular/plural text is correct.
10. Permission acceptance (Android 13+): with permission reset/denied, save an item with reminders or send a test notification. Read the in-app explanation, continue, allow the system prompt, and confirm enabled status.
11. Permission denial (Android 13+): reset permission, trigger the contextual prompt, deny it, and confirm the record still saves and Settings explains notification status without crashing.
12. Test notification: enable permission, tap Send test notification, verify the success message and notification. With private content enabled, confirm no saved title is exposed.
13. Notification navigation: use a short test schedule or debug tooling, tap a delivered item reminder, and confirm the correct detail screen opens. Delete the record before tapping an old notification and confirm the missing state is graceful.
14. Reminder reschedule: schedule reminders, edit the expiry date/intervals, and inspect WorkManager with Android Studio App Inspection or `adb shell dumpsys jobscheduler`. Confirm old item-tagged work is cancelled and only current unique jobs remain.
15. Delete/cancel: delete from details, confirm the dialog first, then verify the item disappears and no item-tagged pending reminder work remains.
16. Themes: switch among System default, Light, and Dark. Confirm the change is immediate, survives restart, and follows the system for System default.
17. Font/layout: set the device font and display size high, open every screen, use the editor with the keyboard visible, and confirm content scrolls with no clipped actions or text.
18. Rotation/recreation: rotate while editing and confirm entered fields remain. Enable Developer options “Don't keep activities,” background/return, and confirm persisted screens recover without crashes.
19. Reboot: reboot the device, reopen DocAlert, confirm records/settings persist, and inspect that WorkManager retains or restores future work.
20. Offline: disable Wi-Fi and mobile data, repeat CRUD/search/settings actions, and confirm all functionality remains available.

## Additional quality checks

- Use TalkBack to navigate app bars, Add, cards, status labels, fields, date controls, reminder choices, and destructive confirmations.
- Check light/dark contrast and verify status is always communicated with text/icon, not colour alone.
- Confirm a failed validation retains every entered value and focuses attention through field-level English messages.
- Confirm issue date after expiry is rejected and past expiry dates are accepted.
- In Android Studio's merged manifest, confirm `android.permission.INTERNET` is absent and exported components are limited to the launcher activity.

