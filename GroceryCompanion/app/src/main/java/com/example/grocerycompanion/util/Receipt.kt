package com.example.grocerycompanion.util

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Receipt(
    @SerialName("store_name") val storeName: String,
    @SerialName("address") val address: String,
    @SerialName("items") val items: List<ReceiptItem>
)

@Serializable
data class ReceiptItem(
    @SerialName("item_name") val name: String,
    @SerialName("price") val price: String
)