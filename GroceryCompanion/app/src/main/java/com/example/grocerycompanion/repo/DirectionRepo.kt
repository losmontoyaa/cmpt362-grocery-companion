package com.example.grocerycompanion.repo

import com.example.grocerycompanion.BuildConfig
import com.example.grocerycompanion.util.DirectionService
import com.google.android.gms.maps.model.LatLng
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object DirectionRepo {

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://maps.googleapis.com/maps/api/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val service = retrofit.create(DirectionService::class.java)

    suspend fun getRoute(origin: String, destination: String): List<LatLng> {
        val apiKey = BuildConfig.MAPS_API_KEY
        val response = service.getRoute(origin, destination, "driving", apiKey)
        println(response.toString())

        val polyline = response.routes.firstOrNull()
            ?.overviewPolyline?.points ?: return emptyList()

        return decodePolyline(polyline)
    }
}

private fun decodePolyline(encoded: String): List<LatLng> {
    val poly = ArrayList<LatLng>()
    var index = 0
    val len = encoded.length
    var lat = 0
    var lng = 0

    while (index < len) {
        var b: Int
        var shift = 0
        var result = 0
        do {
            b = encoded[index++].code - 63
            result = result or (b and 0x1f shl shift)
            shift += 5
        } while (b >= 0x20)
        val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
        lat += dlat

        shift = 0
        result = 0
        do {
            b = encoded[index++].code - 63
            result = result or (b and 0x1f shl shift)
            shift += 5
        } while (b >= 0x20)
        val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
        lng += dlng

        val latitude = lat / 1e5
        val longitude = lng / 1e5
        poly.add(LatLng(latitude, longitude))
    }

    return poly
}