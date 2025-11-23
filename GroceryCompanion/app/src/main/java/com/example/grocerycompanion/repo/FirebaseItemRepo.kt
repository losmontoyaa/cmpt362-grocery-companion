package com.example.grocerycompanion.repo

import com.example.grocerycompanion.model.Item
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Items live in Firestore under collection "items".
 *
 * items/{itemId} {
 *   product_name: String,
 *   brand: String,
 *   barcode: String,
 *   url: String,
 *   category: String,
 *   avgRating: Double,
 *   ratingsCount: Long
 * }
 */
class FirebaseItemRepo(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val itemsRef = db.collection("items")

    // 🔁 Live stream of all items
    fun streamAll(): Flow<List<Item>> = callbackFlow {
        val listener = itemsRef.addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener

            val list = snapshot?.documents?.mapNotNull { doc ->
                val name = doc.getString("product_name") ?: return@mapNotNull null

                Item(
                    id = doc.id,
                    name = name,
                    brand = doc.getString("brand") ?: "",
                    barcode = doc.getString("barcode") ?: "",
                    imgUrl = doc.getString("url") ?: "",
                    category = doc.getString("category") ?: "",
                    avgRating = doc.getDouble("avgRating") ?: 0.0,
                    ratingsCount = (doc.getLong("ratingsCount") ?: 0L).toInt()
                )
            } ?: emptyList()

            trySend(list)
        }

        awaitClose { listener.remove() }
    }

    // One-shot load
    suspend fun getAll(): List<Item> {
        val snapshot = itemsRef.get().await()
        return snapshot.documents.mapNotNull { doc ->
            val name = doc.getString("product_name") ?: return@mapNotNull null

            Item(
                id = doc.id,
                name = name,
                brand = doc.getString("brand") ?: "",
                barcode = doc.getString("barcode") ?: "",
                imgUrl = doc.getString("url") ?: "",
                category = doc.getString("category") ?: "",
                avgRating = doc.getDouble("avgRating") ?: 0.0,
                ratingsCount = (doc.getLong("ratingsCount") ?: 0L).toInt()
            )
        }
    }

    suspend fun get(itemId: String): Item? {
        val doc = itemsRef.document(itemId).get().await()
        if (!doc.exists()) return null

        val name = doc.getString("product_name") ?: return null

        return Item(
            id = doc.id,
            name = name,
            brand = doc.getString("brand") ?: "",
            barcode = doc.getString("barcode") ?: "",
            imgUrl = doc.getString("url") ?: "",
            category = doc.getString("category") ?: "",
            avgRating = doc.getDouble("avgRating") ?: 0.0,
            ratingsCount = (doc.getLong("ratingsCount") ?: 0L).toInt()
        )
    }

    suspend fun add(item: Item) {
        val data = mapOf(
            "product_name" to item.name,
            "brand" to item.brand,
            "barcode" to item.barcode,
            "url" to item.imgUrl,
            "category" to item.category,
            "avgRating" to item.avgRating,
            "ratingsCount" to item.ratingsCount
        )
        itemsRef.document(item.id).set(data).await()
    }
}
