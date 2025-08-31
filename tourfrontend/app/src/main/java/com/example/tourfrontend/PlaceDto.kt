package com.example.tourfrontend

data class PlaceDto(
    val id: Long,
    val name: String,
    val type: String,
    val latitude: Double,
    val longitude: Double,
    val isFree: Boolean,
    val cityName: String,
    val description: String,
    val photoUrl: String? = null,
    val rating: Double = 0.0
)