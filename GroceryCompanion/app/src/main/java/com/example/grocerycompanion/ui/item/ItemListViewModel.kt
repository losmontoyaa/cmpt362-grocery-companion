package com.example.grocerycompanion.ui.item

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grocerycompanion.model.Item
import com.example.grocerycompanion.repo.FirebaseItemRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class ItemListViewModel(
    private val itemRepo: FirebaseItemRepo
) : ViewModel() {

    private val _items = MutableStateFlow<List<Item>>(emptyList())
    val items: StateFlow<List<Item>> = _items

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        startCollecting()
    }

    private fun startCollecting() {
        viewModelScope.launch {
            itemRepo.streamAll()
                .onStart {
                    _isLoading.value = true
                    _errorMessage.value = null
                }
                .catch { e ->
                    _isLoading.value = false
                    _errorMessage.value = e.message ?: "Failed to load items."
                }
                .collect { list ->
                    _items.value = list
                    _isLoading.value = false
                }
        }
    }

    fun retry() {
        startCollecting()
    }

    fun addItem(
        name: String,
        brand: String,
        barcode: String,
        category: String,
        storeName: String,
        latitude: Double?,
        longitude: Double?
    ) = viewModelScope.launch {
        try {
            val id = "item-${System.currentTimeMillis()}"

            val item = Item(
                id = id,
                name = name,
                brand = brand,
                barcode = barcode,
                imgUrl = "https://picsum.photos/600/400?random=$id",
                category = category,
                avgRating = 0.0,
                ratingsCount = 0
            )

            itemRepo.add(
                item = item,
                storeName = storeName,
                latitude = latitude,
                longitude = longitude
            )
        } catch (e: Exception) {
            _errorMessage.value = e.message ?: "Failed to add item."
        }
    }
}
