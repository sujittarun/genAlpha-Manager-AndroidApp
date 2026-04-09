# Gen Alpha Academy Android App

This folder contains a native Android Studio project for the academy app.

## What it does

- uses native Kotlin + Jetpack Compose screens
- talks directly to Supabase from Android code
- keeps the academy workflows for login, add/edit/delete, renewals, slot filters, and discontinued players
- is designed for faster mobile interaction than a browser or WebView wrapper

## Open in Android Studio

1. Open Android Studio
2. Choose `Open`
3. Select this folder:
   `android-app`
4. Let Gradle sync
5. Run on an emulator or Android phone

## Build APK

1. In Android Studio, open `Build`
2. Choose `Build Bundle(s) / APK(s)`
3. Choose `Build APK(s)`

## Important

- Supabase config for Android lives in:
  `app/src/main/java/com/genalpha/cricketacademy/data/SupabaseConfig.kt`
- If you change your Supabase project later, update that file and rebuild.
