package com.example.grocerycompanion.model

import com.google.firebase.firestore.GeoPoint
import io.ktor.http.Url

data class Product(
    val id: String = "",
    val avgRating: Double = 0.0,
    val barcode: String = "",
    val brand: String = "",
    val category: String = "",
    val location: GeoPoint? = null,
    val product_name: String = "",
    val ratingsCount: Int = 0,
    val size: String = "",
    val store_id: String = "",
    val store_name: String = "",
    val total_price: Double = 0.0,
    val unit_price: String = "",
    val url: String = "",

    val imgUrl: String? = null
)
