package com.example.eatsmart;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Home extends AppCompatActivity {

    private PieChart pieChart;
    private ProgressBar progressCalories, progressProtein, progressFat, progressSaturatedFat;
    private TextView caloriesGoalText, proteinGoalText, fatGoalText, saturatedFatGoalText, calPercent, proPercent, fPercent, sFatPercent;
    private Button refreshDataButton, addFoodButton, Pfp;
    private DatabaseReference userRef;

    private String[] labels = {"Calories", "Protein", "Fat", "Saturated Fat"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        // UI References
        pieChart = findViewById(R.id.pieChart);
        progressCalories = findViewById(R.id.progressCalories);
        progressProtein = findViewById(R.id.progressProtein);
        progressFat = findViewById(R.id.progressFat);
        progressSaturatedFat = findViewById(R.id.progressSaturatedFat);

        caloriesGoalText = findViewById(R.id.caloriesGoal);
        proteinGoalText = findViewById(R.id.proteinGoal);
        fatGoalText = findViewById(R.id.fatGoal);
        saturatedFatGoalText = findViewById(R.id.saturatedFatGoal);

        refreshDataButton = findViewById(R.id.refreshDataButton);
        addFoodButton = findViewById(R.id.addFoodButton);
        Pfp = findViewById(R.id.Pfp);

        sFatPercent=findViewById(R.id.sFatPercent);
        calPercent = findViewById(R.id.calPercent);
        proPercent = findViewById(R.id.proPercent);
        fPercent = findViewById(R.id.fPercent);



        // Firebase Reference
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

        setupPieChart();
        fetchFirebaseData(); // Load intake & goals on startup

        refreshDataButton.setOnClickListener(v -> fetchFirebaseData()); // Manual refresh
        addFoodButton.setOnClickListener(v -> openFoodSelectionScreen()); // Food selection screen
        Pfp.setOnClickListener(v -> openProfile());
    }

    private void setupPieChart() {
        pieChart.getDescription().setEnabled(false);
    }

    private void fetchFirebaseData() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Fetch nutrition goals
                    Float caloriesGoal = snapshot.child("nutritionGoal/calories").getValue(Float.class);
                    Float proteinGoal = snapshot.child("nutritionGoal/protein").getValue(Float.class);
                    Float fatGoal = snapshot.child("nutritionGoal/fat").getValue(Float.class);
                    Float saturatedFatGoal = snapshot.child("nutritionGoal/saturatedFat").getValue(Float.class);

                    // Fetch actual intake values
                    Float caloriesTaken = snapshot.child("nutritionTaken/calories").getValue(Float.class);
                    Float proteinTaken = snapshot.child("nutritionTaken/protein").getValue(Float.class);
                    Float fatTaken = snapshot.child("nutritionTaken/fat").getValue(Float.class);
                    Float saturatedFatTaken = snapshot.child("nutritionTaken/saturatedFat").getValue(Float.class);

                    // Ensure non-null values
                    caloriesGoal = (caloriesGoal != null) ? caloriesGoal : 0f;
                    proteinGoal = (proteinGoal != null) ? proteinGoal : 0f;
                    fatGoal = (fatGoal != null) ? fatGoal : 0f;
                    saturatedFatGoal = (saturatedFatGoal != null) ? saturatedFatGoal : 0f;

                    caloriesTaken = (caloriesTaken != null) ? caloriesTaken : 0f;
                    proteinTaken = (proteinTaken != null) ? proteinTaken : 0f;
                    fatTaken = (fatTaken != null) ? fatTaken : 0f;
                    saturatedFatTaken = (saturatedFatTaken != null) ? saturatedFatTaken : 0f;

                    updatePieChart(caloriesTaken, proteinTaken, fatTaken, saturatedFatTaken);
                    updateProgressBars(caloriesTaken, proteinTaken, fatTaken, saturatedFatTaken,
                            caloriesGoal, proteinGoal, fatGoal, saturatedFatGoal);

                    // Update text views to show goals
                    caloriesGoalText.setText("Goal: " + String.format("%.1f", caloriesGoal) + " kcal");
                    proteinGoalText.setText("Goal: " + String.format("%.1f", proteinGoal) + "g");
                    fatGoalText.setText("Goal: " + String.format("%.1f", fatGoal) + "g");
                    saturatedFatGoalText.setText("Goal: " + String.format("%.1f", saturatedFatGoal) + "g");

                } else {
                    Toast.makeText(Home.this, "No data found in Firebase!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(Home.this, "Error fetching data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updatePieChart(float caloriesTaken, float proteinTaken, float fatTaken, float saturatedFatTaken) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(caloriesTaken, labels[0]));
        entries.add(new PieEntry(proteinTaken, labels[1]));
        entries.add(new PieEntry(fatTaken, labels[2]));
        entries.add(new PieEntry(saturatedFatTaken, labels[3]));

        PieDataSet dataSet = new PieDataSet(entries, "Current Intake");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        PieData data = new PieData(dataSet);

        pieChart.setData(data);
        pieChart.invalidate(); // Refresh chart
    }

    private void updateProgressBars(float caloriesTaken, float proteinTaken, float fatTaken, float saturatedFatTaken,
                                    float caloriesGoal, float proteinGoal, float fatGoal, float saturatedFatGoal) {

        // Set all progress bars to 0–100 (percentage scale)
        progressCalories.setMax(100);
        progressProtein.setMax(100);
        progressFat.setMax(100);
        progressSaturatedFat.setMax(100);

        // Convert nutrition values to percentages
        int caloriesPercent = (int) ((caloriesGoal > 0) ? (caloriesTaken / caloriesGoal) * 100f : 0);
        int proteinPercent = (int) ((proteinGoal > 0) ? (proteinTaken / proteinGoal) * 100f : 0);
        int fatPercent = (int) ((fatGoal > 0) ? (fatTaken / fatGoal) * 100f : 0);
        int satFatPercent = (int) ((saturatedFatGoal > 0) ? (saturatedFatTaken / saturatedFatGoal) * 100f : 0);

        // Animate the progress filling left to right
        animateProgress(progressCalories, caloriesPercent);
        animateProgress(progressProtein, proteinPercent);
        animateProgress(progressFat, fatPercent);
        animateProgress(progressSaturatedFat, satFatPercent);

        if (caloriesPercent >= 100) {
            calPercent.setText("Goal Reached");
        } else {
            calPercent.setText(caloriesPercent + "%");
        }

        if (satFatPercent >= 100) {
            sFatPercent.setText("Goal Reached");
        } else {
            sFatPercent.setText(satFatPercent + "%");
        }

        if (proteinPercent >= 100) {
            proPercent.setText("Goal Reached");
        } else {
            proPercent.setText(proteinPercent + "%");
        }

        if (fatPercent >= 100) {
            fPercent.setText("Goal Reached");
        } else {
            fPercent.setText(fatPercent + "%");
        }





    }





    private void animateProgress(ProgressBar bar, int targetPercent) {
        ObjectAnimator animator = ObjectAnimator.ofInt(bar, "progress", 0, targetPercent);
        animator.setDuration(1000);
        animator.start();
    }



    private void openFoodSelectionScreen() {
        Intent intent = new Intent(Home.this, FoodSelectionActivity.class);
        startActivity(intent);
    }

    private void openProfile(){
        Intent intent = new Intent(Home.this, Profile.class );
        startActivity(intent);
    }
}
