# Privacy

Enough runs on the user's own Android device. Its manifest does not request internet access and it contains no third-party runtime SDKs.

## Data and permissions

**Usage access:** Android provides activity lifecycle timestamps and package identities. These are processed locally to estimate foreground time and restore totals after service interruptions. The raw event stream is not persisted. Android itself maintains its system usage history independently of Enough.

**Accessibility:** A separate in-app disclosure explains this before the user is sent to Android settings. The service observes only window-state changes, reads the package identity, counts selected-app time, and presents a blocking overlay. Screen content retrieval is disabled in the service configuration. No view text, typed text, screenshots, passwords, or message contents are read by the code.

**Installed app discovery:** Only launchable apps visible to the app are enumerated to let the user choose a budget. Essential/system apps and launchers are excluded from the picker. Enough does not request broad `QUERY_ALL_PACKAGES` access.

**Stored information:** selected package identifiers and limits; pending next-day limits; selected-app daily used time; local day/time-zone and boot metadata needed for reset handling; disclosure acceptance; and a local uptime-based service heartbeat.

No analytics, advertising, remote reporting, cloud accounts, remote configuration, or data sale exists. Backup/transfer exclusions are declared in the manifest and backup rules. A device owner with root or debugging access may still inspect device-local files.

## Choice and deletion

Permissions can be disabled in Android settings. Data clearing or uninstall removes Enough's local data and also bypasses its personal-commitment enforcement. Today's budget has no in-app editing or deletion override; modifications are staged for the next day.

Reports shared publicly on GitHub are outside the app. Remove personal information before posting a bug report.
