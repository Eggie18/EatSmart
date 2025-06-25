package com.example.eatsmart;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class WeightInputActivity extends AppCompatActivity {

    private EditText weightInput;
    private Button saveWeightButton;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weight_input);

        weightInput = findViewById(R.id.weightInput);
        saveWeightButton = findViewById(R.id.saveWeightButton);

        userRef = FirebaseDatabase.getInstance().getReference("users");

        saveWeightButton.setOnClickListener(view -> {
            String weightText = weightInput.getText().toString();

            if (!weightText.isEmpty()) {
                float weight = Float.parseFloat(weightText);
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                // Calculate Nutrition Values
                float caloriesGoal = calculateCalories(weight);
                float proteinGoal = calculateProtein(weight);
                float fatGoal = calculateFat(weight);
                float saturatedFatGoal = calculateSaturatedFat(weight);

                // Store Data in Firebase under `nutritionGoal`
                Map<String, Object> userData = new HashMap<>();
                userData.put("weight", weight);

                Map<String, Object> nutritionGoal = new HashMap<>();
                nutritionGoal.put("calories", caloriesGoal);
                nutritionGoal.put("protein", proteinGoal);
                nutritionGoal.put("fat", fatGoal);
                nutritionGoal.put("saturatedFat", saturatedFatGoal);

                userData.put("nutritionGoal", nutritionGoal);

                // Initialize `nutritionTaken` with zero values
                Map<String, Object> nutritionTaken = new HashMap<>();
                nutritionTaken.put("calories", 0f);
                nutritionTaken.put("protein", 0f);
                nutritionTaken.put("fat", 0f);
                nutritionTaken.put("saturatedFat", 0f);

                userData.put("nutritionTaken", nutritionTaken);

                userRef.child(userId).updateChildren(userData).addOnSuccessListener(aVoid -> {
                    Toast.makeText(WeightInputActivity.this, "Data saved!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(WeightInputActivity.this, Home.class));
                    finish();
                }).addOnFailureListener(e -> {
                    Toast.makeText(WeightInputActivity.this, "Error saving data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            } else {
                Toast.makeText(WeightInputActivity.this, "Please enter a valid weight", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Example Methods for Calculations
    private float calculateCalories(float weight) {
        return weight * 30; // Rough estimation for daily calorie intake
    }

    private float calculateProtein(float weight) {
        return weight * 0.8f; // Protein intake estimate (grams per kg)
    }

    private float calculateFat(float weight) {
        return weight * 0.3f; // Fat intake estimate
    }

    private float calculateSaturatedFat(float weight) {
        return calculateFat(weight) * 0.3f; // 30% of fat intake is saturated fat
    }
}
