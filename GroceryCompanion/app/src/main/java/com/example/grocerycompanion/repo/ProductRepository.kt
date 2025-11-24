package com.example.grocerycompanion.repo

import com.example.grocerycompanion.model.Product
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class ProductRepository {
    suspend fun getProductByName(productName: String): List<Product> {
        val db = Firebase.firestore
        return db.collection("products")
            .whereEqualTo("product_name", productName)
            .get()
            .await()
            .documents
            .mapNotNull { it.toObject(Product::class.java) }
    }
}