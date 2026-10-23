# Smart Pantry Manager

Smart Pantry Manager is an Android app that allows users to manage their pantry inventory and automatically suggest recipes that users can prepare using the ingredients in their pantry.

---

## Key Features

* **Pantry Inventory Management (CRUD):**
  * Shows existing pantry items along with the available quantity and unit (grams, pieces, liters, etc.).
  * Adds ingredients to the pantry.
  * Edits existing ingredient information and updates the quantity.
  * Deletes ingredients from the stock once they are finished.

* **Smart Recipe Matcher:**
  * Checks the available pantry inventory against the list of recipe requirements.
  * Dynamically finds out the recipes which can be prepared using the availability of the ingredients and sufficient quantities ($\text{Pantry Quantity} \ge \text{Recipe Quantity Required}$).

* **Recipe & Instruction Viewer:**
  * Displays step-by-step cooking instructions.
  * Breaks down the required ingredients and the exact quantities for each recipe.

* **Local Database Storage:**
  * Uses SQLite through Android `SQLiteOpenHelper`.

---

## Architecture & Component Overview

The app uses the standard Android architecture design principles with activity-based UI screens and a database helper class.

```
SmartPantryManager/
├── app/src/main/
│   ├── java/com/example/smartpantry/
│   │   ├── MainActivity.java                # Main Pantry Dashboard & List View
│   │   ├── AddEditIngredientActivity.java   # Create & Update Pantry Items
│   │   ├── SuggestedRecipesActivity.java   # Recipe Matching Engine
│   │   ├── RecipeDetailActivity.java      # Detailed Recipe & Required Ingredients
│   │   ├── SettingsActivity.java          # App Configuration
│   │   └── DatabaseHelper.java            # SQLite Database Operations
│   └── res/layout/                        # XML Layout Files
└── build.gradle                           # Gradle Build Configuration
```

---

## ⚙️ Tech Stack & Requirements

* **Language:** Java
* **Platform:** Android SDK (API Level 21+)
* **Database:** SQLite (`SQLiteOpenHelper`)
* **UI Components:** `RecyclerView`, `CardView`, `ConstraintLayout`, AndroidX Libraries
* **Build System:** Gradle 8.x

---

##  How to Build & Run

### 1. Build via Terminal (Gradle)
In your terminal or GitHub Codespaces environment, execute:

```bash
cd SmartPantryManager
./gradlew assembleDebug
```

The compiled APK file will be output to:
`app/build/outputs/apk/debug/app-debug.apk`

### 2. Running on Devices / Emulators
* **Physical Device:** Install `app-debug.apk` directly on an Android device via USB debugging or direct download.
* **Web Emulators:** Upload `app-debug.apk` to an online testing emulator (such as Appetize.io or APKOnline).

---
