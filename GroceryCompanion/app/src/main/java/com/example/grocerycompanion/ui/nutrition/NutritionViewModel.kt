// NutritionViewModel.kt
package com.example.grocerycompanion.ui.nutrition

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grocerycompanion.data.usda.BASE_URL
import com.example.grocerycompanion.data.usda.FoodNutrient
import com.example.grocerycompanion.data.usda.UsdaApiService
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory

/*
  NutritionViewModel.kt

  ViewModel responsible for fetching nutrition information for a grocery item
  using the USDA FoodData Central API. The ViewModel:

   • Builds a lightweight Retrofit client on demand
   • Fetches the first match for a given item name
   • Extracts key nutrients (energy, protein, fat, carbs, sugars)
   • Separates other nutrients into a secondary list for UI display

  UI observes `nutritionData` to update automatically when results load.
 */

// UI-layer container for nutrients split into meaningful + extra groups

data class NutritionUiData(
    val mainNutrients: List<FoodNutrient> = emptyList(),
    val extraNutrients: List<FoodNutrient> = emptyList()
)

class NutritionViewModel : ViewModel() {

    private val _nutritionData = mutableStateOf(NutritionUiData())
    val nutritionData: State<NutritionUiData> = _nutritionData

    fun fetchNutritionForGroceryItem(itemName: String) {
        viewModelScope.launch {
            try {
                // Lenient JSON parser so extra fields from USDA don't break deserialization

                val json = Json { ignoreUnknownKeys = true }
                // Create Retrofit instance specifically for this request

                val retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                    .build()

                val service = retrofit.create(UsdaApiService::class.java)
                // Query USDA by item name

                val response = service.searchFoods(query = itemName)
                val firstFood = response.foods.firstOrNull()

                if (firstFood != null) {
                    val nutrients = firstFood.foodNutrients

                    fun pick(nameContains: String): FoodNutrient? =
                        nutrients.firstOrNull {
                            it.nutrientName.contains(nameContains, ignoreCase = true)
                        }
                    // Extract key nutrients based on common USDA naming variations

                    val energy  = pick("Energy")
                    val protein = pick("Protein")
                    val fat     = pick("Total lipid") ?: pick("Fat")
                    val carbs   = pick("Carbohydrate")
                    val sugar   = pick("Sugars") // sugar content

                    val main: List<FoodNutrient> = listOfNotNull(
                        energy,
                        protein,
                        fat,
                        carbs,
                        sugar
                    )
                    // Extras = all nutrients except the ones already in main
                    // Matches by nutrientId when available (avoids false collisions)
                    val extras = nutrients
                        .filter { n -> main.none { it.nutrientId != null && it.nutrientId == n.nutrientId } }
                        .sortedBy { it.nutrientName }
                    // Update UI state

                    _nutritionData.value = NutritionUiData(
                        mainNutrients = main,
                        extraNutrients = extras
                    )
                } else {
                    _nutritionData.value = NutritionUiData()
                }
            } catch (e: Exception) {
                // On error, just clear data
                _nutritionData.value = NutritionUiData()
            }
        }
    }
}


