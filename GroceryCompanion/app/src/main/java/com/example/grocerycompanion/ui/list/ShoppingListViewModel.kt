package com.example.grocerycompanion.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grocerycompanion.repo.FirebasePriceRepo

import com.example.grocerycompanion.repo.FirebaseShoppingListRepo
import com.example.grocerycompanion.model.ShoppingListItem
import com.example.grocerycompanion.repo.FirebaseStoreRepo
import com.example.grocerycompanion.util.haversineKm
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

// keep this at top-level (you already have it)
data class ItemPickUi(
    val storeName: String,
    val price: Double
)

class ShoppingListViewModel(
    private val listRepo: FirebaseShoppingListRepo,
    private val priceRepo: FirebasePriceRepo,
    private val storeRepo: FirebaseStoreRepo
) : ViewModel() {

    val list: StateFlow<List<ShoppingListItem>> =
        listRepo.streamList().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    suspend fun computePerStoreTotals(userLat: Double? = null, userLng: Double? = null): List<StoreTotal> {
        val items = list.value
        val allItemIds = items.map { it.itemId }.toSet()

        val perItemPrices = allItemIds.associateWith { id ->
            priceRepo.latestPricesByStore(id)
        }

        val stores = storeRepo.getStores(perItemPrices.values.flatten().map { it.storeId }.toSet())

        val totals = mutableMapOf<String, Double>() // storeId -> total
        items.forEach { sli ->
            val prices = perItemPrices[sli.itemId].orEmpty()
            val cheapest = prices.minByOrNull { it.price } ?: return@forEach
            totals[cheapest.storeId] = (totals[cheapest.storeId] ?: 0.0) + (cheapest.price * sli.qty)
        }

        return totals.map { (sid, total) ->
            val s = stores[sid]
            val dist = if (s != null && userLat != null && userLng != null) {
                haversineKm(userLat, userLng, s.lat, s.lng)
            } else null
            StoreTotal(storeId = sid, storeName = s?.name ?: sid, total = total, distanceKm = dist)
        }.sortedWith(compareBy<StoreTotal> { it.total }.thenBy { it.distanceKm ?: Double.MAX_VALUE })
    }

    // NEW: returns UI-ready picks so the Fragment/Adapter don’t need to transform types
    suspend fun computePerItemCheapestUi(): Map<String, ItemPickUi> {
        val items = list.value
        val ids = items.map { it.itemId }.toSet()

        val perItemPrices = ids.associateWith { id ->
            priceRepo.latestPricesByStore(id)
        }

        val storeIds = perItemPrices.values.flatten().map { it.storeId }.toSet()
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

data class StoreTotal(val storeId: String, val storeName: String, val total: Double, val distanceKm: Double?)
