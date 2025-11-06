package com.example.grocerycompanion.repo

import com.example.grocerycompanion.data.InMemoryDataSource
import com.example.grocerycompanion.model.ShoppingListItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ShoppingListRepo {
    private val listFlow = MutableStateFlow<List<ShoppingListItem>>(InMemoryDataSource.shoppingList.toList())

    fun streamList(): StateFlow<List<ShoppingListItem>> = listFlow

    suspend fun add(itemId: String, qty: Int) {
        val idx = InMemoryDataSource.shoppingList.indexOfFirst { it.itemId == itemId }
        if (idx >= 0) {
            val old = InMemoryDataSource.shoppingList[idx]
            InMemoryDataSource.shoppingList[idx] = old.copy(qty = old.qty + qty)
        } else {
            InMemoryDataSource.shoppingList.add(ShoppingListItem(itemId, qty))
        }
        listFlow.value = InMemoryDataSource.shoppingList.toList()
    }

    suspend fun remove(itemId: String) {
        InMemoryDataSource.shoppingList.removeAll { it.itemId == itemId }
        listFlow.value = InMemoryDataSource.shoppingList.toList()
    }

    suspend fun setQty(itemId: String, qty: Int) {
        val idx = InMemoryDataSource.shoppingList.indexOfFirst { it.itemId == itemId }
        if (idx >= 0) {
            InMemoryDataSource.shoppingList[idx] = InMemoryDataSource.shoppingList[idx].copy(qty = qty)
            listFlow.value = InMemoryDataSource.shoppingList.toList()
        }
    }

    fun getAll(): List<ShoppingListItem> = listFlow.value
}
