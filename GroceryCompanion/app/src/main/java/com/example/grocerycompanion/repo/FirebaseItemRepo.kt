// repo/FirebaseItemRepo.kt
package com.example.grocerycompanion.repo

import com.example.grocerycompanion.model.Item
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseItemRepo(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    // Your two collections:
    //  - products  = pre-scraped grocery products (LD01, SS01, WM01…)
    //  - items     = user-added custom items (Duck, etc.)
    private val productsCol = db.collection("products")
    private val itemsCol = db.collection("items")

    /**
     * Stream ALL items shown in the browse screen:
     * - All docs from products
     * - All docs from items
     */
    fun streamAll(): Flow<List<Item>> = callbackFlow {
        var latestProducts: List<Item> = emptyList()
        var latestCustom: List<Item> = emptyList()

        fun push() {
            // products first, then user items
            trySend(latestProducts + latestCustom)
        }

        // Listen to products collection
        val productsListener = productsCol.addSnapshotListener { snap, error ->
            if (error != null) return@addSnapshotListener
            latestProducts = snap?.documents
                ?.mapNotNull { it.toItemFromProducts() }
                ?: emptyList()
            push()
        }

        // Listen to user-added items collection
        val itemsListener = itemsCol.addSnapshotListener { snap, error ->
            if (error != null) return@addSnapshotListener
            latestCustom = snap?.documents
                ?.mapNotNull { it.toItemFromItems() }
                ?: emptyList()
            push()
        }

        awaitClose {
            productsListener.remove()
            itemsListener.remove()
        }
    }

    /**
     * Get a single item by id:
     * 1) try products/{id}
     * 2) fall back to items/{id}
     */
    suspend fun get(id: String): Item? {
        // Try products first
        val prodSnap = productsCol.document(id).get().await()
        if (prodSnap.exists()) {
            prodSnap.toItemFromProducts()?.let { return it }
        }

        // Then custom items
        val itemSnap = itemsCol.document(id).get().await()
        if (itemSnap.exists()) {
            itemSnap.toItemFromItems()?.let { return it }
        }

        return null
    }

    /**
     * Add a user-created item to items/{id}.
     * (Your "Add item" dialog uses this.)
     */
    suspend fun add(item: Item) {
        val data = mapOf(
            "name" to item.name,
            "brand" to item.brand,
            "barcode" to item.barcode,
            "imgUrl" to item.imgUrl,
            "category" to item.category,
            "avgRating" to item.avgRating,
            "ratingsCount" to item.ratingsCount
        )
        itemsCol.document(item.id).set(data).await()
    }
}

/**
 * Map a document from PRODUCTS → Item
 *
 * Firestore doc example:
 *  - product_name
 *  - brand
 *  - barcode
 *  - category / cateory (typo in some docs)
 *  - url
 *  - avgRating
 *  - ratingsCount
 */
private fun DocumentSnapshot.toItemFromProducts(): Item? {
    val id = id                     // LD01 / SS01 / WM01 / etc.
    val name = getString("product_name") ?: return null
    val brand = getString("brand") ?: ""
    val barcode = getString("barcode") ?: ""
    val category =
        getString("category") ?:    // normal field
        getString("cateory") ?: ""  // typo fallback
    val imgUrl = getString("url") ?: "" // we’ll use product URL as the image URL for now
    val avgRating = getDouble("avgRating") ?: 0.0
    val ratingsCount = getLong("ratingsCount")?.toInt() ?: 0

    return Item(
        id = id,
        name = name,
        brand = brand,
        barcode = barcode,
        imgUrl = imgUrl,
        category = category,
        avgRating = avgRating,
        ratingsCount = ratingsCount
    )
}

/**
 * Map a document from ITEMS → Item
 *
 * Your custom docs look like:
 *  - name
 *  - brand
 *  - barcode
 *  - imgUrl
 *  - category
 *  - avgRating
 *  - ratingsCount
 */
private fun DocumentSnapshot.toItemFromItems(): Item? {
    val id = id
    val name = getString("name") ?: return null
    val brand = getString("brand") ?: ""
    val barcode = getString("barcode") ?: ""
    val category = getString("category") ?: ""
    val imgUrl = getString("imgUrl") ?: ""
    val avgRating = getDouble("avgRating") ?: 0.0
    val ratingsCount = getLong("ratingsCount")?.toInt() ?: 0

    return Item(
        id = id,
        name = name,
        brand = brand,
        barcode = barcode,
        imgUrl = imgUrl,
        category = category,
        avgRating = avgRating,
        ratingsCount = ratingsCount
    )
}
