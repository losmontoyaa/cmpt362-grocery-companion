package com.example.grocerycompanion.ui.item

import androidx.lifecycle.*
import com.example.grocerycompanion.model.Item
import com.example.grocerycompanion.model.Price
import com.example.grocerycompanion.model.Store
import com.example.grocerycompanion.repo.ItemRepo
import com.example.grocerycompanion.repo.PriceRepo
import com.example.grocerycompanion.repo.ShoppingListRepo
import com.example.grocerycompanion.repo.StoreRepo
import kotlinx.coroutines.launch

class ItemViewModel(
    private val itemId: String,
    private val itemRepo: ItemRepo,
    private val priceRepo: PriceRepo,
    private val storeRepo: StoreRepo,
    private val listRepo: ShoppingListRepo
) : ViewModel() {

    private val _item = MutableLiveData<Item?>()
    val item: LiveData<Item?> = _item

    private val _storePrices = MutableLiveData<List<Pair<Price, Store>>>()
    val storePrices: LiveData<List<Pair<Price, Store>>> = _storePrices

    fun load() = viewModelScope.launch {
        val it = itemRepo.get(itemId)
        _item.postValue(it)

        val prices = priceRepo.latestPricesByStore(itemId)
        val stores = storeRepo.getStores(prices.map { p -> p.storeId }.toSet())
        _storePrices.postValue(
            prices.mapNotNull { p -> stores[p.storeId]?.let { s -> p to s } }
        )
    }

    fun addToList(qty: Int) = viewModelScope.launch {
        listRepo.add(itemId, qty)
    }
}
