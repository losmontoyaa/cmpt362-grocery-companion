package com.example.grocerycompanion.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grocerycompanion.repo.ProductRepository
import com.example.grocerycompanion.util.sortProductsByDistanceKm
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/*
ViewModel to search database for products based on name + brand. Orders the results by distance from the user.
Fetches the list of products using the ProductRepository and holds them in a list that contains pairs: (Product, distance)
 */
class ProductSearchViewModel(
    private val repository: ProductRepository = ProductRepository()
) : ViewModel() {
    private val _products = MutableStateFlow<List<Pair<Product, Double>>>(emptyList())
    val products: StateFlow<List<Pair<Product, Double>>> = _products

    // Search the database for products that match the product name and brand. Sorted by distance from the user to the product's store location
    fun searchProducts(productName: String, brand: String, userLat: Double, userLng: Double) {
        viewModelScope.launch {
            val fetched = repository.getProductByNameAndBrand(productName, brand)
            _products.value = sortProductsByDistanceKm(fetched, userLat, userLng)
        }
    }
}