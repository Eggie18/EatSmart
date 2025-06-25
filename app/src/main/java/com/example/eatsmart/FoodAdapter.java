package com.example.eatsmart;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import com.google.firebase.database.*;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.ViewHolder> {
    private Context context;
    private List<FoodItem> foodList;

    public FoodAdapter(Context context, List<FoodItem> foodList) {
        this.context = context;
        this.foodList = foodList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.food_item_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FoodItem food = foodList.get(position);
        holder.foodName.setText(food.getName());
        holder.foodCalories.setText(food.getCalories() + " kcal");
        holder.foodCarbs.setText("Carbs: "+food.getCarbs()+"");
        holder.getFat.setText("Fat: "+food.getFat());
        holder.getProtein.setText("Protein: "+food.getProtein());
        holder.addFoodButton.setOnClickListener(v -> {
            if (listener != null) {
                String gramsInput = holder.grams.getText().toString().trim();
                if (!gramsInput.isEmpty()) {
                    try {
                        double grams = Double.parseDouble(gramsInput);
                        FoodItem originalFood = foodList.get(holder.getAdapterPosition());

                        double scale = grams / 100.0;
                        FoodItem scaledFood = new FoodItem(
                                originalFood.getName(),
                                originalFood.getCalories() * scale,
                                originalFood.getCarbs() * scale,
                                originalFood.getFat() * scale,
                                originalFood.getProtein() * scale,
                                originalFood.getSatFat()*scale
                        );

                        listener.onAddClicked(scaledFood);
                    } catch (NumberFormatException e) {
                        Toast.makeText(context, "Invalid grams input", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "Please enter grams", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    @Override
    public int getItemCount() {
        return foodList.size();
    }

    public void updateData(List<FoodItem> newFoodList) {
        foodList.clear();
        foodList.addAll(newFoodList);
        notifyDataSetChanged();
    }

    public interface OnFoodAddListener {
        void onAddClicked(FoodItem item);
    }

    private OnFoodAddListener listener;

    public FoodAdapter(Context context, List<FoodItem> foodList, OnFoodAddListener listener) {
        this.context = context;
        this.foodList = foodList;
        this.listener = listener;
    }

    private DatabaseReference selectedFoodsRef;

    public FoodAdapter(Context context, List<FoodItem> foodList, String userId) {
        this.context = context;
        this.foodList = foodList;
        this.selectedFoodsRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("selected_foods");
    }




    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView foodName, foodCalories,foodCarbs,getFat,getProtein;
        Button addFoodButton;

        EditText grams;

        public ViewHolder(View view) {
            super(view);
            foodName = view.findViewById(R.id.foodName);
            foodCalories = view.findViewById(R.id.foodCalories);
            addFoodButton = view.findViewById(R.id.addFoodButton);
            foodCarbs = view.findViewById(R.id.foodCarbs);
            getFat = view.findViewById(R.id.foodFat);
            getProtein = view.findViewById(R.id.foodProtein);
            grams = view.findViewById(R.id.grams);
        }
    }
}
