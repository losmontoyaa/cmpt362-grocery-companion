package com.example.grocerycompanion.repo

import com.example.grocerycompanion.model.Price
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebasePriceRepo {

    private val db = FirebaseFirestore.getInstance()
    private val products = db.collection("products")

    /**
     * Return the price(s) for a given product document ID (e.g. "ss17").
     */
    suspend fun pricesForItemId(itemId: String): List<Price> {
        val doc = products.document(itemId).get().await()
        if (!doc.exists()) return emptyList()

        val totalPrice = doc.getDouble("total_price") ?: return emptyList()
        val storeId = doc.getString("store_id") ?: return emptyList()
        val unitPrice = doc.getString("unit_price") ?: ""

        return listOf(
            Price(
                itemId = itemId,
                storeId = storeId,
                price = totalPrice,
                unit = unitPrice,
                timestamp = 0L,
                isDeal = false,
                source = "products"
            )
        )
    }

    // Given an itemId, update its price with a new price
    suspend fun updatePrice(itemId: String, newPrice: Double) {
        products
            .document(itemId)
            .update("total_price", newPrice)
            .await()
    }
}
