# AS MASTER Ad Blocker (Android, Kotlin + Jetpack Compose)

Taps YouTube's **Skip Ads** button automatically when it is visible. Bangla/English, dark/light, stats, DNS presets.

## Build the APK (Android Studio)
1. Install Android Studio (Koala or newer), open this folder, let Gradle sync.
2. Build > Build Bundle(s) / APK(s) > Build APK(s)
3. APK: `app/build/outputs/apk/debug/app-debug.apk` -> copy to phone and install.

Command line (Gradle 8.7+, JDK 17, Android SDK 34): `gradle --no-daemon assembleDebug`

## After installing
Open app > "Open Accessibility settings" > Installed apps > AS MASTER Ad Blocker > turn ON.
(Android 13+: if blocked, App info > ⋮ > Allow restricted settings first.)

## Notes
- Applicationid is `com.as.masteradblocker`; the Kotlin source package is `com.asmaster.adblocker` because `as` is a reserved Kotlin keyword.
- Ads WITHOUT a Skip button cannot be skipped by any app. DNS cannot remove all YouTube in-stream ads.
- The optional local VPN DNS filter is NOT implemented.

## Build APK with GitHub Actions (no computer needed)
1. Create a new GitHub repository (github.com > New repository).
2. Upload ALL files of this folder (including the hidden `.github` folder) to the repo, branch `main`.
3. Open the repo > Actions tab > "Build APK" (it starts automatically; or press "Run workflow").
4. Wait ~5-8 minutes until it turns green.
5. Open that run > scroll to Artifacts > download `AS_MASTER_Ad_Blocker_APK` (a ZIP containing AS_MASTER_Ad_Blocker.apk) > extract and install.
