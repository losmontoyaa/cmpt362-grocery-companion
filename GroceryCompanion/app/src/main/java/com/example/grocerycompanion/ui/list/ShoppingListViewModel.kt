package com.example.grocerycompanion.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grocerycompanion.model.Item
import com.example.grocerycompanion.model.Price
import com.example.grocerycompanion.model.ShoppingListItem
import com.example.grocerycompanion.repo.FirebaseItemRepo
import com.example.grocerycompanion.repo.FirebasePriceRepo
import com.example.grocerycompanion.repo.FirebaseShoppingListRepo
import com.example.grocerycompanion.repo.FirebaseStoreRepo
import com.example.grocerycompanion.util.haversineKm
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

data class ItemPickUi(
    val storeName: String,
    val price: Double
)

data class StoreTotal(
    val storeId: String,
    val storeName: String,
    val total: Double,
    val distanceKm: Double?
)

class ShoppingListViewModel(
    private val listRepo: FirebaseShoppingListRepo,
    private val priceRepo: FirebasePriceRepo,
    private val storeRepo: FirebaseStoreRepo,
    private val itemRepo: FirebaseItemRepo
) : ViewModel() {

    private val _list = MutableStateFlow<List<ShoppingListItem>>(emptyList())
    val list: StateFlow<List<ShoppingListItem>> = _list

    // productId (e.g. "ss17") -> Item (from products collection)
    private val _productsById = MutableStateFlow<Map<String, Item>>(emptyMap())
    val productsById: StateFlow<Map<String, Item>> = _productsById

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        startCollecting()
    }

    private fun startCollecting() {
        viewModelScope.launch {
            listRepo.streamList()
                .onStart {
                    _isLoading.value = true
                    _errorMessage.value = null
                }
                .catch { e ->
                    _isLoading.value = false
                    _errorMessage.value = e.message ?: "Failed to load shopping list."
                }
                .collect { items ->
                    _list.value = items
                    _isLoading.value = false

                    // Whenever the list changes, refresh product details for those IDs
                    launch {
                        loadProductsForList(items)
                    }
                }
        }
    }

    private suspend fun loadProductsForList(items: List<ShoppingListItem>) {
        try {
            val ids = items.map { it.itemId }.toSet()
            val map = mutableMapOf<String, Item>()
            for (id in ids) {
                val product = itemRepo.get(id)
                if (product != null) {
                    map[id] = product
                }
            }
            _productsById.value = map
        } catch (e: Exception) {
            // Silent fail – list still works, just shows IDs if lookup fails
        }
    }

    fun retry() {
        startCollecting()
    }

    /*
     * Compute total per store using cheapest price per item.
     * itemId is the PRODUCT DOCUMENT ID (e.g. "ss17").
     */
    suspend fun computePerStoreTotals(
        userLat: Double? = null,
        userLng: Double? = null
    ): List<StoreTotal> {
        val items = list.value
        if (items.isEmpty()) return emptyList()

        // Each ShoppingListItem.itemId is your product doc ID (e.g. "ss17")
        val itemIds = items.map { it.itemId }.toSet()

        // 1) Load all prices for each product ID
        val perItemPrices = mutableMapOf<String, List<Price>>()
        for (id in itemIds) {
            perItemPrices[id] = priceRepo.pricesForItemId(id)
        }

        // 2) Find all stores that appear anywhere
        val allStoreIds = perItemPrices.values
            .flatten()
            .map { it.storeId }
            .toSet()

        // 3) Keep only stores that have a price for EVERY item in the list
        var candidateStores = allStoreIds.toMutableSet()
        for (id in itemIds) {
            val storesForItem = perItemPrices[id]
                .orEmpty()
                .map { it.storeId }
                .toSet()

            candidateStores.retainAll(storesForItem)
        }

        // If no store covers the full list, there is no meaningful
        // "Best overall store" → return empty so UI hides the hero.
        if (candidateStores.isEmpty()) {
            return emptyList()
        }

        // 4) Load store metadata only for candidate stores
        val stores = storeRepo.getStores(candidateStores)

        // 5) Compute total for each candidate store
        val totals = mutableListOf<StoreTotal>()

        for (sid in candidateStores) {
            var total = 0.0
            var missingPrice = false

            for (sli in items) {
                // price for *this specific item* at this store
                val priceForThisStore = perItemPrices[sli.itemId]
                    ?.filter { it.storeId == sid }
                    ?.minByOrNull { it.price }

                if (priceForThisStore == null) {
                    // should not happen because we filtered candidateStores,
                    // but be safe and drop this store if any price is missing
                    missingPrice = true
                    break
                } else {
                    total += priceForThisStore.price * sli.qty
                }
            }

            if (!missingPrice) {
                val s = stores[sid]
                val dist = if (s != null && userLat != null && userLng != null) {
                    haversineKm(userLat, userLng, s.lat, s.lng)
                } else null

                totals += StoreTotal(
                    storeId = sid,
                    storeName = s?.name ?: sid,
                    total = total,
                    distanceKm = dist
                )
            }
        }

        // 6) Sort cheapest total first, then by distance
        return totals.sortedWith(
            compareBy<StoreTotal> { it.total }
                .thenBy { it.distanceKm ?: Double.MAX_VALUE }
        )
    }
    /*
     * For each item (product ID), pick the cheapest store and convert it into a UI object.
     */
    suspend fun computePerItemCheapestUi(): Map<String, ItemPickUi> {
        val items = list.value
        val ids = items.map { it.itemId }.toSet()

        val perItemPrices = mutableMapOf<String, List<Price>>()
        for (id in ids) {
            perItemPrices[id] = priceRepo.pricesForItemId(id)
        }

        val storeIds = perItemPrices.values
            .flatten()
            .map { it.storeId }
            .toSet()
        val stores = storeRepo.getStores(storeIds)

        val picks = mutableMapOf<String, ItemPickUi>()
        ids.forEach { itemId ->
            val prices = perItemPrices[itemId].orEmpty()
            val cheapest = prices.minByOrNull { it.price } ?: return@forEach
            val s = stores[cheapest.storeId]
            picks[itemId] = ItemPickUi(
                storeName = s?.name ?: cheapest.storeId,
                price = cheapest.price
            )
        }
        return picks
    }

    suspend fun remove(itemId: String) = listRepo.remove(itemId)

    suspend fun setQty(itemId: String, qty: Int) = listRepo.setQty(itemId, qty)
}

