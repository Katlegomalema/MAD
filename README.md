# Smart Pantry Manager

Smart Pantry Manager is a Java Android application for managing pantry inventory and suggesting recipes that can be made using the ingredients already available.

## Features

- Create, view, update, and delete pantry ingredients, including quantity, unit, and optional expiry date.
- Store pantry items and recipes locally with SQLite.
- Suggest recipes only when all required ingredients and quantities are present.
- View recipe ingredients and preparation steps.
- Access app settings and reset the pantry and seeded recipes.

## Database choice

The app uses SQLite through Android's `SQLiteOpenHelper`. SQLite is a good fit because this is a single-device pantry app: it stores data persistently on the device, works offline, and does not require a server or account setup.

## Project structure

```text
SmartPantryManager/app/src/main/
├── java/com/example/smartpantrymanager/
│   ├── MainActivity.java
│   ├── AddEditIngredientActivity.java
│   ├── SuggestedRecipesActivity.java
│   ├── RecipeDetailActivity.java
│   ├── SettingsActivity.java
│   └── DatabaseHelper.java
└── res/
    └── layout/
```

## Requirements

- Android Studio
- Android SDK 34
- Java 8-compatible toolchain
- Minimum Android version: API 24

## Build and run

From the repository root, build the debug app with:

```bash
./gradlew assembleDebug
```

The APK is generated at `SmartPantryManager/app/build/outputs/apk/debug/app-debug.apk`. Open the repository in Android Studio to run it on an emulator or a connected Android device.

For a source-only submission archive, include the project source and Gradle wrapper but omit generated `.gradle/` and `build/` directories and APK files.
