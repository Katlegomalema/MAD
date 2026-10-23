package com.example.smartpantrymanager;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SuggestedRecipesActivity extends AppCompatActivity {

    private RecyclerView recyclerViewRecipes;
    private TextView tvNoRecipes;
    private DatabaseHelper dbHelper;
    private RecipeAdapter recipeAdapter;
    private List<RecipeItem> matchedRecipeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_recipes);
        setTitle("Suggested Recipes (Strict Match)");

        dbHelper = new DatabaseHelper(this);
        recyclerViewRecipes = findViewById(R.id.recyclerViewRecipes);
        tvNoRecipes = findViewById(R.id.tvNoRecipes);

        recyclerViewRecipes.setLayoutManager(new LinearLayoutManager(this));
        matchedRecipeList = new ArrayList<>();

        loadStrictMatchedRecipes();
    }

    private void loadStrictMatchedRecipes() {
        matchedRecipeList.clear();

        // 1. Collect all ingredient names currently available in the pantry
        Set<String> pantryIngredients = new HashSet<>();
        Cursor pantryCursor = dbHelper.getAllPantryItems();
        if (pantryCursor != null && pantryCursor.moveToFirst()) {
            do {
                String name = pantryCursor.getString(pantryCursor.getColumnIndexOrThrow(DatabaseHelper.COL_PANTRY_NAME));
                if (name != null) {
                    pantryIngredients.add(name.toLowerCase().trim());
                }
            } while (pantryCursor.moveToNext());
            pantryCursor.close();
        }

        // 2. Fetch all recipes from the database
        Cursor recipeCursor = dbHelper.getReadableDatabase().rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_RECIPES, null);
        if (recipeCursor != null && recipeCursor.moveToFirst()) {
            do {
                int recipeId = recipeCursor.getInt(recipeCursor.getColumnIndexOrThrow(DatabaseHelper.COL_RECIPE_ID));
                String recipeName = recipeCursor.getString(recipeCursor.getColumnIndexOrThrow(DatabaseHelper.COL_RECIPE_NAME));
                String prepSteps = recipeCursor.getString(recipeCursor.getColumnIndexOrThrow(DatabaseHelper.COL_RECIPE_STEPS));

                // 3. Fetch required ingredients for the current recipe
                List<String> requiredIngredients = new ArrayList<>();
                Cursor reqCursor = dbHelper.getReadableDatabase().rawQuery(
                        "SELECT " + DatabaseHelper.COL_RI_ING_NAME + " FROM " + DatabaseHelper.TABLE_RECIPE_INGREDIENTS +
                                " WHERE " + DatabaseHelper.COL_RI_RECIPE_ID + " = ?", new String[]{String.valueOf(recipeId)});

                if (reqCursor != null && reqCursor.moveToFirst()) {
                    do {
                        String reqIng = reqCursor.getString(reqCursor.getColumnIndexOrThrow(DatabaseHelper.COL_RI_ING_NAME));
                        if (reqIng != null) {
                            requiredIngredients.add(reqIng.toLowerCase().trim());
                        }
                    } while (reqCursor.moveToNext());
                    reqCursor.close();
                }

                // 4. Strict Matching Algorithm: Pantry must contain ALL required ingredients
                if (!requiredIngredients.isEmpty() && pantryIngredients.containsAll(requiredIngredients)) {
                    matchedRecipeList.add(new RecipeItem(recipeId, recipeName, prepSteps));
                }

            } while (recipeCursor.moveToNext());
            recipeCursor.close();
        }

        // 5. Update UI based on match results
        if (matchedRecipeList.isEmpty()) {
            recyclerViewRecipes.setVisibility(View.GONE);
            tvNoRecipes.setVisibility(View.VISIBLE);
        } else {
            recyclerViewRecipes.setVisibility(View.VISIBLE);
            tvNoRecipes.setVisibility(View.GONE);
            recipeAdapter = new RecipeAdapter(matchedRecipeList);
            recyclerViewRecipes.setAdapter(recipeAdapter);
        }
    }

    public static class RecipeItem {
        int id;
        String name;
        String steps;

        public RecipeItem(int id, String name, String steps) {
            this.id = id;
            this.name = name;
            this.steps = steps;
        }
    }

    private class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {
        private final List<RecipeItem> recipes;

        public RecipeAdapter(List<RecipeItem> recipes) {
            this.recipes = recipes;
        }

        @NonNull
        @Override
        public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
            return new RecipeViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
            RecipeItem recipe = recipes.get(position);
            holder.text1.setText(recipe.name);
            holder.text2.setText("Tap to view preparation steps");

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(SuggestedRecipesActivity.this, RecipeDetailActivity.class);
                intent.putExtra("RECIPE_NAME", recipe.name);
                intent.putExtra("RECIPE_STEPS", recipe.steps);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return recipes.size();
        }

        public class RecipeViewHolder extends RecyclerView.ViewHolder {
            TextView text1, text2;

            public RecipeViewHolder(@NonNull View itemView) {
                super(itemView);
                text1 = itemView.findViewById(android.R.id.text1);
                text2 = itemView.findViewById(android.R.id.text2);
            }
        }
    }
}