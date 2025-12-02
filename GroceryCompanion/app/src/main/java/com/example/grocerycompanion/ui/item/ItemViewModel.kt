package com.example.grocerycompanion.ui.item

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grocerycompanion.model.Item
import com.example.grocerycompanion.model.Price
import com.example.grocerycompanion.model.Store
import com.example.grocerycompanion.repo.FirebaseItemRepo
import com.example.grocerycompanion.repo.FirebasePriceRepo
import com.example.grocerycompanion.repo.FirebaseShoppingListRepo
import com.example.grocerycompanion.repo.FirebaseStoreRepo
import kotlinx.coroutines.launch

class ItemViewModel(
    private val itemId: String,
    private val itemRepo: FirebaseItemRepo,
    private val priceRepo: FirebasePriceRepo,
    private val storeRepo: FirebaseStoreRepo,
    private val listRepo: FirebaseShoppingListRepo
) : ViewModel() {

    private val _item = MutableLiveData<Item?>()
    val item: LiveData<Item?> = _item

    // Triple<price, store, isCheapest>
    private val _storePrices = MutableLiveData<List<Triple<Price, Store, Boolean>>>()
    val storePrices: LiveData<List<Triple<Price, Store, Boolean>>> = _storePrices

    fun load() = viewModelScope.launch {
        // 1) Load item meta from Firestore "products"/"items"
        val it = itemRepo.get(itemId) ?: run {
            _item.postValue(null)
            _storePrices.postValue(emptyList())
            return@launch
        }
        _item.postValue(it)

        // 2) Use PRODUCT DOCUMENT ID to look up price
        val prices = priceRepo.pricesForItemId(it.id)
        if (prices.isEmpty()) {
            _storePrices.postValue(emptyList())
            return@launch
        }

        val minPrice = prices.minByOrNull { p -> p.price }?.price

        // collect unique store ids
        val storeIds: Set<String> = prices.map { p -> p.storeId }.toSet()

        // 3) Load store metadata from FirebaseStoreRepo
        val stores: Map<String, Store> = storeRepo.getStores(storeIds)

        // 4) Build UI triples
        val triples = prices.mapNotNull { p ->
            val s = stores[p.storeId] ?: return@mapNotNull null
            val isCheapest = (minPrice != null && p.price == minPrice)
            Triple(p, s, isCheapest)
        }

        _storePrices.postValue(triples)
    }

    fun addToList(qty: Int) = viewModelScope.launch {
        val currentItem = _item.value ?: return@launch

        val rawKey = currentItem.id

        // 2. Make it Firestore-safe: no slashes
        val safeKey = rawKey.replace("/", "_")

        // 3. Use safeKey as the list item ID
        listRepo.add(safeKey, qty)
    }

    // Update the price of the item to the database
    fun updatePrice(newPrice: Double) = viewModelScope.launch {
        val currentItem = _item.value ?: return@launch

        val priceEntry = _storePrices.value
            ?.firstOrNull { it.first.itemId == currentItem.id }
            ?.first

        if (priceEntry != null) {
            priceRepo.updatePrice(priceEntry.itemId, newPrice = newPrice)

            load()
        }
    }
}
