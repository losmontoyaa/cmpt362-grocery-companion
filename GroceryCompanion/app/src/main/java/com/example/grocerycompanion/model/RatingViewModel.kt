package com.example.grocerycompanion.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import java.util.UUID

// Used for loading ratings for a product and submitting ratings for that product
class RatingViewModel : ViewModel() {

    private val db = Firebase.firestore

    var ratings by mutableStateOf<List<Rating>>(emptyList())
        private set

    val averageRating: Double
        get() = if (ratings.isEmpty()) 0.0 else ratings.map { it.stars }.average()

    // Get ratings that match the product name and brand
    fun loadRatings(productName: String, brand: String) {
        db.collection("ratings")
            .whereEqualTo("product_name", productName)
            .whereEqualTo("brand", brand)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val loadedRatings = querySnapshot.documents.mapNotNull { doc ->
                    doc.toObject(Rating::class.java)
                }
                ratings = loadedRatings
            }
            .addOnFailureListener { exception ->
                println("Failed to load ratings: $exception")
            }
    }

    //
    fun submitRating(
        itemId: String,
        productName: String,
        brand: String,
        stars: Int,
        comment: String,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        val newRating = Rating(
            id = UUID.randomUUID().toString(),
            itemId = itemId,
            product_name = productName,
            brand = brand,
            userId = userId ?: "",
            stars = stars,
            comment = comment,
            timestamp = System.currentTimeMillis()
        )

        // Get the document for the product to update its average rating and number of ratings
        val productRef = db.collection("products").document(itemId)
        val ratingRef = db.collection("ratings").document()

        db.runTransaction { transaction ->
            val snapshot = transaction.get(productRef)
            val oldAvg = snapshot.getDouble("avgRating") ?: 0.0
            val oldCount = snapshot.getDouble("ratingsCount") ?: 0.0

            val newAvg = (oldAvg * oldCount + stars) / (oldCount + 1)

            transaction.set(ratingRef, newRating, SetOptions.merge())

            transaction.update(productRef, mapOf(
                "avgRating" to newAvg,
                "ratingsCount" to oldCount + 1
            ))
        }.addOnSuccessListener {
            onSuccess()
        }.addOnFailureListener { exception ->
            onFailure(exception)
        }
    }
}