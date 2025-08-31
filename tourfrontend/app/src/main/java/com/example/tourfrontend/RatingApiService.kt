package com.example.tourfrontend

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

data class RatingRequest(val rating: Double)

interface RatingApiService {
    @POST("/api/places/{id}/rate")
    suspend fun ratePlace(
        @Path("id") id: Long,
        @Query("rating") rating: Double
    ): Response<PlaceDto>
}
