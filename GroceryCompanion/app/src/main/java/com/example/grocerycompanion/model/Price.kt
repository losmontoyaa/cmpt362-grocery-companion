package com.example.grocerycompanion.model

data class Price(

    val itemId: String,
    val storeId: String,
    val price: Double,
    val unit: String,
    val timestamp: Long,
    val isDeal: Boolean,
    val source: String
)
