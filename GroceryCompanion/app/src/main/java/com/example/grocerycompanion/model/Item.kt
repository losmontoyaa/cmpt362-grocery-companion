package com.example.grocerycompanion.model

data class Item(
    val id: String,
    val name: String,
    val brand: String,
    val barcode: String,
    val imgUrl: String,
    val category: String,
    val avgRating: Double,
    val ratingsCount: Int
)
