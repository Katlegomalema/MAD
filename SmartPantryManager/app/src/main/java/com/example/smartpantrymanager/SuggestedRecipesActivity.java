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
import java.util.List;

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
        setTitle("Suggested Recipes");

        dbHelper = new DatabaseHelper(this);
        recyclerViewRecipes = findViewById(R.id.recyclerViewRecipes);
        tvNoRecipes = findViewById(R.id.tvNoRecipes);

        recyclerViewRecipes.setLayoutManager(new LinearLayoutManager(this));
        matchedRecipeList = new ArrayList<>();

        loadStrictMatchedRecipes();
    }

    private void loadStrictMatchedRecipes() {
        matchedRecipeList.clear();

        try (Cursor recipeCursor = dbHelper.getReadableDatabase().query(
                DatabaseHelper.TABLE_RECIPES,
                new String[]{DatabaseHelper.COL_RECIPE_ID, DatabaseHelper.COL_RECIPE_NAME, DatabaseHelper.COL_RECIPE_STEPS},
                null,
                null,
                null,
                null,
                DatabaseHelper.COL_RECIPE_NAME)) {
            while (recipeCursor.moveToNext()) {
                int recipeId = recipeCursor.getInt(recipeCursor.getColumnIndexOrThrow(DatabaseHelper.COL_RECIPE_ID));
                String recipeName = recipeCursor.getString(recipeCursor.getColumnIndexOrThrow(DatabaseHelper.COL_RECIPE_NAME));
                String prepSteps = recipeCursor.getString(recipeCursor.getColumnIndexOrThrow(DatabaseHelper.COL_RECIPE_STEPS));

                if (dbHelper.hasSufficientIngredients(recipeId)) {
                    matchedRecipeList.add(new RecipeItem(recipeId, recipeName, prepSteps));
                }
            }
        }

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
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe, parent, false);
            return new RecipeViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
            RecipeItem recipe = recipes.get(position);
            holder.text1.setText(recipe.name);
            holder.text2.setText("View ingredients and preparation");

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(SuggestedRecipesActivity.this, RecipeDetailActivity.class);
                intent.putExtra("RECIPE_ID", recipe.id);
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
                text1 = itemView.findViewById(R.id.tvRecipeName);
                text2 = itemView.findViewById(R.id.tvRecipeSummary);
            }
        }
    }
}