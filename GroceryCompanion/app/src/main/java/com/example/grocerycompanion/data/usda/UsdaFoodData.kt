package com.example.grocerycompanion.data.usda

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/*

below are the data models for the USDA Api search response
the below models use the kotlinx serialization to map only the fields which the
app requires when showing the nutrrional information

the models here are passed through repositories and view models to the UI

 */

@Serializable
data class UsdaSearchResponse(
    @SerialName("foods") val foods: List<FoodItem> = emptyList()
)

@Serializable
data class FoodItem( // single food item in the search result, with follwoing fields
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
