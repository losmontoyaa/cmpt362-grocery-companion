package com.example.grocerycompanion.repo

import com.example.grocerycompanion.data.InMemoryDataSource
import com.example.grocerycompanion.model.Item

class ItemRepo {
    suspend fun get(itemId: String): Item? = InMemoryDataSource.items[itemId]
}
