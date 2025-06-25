package com.example.eatsmart;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class ResetReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = context.getSharedPreferences("EatSmartPrefs", Context.MODE_PRIVATE);
        String userId = prefs.getString("userId", null);


        if (userId != null) {
            DatabaseReference nutritionRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(userId)
                    .child("nutritionTaken");

            // Reset values manually to 0
            nutritionRef.child("calories").setValue(0);
            nutritionRef.child("fat").setValue(0);
            nutritionRef.child("carbs").setValue(0);
            nutritionRef.child("protein").setValue(0);
            nutritionRef.child("saturatedFat").setValue(0);

            Toast.makeText(context, "Nutrition totals reset for the day!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "User ID not found. Reset skipped.", Toast.LENGTH_SHORT).show();
        }

    }
    }

