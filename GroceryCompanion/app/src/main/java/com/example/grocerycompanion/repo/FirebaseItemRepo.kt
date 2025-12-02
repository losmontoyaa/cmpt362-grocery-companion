package com.example.grocerycompanion.repo

import com.example.grocerycompanion.model.Item
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseItemRepo(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    //  - products  = pre-scraped + user-added grocery products (LD01, SS01, WM01, item-123…)
    //  - items     = legacy/custom items (no longer used for new adds)
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

        // Listen to legacy items collection
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
        val prodSnap = productsCol.document(id).get().await()
        if (prodSnap.exists()) {
            prodSnap.toItemFromProducts()?.let { return it }
        }

        val itemSnap = itemsCol.document(id).get().await()
        if (itemSnap.exists()) {
            itemSnap.toItemFromItems()?.let { return it }
        }

        return null
    }

    /**
     * Add a user-created item to products/{id}.
     *
     * Also writes:
     *  - store_id
     *  - store_name
     *  - location (GeoPoint) if we have coordinates
     */
    suspend fun add(
        item: Item,
        storeName: String,
        latitude: Double?,
        longitude: Double?,
        price: Double?
    ) {
        val baseData = mutableMapOf<String, Any>(
            "product_name" to item.name,
            "brand" to item.brand,
            "barcode" to item.barcode,
            "category" to item.category,
            "url" to item.imgUrl,
            "avgRating" to item.avgRating,
            "ratingsCount" to item.ratingsCount
        )

        // derive a simple store_id from the name (e.g. "Real Canadian Superstore" -> "real_canadian_superstore")
        val storeId = storeName.lowercase()
            .replace("\\s+".toRegex(), "_")
            .replace("[^a-z0-9_]+".toRegex(), "")

        baseData["store_id"] = storeId
        baseData["store_name"] = storeName
        baseData["total_price"] = price as Any

        if (latitude != null && longitude != null) {
            baseData["location"] = GeoPoint(latitude, longitude)
        }

        productsCol.document(item.id).set(baseData).await()
    }
}

/**
 * Map a document from PRODUCTS → Item
 */
private fun DocumentSnapshot.toItemFromProducts(): Item? {
    val id = id
    val name = getString("product_name") ?: return null
    val brand = getString("brand") ?: ""
    val barcode = getString("barcode") ?: ""
    val category =
        getString("category") ?:
        getString("cateory") ?: ""
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

/**
 * Map a document from ITEMS → Item (legacy collection)
 */
private fun DocumentSnapshot.toItemFromItems(): Item? {
    val id = id
    val name = getString("name") ?: return null
    val brand = getString("brand") ?: ""
    val barcode = getString("barcode") ?: ""
    val category = getString("category") ?: ""
    val imgUrl = getString("imgUrl") ?: getString("imgUrl") ?: "" // updated by nav
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

