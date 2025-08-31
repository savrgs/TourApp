package com.example.tourfrontend

import android.location.Location

import com.example.tourfrontend.PlaceDto

// Route optimization: nearest neighbor algorithm
fun optimizeRoute(startLocation: Location?, places: List<PlaceDto>): List<PlaceDto> {
    if (startLocation == null || places.size < 2) return places
    val unvisited = places.toMutableList()
    val route = mutableListOf<PlaceDto>()
    var currentLocation = Location("").apply {
        latitude = startLocation.latitude
        longitude = startLocation.longitude
    }
    while (unvisited.isNotEmpty()) {
        val next = unvisited.minByOrNull { place ->
            val loc = Location("").apply {
                latitude = place.latitude
                longitude = place.longitude
            }
            currentLocation.distanceTo(loc)
        }
        if (next != null) {
            route.add(next)
            currentLocation = Location("").apply {
                latitude = next.latitude
                longitude = next.longitude
            }
            unvisited.remove(next)
        }
    }
    return route
}
