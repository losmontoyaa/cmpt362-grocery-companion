package com.example.grocerycompanion.data.usda

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query

/*
  UsdaApiService.kt

  This file defines the Retrofit service and API configuration used to access
  the USDA FoodData Central (FDC) API. The service exposes a single endpoint
  (`foods/search`) which returns nutrition information for a given query.

  The API uses:
   - Retrofit for HTTP networking
   - Kotlinx Serialization for JSON parsing
   - A lazily-initialized singleton (`UsdaApi.service`) to ensure only one
     Retrofit instance is created during the app lifecycle
 */

const val BASE_URL = "https://api.nal.usda.gov/fdc/v1/" // Base URL for all USDA FDC API requests


const val API_KEY ="NrZzpJbJwwim1cRM58pQLRAYD4EBZDYmBbx0I2PR" //USDA API KEY Here

// defines the endpoint
interface UsdaApiService {
    @GET("foods/search")
    suspend fun searchFoods(
        @Query("query") query: String,
        @Query("api_key") apiKey: String = API_KEY
    ): UsdaSearchResponse
}

// here the code parses the json and accepts extra fields from the USDA response

@OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
object UsdaApi {
    private val json = Json { ignoreUnknownKeys = true }

    val service: UsdaApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(
                json.asConverterFactory("application/json".toMediaType())
            )
            .build()
            .create(UsdaApiService::class.java)
    }
}
