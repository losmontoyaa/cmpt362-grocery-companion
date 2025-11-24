package com.example.grocerycompanion.repo

import com.example.grocerycompanion.model.Price
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebasePriceRepo {

    private val db = FirebaseFirestore.getInstance()
    private val products = db.collection("products")

    suspend fun latestPricesForBarcode(barcode: String): List<Price> {
        val snap = products
            .whereEqualTo("barcode", barcode)
            .get()
            .await()

        return snap.documents.mapNotNull { doc ->
            val totalPrice = doc.getDouble("total_price") ?: return@mapNotNull null
            val storeId = doc.getString("store_id") ?: return@mapNotNull null
            val unitPrice = doc.getString("unit_price") ?: ""

            Price(
                itemId = barcode,
                storeId = storeId,
                price = totalPrice,
                unit = unitPrice,
                timestamp = 0L,
                isDeal = false,
                source = "products"
            )
        }
    }
}
