package com.example.tourfrontend

import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Response

// Data model for Directions API response (minimal, for polyline extraction)
data class DirectionsResponse(
    val routes: List<Route>
) {
    data class Route(
        val overview_polyline: OverviewPolyline
    ) {
        data class OverviewPolyline(
            val points: String
        )
    }
}

interface DirectionsApiService {
    @GET("/maps/api/directions/json")
    suspend fun getDirections(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("waypoints") waypoints: String?,
        @Query("mode") mode: String = "walking",
        @Query("alternatives") alternatives: Boolean = false,
        @Query("key") apiKey: String
    ): Response<DirectionsResponse>
}
