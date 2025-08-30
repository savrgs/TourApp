package com.example.tourfrontend

import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Response

// Data model for WeatherAPI.com response (simplified for current weather)
data class WeatherResponse(
    val location: Location,
    val current: Current
) {
    data class Location(
        val name: String,
        val region: String?,
        val country: String?,
        val lat: Double,
        val lon: Double
    )
    data class Current(
        val temp_c: Double,
        val condition: Condition,
        val wind_kph: Double,
        val humidity: Int
    ) {
        data class Condition(
            val text: String,
            val icon: String
        )
    }
}

interface WeatherApiService {
    @GET("/v1/forecast.json")
    suspend fun getForecast(
        @Query("key") apiKey: String,
        @Query("q") query: String,
        @Query("days") days: Int = 5
    ): Response<ForecastResponse>
    @GET("/v1/current.json")
    suspend fun getCurrentWeather(
        @Query("key") apiKey: String,
        @Query("q") query: String
    ): Response<WeatherResponse>
}
