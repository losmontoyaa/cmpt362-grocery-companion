package com.example.grocerycompanion.repo

import com.example.grocerycompanion.model.Store
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Stores live in collection "stores".
 *
 * stores/{storeId} {
 *    name: String,
 *    address: String,
 *    lat: Double,
 *    lng: Double
 * }
 *
 * If your existing data has only store_name + location,
 * map accordingly in the parsing below.
 */
class FirebaseStoreRepo(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val storesRef = db.collection("stores")

    suspend fun getStores(ids: Set<String>): Map<String, Store> {
        if (ids.isEmpty()) return emptyMap()

        // Firestore whereIn supports up to 10 values.
        // For simplicity assume small set; if you ever exceed,
        // you can batch in chunks of 10.
        val snapshot = storesRef
            .whereIn(FieldPath.documentId(), ids.toList())
            .get()
            .await()

        val result = mutableMapOf<String, Store>()
        for (doc in snapshot.documents) {
            val id = doc.id
            val name = doc.getString("name") ?: doc.getString("store_name") ?: id
            val address = doc.getString("address") ?: ""
            val lat = doc.getDouble("lat") ?: doc.getGeoPoint("location")?.latitude ?: 0.0
            val lng = doc.getDouble("lng") ?: doc.getGeoPoint("location")?.longitude ?: 0.0

            result[id] = Store(
                id = id,
                name = name,
                address = address,
                lat = lat,
                lng = lng
            )
        }
        return result
    }

    suspend fun getAll(): List<Store> {
        val snapshot = storesRef.get().await()
        return snapshot.documents.map { doc ->
            val id = doc.id
            val name = doc.getString("name") ?: doc.getString("store_name") ?: id
            val address = doc.getString("address") ?: ""
            val lat = doc.getDouble("lat") ?: doc.getGeoPoint("location")?.latitude ?: 0.0
            val lng = doc.getDouble("lng") ?: doc.getGeoPoint("location")?.longitude ?: 0.0

            Store(
                id = id,
                name = name,
                address = address,
                lat = lat,
                lng = lng
            )
        }
    }
}
