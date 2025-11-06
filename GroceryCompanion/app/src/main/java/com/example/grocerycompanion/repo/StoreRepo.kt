package com.example.grocerycompanion.repo

import com.example.grocerycompanion.data.InMemoryDataSource
import com.example.grocerycompanion.model.Store

class StoreRepo {
    suspend fun getStores(ids: Set<String>): Map<String, Store> =
        InMemoryDataSource.stores.filterKeys { it in ids }
}
