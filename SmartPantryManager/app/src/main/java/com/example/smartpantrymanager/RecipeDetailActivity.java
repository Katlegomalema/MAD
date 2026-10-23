package com.example.smartpantrymanager;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class RecipeDetailActivity extends AppCompatActivity {

    private TextView tvRecipeTitle, tvRecipeSteps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        tvRecipeTitle = findViewById(R.id.tvRecipeDetailTitle);
        tvRecipeSteps = findViewById(R.id.tvRecipeDetailSteps);

        // Retrieve data passed from SuggestedRecipesActivity
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
    }
}