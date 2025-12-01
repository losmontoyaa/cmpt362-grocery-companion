package com.example.grocerycompanion.data.usda

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query

const val BASE_URL = "https://api.nal.usda.gov/fdc/v1/"

const val API_KEY ="NrZzpJbJwwim1cRM58pQLRAYD4EBZDYmBbx0I2PR"

interface UsdaApiService {
    @GET("foods/search")
    suspend fun searchFoods(
        @Query("query") query: String,
        @Query("api_key") apiKey: String = API_KEY
    ): UsdaSearchResponse
}

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
