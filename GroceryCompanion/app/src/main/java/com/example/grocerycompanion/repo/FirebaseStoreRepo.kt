package com.example.grocerycompanion.repo

import com.example.grocerycompanion.model.Store
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/*
      Given store IDs (e.g., "london drugs", "walmart", "superstore"),
      returns a map of Store objects keyed by id.
      used by higher level components when they need details
      about stores that are already referenced by id on product documents.
      it returns `Store` models that the UI can display directly.
     */

class FirebaseStoreRepo(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    // Root collection that holds product documents which contain store info
    private val productsCol = db.collection("products")

    suspend fun getStores(ids: Set<String>): Map<String, Store> {
        if (ids.isEmpty()) return emptyMap()

        // Firestore whereIn limit is 10
        val chunks = ids.chunked(10)
        val result = mutableMapOf<String, Store>()

        for (chunk in chunks) {
            val snap = productsCol
                .whereIn("store_id", chunk)
                .get()
                .await()

            snap.documents.forEach { doc ->
                val storeId = doc.getString("store_id") ?: return@forEach
                if (result.containsKey(storeId)) return@forEach

                val name = doc.getString("store_name") ?: storeId
                val geo = doc.getGeoPoint("location")

                result[storeId] = Store(
                    id = storeId,
                    name = name,
                    address = "",
                    lat = geo?.latitude ?: 0.0,
                    lng = geo?.longitude ?: 0.0
                )
            }
        }

        return result
    }
}
