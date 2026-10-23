package com.example.smartpantrymanager;
// future me: keep names lowercase so recipe matching doesn't get picky about capitals!
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "smart_pantry.db";
    private static final int DATABASE_VERSION = 2;

    // Table Names
    public static final String TABLE_PANTRY = "pantry";
    public static final String TABLE_RECIPES = "recipes";
    public static final String TABLE_RECIPE_INGREDIENTS = "recipe_ingredients";

    // Pantry Table Columns
    public static final String COL_PANTRY_ID = "id";
    public static final String COL_PANTRY_NAME = "name";
    public static final String COL_PANTRY_QTY = "quantity";
    public static final String COL_PANTRY_UNIT = "unit";
    public static final String COL_PANTRY_EXPIRY = "expiry_date";

    // Recipes Table Columns
    public static final String COL_RECIPE_ID = "id";
    public static final String COL_RECIPE_NAME = "name";
    public static final String COL_RECIPE_STEPS = "prep_steps";

    // Recipe Ingredients Columns
    public static final String COL_RI_ID = "id";
    public static final String COL_RI_RECIPE_ID = "recipe_id";
    public static final String COL_RI_ING_NAME = "ingredient_name";
    public static final String COL_RI_QTY = "required_quantity";
    public static final String COL_RI_UNIT = "required_unit";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Pantry Table
        String createPantryTable = "CREATE TABLE " + TABLE_PANTRY + " (" +
                COL_PANTRY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_PANTRY_NAME + " TEXT UNIQUE, " +
                COL_PANTRY_QTY + " REAL, " +
                COL_PANTRY_UNIT + " TEXT, " +
                COL_PANTRY_EXPIRY + " TEXT)";
        db.execSQL(createPantryTable);

        // Create Recipes Table
        String createRecipesTable = "CREATE TABLE " + TABLE_RECIPES + " (" +
                COL_RECIPE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_RECIPE_NAME + " TEXT, " +
                COL_RECIPE_STEPS + " TEXT)";
        db.execSQL(createRecipesTable);

        // Create Recipe Ingredients Table (for strict-matching)
        String createRecipeIngredientsTable = "CREATE TABLE " + TABLE_RECIPE_INGREDIENTS + " (" +
                COL_RI_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_RI_RECIPE_ID + " INTEGER, " +
                COL_RI_ING_NAME + " TEXT, " +
                COL_RI_QTY + " REAL, " +
                COL_RI_UNIT + " TEXT, " +
                "FOREIGN KEY(" + COL_RI_RECIPE_ID + ") REFERENCES " + TABLE_RECIPES + "(" + COL_RECIPE_ID + "))";
        db.execSQL(createRecipeIngredientsTable);

        // Seed recipes(data) and required ingredients
        seedInitialData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_RECIPE_INGREDIENTS + " ADD COLUMN " + COL_RI_UNIT + " TEXT");
            db.execSQL("UPDATE " + TABLE_RECIPE_INGREDIENTS + " SET " + COL_RI_UNIT +
                    " = CASE " +
                    "WHEN " + COL_RI_ING_NAME + " IN ('eggs', 'bread', 'avocado', 'tomatoes', 'onions') THEN 'pcs' " +
                    "WHEN " + COL_RI_ING_NAME + " = 'garlic' THEN 'cloves' " +
                    "WHEN " + COL_RI_ING_NAME + " = 'butter' THEN 'tbsp' " +
                    "WHEN " + COL_RI_ING_NAME + " = 'coffee' THEN 'tsp' " +
                    "WHEN " + COL_RI_ING_NAME + " = 'milk' THEN 'ml' " +
                    "ELSE 'g' END");
        }
    }

    public Cursor getRecipeIngredients(int recipeId) {
        return getReadableDatabase().query(
                TABLE_RECIPE_INGREDIENTS,
                new String[]{COL_RI_ING_NAME, COL_RI_QTY, COL_RI_UNIT},
                COL_RI_RECIPE_ID + " = ?",
                new String[]{String.valueOf(recipeId)},
                null,
                null,
                COL_RI_ID);
    }

    public boolean hasSufficientIngredients(int recipeId) {
        Map<String, PantryStock> pantryStock = new HashMap<>();
        try (Cursor pantryCursor = getAllPantryItems()) {
            while (pantryCursor.moveToNext()) {
                String name = normalize(pantryCursor.getString(
                        pantryCursor.getColumnIndexOrThrow(COL_PANTRY_NAME)));
                if (!name.isEmpty()) {
                    pantryStock.put(name, new PantryStock(
                            pantryCursor.getDouble(pantryCursor.getColumnIndexOrThrow(COL_PANTRY_QTY)),
                            pantryCursor.getString(pantryCursor.getColumnIndexOrThrow(COL_PANTRY_UNIT))));
                }
            }
        }

        boolean hasIngredients = false;
        try (Cursor ingredientCursor = getRecipeIngredients(recipeId)) {
            while (ingredientCursor.moveToNext()) {
                hasIngredients = true;
                String name = normalize(ingredientCursor.getString(
                        ingredientCursor.getColumnIndexOrThrow(COL_RI_ING_NAME)));
                PantryStock stock = pantryStock.get(name);
                double requiredQuantity = ingredientCursor.getDouble(
                        ingredientCursor.getColumnIndexOrThrow(COL_RI_QTY));
                String requiredUnit = ingredientCursor.getString(
                        ingredientCursor.getColumnIndexOrThrow(COL_RI_UNIT));
                Double availableQuantity = stock == null
                        ? null
                        : convertQuantity(stock.quantity, stock.unit, requiredUnit);
                if (availableQuantity == null || !Double.isFinite(availableQuantity) ||
                        !Double.isFinite(requiredQuantity) || requiredQuantity <= 0 ||
                        availableQuantity < requiredQuantity) {
                    return false;
                }
            }
        }
        return hasIngredients;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private static Double convertQuantity(double quantity, String pantryUnit, String requiredUnit) {
        String from = normalizeUnit(pantryUnit);
        String to = normalizeUnit(requiredUnit);
        if (from.equals(to)) {
            return quantity;
        }
        if ("kg".equals(from) && "g".equals(to)) {
            return quantity * 1000;
        }
        if ("g".equals(from) && "kg".equals(to)) {
            return quantity / 1000;
        }
        if ("l".equals(from) && "ml".equals(to)) {
            return quantity * 1000;
        }
        if ("ml".equals(from) && "l".equals(to)) {
            return quantity / 1000;
        }
        return null;
    }

    private static String normalizeUnit(String unit) {
        String normalized = normalize(unit).replace(".", "");
        if ("piece".equals(normalized) || "pieces".equals(normalized) ||
                "pc".equals(normalized)) {
            return "pcs";
        }
        if ("clove".equals(normalized)) {
            return "cloves";
        }
        if ("tablespoon".equals(normalized) || "tablespoons".equals(normalized)) {
            return "tbsp";
        }
        if ("teaspoon".equals(normalized) || "teaspoons".equals(normalized)) {
            return "tsp";
        }
        if ("gram".equals(normalized) || "grams".equals(normalized)) {
            return "g";
        }
        if ("kilogram".equals(normalized) || "kilograms".equals(normalized) || "kgs".equals(normalized)) {
            return "kg";
        }
        if ("milliliter".equals(normalized) || "milliliters".equals(normalized) ||
                "millilitre".equals(normalized) || "millilitres".equals(normalized)) {
            return "ml";
        }
        if ("liter".equals(normalized) || "liters".equals(normalized) ||
                "litre".equals(normalized) || "litres".equals(normalized)) {
            return "l";
        }
        return normalized;
    }

    private static class PantryStock {
        final double quantity;
        final String unit;

        PantryStock(double quantity, String unit) {
            this.quantity = quantity;
            this.unit = unit;
        }
    }

    // --- PANTRY CRUD OPERATIONS ---

    public boolean insertPantryItem(String name, double quantity, String unit, String expiry) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        // future me: keep names lowercase so recipe matching doesn't get picky about capitals!
        values.put(COL_PANTRY_NAME, name.toLowerCase().trim());
        values.put(COL_PANTRY_QTY, quantity);
        values.put(COL_PANTRY_UNIT, unit);
        values.put(COL_PANTRY_EXPIRY, expiry);

        long result = db.insert(TABLE_PANTRY, null, values);
        return result != -1;
    }

    public Cursor getAllPantryItems() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_PANTRY, null);
    }

    public boolean updatePantryItem(int id, String name, double quantity, String unit, String expiry) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_PANTRY_NAME, name.toLowerCase().trim());
        values.put(COL_PANTRY_QTY, quantity);
        values.put(COL_PANTRY_UNIT, unit);
        values.put(COL_PANTRY_EXPIRY, expiry);

        int rowsAffected = db.update(TABLE_PANTRY, values, COL_PANTRY_ID + " = ?", new String[]{String.valueOf(id)});
        return rowsAffected > 0;
    }

    public boolean deletePantryItem(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete(TABLE_PANTRY, COL_PANTRY_ID + " = ?", new String[]{String.valueOf(id)});
        return rowsAffected > 0;
    }

    // --- SEEDING INITIAL RECIPES (Satisfies the 15-20 requirement) ---

    private void seedInitialData(SQLiteDatabase db) {
        // Recipe 1: Garlic Chicken Alfredo
        long r1 = insertRecipe(db, "Garlic Chicken Alfredo", "Boil pasta. Cook chicken strips in garlic and butter, mix with cream and pasta.");
        insertRecipeIngredient(db, r1, "pasta", 500);
        insertRecipeIngredient(db, r1, "chicken", 300);
        insertRecipeIngredient(db, r1, "garlic", 3);
        insertRecipeIngredient(db, r1, "butter", 2);

        // Recipe 2: Scrambled Eggs and Avo on Toast
        long r2 = insertRecipe(db, "Scrambled Eggs and Avo on Toast", "Toast bread. Scramble eggs in a pan. Slice avocado and assemble on toast.");
        insertRecipeIngredient(db, r2, "eggs", 3);
        insertRecipeIngredient(db, r2, "bread", 2);
        insertRecipeIngredient(db, r2, "avocado", 1);
        insertRecipeIngredient(db, r2, "butter", 1);

        // Recipe 3: Cheese Omelette 
        long r3 = insertRecipe(db, "Cheese Omelette", "Whisk eggs, pour into hot pan, fold with grated cheese.");
        insertRecipeIngredient(db, r3, "eggs", 2);
        insertRecipeIngredient(db, r3, "cheese", 100);
        insertRecipeIngredient(db, r3, "butter", 1);

        // Recipe 4: Pap and Tomato Sauté and Roasted Chicken
        long r4 = insertRecipe(db, "Pap and Tomato Sauté and Roasted Chicken", "Cook pap with maize meal and water. Sauté tomatoes and onions. Serve with roasted chicken.");
        insertRecipeIngredient(db, r4, "maize meal", 500);
        insertRecipeIngredient(db, r4, "tomatoes", 4);
        insertRecipeIngredient(db, r4, "chicken", 400);

        // Recipe 5: Simple Grilled Cheese Sandwich
        long r5 = insertRecipe(db, "Grilled Cheese Sandwich", "Place cheese between two slices of bread, grill in pan with butter until golden.");
        insertRecipeIngredient(db, r5, "bread", 2);
        insertRecipeIngredient(db, r5, "cheese", 50);
        insertRecipeIngredient(db, r5, "butter", 1);

        // Recipe 6: Classic American style Pancakes
        long r6 = insertRecipe(db, "Classic Pancakes", "Mix flour, eggs, and milk. Cook small ladles on a hot griddle.");
        insertRecipeIngredient(db, r6, "flour", 300);
        insertRecipeIngredient(db, r6, "eggs", 2);
        insertRecipeIngredient(db, r6, "milk", 200);

        // Recipe 7: Pap with Cabbage and Beef or Lamb Chops
        long r7 = insertRecipe(db, "Pap with Cabbage and Beef or Lamb Chops", "Cook traditional pap with maize meal and water. Pan-fry or braise beef or lamb chops until tender, and steam cabbage with onions.");
        insertRecipeIngredient(db, r7, "maize meal", 400);
        insertRecipeIngredient(db, r7, "cabbage", 300);
        insertRecipeIngredient(db, r7, "beef", 300);
        insertRecipeIngredient(db, r7, "onions", 1);

        // Recipe 8: Garlic Bread
        long r8 = insertRecipe(db, "Garlic Bread", "Mix minced garlic and butter, spread over sliced bread, and bake.");
        insertRecipeIngredient(db, r8, "bread", 4);
        insertRecipeIngredient(db, r8, "garlic", 2);
        insertRecipeIngredient(db, r8, "butter", 2);

        // Recipe 9: Fried Eggs and Toast
        long r9 = insertRecipe(db, "Fried Eggs and Toast", "Fry eggs sunny-side up in a pan and serve over buttered toast.");
        insertRecipeIngredient(db, r9, "eggs", 2);
        insertRecipeIngredient(db, r9, "bread", 2);
        insertRecipeIngredient(db, r9, "butter", 1);

        // Recipe 10: Spaghetti Bolognese
        long r10 = insertRecipe(db, "Spaghetti Bolognese", "Boil spaghetti. Cook minced beef with tomato sauce and herbs, combine.");
        insertRecipeIngredient(db, r10, "spaghetti", 400);
        insertRecipeIngredient(db, r10, "mince", 300);
        insertRecipeIngredient(db, r10, "tomatoes", 4);

        // Recipe 11: Egg Fried Rice with Stir Fry
        long r11 = insertRecipe(db, "Egg Fried Rice with Stir Fry", "Stir-fry rice with mixed vegetables, scrambled eggs, and soy sauce.");
        insertRecipeIngredient(db, r11, "rice", 400);
        insertRecipeIngredient(db, r11, "eggs", 2);
        insertRecipeIngredient(db, r11, "vegetables", 200);

        // Recipe 12: French Toast
        long r12 = insertRecipe(db, "French Toast", "Dip bread slices in a mix of beaten eggs and milk, then pan-fry.");
        insertRecipeIngredient(db, r12, "bread", 2);
        insertRecipeIngredient(db, r12, "eggs", 1);
        insertRecipeIngredient(db, r12, "milk", 100);

        // Recipe 13: Mince and Toast
        long r13 = insertRecipe(db, "Mince and Toast", "Cook savory minced beef in a pan and spoon generously over buttered toast.");
        insertRecipeIngredient(db, r13, "mince", 250);
        insertRecipeIngredient(db, r13, "bread", 2);
        insertRecipeIngredient(db, r13, "butter", 1);

        // Recipe 14: Cheese Toast
        long r14 = insertRecipe(db, "Cheese Toast", "Top bread with cheese slices and toast in an oven until melted.");
        insertRecipeIngredient(db, r14, "bread", 2);
        insertRecipeIngredient(db, r14, "cheese", 80);

        // Recipe 15: Coffee
        long r15 = insertRecipe(db, "Coffee", "Brew instance coffee with hot water and add milk or sugar to taste.");
        insertRecipeIngredient(db, r15, "coffee", 2);
        insertRecipeIngredient(db, r15, "milk", 50);
    }
        

    private long insertRecipe(SQLiteDatabase db, String name, String steps) {
        ContentValues values = new ContentValues();
        values.put(COL_RECIPE_NAME, name);
        values.put(COL_RECIPE_STEPS, steps);
        return db.insert(TABLE_RECIPES, null, values);
    }

    private void insertRecipeIngredient(SQLiteDatabase db, long recipeId, String ingredientName, double qty) {
        ContentValues values = new ContentValues();
        values.put(COL_RI_RECIPE_ID, recipeId);
        values.put(COL_RI_ING_NAME, ingredientName.toLowerCase().trim());
        values.put(COL_RI_QTY, qty);
        values.put(COL_RI_UNIT, getRecipeUnit(ingredientName));
        db.insert(TABLE_RECIPE_INGREDIENTS, null, values);
    }

    private String getRecipeUnit(String ingredientName) {
        switch (ingredientName) {
            case "eggs":
            case "bread":
            case "avocado":
            case "tomatoes":
            case "onions":
                return "pcs";
            case "garlic":
                return "cloves";
            case "butter":
                return "tbsp";
            case "coffee":
                return "tsp";
            case "milk":
                return "ml";
            default:
                return "g";
        }
    }
}
