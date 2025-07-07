package com.example.tourfrontend

import retrofit2.http.GET
import retrofit2.http.Path

interface CityApiService {
    @GET("/api/cities")
    suspend fun getCities(): List<CityDto>
    
    @GET("/api/cities/{id}/places")
    suspend fun getPlacesByCityId(@Path("id") cityId: Long): List<PlaceDto>
} 