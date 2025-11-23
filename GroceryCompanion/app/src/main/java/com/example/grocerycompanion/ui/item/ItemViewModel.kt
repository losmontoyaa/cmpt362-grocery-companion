package com.example.grocerycompanion.ui.item

import androidx.lifecycle.*
import com.example.grocerycompanion.repo.FirebaseShoppingListRepo
import com.example.grocerycompanion.repo.FirebaseItemRepo
import com.example.grocerycompanion.repo.FirebasePriceRepo
import com.example.grocerycompanion.model.Item
import com.example.grocerycompanion.model.Price
import com.example.grocerycompanion.model.Store
import com.example.grocerycompanion.repo.FirebaseStoreRepo
import kotlinx.coroutines.launch

class ItemViewModel(
    private val itemId: String,
    private val itemRepo: FirebaseItemRepo,
    private val priceRepo: FirebasePriceRepo,     // we’ll define this next
    private val storeRepo: FirebaseStoreRepo,
    private val listRepo: FirebaseShoppingListRepo
) : ViewModel() {

    private val _item = MutableLiveData<Item?>()
    val item: LiveData<Item?> = _item

    private val _storePrices = MutableLiveData<List<Triple<Price, Store, Boolean>>>()
    val storePrices: LiveData<List<Triple<Price, Store, Boolean>>> = _storePrices


    fun load() = viewModelScope.launch {
        val it = itemRepo.get(itemId)
        _item.postValue(it)

        val prices = priceRepo.latestPricesByStore(itemId)
        val minPrice = prices.minByOrNull { it.price }?.price
        val stores = storeRepo.getStores(prices.map { p -> p.storeId }.toSet())
        _storePrices.postValue(
            prices.mapNotNull { p ->
                val s = stores[p.storeId] ?: return@mapNotNull null
                val isCheapest = (minPrice != null && p.price == minPrice)
                Triple(p, s, isCheapest)
            }
        )
    }

    fun addToList(qty: Int) = viewModelScope.launch {
        listRepo.add(itemId, qty)
    }

    fun updatePrice(storeId: String, newPrice: Double) = viewModelScope.launch {
        val currentPair = _storePrices.value?.firstOrNull { it.second.id == storeId }
        val unit = currentPair?.first?.unit ?: ""
        priceRepo.upsertPrice(
            itemId = itemId,
            storeId = storeId,
            price = newPrice,
            unit = unit
        )
        // refresh prices
        load()
    }

}
