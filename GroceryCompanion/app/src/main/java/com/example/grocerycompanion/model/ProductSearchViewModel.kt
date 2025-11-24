package com.example.grocerycompanion.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grocerycompanion.repo.ProductRepository
import com.example.grocerycompanion.util.sortProductsByDistanceKm
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProductSearchViewModel(
    private val repository: ProductRepository = ProductRepository()
) : ViewModel() {
    private val _products = MutableStateFlow<List<Pair<Product, Double>>>(emptyList())
    val products: StateFlow<List<Pair<Product, Double>>> = _products

    fun searchProducts(productName: String, userLat: Double, userLng: Double) {
        viewModelScope.launch {
            val fetched = repository.getProductByName(productName)
            _products.value = sortProductsByDistanceKm(fetched, userLat, userLng)
        }
    }
}