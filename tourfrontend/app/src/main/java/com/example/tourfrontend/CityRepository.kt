package com.example.tourfrontend

class CityRepository(private val api: CityApiService) {
    suspend fun getCities(): List<CityDto> = api.getCities()
    suspend fun getPlacesByCityId(cityId: Long): List<PlaceDto> = api.getPlacesByCityId(cityId)
} 