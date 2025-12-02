package com.example.grocerycompanion.repo

import com.example.grocerycompanion.model.Product
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

/* Queries the database to return Products according to their product_name and brand */
class ProductRepository {
    suspend fun getProductByNameAndBrand(productName: String, brand: String): List<Product> {
        val db = Firebase.firestore
        return db.collection("products")
            .whereEqualTo("product_name", productName)
            .whereEqualTo("brand", brand)
            .get()
            .await()
            .documents
            .mapNotNull { it.toObject(Product::class.java) }
    }
}