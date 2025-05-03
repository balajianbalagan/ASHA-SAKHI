package com.littleb01s.ashasakhichat.data.model

data class DietPlan(
    val diet_plan: Map<String, DayMeals>,
    val region: String
)

data class DayMeals(
    val breakfast: String,
    val morning_snack: String,
    val lunch: String,
    val evening_snack: String,
    val dinner: String
) 