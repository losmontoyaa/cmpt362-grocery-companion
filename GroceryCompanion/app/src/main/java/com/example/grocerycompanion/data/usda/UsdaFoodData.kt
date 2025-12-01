package com.example.grocerycompanion.data.usda

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UsdaSearchResponse(
    @SerialName("foods") val foods: List<FoodItem> = emptyList()
)

@Serializable
data class FoodItem(
    @SerialName("description") val description: String,
    @SerialName("fdcId") val fdcId: Int,
    @SerialName("foodNutrients") val foodNutrients: List<FoodNutrient> = emptyList()
)

@Serializable
data class FoodNutrient(
    @SerialName("nutrientName") val nutrientName: String,
    @SerialName("unitName") val unitName: String,          // "G", "KCAL", etc.
    @SerialName("value") val amount: Float,                // numeric amount
    @SerialName("nutrientId") val nutrientId: Int? = null  // may be null in some records
)
