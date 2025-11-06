package com.example.grocerycompanion.repo

import com.example.grocerycompanion.data.InMemoryDataSource
import com.example.grocerycompanion.model.Price

class PriceRepo {
    suspend fun latestPricesByStore(itemId: String): List<Price> =
        InMemoryDataSource.prices.filter { it.itemId == itemId }
}
