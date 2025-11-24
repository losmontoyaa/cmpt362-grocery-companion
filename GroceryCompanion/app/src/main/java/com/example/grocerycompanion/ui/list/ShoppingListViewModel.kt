package com.example.grocerycompanion.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grocerycompanion.model.Price
import com.example.grocerycompanion.model.ShoppingListItem
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
    private val storeRepo: FirebaseStoreRepo
) : ViewModel() {

    private val _list = MutableStateFlow<List<ShoppingListItem>>(emptyList())
    val list: StateFlow<List<ShoppingListItem>> = _list

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
                }
        }
    }

    fun retry() {
        startCollecting()
    }

    /**
     * Compute total per store using cheapest price per item.
     * itemId is BARCODE; FirebasePriceRepo looks products up by barcode.
     */
    suspend fun computePerStoreTotals(
        userLat: Double? = null,
        userLng: Double? = null
    ): List<StoreTotal> {
        val items = list.value
        val allItemIds = items.map { it.itemId }.toSet()

        // 🔹 fetch prices per BARCODE (itemId)
        val perItemPrices = mutableMapOf<String, List<Price>>()
        for (id in allItemIds) {
            val pricesForItem = priceRepo.latestPricesForBarcode(id)
            perItemPrices[id] = pricesForItem
        }

        val allStoreIds = perItemPrices.values
            .flatten()
            .map { it.storeId }
            .toSet()

        val stores = storeRepo.getStores(allStoreIds)

        val totals = mutableMapOf<String, Double>() // storeId -> total
        items.forEach { sli ->
            val prices = perItemPrices[sli.itemId].orEmpty()
            val cheapest = prices.minByOrNull { it.price } ?: return@forEach
            totals[cheapest.storeId] =
                (totals[cheapest.storeId] ?: 0.0) + (cheapest.price * sli.qty)
        }

        return totals.map { (sid, total) ->
            val s = stores[sid]
            val dist = if (s != null && userLat != null && userLng != null) {
                haversineKm(userLat, userLng, s.lat, s.lng)
            } else null
            StoreTotal(
                storeId = sid,
                storeName = s?.name ?: sid,
                total = total,
                distanceKm = dist
            )
        }.sortedWith(
            compareBy<StoreTotal> { it.total }
                .thenBy { it.distanceKm ?: Double.MAX_VALUE }
        )
    }

    /**
     * For each item barcode, pick the cheapest store and convert it into a UI object.
     */
    suspend fun computePerItemCheapestUi(): Map<String, ItemPickUi> {
        val items = list.value
        val ids = items.map { it.itemId }.toSet()

        val perItemPrices = mutableMapOf<String, List<Price>>()
        for (id in ids) {
            perItemPrices[id] = priceRepo.latestPricesForBarcode(id)
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