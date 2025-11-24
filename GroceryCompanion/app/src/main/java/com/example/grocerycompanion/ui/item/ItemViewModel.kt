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
        // 1) Load item meta from Firestore "items" collection
        val it = itemRepo.get(itemId) ?: run {
            _item.postValue(null)
            _storePrices.postValue(emptyList())
            return@launch
        }
        _item.postValue(it)

        // 2) Use BARCODE to look up all store prices from "products"
        val prices = priceRepo.latestPricesForBarcode(it.barcode)
        if (prices.isEmpty()) {
            _storePrices.postValue(emptyList())
            return@launch
        }

        val minPrice = prices.minByOrNull { p -> p.price }?.price

        // collect unique store ids
        val storeIds: Set<String> = prices.map { p -> p.storeId }.toSet()

        // 3) Load store metadata from FirebaseStoreRepo (Costco/Walmart/etc.)
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

        // 1. Prefer barcode, else fall back to the Firestore item doc id
        val rawKey = currentItem.barcode.ifBlank { currentItem.id }

        // 2. Make it Firestore-safe: no slashes
        val safeKey = rawKey.replace("/", "_")

        // 3. Use safeKey as the list item ID
        listRepo.add(safeKey, qty)
    }


    fun updatePrice(storeId: String, newPrice: Double) = viewModelScope.launch {
        // With your current Firestore shape (products = read-only scraped data),
        // you probably do NOT want users overwriting those docs.
        // If you later add a separate "userPrices" collection, you handle it here.
    }
}
