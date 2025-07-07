package com.group8.guidetour.tourbackend.dto

data class PlaceDTO(
    val id: Long,
    val name: String,
    val type: String,
    val latitude: Double,
    val longitude: Double,
    val isFree: Boolean
) 