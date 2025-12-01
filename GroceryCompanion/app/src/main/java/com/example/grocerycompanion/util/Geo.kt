package com.example.grocerycompanion.util
import com.example.grocerycompanion.model.Product
import kotlin.math.*

fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val R = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat/2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon/2).pow(2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return R * c
}

fun sortProductsByDistanceKm(products: List<Product>, userLat: Double, userLng: Double)
        : List<Pair<Product, Double>> {
    return products.mapNotNull { product ->
        product.location?.let {
            val distanceKm = haversineKm(userLat, userLng, it.latitude, it.longitude)
            product to distanceKm
        }
    }.sortedBy { it.second }
}