package com.example.smartpantrymanager;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private Button btnResetData, btnAppInfo;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setTitle("Settings");

        dbHelper = new DatabaseHelper(this);

        btnResetData = findViewById(R.id.btnResetDatabase);
        btnAppInfo = findViewById(R.id.btnAppInfo);

        if (btnResetData != null) {
            btnResetData.setOnClickListener(v -> {
                new AlertDialog.Builder(this)
                        .setTitle("Reset Pantry & Recipes")
                        .setMessage("This will reset your database and reload default recipes. Are you sure?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            dbHelper.onUpgrade(dbHelper.getWritableDatabase(), 1, 1);
                            Toast.makeText(this, "Database reset and re-seeded successfully", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("No", null)
                        .show();
            });
        }

        if (btnAppInfo != null) {
            btnAppInfo.setOnClickListener(v -> {
                new AlertDialog.Builder(this)
                        .setTitle("About Smart Pantry Manager")
                        .setMessage("Smart Pantry Manager v1.0\nMobile App Development Project\nFeatures strict-matching recipe suggestions based on current pantry items.")
                        .setPositiveButton("OK", null)
                        .show();
            });
        }
    }
}