# Contributing

Use JDK 17 and the checked-in Gradle wrapper. Run unit tests, Android lint, and debug assembly before submitting changes. For enforcement changes, include device/OS details and the relevant manual test results from TESTING.md.

Keep the central promise: no in-app override of the current day's committed limit. Plan edits must take effect tomorrow. Essential apps and system controls must remain available. Never hide permissions, interfere with uninstall/settings, add remote control, or describe personal commitment mode as tamper-proof.

Keep the app offline and minimize data access. Explain any proposed new permission before implementing it. Accessibility changes must preserve the explicit disclosure and must not enable reading screen contents for budget enforcement.

For bug reports, describe expected/actual behavior and reproduction steps. Do not include private app usage, messages, credentials, or personal screenshots. Do not report bypasses already documented in README as unknown vulnerabilities.

The project is MIT licensed. By submitting a contribution, you agree that your contribution can be distributed under that license.
