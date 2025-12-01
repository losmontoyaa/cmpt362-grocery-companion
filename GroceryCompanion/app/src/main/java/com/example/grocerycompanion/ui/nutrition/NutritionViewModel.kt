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
//import retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory


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
                val json = Json { ignoreUnknownKeys = true }

                val retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                    .build()

                val service = retrofit.create(UsdaApiService::class.java)
                val response = service.searchFoods(query = itemName)
                val firstFood = response.foods.firstOrNull()

                if (firstFood != null) {
                    val nutrients = firstFood.foodNutrients

                    fun pick(nameContains: String): FoodNutrient? =
                        nutrients.firstOrNull {
                            it.nutrientName.contains(nameContains, ignoreCase = true)
                        }

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

                    val extras = nutrients
                        .filter { n -> main.none { it.nutrientId != null && it.nutrientId == n.nutrientId } }
                        .sortedBy { it.nutrientName }

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


