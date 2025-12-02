package com.example.grocerycompanion.util

import com.example.grocerycompanion.model.DirectionsResponse
import retrofit2.http.GET
import retrofit2.http.Query

//Service interface to use with Retrofit to retrieve directions in DirectionRepo

interface DirectionService {
    @GET("directions/json")
    suspend fun getRoute(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("mode") mode: String = "driving",
        @Query("key") apiKey: String
    ): DirectionsResponse
}