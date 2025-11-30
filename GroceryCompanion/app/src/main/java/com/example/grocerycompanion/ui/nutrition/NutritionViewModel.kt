package com.example.grocerycompanion.ui.nutrition

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grocerycompanion.data.usda.FoodNutrient
import com.example.grocerycompanion.data.usda.UsdaApi
import kotlinx.coroutines.launch

class NutritionViewModel : ViewModel() {

    private val _nutritionData = mutableStateOf<List<FoodNutrient>>(emptyList())
    val nutritionData: State<List<FoodNutrient>> = _nutritionData

    fun fetchNutritionForGroceryItem(itemName: String) {
        viewModelScope.launch {
            try {
                val response = UsdaApi.service.searchFoods(query = itemName)
                val firstFood = response.foods.firstOrNull()

                _nutritionData.value = if (firstFood != null) {
                    firstFood.foodNutrients.filter { n ->
                        n.id in listOf(203, 204, 205, 208) ||        // protein, fat, carbs, energy
                                n.name.contains("Protein", true) ||
                                n.name.contains("Total lipid", true) ||
                                n.name.contains("Carbohydrate", true) ||
                                n.name.contains("Energy", true)
                    }
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                println("USDA API error: ${e.message}")
                _nutritionData.value = emptyList()
            }
        }
    }
}
