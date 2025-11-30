package com.example.grocerycompanion.data.usda

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UsdaSearchResponse(
    val foods: List<FoodItem> = emptyList()
)

@Serializable
data class FoodItem(
    val description: String,
    val fdcId: Int,
    val foodNutrients: List<FoodNutrient> = emptyList()
)

@Serializable
data class FoodNutrient(
    @SerialName("nutrientId") val id: Int? = null,
    @SerialName("nutrientName") val name: String,
    @SerialName("nutrientNumber") val number: String? = null,
    val unitName: String,
    @SerialName("value") val amount: Float
)
