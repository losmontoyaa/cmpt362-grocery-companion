package com.example.grocerycompanion.ui.item

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grocerycompanion.model.Item
import com.example.grocerycompanion.repo.FirebaseItemRepo
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ItemListViewModel(
    private val itemRepo: FirebaseItemRepo
) : ViewModel() {

    // Live list from Firestore
    val items: StateFlow<List<Item>> =
        itemRepo.streamAll()
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun addItem(
        name: String,
        brand: String,
        barcode: String,
        category: String
    ) = viewModelScope.launch {
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

        itemRepo.add(item)
        // No need to manually refresh – snapshotListener pushes updates
    }
}
