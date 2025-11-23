package com.example.grocerycompanion.model

data class Rating(
    val id: String = "",
    val itemId: String = "",
    val userId: String = "",
    val stars: Int = 0,
    val comment: String = "",
    val timestamp: Long = System.currentTimeMillis()
)