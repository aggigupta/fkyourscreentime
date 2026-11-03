# Verification and device test checklist

## Automated

`./gradlew testDebugUnitTest lintDebug assembleDebug`

Policy tests cover exact budget exhaustion, selected/unselected apps, same-boot wall-clock changes, reboot fallback, midnight in Asia/Kolkata, a daylight-saving transition, minute validation, immutable committed plans, deferred removals/increases, and defensive copies of proposed plans.

Android lint and APK assembly check resources, manifest, APIs, and Java compilation. They do not prove that Android delivers events reliably on a phone.

## Manual — still required for the alpha

Use a personal test phone or a clean Android emulator, not somebody else's configured device. Record device/OS version, grant the disclosed permissions, and confirm the live status before testing. A one-minute app budget is available for short tests.

- [ ] Fresh install shows setup, not false protection status.
- [ ] Denying either permission leaves the app usable but clearly marks protection as unavailable.
- [ ] Select a downloaded test app and commit one minute. Previous usage today counts.
- [ ] Keep the selected app in the foreground: overlay appears once its budget is exhausted.
- [ ] Press Home, reopen the app, open through a notification, and use Recents: exhausted app content remains covered while protection is running.
- [ ] Phone dialer, Settings, launcher, and Enough remain accessible.
- [ ] Propose more time or remove the selected app: today's blocker remains; the UI says the change is for tomorrow.
- [ ] Switch to an unselected app and turn the screen off: no continuous selected-app foreground charge while away.
- [ ] Reconnect the service: previously used time is not refunded.
- [ ] Reboot with the correct clock: budgets and totals survive and the service reconnects if Android permits it.
- [ ] Cross real midnight: totals reset and the staged plan applies.
- [ ] Change wall clock/time zone without reboot: the existing monotonic day fence does not expire early.
- [ ] Verify documented limitations with split-screen, keyboard/system dialogs, picture-in-picture, cloned apps, and background media.
- [ ] Revoke usage/accessibility and force-stop the app: the dashboard must not claim working protection. These are known bypasses, not hidden anti-uninstall features.
- [ ] Test screen reader navigation, large fonts, small screens, Android gesture navigation, and display cutouts.
- [ ] Repeat on multiple manufacturers and current Android versions before a stable release.

Do not manufacture screenshots or a passing device test report when no phone test has been performed.

## v0.2.0 hardening checks

Automated tests additionally cover the monotonic exhaustion latch, empty/lower usage snapshots, deferred removals, lookback accounting, shutdown, repeated pauses, and out-of-order events.

- [ ] Exhaust Instagram, revoke Usage Access only, and reopen it while accessibility remains enabled: the saved lockout still returns Home.
- [ ] Keyboard/pop-up windows do not clear an exhausted app’s guard. Enabled keyboard apps cannot be selected.
- [ ] A newly reset day clears the exhaustion latch; a missing Android snapshot within the same day does not.
- [ ] Instagram is selectable when preinstalled, while other system apps remain excluded.
- [ ] Confirm update installation keeps the existing ledger and uses the same signing certificate.

Website tests: run `node --test test/*.test.js` inside website/. These do not establish native Android enforcement.
