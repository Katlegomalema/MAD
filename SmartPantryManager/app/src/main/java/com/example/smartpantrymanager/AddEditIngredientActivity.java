package com.example.smartpantrymanager;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AddEditIngredientActivity extends AppCompatActivity {

    private EditText etName, etQuantity, etUnit, etExpiry;
    private Button btnSave;
    private DatabaseHelper dbHelper;

    private int itemId = -1; // Default to -1 indicating a new item
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit);

        dbHelper = new DatabaseHelper(this);

        etName = findViewById(R.id.etIngredientName);
        etQuantity = findViewById(R.id.etIngredientQty);
        etUnit = findViewById(R.id.etIngredientUnit);
        etExpiry = findViewById(R.id.etIngredientExpiry);
        btnSave = findViewById(R.id.btnSaveIngredient);

        // Check if we passed data for editing
        if (getIntent().hasExtra("ID")) {
            isEditMode = true;
            itemId = getIntent().getIntExtra("ID", -1);
            etName.setText(getIntent().getStringExtra("NAME"));
            etQuantity.setText(String.valueOf(getIntent().getDoubleExtra("QTY", 0.0)));
            etUnit.setText(getIntent().getStringExtra("UNIT"));
            etExpiry.setText(getIntent().getStringExtra("EXPIRY"));
            setTitle("Edit Ingredient");
            btnSave.setText("Update Ingredient");
        } else {
            setTitle("Add Ingredient");
            btnSave.setText("Save Ingredient");
        }

        btnSave.setOnClickListener(v -> saveIngredient());
    }

    private void saveIngredient() {
        String name = etName.getText().toString().trim();
        String qtyStr = etQuantity.getText().toString().trim();
        String unit = etUnit.getText().toString().trim();
        String expiry = etExpiry.getText().toString().trim();

        if (name.isEmpty()) {
            etName.setError("Ingredient name is required");
            etName.requestFocus();
            return;
        }

        if (qtyStr.isEmpty()) {
            etQuantity.setError("Quantity is required");
            etQuantity.requestFocus();
            return;
        }

        double quantity;
        try {
            quantity = Double.parseDouble(qtyStr);
        } catch (NumberFormatException e) {
            etQuantity.setError("Enter a valid quantity");
            etQuantity.requestFocus();
            return;
        }
        if (!Double.isFinite(quantity) || quantity <= 0) {
            etQuantity.setError("Quantity must be a positive number");
            etQuantity.requestFocus();
            return;
        }

        if (unit.isEmpty()) {
            etUnit.setError("Unit is required");
            etUnit.requestFocus();
            return;
        }
        etName.setError(null);
        etQuantity.setError(null);
        etUnit.setError(null);

        boolean success;
        if (isEditMode) {
            success = dbHelper.updatePantryItem(itemId, name, quantity, unit, expiry);
            if (success) {
                Toast.makeText(this, "Ingredient updated successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Error updating ingredient", Toast.LENGTH_SHORT).show();
            }
        } else {
            success = dbHelper.insertPantryItem(name, quantity, unit, expiry);
            if (success) {
                Toast.makeText(this, "Ingredient added successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Error adding ingredient (Item may already exist)", Toast.LENGTH_SHORT).show();
            }
        }

        if (success) {
            finish(); // Return to MainActivity
        }
    }
}