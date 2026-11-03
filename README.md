# Enough

**Decide your limit once. Get your day back.**

Enough is a small, offline Android app for people who open Instagram for a minute and lose an hour. Choose distracting apps, commit a daily time budget, and see a blocking screen when the budget runs out. There is no extra-time button.

**Status: early Android alpha.** The source builds into an APK and includes automated budget-policy tests. Real-device enforcement testing is still required before depending on it. This is personal commitment mode, not an unbreakable device lock.

## What it does

- Select downloaded, launchable apps and assign 1–240 minutes per app per day.
- Count time already spent today, rather than giving a fresh budget at installation.
- Freeze today's committed limits. Raising a limit or removing an app is scheduled for tomorrow.
- Show a full-screen accessibility overlay over a selected app once its budget is exhausted.
- Preserve daily totals in private on-device storage and reconcile them with Android usage events after a restart.
- Reset at the next local midnight. The day's time zone is captured when that day starts; during the same boot, a monotonic deadline prevents a wall-clock change from immediately resetting the budget.
- Keep launchers, system apps, the default phone dialer, and Enough itself out of the selectable app list.

## What it cannot guarantee

Android lets a personal phone's owner disable accessibility, revoke usage access, force-stop or uninstall an ordinary app, or clear its data. Enough deliberately does not interfere with Settings or hide its uninstall controls. A time change followed by reboot can also defeat the local day fence. Rooted devices are outside the threat model.

Enforcement is an overlay, **not OS-level package suspension**. There may be a brief app appearance before the overlay is shown. Split-screen, picture-in-picture, cloned apps, other user profiles, browser versions of services, and manufacturer background restrictions need separate consideration. Preinstalled system apps are excluded in this alpha. Background media playback is not billed as foreground use. Time accounting is approximate, particularly around system dialogs and multi-window use.

Android's [package suspension API](https://developer.android.com/reference/android/app/admin/DevicePolicyManager#setPackagesSuspended(android.content.ComponentName,%20java.lang.String%5B%5D,%20boolean)) requires device/profile-owner or delegated management privileges. That is a different setup from installing an ordinary personal app and is **not included** here.

## Install and set up

1. Install the debug APK from this repository's alpha release, or build it using the steps below. Debug builds are for testing, not a verified store release.
2. Open **Enough** and enable **Usage access** for Enough in Android Settings.
3. Read the separate accessibility disclosure and choose **I agree — open settings**. Enable **Enough app limits** in Accessibility settings.
4. Return to Enough. Wait until it says **Protection active**.
5. Choose apps, enter daily minutes, and confirm **Commit limits**. Already-used time counts. If today's usage is already over the selected limit, that app is blocked immediately.
6. To change your goals, choose **Plan tomorrow's limits**. It cannot increase today's remaining time.

Some Android versions apply restricted-setting protections to sideloaded apps. If the system will not allow enabling the service, follow your device's documented instructions for apps you trust; Enough does not bypass Android's approval screens. If protection is off, the dashboard says so rather than claiming that apps are blocked.

Disabling permissions or uninstalling Enough remains possible. Do not test this on somebody else's phone without their agreement.

## Privacy

- No `INTERNET` permission. No accounts, analytics, advertising SDKs, or servers.
- Accessibility listens only for window-state changes to identify app packages. `canRetrieveWindowContent` is false. There is no screen-tree traversal, message reading, typing capture, screenshot capture, or gesture automation.
- Usage access reads app lifecycle timestamps in memory. Only selected-app totals, budgets, reset metadata, disclosure consent, and a local service-health timestamp are persisted.
- Cloud backup and device transfer of the app's private data are excluded by configuration.
- Settings and uninstall stay accessible. This is not parental control, spyware, or an enterprise device manager.

See [PRIVACY.md](PRIVACY.md) for details.

## Build

Requirements: JDK 17, Android SDK platform 35 and build tools 35.0.0, and an Android 8.0+ device for installation. The Gradle wrapper downloads Gradle 8.11.1; dependency downloads require network access on the development computer. The installed app does not.

Open the repository in Android Studio and let it locate your SDK, or set `ANDROID_HOME`/an untracked `local.properties` file.

```sh
# macOS / Linux
bash gradlew testDebugUnitTest lintDebug assembleDebug

# Windows
.\gradlew.bat testDebugUnitTest lintDebug assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`.

The GitHub Actions workflow runs the same checks and makes the debug APK available as a workflow artifact. It does not contain or produce a production signing key.

## Architecture

| Component | Responsibility |
| --- | --- |
| `MainActivity` | Native permission onboarding, daily budget dashboard, tomorrow-only editor |
| `BudgetStore` | Private persistent budgets, totals, pending plan, daily reset fence |
| `PlanCommitment` | Pure policy preventing proposals from mutating today's committed plan |
| `BudgetRules` | Pure budget boundaries and time-zone-aware reset calculation |
| `UsageReader` | Android activity-event reconciliation; event stream stays in memory |
| `BudgetService` | Foreground package observation, local timer, blocking overlay |
| `PackageSafety` | Excludes system and essential apps from selection |

The implementation uses Android platform views and Java 17. It has no third-party runtime libraries.

## Before calling this production-ready

Follow [TESTING.md](TESTING.md), including screen-off, reboot, background-kill, clock-change, midnight, notification navigation, and multi-window checks on real devices. Timing and background behavior vary by manufacturer. A compiled APK and passing unit tests do not establish real-device reliability.

Distribution through Google Play would also require the appropriate [accessibility declaration, disclosure, consent, and review](https://support.google.com/googleplay/android-developer/answer/10964491). This alpha has not been submitted or approved.

## Contribute

Start with reproducible device reports and the test checklist. Include Android version and manufacturer, but **do not attach private app usage or personal screenshots**. Useful next work: deterministic usage-event tests, device instrumentation tests, lifecycle reliability, translations, and accessibility improvements to the UI. See [CONTRIBUTING.md](CONTRIBUTING.md).

## License

MIT. The Gradle wrapper and build-tool dependencies retain their own upstream licenses.
