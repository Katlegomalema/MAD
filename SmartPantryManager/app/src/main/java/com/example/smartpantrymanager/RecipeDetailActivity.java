package com.example.smartpantrymanager;

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
import java.util.Locale;

public class RecipeDetailActivity extends AppCompatActivity {

    private TextView tvRecipeTitle, tvRecipeSteps;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        dbHelper = new DatabaseHelper(this);
        tvRecipeTitle = findViewById(R.id.tvRecipeDetailTitle);
        tvRecipeSteps = findViewById(R.id.tvRecipeDetailSteps);
        RecyclerView ingredientsList = findViewById(R.id.recyclerViewRecipeIngredients);
        ingredientsList.setLayoutManager(new LinearLayoutManager(this));
        ingredientsList.setNestedScrollingEnabled(false);

        int recipeId = getIntent().getIntExtra("RECIPE_ID", -1);
        String recipeName = getIntent().getStringExtra("RECIPE_NAME");
        String recipeSteps = getIntent().getStringExtra("RECIPE_STEPS");

        if (recipeName != null) {
            setTitle(recipeName);
            if (tvRecipeTitle != null) {
                tvRecipeTitle.setText(recipeName);
            }
        }

        if (recipeSteps != null && tvRecipeSteps != null) {
            tvRecipeSteps.setText(recipeSteps);
        }

        List<RecipeIngredient> ingredients = new ArrayList<>();
        if (recipeId != -1) {
            try (Cursor cursor = dbHelper.getRecipeIngredients(recipeId)) {
                while (cursor.moveToNext()) {
                    ingredients.add(new RecipeIngredient(
                            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_RI_ING_NAME)),
                            cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_RI_QTY)),
                            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_RI_UNIT))));
                }
            }
        }
        ingredientsList.setAdapter(new IngredientAdapter(ingredients));
    }

    private static String formatQuantity(double quantity) {
        if (quantity == Math.rint(quantity)) {
            return String.valueOf((long) quantity);
        }
        return String.valueOf(quantity);
    }

    private static class RecipeIngredient {
        final String name;
        final double quantity;
        final String unit;

        RecipeIngredient(String name, double quantity, String unit) {
            this.name = name;
            this.quantity = quantity;
            this.unit = unit;
        }
    }

    private class IngredientAdapter extends RecyclerView.Adapter<IngredientAdapter.IngredientViewHolder> {
        private final List<RecipeIngredient> ingredients;

        IngredientAdapter(List<RecipeIngredient> ingredients) {
            this.ingredients = ingredients;
        }

        @NonNull
        @Override
        public IngredientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_recipe_ingredient, parent, false);
            return new IngredientViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull IngredientViewHolder holder, int position) {
            RecipeIngredient ingredient = ingredients.get(position);
            String displayName = ingredient.name.isEmpty()
                    ? ingredient.name
                    : ingredient.name.substring(0, 1).toUpperCase(Locale.ROOT) + ingredient.name.substring(1);
            holder.name.setText(displayName);
            holder.amount.setText(formatQuantity(ingredient.quantity) + " " + ingredient.unit);
        }

        @Override
        public int getItemCount() {
            return ingredients.size();
        }

        class IngredientViewHolder extends RecyclerView.ViewHolder {
            final TextView name;
            final TextView amount;

            IngredientViewHolder(@NonNull View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.tvRecipeIngredientName);
                amount = itemView.findViewById(R.id.tvRecipeIngredientAmount);
            }
        }
    }
}