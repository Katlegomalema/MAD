package com.example.smartpantrymanager;

import android.app.Activity;

public class MainActivity extends Activity {

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerViewPantry;
    private TextView tvEmptyPantry;
    private FloatingActionButton fabAddPantry;
    private DatabaseHelper dbHelper;
    private PantryAdapter pantryAdapter;
    private List<PantryItem> pantryList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);
        recyclerViewPantry = findViewById(R.id.recyclerViewPantry);
        tvEmptyPantry = findViewById(R.id.tvEmptyPantry);
        fabAddPantry = findViewById(R.id.fabAddPantry);

        recyclerViewPantry.setLayoutManager(new LinearLayoutManager(this));
        pantryList = new ArrayList<>();

        // Setup FAB click to open Add/Edit screen
        fabAddPantry.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddEditIngredientActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPantryData();
    }

    private void loadPantryData() {
        pantryList.clear();
        Cursor cursor = dbHelper.getAllPantryItems();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PANTRY_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PANTRY_NAME));
                double quantity = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PANTRY_QTY));
                String unit = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PANTRY_UNIT));
                String expiry = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PANTRY_EXPIRY));

                pantryList.add(new PantryItem(id, name, quantity, unit, expiry));
            } while (cursor.moveToNext());
            cursor.close();
        }

        if (pantryList.isEmpty()) {
            recyclerViewPantry.setVisibility(View.GONE);
            tvEmptyPantry.setVisibility(View.VISIBLE);
        } else {
            recyclerViewPantry.setVisibility(View.VISIBLE);
            tvEmptyPantry.setVisibility(View.GONE);
            pantryAdapter = new PantryAdapter(pantryList);
            recyclerViewPantry.setAdapter(pantryAdapter);
        }
    }

    // Model class for Pantry Items
    public static class PantryItem {
        int id;
        String name;
        double quantity;
        String unit;
        String expiry;

        public PantryItem(int id, String name, double quantity, String unit, String expiry) {
            this.id = id;
            this.name = name;
            this.quantity = quantity;
            this.unit = unit;
            this.expiry = expiry;
        }
    }

    // RecyclerView Adapter for Pantry List
    private class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.PantryViewHolder> {

        private final List<PantryItem> items;

        public PantryAdapter(List<PantryItem> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public PantryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pantry, parent, false);
            return new PantryViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PantryViewHolder holder, int position) {
            PantryItem item = items.get(position);
            holder.tvName.setText(item.name);
            holder.tvQuantity.setText(item.quantity + " " + item.unit);
            holder.tvExpiry.setText("Expiry: " + (item.expiry != null && !item.expiry.isEmpty() ? item.expiry : "N/A"));

            // Click to Edit item
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, AddEditIngredientActivity.class);
                intent.putExtra("ID", item.id);
                intent.putExtra("NAME", item.name);
                intent.putExtra("QTY", item.quantity);
                intent.putExtra("UNIT", item.unit);
                intent.putExtra("EXPIRY", item.expiry);
                startActivity(intent);
            });

            // Long click to Delete item with confirmation dialog
            holder.itemView.setOnLongClickListener(v -> {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Delete Ingredient")
                        .setMessage("Are you sure you want to delete " + item.name + "?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            boolean success = dbHelper.deletePantryItem(item.id);
                            if (success) {
                                Toast.makeText(MainActivity.this, "Deleted successfully", Toast.LENGTH_SHORT).show();
                                loadPantryData();
                            } else {
                                Toast.makeText(MainActivity.this, "Error deleting item", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
                return true;
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public class PantryViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvQuantity, tvExpiry;

            public PantryViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvIngredientName);
                tvQuantity = itemView.findViewById(R.id.tvIngredientQuantity);
                tvExpiry = itemView.findViewById(R.id.tvExpiryDate);
            }
        }
    }
}

