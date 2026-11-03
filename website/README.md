# Enough website

Static, account-free website for direct Android APK downloads and a voluntary browser-session timer. No build dependencies or Supabase database are needed.

## Local use

`node server.mjs` serves http://127.0.0.1:4173. Run `npm test` for the browser-session policy tests.

## Deployment

Deploy this directory as a Vercel static project with no framework, install command, or build command. The output directory is `.`. `vercel.json` provides security and APK download headers. Do not deploy the Android repository root as the website.

For a new Android release, copy the built APK and checksum into `downloads/`, update the versioned download links in index.html, and deploy the exact matching website source. APKs are generated release artifacts and are intentionally ignored by the source repository's .gitignore.

## What browser mode does

It stores a daily session allowance and sessions in this browser's localStorage. A session charges elapsed wall time even if the page is left, closed, or backgrounded. A second session cannot be started while one is active. Budget changes are deferred until the next captured local daily reset. No app names, browsing history, or message content are observed.

This is not device-level enforcement: it cannot block Instagram, stop other tabs, or prevent site-data clearing, clock changes, or using another browser. It is labelled as a voluntary timer everywhere the feature is presented. Use the installed Android app for its separately disclosed app-level guard.

No analytics or account system is included. The hosting provider processes ordinary requests to serve the site and release files. Timer data remains local. The service worker caches the website shell, not APK downloads.
