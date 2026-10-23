# Smart Pantry Manager 🥗📱

Smart Pantry Manager is an Android application designed to help users track their kitchen pantry inventory and automatically discover recipes they can cook based on the ingredients they currently have on hand.

---

## 🌟 Key Features

* **Pantry Inventory Management (CRUD):**
  * View current pantry items with available quantities and units (e.g., grams, pieces, liters).
  * Add new ingredients to the pantry.
  * Edit existing ingredient details and update stock levels.
  * Remove depleted ingredients from stock.

* **Smart Recipe Matcher:**
  * Analyzes available pantry inventory against recipe requirement lists.
  * Dynamically calculates available recipes based on ingredient existence and quantity sufficiency ($\text{Pantry Quantity} \ge \text{Recipe Quantity Required}$).

* **Recipe & Instruction Viewer:**
  * View step-by-step cooking instructions.
  * Detailed breakdown of required ingredients and exact quantities per recipe.

* **Local Database Storage:**
  * Uses SQLite via Android's `SQLiteOpenHelper` for reliable offline persistent storage.

---

## 🏗️ Architecture & Component Overview

The application follows standard Android architecture patterns with activity-driven UI screens and a centralized database helper class.

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

## 🚀 How to Build & Run

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

## 📋 Future Enhancements

* Expiration date tracking and alerts for perishable pantry items.
* Unit conversion engine (e.g., converting kg to grams automatically when matching recipes).
* Cloud sync and multi-user shared household pantries.