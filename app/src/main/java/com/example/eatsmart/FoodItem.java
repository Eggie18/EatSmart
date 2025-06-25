package com.example.eatsmart;



public class FoodItem {
    private String name;
    private double calories;
    private double fat;
    private double carbs;
    private double protein;
    private double satFat;

    public FoodItem(String name, double calories, double fat, double carbs, double protein, double satFat) {
        this.name = name;
        this.calories = calories;
        this.fat = fat;
        this.carbs = carbs;
        this.protein = protein;
        this.satFat = satFat;
    }

    public String getName() { return name; }
    public double getCalories() { return calories; }
    public double getFat() { return fat; }
    public double getCarbs() { return carbs; }
    public double getProtein() { return protein; }
    public double getSatFat() { return satFat; }
}
