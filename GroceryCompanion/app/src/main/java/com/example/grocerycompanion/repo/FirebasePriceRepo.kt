package com.example.grocerycompanion.repo

import com.example.grocerycompanion.model.Price
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Prices live in collection "prices".
 *
 * prices/{autoId or item_store id} {
 *    itemId: String,
 *    storeId: String,
 *    price: Double,
 *    unit: String,          // e.g. "4L", "/kg", "each"
 *    timestamp: Long,
 *    isDeal: Boolean,
 *    source: String         // "manual", "crawler", etc.
 * }
 */
class FirebasePriceRepo(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val pricesRef = db.collection("prices")

    suspend fun latestPricesByStore(itemId: String): List<Price> {
        val snapshot = pricesRef
            .whereEqualTo("itemId", itemId)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            val storeId = doc.getString("storeId") ?: return@mapNotNull null
            val price = doc.getDouble("price") ?: return@mapNotNull null
            val unit = doc.getString("unit") ?: ""
            val timestamp = doc.getLong("timestamp") ?: 0L
            val isDeal = doc.getBoolean("isDeal") ?: false
            val source = doc.getString("source") ?: "manual"

            Price(
                itemId = itemId,
                storeId = storeId,
                price = price,
                unit = unit,
                timestamp = timestamp,
                isDeal = isDeal,
                source = source
            )
        }
    }

    suspend fun upsertPrice(
        itemId: String,
        storeId: String,
        price: Double,
        unit: String
    ) {
        // Deterministic doc ID per (item, store)
        val docId = "${itemId}_$storeId"
        val now = System.currentTimeMillis()

        val data = mapOf(
            "itemId" to itemId,
            "storeId" to storeId,
            "price" to price,
            "unit" to unit,
            "timestamp" to now,
            "isDeal" to false,
            "source" to "manual"
        )

        pricesRef.document(docId).set(data).await()
    }
}
