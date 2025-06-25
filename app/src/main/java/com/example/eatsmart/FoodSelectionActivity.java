package com.example.eatsmart;

import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;

public class FoodSelectionActivity extends AppCompatActivity {

    private SearchView searchFood;
    private RecyclerView foodRecyclerView;
    private Button confirmButton;
    private FoodAdapter foodAdapter;
    private DatabaseReference foodRef;
    private ArrayList<FoodItem> foodList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_selection);

        searchFood = findViewById(R.id.searchFood);
        foodRecyclerView = findViewById(R.id.foodRecyclerView);
        confirmButton = findViewById(R.id.confirmButton);

        foodRef = FirebaseDatabase.getInstance().getReference("food_data");

        foodRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        foodAdapter = new FoodAdapter(this, foodList, selectedFood -> {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(userId);

            // Save selected food to 'food_selected'
            userRef.child("food_selected").push().setValue(selectedFood);

            // Update nutritionTaken totals
            DatabaseReference nutritionRef = userRef.child("nutritionTaken");
            nutritionRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    int calories = snapshot.child("calories").getValue(Integer.class) != null ? snapshot.child("calories").getValue(Integer.class) : 0;
                    int fat = snapshot.child("fat").getValue(Integer.class) != null ? snapshot.child("fat").getValue(Integer.class) : 0;
                    int carbs = snapshot.child("carbs").getValue(Integer.class) != null ? snapshot.child("carbs").getValue(Integer.class) : 0;
                    int protein = snapshot.child("protein").getValue(Integer.class) != null ? snapshot.child("protein").getValue(Integer.class) : 0;
                    int satFat = snapshot.child("saturatedFat").getValue(Integer.class) != null ? snapshot.child("saturatedFat").getValue(Integer.class) : 0;

                    // Add food's nutrition values
                    calories += selectedFood.getCalories();
                    fat += selectedFood.getFat();
                    carbs += selectedFood.getCarbs();
                    protein += selectedFood.getProtein();
                    satFat += selectedFood.getSatFat();

                    // Save updated totals
                    nutritionRef.child("calories").setValue(calories);
                    nutritionRef.child("fat").setValue(fat);
                    nutritionRef.child("carbs").setValue(carbs);
                    nutritionRef.child("protein").setValue(protein);
                    nutritionRef.child("saturatedFat").setValue(satFat);

                    Toast.makeText(FoodSelectionActivity.this, selectedFood.getName() + " added & logged!", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(FoodSelectionActivity.this, "Error updating nutrition", Toast.LENGTH_SHORT).show();
                }
            });
        });



        foodRecyclerView.setAdapter(foodAdapter);

        loadFoodData();

        searchFood.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchFoodInFirebase(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchFoodInFirebase(newText);
                return true;
            }
        });

        confirmButton.setOnClickListener(v -> confirmSelection());
    }

    private void loadFoodData() {
        foodRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                foodList.clear();
                for (DataSnapshot foodSnapshot : snapshot.getChildren()) {
                    try {
                        String foodName = foodSnapshot.child("Food").getValue(String.class);
                        if (foodName != null) {
                            foodList.add(new FoodItem(
                                    foodName,
                                    getSafeInt(foodSnapshot, "Calories"),
                                    getSafeInt(foodSnapshot, "Fat"),
                                    getSafeInt(foodSnapshot, "Carbs"),
                                    getSafeInt(foodSnapshot, "Protein"),
                                    getSafeInt(foodSnapshot, "SatFat")
                            ));
                        }
                    } catch (Exception e) {
                        Log.e("FoodParse", "Error parsing food item: " + foodSnapshot.getKey(), e);
                    }
                }
                foodAdapter.updateData(foodList);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(FoodSelectionActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchFoodInFirebase(String query) {
        Query searchQuery = foodRef.orderByChild("food_lowercase")
                .startAt(query.toLowerCase())
                .endAt(query.toLowerCase() + "\uf8ff");

        searchQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                ArrayList<FoodItem> filteredList = new ArrayList<>();

                for (DataSnapshot foodSnapshot : snapshot.getChildren()) {
                    try {
                        String foodName = foodSnapshot.child("Food").getValue(String.class);
                        if (foodName != null) {
                            filteredList.add(new FoodItem(
                                    foodName,
                                    getSafeInt(foodSnapshot, "Calories"),
                                    getSafeInt(foodSnapshot, "Fat"),
                                    getSafeInt(foodSnapshot, "Carbs"),
                                    getSafeInt(foodSnapshot, "Protein"),
                                    getSafeInt(foodSnapshot, "SatFat")
                            ));
                        }
                    } catch (Exception e) {
                        Log.e("FoodSearch", "Error filtering food item: " + foodSnapshot.getKey(), e);
                    }
                }

                foodAdapter.updateData(filteredList);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(FoodSelectionActivity.this, "Search error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private int getSafeInt(DataSnapshot snapshot, String key) {
        try {
            Long value = snapshot.child(key).getValue(Long.class);
            return (value != null) ? value.intValue() : 0;
        } catch (Exception e) {
            Log.w("SafeParse", "Field " + key + " parse error, returning 0");
            return 0;
        }
    }

    private void confirmSelection() {
        Toast.makeText(this, "Selected food added!", Toast.LENGTH_SHORT).show();
        finish();
    }
}
